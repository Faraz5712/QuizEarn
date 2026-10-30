package com.quizearn.app.domain.model

/**
 * Lifecycle status of a points redemption request.
 */
enum class RedemptionStatus(val displayName: String) {
    PENDING("Pending Verification"),
    PROCESSING("Processing"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

/**
 * Domain representation of a user redemption request.
 * Transitions through PENDING -> PROCESSING -> APPROVED (with digital voucher code)
 * or REJECTED (with mandatory rejection reason and automated ledger points refund).
 */
data class Redemption(
    val id: String,
    val userId: String,
    val rewardId: String,
    val rewardTitle: String,
    val pointsDeducted: Long,
    val payoutDetails: String,
    val status: RedemptionStatus = RedemptionStatus.PENDING,
    val rejectionReason: String? = null,
    val digitalCode: String? = null,
    val createdAtIso: String,
    val processedAtIso: String? = null
)
