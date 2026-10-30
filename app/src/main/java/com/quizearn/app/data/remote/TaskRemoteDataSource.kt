package com.quizearn.app.data.remote

import com.quizearn.app.domain.model.AchievementBadge
import com.quizearn.app.domain.model.LeaderboardEntry
import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.domain.model.LeaderboardUiState
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TournamentEvent
import com.quizearn.app.domain.model.TournamentParticipant

/**
 * Remote data source contract for Tasks, Tournaments, and Leaderboard operations.
 */
interface TaskRemoteDataSource {

    // --- Task Progression & Claims ---
    suspend fun getDailyTasks(userId: String): List<TaskItem>
    suspend fun getWeeklyTasks(userId: String): List<TaskItem>
    suspend fun getAchievements(userId: String): List<AchievementBadge>
    suspend fun claimTaskReward(userId: String, taskId: String, idempotencyKey: String): Result<Long>
    suspend fun recordQuizProgression(
        userId: String,
        category: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        streakDays: Int
    )
    suspend fun checkUtcMidnightRollover(userId: String)

    // --- Tournaments ---
    suspend fun getTournaments(userId: String): List<TournamentEvent>
    suspend fun getTournamentDetails(tournamentId: String): TournamentEvent?
    suspend fun registerForTournament(userId: String, tournamentId: String, idempotencyKey: String): Result<TournamentEvent>
    suspend fun submitTournamentScore(
        userId: String,
        tournamentId: String,
        score: Int,
        accuracyRate: Double,
        timeElapsedSeconds: Int,
        idempotencyKey: String
    ): Result<TournamentParticipant>
    suspend fun submitTournamentResult(
        userId: String,
        tournamentId: String,
        sessionId: String,
        idempotencyKey: String
    ): Result<TournamentParticipant> = Result.failure(
        UnsupportedOperationException("Verified tournament result submission is unavailable.")
    )
    suspend fun getTournamentLeaderboard(tournamentId: String): List<TournamentParticipant>
    suspend fun finalizeTournament(tournamentId: String): Result<List<TournamentParticipant>>

    // --- Leaderboards ---
    suspend fun getLeaderboard(
        timeframe: LeaderboardTimeframe,
        currentUserId: String,
        currentUsername: String,
        forceRefresh: Boolean = false
    ): Result<LeaderboardUiState>
}
