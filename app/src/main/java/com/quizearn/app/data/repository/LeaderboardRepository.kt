package com.quizearn.app.data.repository

import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.domain.model.LeaderboardUiState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for retrieving cached, rate-limited, and sanitized leaderboard rankings.
 */
interface LeaderboardRepository {
    fun getLeaderboard(
        timeframe: LeaderboardTimeframe,
        forceRefresh: Boolean = false
    ): Flow<Result<LeaderboardUiState>>

    suspend fun refreshLeaderboard(timeframe: LeaderboardTimeframe): Result<LeaderboardUiState>
}
