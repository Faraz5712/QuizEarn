package com.quizearn.app.domain.model

/**
 * Immutable financial ledger record representing a single point balance modification.
 * Adheres to double-entry accounting standards where the user's total balance
 * is strictly derived from the sum of credits minus debits.
 */
data class Transaction(
    val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val reason: TransactionReason,
    val referenceId: String? = null,
    val description: String,
    val timestampIso: String,
    val idempotencyKey: String = ""
)

/**
 * Ledger entry direction: CREDIT increases user points, DEBIT reduces them.
 */
enum class TransactionType {
    CREDIT,
    DEBIT
}

/**
 * Authoritative business reasons for ledger transactions.
 */
enum class TransactionReason {
    QUIZ_REWARD,
    DAILY_CHECKIN,
    TASK_COMPLETED,
    TOURNAMENT_PRIZE,
    TOURNAMENT_ENTRY_FEE,
    REDEMPTION_HOLD,
    REDEMPTION_REFUND,
    WELCOME_BONUS,
    ADMIN_ADJUSTMENT
}
