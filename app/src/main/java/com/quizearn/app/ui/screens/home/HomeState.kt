package com.quizearn.app.ui.screens.home

import com.quizearn.app.domain.model.User

/**
 * Presentation model for user's daily check-in reward status.
 */
data class DailyRewardStatus(
    val isClaimedToday: Boolean = false,
    val currentStreakDays: Int = 3,
    val bonusPoints: Long = 25L,
    val nextBonusAtEpochMs: Long = 0L
)

/**
 * Presentation model for featured high-reward quiz banners in the home carousel.
 */
data class FeaturedQuiz(
    val id: String,
    val title: String,
    val subtitle: String,
    val categoryId: String,
    val rewardPoints: Long,
    val questionCount: Int,
    val durationMinutes: Int,
    val tag: String,
    val accentColorHex: String
)

/**
 * Category display model with presentation-specific styling and icons.
 */
data class QuizCategoryDisplay(
    val id: String,
    val name: String,
    val slug: String,
    val totalQuestions: Int,
    val difficulty: String,
    val accentColorHex: String,
    val iconKey: CategoryIconKey
)

enum class CategoryIconKey {
    GENERAL,
    SCIENCE,
    TECH,
    HISTORY,
    SPORTS,
    ARTS
}

/**
 * Preview model for active live tournaments.
 */
data class LiveTournamentPreview(
    val id: String,
    val title: String,
    val prizePool: Long,
    val remainingTimeFormatted: String,
    val activeContenders: Int
)

/**
 * Daily challenge progression indicator.
 */
data class DailyChallengeProgress(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetGoal: Int,
    val rewardBonus: Long
)

/**
 * Overall state container for the Home Dashboard.
 */
data class HomeUiState(
    val currentUser: User? = null,
    val dailyReward: DailyRewardStatus = DailyRewardStatus(),
    val featuredQuizzes: List<FeaturedQuiz> = emptyList(),
    val categories: List<QuizCategoryDisplay> = emptyList(),
    val activeTournament: LiveTournamentPreview? = null,
    val dailyChallenge: DailyChallengeProgress? = null,
    val isLoading: Boolean = false,
    val isClaimingReward: Boolean = false,
    val errorMessage: String? = null
)
