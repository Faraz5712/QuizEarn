package com.quizearn.app.domain.model

/**
 * Timeframe options for competitive ranking.
 */
enum class LeaderboardTimeframe(val label: String) {
    DAILY("Today"),
    WEEKLY("This Week"),
    ALL_TIME("All Time")
}

/**
 * Representation of a ranked player in the leaderboard.
 * Respects Fair Play and Privacy principles: uses sanitized usernames only, zero PII.
 */
data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val points: Int,
    val pointsFormatted: String,
    val avatarInitials: String,
    val avatarColorHex: Long = 0xFF5B5FEF,
    val isCurrentUser: Boolean = false,
    val quizzesWon: Int = 0,
    val winStreak: Int = 0,
    val accuracyRate: Double = 0.0
)

/**
 * Complete UI / state representation for the Leaderboard.
 */
data class LeaderboardUiState(
    val selectedTimeframe: LeaderboardTimeframe = LeaderboardTimeframe.WEEKLY,
    val topThree: List<LeaderboardEntry> = emptyList(),
    val remainingEntries: List<LeaderboardEntry> = emptyList(),
    val currentUserEntry: LeaderboardEntry? = null,
    val isLoading: Boolean = false,
    val isRateLimited: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
