package com.quizearn.app.data.repository

import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.domain.model.NotificationType
import com.quizearn.app.domain.model.SupportTicket
import com.quizearn.app.domain.model.TicketCategory
import com.quizearn.app.domain.model.TicketStatus
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
import kotlin.random.Random

/**
 * Production implementation of [SupportRepository] managing ticket creation,
 * input sanitization, ticket history, and resolution notification updates.
 */
class SupportRepositoryImpl(
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepositoryImpl.getInstance()
) : SupportRepository {

    private val mutex = Mutex()
    private val _tickets = MutableStateFlow<List<SupportTicket>>(createDefaultTickets())

    override fun getTickets(): Flow<List<SupportTicket>> = _tickets.asStateFlow()

    override fun getTicketDetails(ticketId: String): Flow<SupportTicket?> = _tickets.map { list ->
        list.find { it.id == ticketId }
    }

    private suspend fun getActiveUserId(): String {
        return sessionManager.currentSession.firstOrNull()?.userId ?: "mock_user_123"
    }

    override suspend fun submitTicket(
        subject: String,
        category: TicketCategory,
        message: String
    ): Result<SupportTicket> = mutex.withLock {
        val sanitizedSubject = subject.trim()
        val sanitizedMessage = message.trim()

        // 1. Validation Rules
        if (sanitizedSubject.length < 3) {
            return Result.failure(IllegalArgumentException("Subject must be at least 3 characters."))
        }
        if (sanitizedMessage.length < 10) {
            return Result.failure(IllegalArgumentException("Message must be at least 10 characters."))
        }

        val userId = getActiveUserId()
        val ticketId = "TKT-" + (1000 + Random.nextInt(9000))
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val createdDate = dateFormat.format(Date())

        val newTicket = SupportTicket(
            id = ticketId,
            userId = userId,
            subject = sanitizedSubject,
            category = category,
            message = sanitizedMessage,
            status = TicketStatus.OPEN,
            createdAtIso = createdDate
        )

        _tickets.value = listOf(newTicket) + _tickets.value

        // 2. Trigger In-App Confirmation Notification
        notificationRepository.triggerNotification(
            title = "Support Ticket Created ($ticketId)",
            body = "We received: '$sanitizedSubject'. Our team will reply within 24 hours.",
            type = NotificationType.SUPPORT_UPDATE,
            actionRoute = "help"
        )

        return Result.success(newTicket)
    }

    override suspend fun addAdminReply(
        ticketId: String,
        reply: String,
        newStatus: TicketStatus
    ): Result<SupportTicket> = mutex.withLock {
        val list = _tickets.value.toMutableList()
        val index = list.indexOfFirst { it.id == ticketId }
        if (index == -1) {
            return Result.failure(IllegalArgumentException("Ticket '$ticketId' was not found."))
        }

        val current = list[index]
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val updated = current.copy(
            adminResponse = reply.trim(),
            status = newStatus,
            updatedAtIso = dateFormat.format(Date())
        )
        list[index] = updated
        _tickets.value = list

        // Trigger Notification for User
        notificationRepository.triggerNotification(
            title = "Support Reply ($ticketId)",
            body = "Support response received: '${reply.take(50)}...'",
            type = NotificationType.SUPPORT_UPDATE,
            actionRoute = "help"
        )

        return Result.success(updated)
    }

    private fun createDefaultTickets(): List<SupportTicket> = listOf(
        SupportTicket(
            id = "TKT-4821",
            userId = "mock_user_123",
            subject = "Question about Google Play voucher redemption",
            category = TicketCategory.WALLET_REDEMPTION,
            message = "Hi, I redeemed 5,000 points yesterday for a Google Play voucher. When will the code be visible?",
            status = TicketStatus.RESOLVED,
            createdAtIso = "Oct 27, 2026",
            updatedAtIso = "Oct 28, 2026",
            adminResponse = "Hello! Your redemption has been reviewed and approved. You can view your voucher code in the Reward Catalog under 'My Redemptions'."
        ),
        SupportTicket(
            id = "TKT-4930",
            userId = "mock_user_123",
            subject = "Science quiz question accuracy",
            category = TicketCategory.GAMEPLAY,
            message = "I noticed question 3 on the science category regarding Mars moons. Can you verify the explanation?",
            status = TicketStatus.IN_PROGRESS,
            createdAtIso = "Oct 28, 2026",
            updatedAtIso = "Oct 29, 2026",
            adminResponse = "Our content team is reviewing the question source and will update the explanation if needed. Thank you for reporting!"
        )
    )

    companion object {
        @Volatile
        private var instance: SupportRepositoryImpl? = null

        fun getInstance(
            sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
            notificationRepository: NotificationRepository = NotificationRepositoryImpl.getInstance()
        ): SupportRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: SupportRepositoryImpl(
                    sessionManager = sessionManager,
                    notificationRepository = notificationRepository
                ).also { instance = it }
            }
        }
    }
}
