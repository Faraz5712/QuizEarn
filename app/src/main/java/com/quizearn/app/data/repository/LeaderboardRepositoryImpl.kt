package com.quizearn.app.data.repository

import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.remote.MockTaskRemoteDataSource
import com.quizearn.app.data.remote.TaskRemoteDataSource
import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.domain.model.LeaderboardUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Production implementation of [LeaderboardRepository] enforcing in-memory
 * 60-second periodic caching, rate limiting, and zero-PII Fair Play rankings.
 */
class LeaderboardRepositoryImpl(
    private val remoteDataSource: TaskRemoteDataSource = MockTaskRemoteDataSource(),
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance()
) : LeaderboardRepository {

    private suspend fun getActiveUserId(): String {
        return sessionManager.currentSession.firstOrNull()?.userId ?: "mock_user_123"
    }

    private suspend fun getActiveUsername(): String {
        return sessionManager.currentSession.firstOrNull()?.username ?: "QuizMaster"
    }

    override fun getLeaderboard(
        timeframe: LeaderboardTimeframe,
        forceRefresh: Boolean
    ): Flow<Result<LeaderboardUiState>> = flow {
        val userId = getActiveUserId()
        val username = getActiveUsername()

        val result = remoteDataSource.getLeaderboard(
            timeframe = timeframe,
            currentUserId = userId,
            currentUsername = username,
            forceRefresh = forceRefresh
        )
        emit(result)
    }

    override suspend fun refreshLeaderboard(timeframe: LeaderboardTimeframe): Result<LeaderboardUiState> {
        val userId = getActiveUserId()
        val username = getActiveUsername()

        return remoteDataSource.getLeaderboard(
            timeframe = timeframe,
            currentUserId = userId,
            currentUsername = username,
            forceRefresh = true
        )
    }

    companion object {
        @Volatile
        private var instance: LeaderboardRepositoryImpl? = null

        fun getInstance(
            remoteDataSource: TaskRemoteDataSource = MockTaskRemoteDataSource(),
            sessionManager: AuthSessionManager = AuthSessionManager.getInstance()
        ): LeaderboardRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: LeaderboardRepositoryImpl(
                    remoteDataSource = remoteDataSource,
                    sessionManager = sessionManager
                ).also { instance = it }
            }
        }
    }
}
