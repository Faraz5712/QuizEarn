package com.quizearn.app.domain.model

/**
 * Digital voucher or gift card available for point redemption.
 */
data class Reward(
    val id: String,
    val title: String,
    val description: String,
    val pointsCost: Long,
    val imageUrl: String? = null,
    val category: String = "GIFT_CARD",
    val stockQuantity: Int = 50,
    val isActive: Boolean = true,
    val minLevelRequired: Int = 1
)
