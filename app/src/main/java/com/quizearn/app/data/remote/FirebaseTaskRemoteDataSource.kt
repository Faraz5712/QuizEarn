package com.quizearn.app.data.remote

import com.quizearn.app.domain.model.AchievementBadge
import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.domain.model.LeaderboardUiState
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TaskType
import com.quizearn.app.domain.model.TournamentEvent
import com.quizearn.app.domain.model.TournamentParticipant
import com.quizearn.app.domain.model.TournamentStatus
class FirebaseTaskRemoteDataSource : TaskRemoteDataSource {
    override suspend fun getDailyTasks(userId: String): List<TaskItem> =
        taskState().filter { it.type == TaskType.DAILY }

    override suspend fun getWeeklyTasks(userId: String): List<TaskItem> =
        taskState().filter { it.type == TaskType.WEEKLY }

    override suspend fun getAchievements(userId: String): List<AchievementBadge> = emptyList()

    override suspend fun claimTaskReward(
        userId: String,
        taskId: String,
        idempotencyKey: String
    ): Result<Long> = runCatching {
        val response = FirebaseBackend.call(
            "claimTaskReward",
            mapOf("taskId" to taskId, "idempotencyKey" to idempotencyKey)
        )
        response.number("pointsEarned").toLong()
    }

    override suspend fun recordQuizProgression(
        userId: String,
        category: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        streakDays: Int
    ) {
        // Progress is derived from the verified quiz result inside the callable transaction.
    }

    override suspend fun checkUtcMidnightRollover(userId: String) = Unit

    override suspend fun getTournaments(userId: String): List<TournamentEvent> =
        tournamentList().map { it.toTournamentEvent() }

    override suspend fun getTournamentDetails(tournamentId: String): TournamentEvent? =
        tournamentList().firstOrNull { it["id"] == tournamentId }?.toTournamentEvent()

    override suspend fun registerForTournament(
        userId: String,
        tournamentId: String,
        idempotencyKey: String
    ): Result<TournamentEvent> = runCatching {
        FirebaseBackend.call(
            "registerForTournament",
            mapOf("tournamentId" to tournamentId, "idempotencyKey" to idempotencyKey)
        )
        getTournamentDetails(tournamentId) ?: error("Tournament was not returned by the backend.")
    }

    override suspend fun submitTournamentScore(
        userId: String,
        tournamentId: String,
        score: Int,
        accuracyRate: Double,
        timeElapsedSeconds: Int,
        idempotencyKey: String
    ): Result<TournamentParticipant> = Result.failure(
        SecurityException("Tournament scores must be submitted using a verified quiz result.")
    )

    override suspend fun submitTournamentResult(
        userId: String,
        tournamentId: String,
        sessionId: String,
        idempotencyKey: String
    ): Result<TournamentParticipant> = runCatching {
        FirebaseBackend.call(
            "submitTournamentResult",
            mapOf(
                "tournamentId" to tournamentId,
                "sessionId" to sessionId,
                "idempotencyKey" to idempotencyKey
            )
        ).toTournamentParticipant()
    }

    override suspend fun getTournamentLeaderboard(tournamentId: String): List<TournamentParticipant> {
        val response = FirebaseBackend.call("getTournamentLeaderboard", mapOf("tournamentId" to tournamentId))
        return response.listOfMaps("participants").map { it.toTournamentParticipant() }
    }

    override suspend fun finalizeTournament(tournamentId: String): Result<List<TournamentParticipant>> =
        runCatching {
            FirebaseBackend.call("finalizeTournament", mapOf("tournamentId" to tournamentId))
            getTournamentLeaderboard(tournamentId)
        }

    override suspend fun getLeaderboard(
        timeframe: LeaderboardTimeframe,
        currentUserId: String,
        currentUsername: String,
        forceRefresh: Boolean
    ): Result<LeaderboardUiState> = Result.failure(
        UnsupportedOperationException("Firebase leaderboard callable is not implemented.")
    )

    private suspend fun taskState(): List<TaskItem> =
        FirebaseBackend.call("getTaskState").listOfMaps("tasks").map { task ->
            TaskItem(
                id = task.string("id"),
                title = task.string("title"),
                description = task.string("description"),
                rewardPoints = task.number("rewardPoints").toLong(),
                currentProgress = task.number("currentProgress").toInt(),
                targetGoal = task.number("targetGoal").toInt(),
                isClaimed = task["isClaimed"] as? Boolean ?: false,
                type = when (task.string("type")) {
                    "WEEKLY" -> TaskType.WEEKLY
                    else -> TaskType.DAILY
                },
                categoryKey = task["categoryKey"] as? String,
                claimedAtTimestamp = (task["claimedAtTimestamp"] as? Number)?.toLong()
            )
        }

    private suspend fun tournamentList(): List<Map<String, Any?>> =
        FirebaseBackend.call("getTournaments").listOfMaps("tournaments")

    private fun Map<String, Any?>.toTournamentEvent(): TournamentEvent {
        val start = string("startTimeIso")
        val end = string("endTimeIso")
        return TournamentEvent(
            id = string("id"),
            title = string("title"),
            description = string("description"),
            categoryName = string("categoryName"),
            categoryId = string("categoryId"),
            entryFeePoints = number("entryFeePoints").toLong(),
            prizePoolPoints = number("prizePoolPoints").toLong(),
            registeredParticipants = number("registeredParticipants").toInt(),
            remainingTimeFormatted = end,
            status = when (string("status")) {
                "LIVE" -> TournamentStatus.LIVE
                "COMPLETED", "ENDED", "CANCELLED" -> TournamentStatus.COMPLETED
                else -> TournamentStatus.UPCOMING
            },
            isRegistered = this["isRegistered"] as? Boolean ?: false,
            minLevelRequired = number("minLevelRequired", 1.0).toInt(),
            startTimeIso = start,
            endTimeIso = end,
            topPrizePoints = (number("prizePoolPoints").toLong() * 50L) / 100L
        )
    }

    private fun Map<String, Any?>.toTournamentParticipant(): TournamentParticipant =
        TournamentParticipant(
            userId = string("userId"),
            username = string("username"),
            score = number("score").toInt(),
            accuracyRate = number("accuracyRate").toDouble(),
            timeElapsedSeconds = number("timeElapsedSeconds").toInt(),
            completedAtTimestamp = number("completedAtTimestamp").toLong(),
            rank = number("rank").toInt(),
            avatarInitials = string("username").take(2).uppercase()
        )

    private fun Map<String, Any?>.listOfMaps(key: String): List<Map<String, Any?>> {
        val values = this[key] as? List<*> ?: error("Firebase callable response is missing '$key'.")
        return values.map { value ->
            val map = value as? Map<*, *> ?: error("Firebase callable returned an invalid '$key' item.")
            map.entries.associate { (entryKey, entryValue) ->
                (entryKey as? String ?: error("Firebase callable returned a non-string key.")) to entryValue
            }
        }
    }

    private fun Map<String, Any?>.string(key: String): String =
        this[key] as? String ?: error("Firebase callable response is missing '$key'.")

    private fun Map<String, Any?>.number(key: String, default: Double? = null): Number =
        this[key] as? Number ?: default ?: error("Firebase callable response is missing '$key'.")
}
