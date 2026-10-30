package com.quizearn.app.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType

/**
 * Room entity representing an immutable ledger entry.
 * Enforces a unique index on [idempotencyKey] to prevent duplicate transactions at the database level.
 */
@Entity(
    tableName = "wallet_transactions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["idempotencyKey"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: String, // "CREDIT" or "DEBIT"
    val amount: Long,
    val balanceAfter: Long,
    val reason: String,
    val referenceId: String?,
    val description: String,
    val timestampIso: String,
    val idempotencyKey: String
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        userId = userId,
        type = try { TransactionType.valueOf(type) } catch (_: Exception) { TransactionType.CREDIT },
        amount = amount,
        balanceAfter = balanceAfter,
        reason = try { TransactionReason.valueOf(reason) } catch (_: Exception) { TransactionReason.QUIZ_REWARD },
        referenceId = referenceId,
        description = description,
        timestampIso = timestampIso,
        idempotencyKey = idempotencyKey
    )

    companion object {
        fun fromDomain(domain: Transaction): TransactionEntity = TransactionEntity(
            id = domain.id,
            userId = domain.userId,
            type = domain.type.name,
            amount = domain.amount,
            balanceAfter = domain.balanceAfter,
            reason = domain.reason.name,
            referenceId = domain.referenceId,
            description = domain.description,
            timestampIso = domain.timestampIso,
            idempotencyKey = if (domain.idempotencyKey.isNotBlank()) domain.idempotencyKey else "generated_${domain.id}"
        )
    }
}
