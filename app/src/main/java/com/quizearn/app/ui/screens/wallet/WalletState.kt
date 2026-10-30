package com.quizearn.app.ui.screens.wallet

/**
 * Categorization of points transactions in the ledger.
 */
enum class TransactionType(val displayName: String) {
    QUIZ_REWARD("Quiz Reward"),
    DAILY_CHECKIN("Daily Check-in"),
    TASK_COMPLETED("Task Completed"),
    TOURNAMENT_PRIZE("Tournament Prize"),
    REDEMPTION("Reward Redemption")
}

/**
 * Status of a points transaction.
 */
enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

/**
 * Single ledger transaction entry.
 */
data class TransactionItem(
    val id: String,
    val type: TransactionType,
    val title: String,
    val timestampFormatted: String,
    val pointsDelta: Int,
    val isCredit: Boolean,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

/**
 * Supported reward provider brands.
 */
enum class RewardBrand(val brandName: String) {
    AMAZON("Amazon"),
    GOOGLE_PLAY("Google Play"),
    APPLE("Apple App Store"),
    MOBILE_RECHARGE("Mobile Top-Up")
}

/**
 * Reward voucher or gift card item in the catalog.
 */
data class RewardItem(
    val id: String,
    val name: String,
    val brand: RewardBrand,
    val description: String,
    val pointsRequired: Int,
    val monetaryValueUsd: Double,
    val isAvailable: Boolean = true,
    val minLevelRequired: Int = 1
)

/**
 * Status of a user reward redemption request.
 */
enum class RedemptionStatus(val displayName: String) {
    PENDING("Pending Verification"),
    PROCESSING("Processing"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

/**
 * Record of a requested reward redemption.
 */
data class RedemptionRequest(
    val id: String,
    val rewardId: String,
    val rewardName: String,
    val pointsSpent: Int,
    val recipientTarget: String,
    val requestedAtFormatted: String,
    val status: RedemptionStatus = RedemptionStatus.PENDING,
    val digitalCodeMasked: String? = null
)

/**
 * Complete UI state for the Wallet and Rewards management suite.
 */
data class WalletUiState(
    val pointsBalance: Int = 5450,
    val pointsInReview: Int = 0,
    val monetaryEquivalentFormatted: String = "$5.45",
    val minRedemptionThreshold: Int = 5000,
    val canRedeem: Boolean = true,
    val recentTransactions: List<TransactionItem> = emptyList(),
    val availableRewards: List<RewardItem> = emptyList(),
    val redemptions: List<RedemptionRequest> = emptyList()
)
