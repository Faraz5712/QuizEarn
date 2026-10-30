package com.quizearn.app.data.repository

import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.remote.ConditionalPushNotificationService
import com.quizearn.app.data.remote.PushNotificationService
import com.quizearn.app.domain.model.NotificationItem
import com.quizearn.app.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Production implementation of [NotificationRepository] managing in-app notifications,
 * read states, badge counts, and conditional FCM dispatch.
 */
class NotificationRepositoryImpl(
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
    private val pushService: PushNotificationService = ConditionalPushNotificationService.getInstance()
) : NotificationRepository {

    private val mutex = Mutex()
    private val _notifications = MutableStateFlow<List<NotificationItem>>(createDefaultNotifications())

    override val notifications: Flow<List<NotificationItem>> = _notifications.asStateFlow()

    override val unreadCount: Flow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }

    private suspend fun getActiveUserId(): String {
        return sessionManager.currentSession.firstOrNull()?.userId ?: "mock_user_123"
    }

    override suspend fun markAsRead(notificationId: String) = mutex.withLock {
        _notifications.value = _notifications.value.map { item ->
            if (item.id == notificationId) item.copy(isRead = true) else item
        }
    }

    override suspend fun markAllAsRead() = mutex.withLock {
        _notifications.value = _notifications.value.map { item ->
            item.copy(isRead = true)
        }
    }

    override suspend fun deleteNotification(notificationId: String) = mutex.withLock {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
    }

    override suspend fun clearAll() = mutex.withLock {
        _notifications.value = emptyList()
    }

    override suspend fun triggerNotification(
        title: String,
        body: String,
        type: NotificationType,
        actionRoute: String?
    ): NotificationItem = mutex.withLock {
        val userId = getActiveUserId()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val newItem = NotificationItem(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            title = title,
            body = body,
            type = type,
            timestampIso = isoFormat.format(Date()),
            isRead = false,
            actionRoute = actionRoute
        )

        _notifications.value = listOf(newItem) + _notifications.value

        // Dispatch optional push notification preview if configured
        pushService.sendPushPreview(
            title = title,
            body = body,
            payload = mapOf("actionRoute" to (actionRoute ?: ""))
        )

        return newItem
    }

    private fun createDefaultNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "notif_01",
            userId = "mock_user_123",
            title = "Weekend Grand Championship Live!",
            body = "The 5,000 Pt tournament is now live. Enter now to compete for top spots!",
            type = NotificationType.TOURNAMENT_ALERT,
            timestampIso = "15 minutes ago",
            isRead = false,
            actionRoute = "tournament_lobby"
        ),
        NotificationItem(
            id = "notif_02",
            userId = "mock_user_123",
            title = "Daily Challenge Completed",
            body = "You completed 'Score 80%+ on Science Quiz'. 30 Pts ready to claim!",
            type = NotificationType.WALLET_CREDIT,
            timestampIso = "2 hours ago",
            isRead = false,
            actionRoute = "tasks"
        ),
        NotificationItem(
            id = "notif_03",
            userId = "mock_user_123",
            title = "Keep Your Streak Alive! 🔥",
            body = "You're on a 4-day check-in streak. Play today to maintain your streak bonus.",
            type = NotificationType.STREAK_REMINDER,
            timestampIso = "5 hours ago",
            isRead = true,
            actionRoute = "home"
        ),
        NotificationItem(
            id = "notif_04",
            userId = "mock_user_123",
            title = "Redemption Approved",
            body = "Your $5 Google Play voucher has been verified. Your code is ready in your wallet.",
            type = NotificationType.WALLET_CREDIT,
            timestampIso = "Yesterday",
            isRead = true,
            actionRoute = "reward_catalog"
        )
    )

    companion object {
        @Volatile
        private var instance: NotificationRepositoryImpl? = null

        fun getInstance(
            sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
            pushService: PushNotificationService = ConditionalPushNotificationService.getInstance()
        ): NotificationRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepositoryImpl(
                    sessionManager = sessionManager,
                    pushService = pushService
                ).also { instance = it }
            }
        }
    }
}
