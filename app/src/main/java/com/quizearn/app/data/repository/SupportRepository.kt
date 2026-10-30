package com.quizearn.app.data.repository

import com.quizearn.app.domain.model.SupportTicket
import com.quizearn.app.domain.model.TicketCategory
import com.quizearn.app.domain.model.TicketStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for submitting support tickets and tracking resolution history.
 */
interface SupportRepository {
    fun getTickets(): Flow<List<SupportTicket>>
    fun getTicketDetails(ticketId: String): Flow<SupportTicket?>

    suspend fun submitTicket(
        subject: String,
        category: TicketCategory,
        message: String
    ): Result<SupportTicket>

    suspend fun addAdminReply(
        ticketId: String,
        reply: String,
        newStatus: TicketStatus = TicketStatus.IN_PROGRESS
    ): Result<SupportTicket>
}
