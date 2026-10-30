package com.quizearn.app.domain.model

/**
 * Lifecycle status of a tournament event.
 */
enum class TournamentStatus {
    UPCOMING,
    LIVE,
    COMPLETED
}

/**
 * Representation of a scheduled live tournament event.
 */
data class TournamentEvent(
    val id: String,
    val title: String,
    val description: String,
    val categoryName: String,
    val categoryId: String = "general_knowledge",
    val entryFeePoints: Long,
    val prizePoolPoints: Long,
    val registeredParticipants: Int,
    val remainingTimeFormatted: String,
    val status: TournamentStatus,
    val isRegistered: Boolean = false,
    val minLevelRequired: Int = 1,
    val startTimeIso: String = "",
    val endTimeIso: String = "",
    val topPrizePoints: Long = prizePoolPoints / 2
)

/**
 * Representation of a participant's submission in a tournament.
 */
data class TournamentParticipant(
    val userId: String,
    val username: String,
    val score: Int,
    val accuracyRate: Double,
    val timeElapsedSeconds: Int,
    val completedAtTimestamp: Long,
    val rank: Int = 0,
    val avatarInitials: String = "",
    val avatarColorHex: Long = 0xFF5B5FEF
)
