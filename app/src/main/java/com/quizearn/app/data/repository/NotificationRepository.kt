package com.quizearn.app.data.repository

import com.quizearn.app.domain.model.NotificationItem
import com.quizearn.app.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing in-app notifications and inbox status.
 */
interface NotificationRepository {
    val notifications: Flow<List<NotificationItem>>
    val unreadCount: Flow<Int>

    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(notificationId: String)
    suspend fun clearAll()

    suspend fun triggerNotification(
        title: String,
        body: String,
        type: NotificationType,
        actionRoute: String? = null
    ): NotificationItem
}
