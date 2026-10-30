package com.quizearn.app.domain.model

/**
 * Category of customer support inquiries.
 */
enum class TicketCategory(val label: String) {
    GAMEPLAY("Gameplay & Questions"),
    WALLET_REDEMPTION("Wallet & Redemptions"),
    BUG_REPORT("Bug Report"),
    ACCOUNT("Account & Profile"),
    OTHER("General Inquiry")
}

/**
 * Lifecycle resolution status of a customer support ticket.
 */
enum class TicketStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved")
}

/**
 * Domain model representing a customer support request.
 */
data class SupportTicket(
    val id: String,
    val userId: String,
    val subject: String,
    val category: TicketCategory,
    val message: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAtIso: String,
    val updatedAtIso: String? = null,
    val adminResponse: String? = null
)
