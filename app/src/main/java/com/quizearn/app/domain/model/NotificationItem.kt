package com.quizearn.app.domain.model

/**
 * Category/Type of in-app and push notification.
 */
enum class NotificationType {
    WALLET_CREDIT,
    TOURNAMENT_ALERT,
    STREAK_REMINDER,
    SUPPORT_UPDATE,
    SYSTEM_ANNOUNCEMENT
}

/**
 * Domain model representing an in-app or push notification.
 */
data class NotificationItem(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val timestampIso: String,
    val isRead: Boolean = false,
    val actionRoute: String? = null
)
