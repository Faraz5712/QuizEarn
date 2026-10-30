package com.quizearn.app.data.remote

import com.quizearn.app.domain.model.AchievementBadge
import com.quizearn.app.domain.model.LeaderboardEntry
import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.domain.model.LeaderboardUiState
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TaskType
import com.quizearn.app.domain.model.TournamentEvent
import com.quizearn.app.domain.model.TournamentParticipant
import com.quizearn.app.domain.model.TournamentStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

/**
 * Server-authoritative mock data source for Tasks, Tournaments, and Leaderboard.
 * Simulates server-side evaluation, UTC midnight scheduling, in-memory caching,
 * rate-limiting, and deterministic tie-breaking.
 */
class MockTaskRemoteDataSource : TaskRemoteDataSource {

    private val mutex = Mutex()

    // --- State Storage ---
    private var lastUtcDateString: String = getCurrentUtcDate()
    private val processedClaims = ConcurrentHashMap<String, Long>() // idempotencyKey -> rewardPoints

    // Daily Tasks Map: userId -> List<TaskItem>
    private val userDailyTasks = ConcurrentHashMap<String, MutableList<TaskItem>>()
    // Weekly Tasks Map: userId -> List<TaskItem>
    private val userWeeklyTasks = ConcurrentHashMap<String, MutableList<TaskItem>>()
    // Achievements Map: userId -> List<AchievementBadge>
    private val userAchievements = ConcurrentHashMap<String, MutableList<AchievementBadge>>()

    // Tournaments Storage: tournamentId -> TournamentEvent
    private val tournaments = ConcurrentHashMap<String, TournamentEvent>()
    // Tournament Registrations: tournamentId -> Set<userId>
    private val tournamentRegistrations = ConcurrentHashMap<String, MutableSet<String>>()
    // Tournament Scores: tournamentId -> MutableList<TournamentParticipant>
    private val tournamentScores = ConcurrentHashMap<String, MutableList<TournamentParticipant>>()

    // Leaderboard Cache: Timeframe -> CachedLeaderboard
    private data class CachedLeaderboard(
        val state: LeaderboardUiState,
        val timestamp: Long
    )
    private val leaderboardCache = ConcurrentHashMap<LeaderboardTimeframe, CachedLeaderboard>()
    private var lastLeaderboardQueryTime: Long = 0L

    companion object {
        const val CACHE_TTL_MS: Long = 60_000L // 60-second periodic cache
        const val RATE_LIMIT_WINDOW_MS: Long = 5_000L // 5-second minimum interval between forced queries
    }

    init {
        initDefaultTournaments()
    }

    // =========================================================================
    // 1. Task Progression & Claims Engine
    // =========================================================================

    override suspend fun getDailyTasks(userId: String): List<TaskItem> = mutex.withLock {
        checkUtcMidnightRolloverInternal(userId)
        val list = userDailyTasks.getOrPut(userId) { createDefaultDailyTasks() }
        list.toList()
    }

    override suspend fun getWeeklyTasks(userId: String): List<TaskItem> = mutex.withLock {
        val list = userWeeklyTasks.getOrPut(userId) { createDefaultWeeklyTasks() }
        list.toList()
    }

    override suspend fun getAchievements(userId: String): List<AchievementBadge> = mutex.withLock {
        val list = userAchievements.getOrPut(userId) { createDefaultAchievements() }
        list.toList()
    }

    override suspend fun claimTaskReward(
        userId: String,
        taskId: String,
        idempotencyKey: String
    ): Result<Long> = mutex.withLock {
        // 1. Idempotency check: prevent duplicate reward credits
        processedClaims[idempotencyKey]?.let { cachedReward ->
            return Result.success(cachedReward)
        }

        // 2. Locate task across daily and weekly lists
        val dailyList = userDailyTasks.getOrPut(userId) { createDefaultDailyTasks() }
        val weeklyList = userWeeklyTasks.getOrPut(userId) { createDefaultWeeklyTasks() }

        val dailyIndex = dailyList.indexOfFirst { it.id == taskId }
        if (dailyIndex != -1) {
            val task = dailyList[dailyIndex]
            if (!task.isCompleted) {
                return Result.failure(IllegalStateException("Task '${task.title}' is not completed yet."))
            }
            if (task.isClaimed) {
                return Result.failure(IllegalStateException("Task '${task.title}' has already been claimed."))
            }
            val updated = task.copy(isClaimed = true, claimedAtTimestamp = System.currentTimeMillis())
            dailyList[dailyIndex] = updated
            processedClaims[idempotencyKey] = task.rewardPoints
            return Result.success(task.rewardPoints)
        }

        val weeklyIndex = weeklyList.indexOfFirst { it.id == taskId }
        if (weeklyIndex != -1) {
            val task = weeklyList[weeklyIndex]
            if (!task.isCompleted) {
                return Result.failure(IllegalStateException("Task '${task.title}' is not completed yet."))
            }
            if (task.isClaimed) {
                return Result.failure(IllegalStateException("Task '${task.title}' has already been claimed."))
            }
            val updated = task.copy(isClaimed = true, claimedAtTimestamp = System.currentTimeMillis())
            weeklyList[weeklyIndex] = updated
            processedClaims[idempotencyKey] = task.rewardPoints
            return Result.success(task.rewardPoints)
        }

        return Result.failure(IllegalArgumentException("Task with ID '$taskId' not found."))
    }

    override suspend fun recordQuizProgression(
        userId: String,
        category: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        streakDays: Int
    ) = mutex.withLock {
        checkUtcMidnightRolloverInternal(userId)

        val accuracy = if (totalQuestions > 0) (score.toDouble() / (totalQuestions * 100)) * 100.0 else 0.0

        // Update Daily Tasks
        val dailyList = userDailyTasks.getOrPut(userId) { createDefaultDailyTasks() }
        for (i in dailyList.indices) {
            val task = dailyList[i]
            if (task.isClaimed) continue

            when (task.id) {
                "d_task_01" -> { // Play 3 Quizzes Today
                    dailyList[i] = task.copy(currentProgress = (task.currentProgress + 1).coerceAtMost(task.targetGoal))
                }
                "d_task_02" -> { // Score 80%+ on Science Quiz
                    if (category.contains("science", ignoreCase = true) && accuracy >= 80.0) {
                        dailyList[i] = task.copy(currentProgress = 1)
                    }
                }
                "d_task_03" -> { // Maintain 3-Day Streak
                    dailyList[i] = task.copy(currentProgress = streakDays.coerceAtMost(task.targetGoal))
                }
                "d_task_04" -> { // Complete 1 Speed Round (<60s)
                    if (durationSeconds in 1..60) {
                        dailyList[i] = task.copy(currentProgress = 1)
                    }
                }
            }
        }

        // Update Weekly Tasks
        val weeklyList = userWeeklyTasks.getOrPut(userId) { createDefaultWeeklyTasks() }
        for (i in weeklyList.indices) {
            val task = weeklyList[i]
            if (task.isClaimed) continue

            when (task.id) {
                "w_task_01" -> { // Quiz Marathon Master (20 Quizzes)
                    weeklyList[i] = task.copy(currentProgress = (task.currentProgress + 1).coerceAtMost(task.targetGoal))
                }
                "w_task_02" -> { // Perfect Score Champion (3 perfect scores)
                    if (accuracy >= 99.9) {
                        weeklyList[i] = task.copy(currentProgress = (task.currentProgress + 1).coerceAtMost(task.targetGoal))
                    }
                }
                "w_task_03" -> { // Trivia Polymath (4 distinct categories)
                    weeklyList[i] = task.copy(currentProgress = (task.currentProgress + 1).coerceAtMost(task.targetGoal))
                }
            }
        }

        // Update Achievements
        val achievements = userAchievements.getOrPut(userId) { createDefaultAchievements() }
        for (i in achievements.indices) {
            val badge = achievements[i]
            if (badge.isUnlocked) continue

            when (badge.id) {
                "badge_01" -> { // First Steps: First Quiz Played
                    achievements[i] = badge.copy(
                        progress = 1,
                        isUnlocked = true,
                        unlockedDateFormatted = "Unlocked Today"
                    )
                }
                "badge_02" -> { // Trivia Scholar: 10 Quizzes
                    val newProgress = badge.progress + 1
                    achievements[i] = badge.copy(
                        progress = newProgress,
                        isUnlocked = newProgress >= badge.targetGoal,
                        unlockedDateFormatted = if (newProgress >= badge.targetGoal) "Unlocked Today" else null
                    )
                }
                "badge_04" -> { // Perfectionist: 100% Score
                    if (accuracy >= 99.9) {
                        achievements[i] = badge.copy(
                            progress = 1,
                            isUnlocked = true,
                            unlockedDateFormatted = "Unlocked Today"
                        )
                    }
                }
                "badge_05" -> { // Streak Flame: 7-Day Streak
                    achievements[i] = badge.copy(
                        progress = streakDays.coerceAtMost(badge.targetGoal),
                        isUnlocked = streakDays >= badge.targetGoal,
                        unlockedDateFormatted = if (streakDays >= badge.targetGoal) "Unlocked Today" else null
                    )
                }
            }
        }
    }

    override suspend fun checkUtcMidnightRollover(userId: String) = mutex.withLock {
        checkUtcMidnightRolloverInternal(userId)
    }

    private fun checkUtcMidnightRolloverInternal(userId: String) {
        val currentUtcDate = getCurrentUtcDate()
        if (currentUtcDate != lastUtcDateString) {
            // UTC Midnight Rollover: Reset daily challenges
            lastUtcDateString = currentUtcDate
            userDailyTasks[userId] = createDefaultDailyTasks()
        }
    }

    private fun getCurrentUtcDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    // =========================================================================
    // 2. Tournament Lifecycle & Deterministic Tie-Breaking
    // =========================================================================

    override suspend fun getTournaments(userId: String): List<TournamentEvent> = mutex.withLock {
        tournaments.values.map { event ->
            val isUserRegistered = tournamentRegistrations[event.id]?.contains(userId) == true
            event.copy(isRegistered = isUserRegistered)
        }.sortedBy { it.status.ordinal }
    }

    override suspend fun getTournamentDetails(tournamentId: String): TournamentEvent? = mutex.withLock {
        tournaments[tournamentId]
    }

    override suspend fun registerForTournament(
        userId: String,
        tournamentId: String,
        idempotencyKey: String
    ): Result<TournamentEvent> = mutex.withLock {
        val tournament = tournaments[tournamentId]
            ?: return Result.failure(IllegalArgumentException("Tournament '$tournamentId' not found."))

        if (tournament.status == TournamentStatus.COMPLETED) {
            return Result.failure(IllegalStateException("Tournament '${tournament.title}' has already ended."))
        }

        val registeredSet = tournamentRegistrations.getOrPut(tournamentId) { mutableSetOf() }
        if (registeredSet.contains(userId)) {
            return Result.success(tournament.copy(isRegistered = true))
        }

        // Register participant
        registeredSet.add(userId)
        val updatedTournament = tournament.copy(
            registeredParticipants = tournament.registeredParticipants + 1,
            isRegistered = true
        )
        tournaments[tournamentId] = updatedTournament
        return Result.success(updatedTournament)
    }

    override suspend fun submitTournamentScore(
        userId: String,
        tournamentId: String,
        score: Int,
        accuracyRate: Double,
        timeElapsedSeconds: Int,
        idempotencyKey: String
    ): Result<TournamentParticipant> = mutex.withLock {
        val tournament = tournaments[tournamentId]
            ?: return Result.failure(IllegalArgumentException("Tournament '$tournamentId' not found."))

        if (tournament.status != TournamentStatus.LIVE) {
            return Result.failure(IllegalStateException("Tournament '${tournament.title}' is not currently active."))
        }

        val registeredSet = tournamentRegistrations[tournamentId]
        if (registeredSet?.contains(userId) != true) {
            return Result.failure(IllegalStateException("User is not registered for tournament '${tournament.title}'."))
        }

        val participants = tournamentScores.getOrPut(tournamentId) { mutableListOf() }
        participants.removeAll { it.userId == userId }

        val newParticipant = TournamentParticipant(
            userId = userId,
            username = "QuizMaster (You)",
            score = score,
            accuracyRate = accuracyRate,
            timeElapsedSeconds = timeElapsedSeconds,
            completedAtTimestamp = System.currentTimeMillis(),
            avatarInitials = "QM",
            avatarColorHex = 0xFF5B5FEF
        )
        participants.add(newParticipant)

        // Authoritative Deterministic Tie-Breaking:
        // 1. Higher score
        // 2. Higher accuracy rate
        // 3. Lower time elapsed (faster)
        // 4. Earlier completion timestamp
        val sorted = participants.sortedWith(
            compareByDescending<TournamentParticipant> { it.score }
                .thenByDescending { it.accuracyRate }
                .thenBy { it.timeElapsedSeconds }
                .thenBy { it.completedAtTimestamp }
        )

        tournamentScores[tournamentId] = sorted.mapIndexed { index, p ->
            p.copy(rank = index + 1)
        }.toMutableList()

        val evaluatedParticipant = tournamentScores[tournamentId]!!.first { it.userId == userId }
        return Result.success(evaluatedParticipant)
    }

    override suspend fun getTournamentLeaderboard(tournamentId: String): List<TournamentParticipant> = mutex.withLock {
        tournamentScores[tournamentId]?.toList() ?: emptyList()
    }

    override suspend fun finalizeTournament(tournamentId: String): Result<List<TournamentParticipant>> = mutex.withLock {
        val tournament = tournaments[tournamentId]
            ?: return Result.failure(IllegalArgumentException("Tournament '$tournamentId' not found."))

        val participants = tournamentScores[tournamentId] ?: mutableListOf()
        val ranked = participants.sortedWith(
            compareByDescending<TournamentParticipant> { it.score }
                .thenByDescending { it.accuracyRate }
                .thenBy { it.timeElapsedSeconds }
                .thenBy { it.completedAtTimestamp }
        ).mapIndexed { idx, p -> p.copy(rank = idx + 1) }

        tournaments[tournamentId] = tournament.copy(status = TournamentStatus.COMPLETED)
        tournamentScores[tournamentId] = ranked.toMutableList()
        return Result.success(ranked)
    }

    // =========================================================================
    // 3. Leaderboard Aggregator, Periodic Cache (60s), Rate Limiting (5s)
    // =========================================================================

    override suspend fun getLeaderboard(
        timeframe: LeaderboardTimeframe,
        currentUserId: String,
        currentUsername: String,
        forceRefresh: Boolean
    ): Result<LeaderboardUiState> = mutex.withLock {
        val now = System.currentTimeMillis()

        // 1. Check Rate Limiting Guardrail for force-refresh queries
        if (forceRefresh) {
            val elapsedSinceLastQuery = now - lastLeaderboardQueryTime
            if (elapsedSinceLastQuery < RATE_LIMIT_WINDOW_MS) {
                // Rate limited: serve cached data with guardrail indicator
                val cached = leaderboardCache[timeframe]
                if (cached != null) {
                    return Result.success(
                        cached.state.copy(
                            isRateLimited = true,
                            errorMessage = "Leaderboard refreshed recently. Serving cached standings."
                        )
                    )
                }
            }
            lastLeaderboardQueryTime = now
        }

        // 2. Check 60-Second In-Memory Periodic Cache
        if (!forceRefresh) {
            val cached = leaderboardCache[timeframe]
            if (cached != null && (now - cached.timestamp < CACHE_TTL_MS)) {
                return Result.success(cached.state)
            }
        }

        // 3. Server-Authoritative Aggregated Computation (Fair Play: 0 PII)
        val state = computeAuthoritativeLeaderboard(timeframe, currentUserId, currentUsername)
        leaderboardCache[timeframe] = CachedLeaderboard(state, now)
        return Result.success(state)
    }

    private fun computeAuthoritativeLeaderboard(
        timeframe: LeaderboardTimeframe,
        currentUserId: String,
        currentUsername: String
    ): LeaderboardUiState {
        val (podium, remaining, currentUserRank) = when (timeframe) {
            LeaderboardTimeframe.DAILY -> Triple(
                listOf(
                    LeaderboardEntry(1, "NovaScholar", 850, "850 Pts", "NS", 0xFFF5B942, quizzesWon = 8, winStreak = 5, accuracyRate = 92.5),
                    LeaderboardEntry(2, "QuantumMind", 720, "720 Pts", "QM", 0xFF94A3B8, quizzesWon = 7, winStreak = 3, accuracyRate = 88.0),
                    LeaderboardEntry(3, "PixelBrain", 610, "610 Pts", "PB", 0xFFCD7F32, quizzesWon = 6, winStreak = 4, accuracyRate = 85.0)
                ),
                listOf(
                    LeaderboardEntry(4, "CyberFalcon", 550, "550 Pts", "CF", 0xFF5B5FEF, quizzesWon = 5, winStreak = 2, accuracyRate = 82.0),
                    LeaderboardEntry(5, "AstroQuizzer", 480, "480 Pts", "AQ", 0xFF8B5CF6, quizzesWon = 4, winStreak = 1, accuracyRate = 79.5),
                    LeaderboardEntry(6, "SonicTrivia", 430, "430 Pts", "ST", 0xFF2DD4BF, quizzesWon = 4, winStreak = 3, accuracyRate = 77.0),
                    LeaderboardEntry(7, "EchoWiz", 390, "390 Pts", "EW", 0xFFEC4899, quizzesWon = 3, winStreak = 1, accuracyRate = 75.0),
                    LeaderboardEntry(8, "VortexPlayer", 350, "350 Pts", "VP", 0xFF3B82F6, quizzesWon = 3, winStreak = 2, accuracyRate = 73.0),
                    LeaderboardEntry(9, "ZenithThinker", 310, "310 Pts", "ZT", 0xFF10B981, quizzesWon = 2, winStreak = 1, accuracyRate = 70.0),
                    LeaderboardEntry(10, "CosmicAce", 280, "280 Pts", "CA", 0xFF6366F1, quizzesWon = 2, winStreak = 0, accuracyRate = 68.0)
                ),
                12
            )
            LeaderboardTimeframe.WEEKLY -> Triple(
                listOf(
                    LeaderboardEntry(1, "QuizChampion", 4250, "4,250 Pts", "QC", 0xFFF5B942, quizzesWon = 32, winStreak = 12, accuracyRate = 94.0),
                    LeaderboardEntry(2, "TriviaKing", 3890, "3,890 Pts", "TK", 0xFF94A3B8, quizzesWon = 28, winStreak = 8, accuracyRate = 91.5),
                    LeaderboardEntry(3, "BrainMaster", 3400, "3,400 Pts", "BM", 0xFFCD7F32, quizzesWon = 25, winStreak = 7, accuracyRate = 89.0)
                ),
                listOf(
                    LeaderboardEntry(4, "StarStudent", 2950, "2,950 Pts", "SS", 0xFF5B5FEF, quizzesWon = 21, winStreak = 5, accuracyRate = 86.0),
                    LeaderboardEntry(5, "QuizLover", 2600, "2,600 Pts", "QL", 0xFF8B5CF6, quizzesWon = 19, winStreak = 4, accuracyRate = 84.0),
                    LeaderboardEntry(6, "HyperMind", 2320, "2,320 Pts", "HM", 0xFF2DD4BF, quizzesWon = 17, winStreak = 6, accuracyRate = 82.5),
                    LeaderboardEntry(7, "AlphaSeeker", 2100, "2,100 Pts", "AS", 0xFFEC4899, quizzesWon = 15, winStreak = 3, accuracyRate = 80.0),
                    LeaderboardEntry(8, "OmegaScholar", 1950, "1,950 Pts", "OS", 0xFF3B82F6, quizzesWon = 14, winStreak = 2, accuracyRate = 78.0),
                    LeaderboardEntry(9, "PrismIntellect", 1820, "1,820 Pts", "PI", 0xFF10B981, quizzesWon = 13, winStreak = 4, accuracyRate = 76.5),
                    LeaderboardEntry(10, "ApexThinker", 1690, "1,690 Pts", "AT", 0xFF6366F1, quizzesWon = 12, winStreak = 2, accuracyRate = 75.0)
                ),
                14
            )
            LeaderboardTimeframe.ALL_TIME -> Triple(
                listOf(
                    LeaderboardEntry(1, "GrandArchon", 28500, "28,500 Pts", "GA", 0xFFF5B942, quizzesWon = 210, winStreak = 24, accuracyRate = 96.0),
                    LeaderboardEntry(2, "ApexLegend", 24100, "24,100 Pts", "AL", 0xFF94A3B8, quizzesWon = 185, winStreak = 18, accuracyRate = 93.5),
                    LeaderboardEntry(3, "NexusSage", 21800, "21,800 Pts", "NS", 0xFFCD7F32, quizzesWon = 160, winStreak = 15, accuracyRate = 91.0)
                ),
                listOf(
                    LeaderboardEntry(4, "QuizChampion", 19200, "19,200 Pts", "QC", 0xFF5B5FEF, quizzesWon = 145, winStreak = 12, accuracyRate = 89.0),
                    LeaderboardEntry(5, "TriviaKing", 17500, "17,500 Pts", "TK", 0xFF8B5CF6, quizzesWon = 130, winStreak = 11, accuracyRate = 87.0),
                    LeaderboardEntry(6, "BrainMaster", 15800, "15,800 Pts", "BM", 0xFF2DD4BF, quizzesWon = 118, winStreak = 9, accuracyRate = 85.5),
                    LeaderboardEntry(7, "OmniScholar", 14200, "14,200 Pts", "OS", 0xFFEC4899, quizzesWon = 105, winStreak = 8, accuracyRate = 84.0),
                    LeaderboardEntry(8, "StarStudent", 12900, "12,900 Pts", "SS", 0xFF3B82F6, quizzesWon = 94, winStreak = 7, accuracyRate = 82.0),
                    LeaderboardEntry(9, "SolarMind", 11600, "11,600 Pts", "SM", 0xFF10B981, quizzesWon = 85, winStreak = 6, accuracyRate = 80.5),
                    LeaderboardEntry(10, "TitanQuizzer", 10400, "10,400 Pts", "TQ", 0xFF6366F1, quizzesWon = 78, winStreak = 5, accuracyRate = 79.0)
                ),
                18
            )
        }

        val userInitials = if (currentUsername.length >= 2) currentUsername.take(2).uppercase() else "QM"
        val currentUserEntry = LeaderboardEntry(
            rank = currentUserRank,
            username = if (currentUsername.contains("(You)")) currentUsername else "$currentUsername (You)",
            points = when (timeframe) {
                LeaderboardTimeframe.DAILY -> 240
                LeaderboardTimeframe.WEEKLY -> 1450
                LeaderboardTimeframe.ALL_TIME -> 5450
            },
            pointsFormatted = when (timeframe) {
                LeaderboardTimeframe.DAILY -> "240 Pts"
                LeaderboardTimeframe.WEEKLY -> "1,450 Pts"
                LeaderboardTimeframe.ALL_TIME -> "5,450 Pts"
            },
            avatarInitials = userInitials,
            avatarColorHex = 0xFF5B5FEF,
            isCurrentUser = true,
            quizzesWon = when (timeframe) {
                LeaderboardTimeframe.DAILY -> 2
                LeaderboardTimeframe.WEEKLY -> 10
                LeaderboardTimeframe.ALL_TIME -> 42
            },
            winStreak = 4
        )

        return LeaderboardUiState(
            selectedTimeframe = timeframe,
            topThree = podium,
            remainingEntries = remaining,
            currentUserEntry = currentUserEntry,
            isLoading = false,
            isRateLimited = false,
            errorMessage = null,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    // =========================================================================
    // Defaults & Seed Initializers
    // =========================================================================

    private fun createDefaultDailyTasks(): MutableList<TaskItem> = mutableListOf(
        TaskItem(
            id = "d_task_01",
            title = "Play 3 Quizzes Today",
            description = "Test your knowledge across any categories",
            rewardPoints = 25L,
            currentProgress = 2,
            targetGoal = 3,
            type = TaskType.DAILY
        ),
        TaskItem(
            id = "d_task_02",
            title = "Score 80%+ on Science Quiz",
            description = "Demonstrate science & nature prowess",
            rewardPoints = 30L,
            currentProgress = 1,
            targetGoal = 1,
            type = TaskType.DAILY
        ),
        TaskItem(
            id = "d_task_03",
            title = "Maintain 3-Day Streak",
            description = "Check in and play every consecutive day",
            rewardPoints = 20L,
            currentProgress = 3,
            targetGoal = 3,
            isClaimed = true,
            type = TaskType.DAILY
        ),
        TaskItem(
            id = "d_task_04",
            title = "Complete 1 Speed Round",
            description = "Finish a 10-question quiz in under 60 seconds",
            rewardPoints = 35L,
            currentProgress = 0,
            targetGoal = 1,
            type = TaskType.DAILY
        )
    )

    private fun createDefaultWeeklyTasks(): MutableList<TaskItem> = mutableListOf(
        TaskItem(
            id = "w_task_01",
            title = "Quiz Marathon Master",
            description = "Complete 20 quizzes within this calendar week",
            rewardPoints = 150L,
            currentProgress = 14,
            targetGoal = 20,
            type = TaskType.WEEKLY
        ),
        TaskItem(
            id = "w_task_02",
            title = "Perfect Score Champion",
            description = "Earn a 100% score on 3 different quizzes",
            rewardPoints = 200L,
            currentProgress = 2,
            targetGoal = 3,
            type = TaskType.WEEKLY
        ),
        TaskItem(
            id = "w_task_03",
            title = "Trivia Polymath",
            description = "Play at least one quiz across 4 distinct categories",
            rewardPoints = 100L,
            currentProgress = 4,
            targetGoal = 4,
            isClaimed = false,
            type = TaskType.WEEKLY
        ),
        TaskItem(
            id = "w_task_04",
            title = "Accumulate 1,000 Points",
            description = "Win points across quizzes and daily streaks",
            rewardPoints = 120L,
            currentProgress = 850,
            targetGoal = 1000,
            type = TaskType.WEEKLY
        )
    )

    private fun createDefaultAchievements(): MutableList<AchievementBadge> = mutableListOf(
        AchievementBadge(
            id = "badge_01",
            name = "First Steps",
            description = "Complete your very first quiz session",
            categoryKey = "first_quiz",
            isUnlocked = true,
            unlockedDateFormatted = "Oct 12, 2026",
            progress = 1,
            targetGoal = 1,
            rewardPoints = 50L
        ),
        AchievementBadge(
            id = "badge_02",
            name = "Trivia Scholar",
            description = "Successfully complete 10 full quiz sessions",
            categoryKey = "quizzes_10",
            isUnlocked = true,
            unlockedDateFormatted = "Oct 18, 2026",
            progress = 10,
            targetGoal = 10,
            rewardPoints = 100L
        ),
        AchievementBadge(
            id = "badge_03",
            name = "Grandmaster",
            description = "Accumulate a career balance of 5,000 points",
            categoryKey = "points_5000",
            isUnlocked = true,
            unlockedDateFormatted = "Oct 25, 2026",
            progress = 5450,
            targetGoal = 5000,
            rewardPoints = 250L
        ),
        AchievementBadge(
            id = "badge_04",
            name = "Perfectionist",
            description = "Score 100% on any 10-question quiz",
            categoryKey = "perfect_score",
            isUnlocked = false,
            progress = 0,
            targetGoal = 1,
            rewardPoints = 150L
        ),
        AchievementBadge(
            id = "badge_05",
            name = "Streak Flame",
            description = "Maintain an uninterrupted 7-day play streak",
            categoryKey = "streak_7",
            isUnlocked = false,
            progress = 4,
            targetGoal = 7,
            rewardPoints = 200L
        ),
        AchievementBadge(
            id = "badge_06",
            name = "Speed Demon",
            description = "Answer all 10 questions in under 45 seconds total",
            categoryKey = "speed_45s",
            isUnlocked = false,
            progress = 0,
            targetGoal = 1,
            rewardPoints = 100L
        )
    )

    private fun initDefaultTournaments() {
        tournaments["tourn_01"] = TournamentEvent(
            id = "tourn_01",
            title = "Weekend Grand Championship",
            description = "Top 10 players win certified prize pool coins",
            categoryName = "General Knowledge",
            categoryId = "cat_general",
            entryFeePoints = 50L,
            prizePoolPoints = 5000L,
            registeredParticipants = 312,
            remainingTimeFormatted = "03h 45m Remaining",
            status = TournamentStatus.LIVE,
            isRegistered = true,
            minLevelRequired = 1
        )
        tournamentRegistrations["tourn_01"] = mutableSetOf("mock_user_123")

        tournaments["tourn_02"] = TournamentEvent(
            id = "tourn_02",
            title = "Science Olympiad Blitz",
            description = "Rapid fire questions on physics, biology, and chemistry",
            categoryName = "Science & Nature",
            categoryId = "cat_science",
            entryFeePoints = 25L,
            prizePoolPoints = 2500L,
            registeredParticipants = 168,
            remainingTimeFormatted = "01h 12m Remaining",
            status = TournamentStatus.LIVE,
            isRegistered = false,
            minLevelRequired = 2
        )

        tournaments["tourn_03"] = TournamentEvent(
            id = "tourn_03",
            title = "Ancient Empires Clash",
            description = "From Mesopotamia to the Roman and Ottoman Dynasties",
            categoryName = "History & Geo",
            categoryId = "cat_history",
            entryFeePoints = 20L,
            prizePoolPoints = 1500L,
            registeredParticipants = 94,
            remainingTimeFormatted = "Starts in 6 hours",
            status = TournamentStatus.UPCOMING,
            isRegistered = false,
            minLevelRequired = 1
        )

        tournaments["tourn_04"] = TournamentEvent(
            id = "tourn_04",
            title = "Silicon Valley Tech Cup",
            description = "Algorithms, computing pioneers, and digital evolution",
            categoryName = "Technology & Code",
            categoryId = "cat_tech",
            entryFeePoints = 30L,
            prizePoolPoints = 3000L,
            registeredParticipants = 120,
            remainingTimeFormatted = "Starts Tomorrow",
            status = TournamentStatus.UPCOMING,
            isRegistered = false,
            minLevelRequired = 3
        )
    }
}
