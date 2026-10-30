package com.quizearn.app.data.repository

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.Reward
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Audit report verifying mathematical double-entry balance integrity:
 * calculatedBalance == totalCredits - totalDebits == recordedBalance.
 */
data class LedgerReconciliationResult(
    val calculatedBalance: Long,
    val recordedBalance: Long,
    val totalCredits: Long,
    val totalDebits: Long,
    val isBalanced: Boolean,
    val transactionCount: Int
)

/**
 * Repository interface for points balance, append-only ledger operations,
 * rewards catalog, and redemptions workflow.
 */
interface WalletRepository {
    /**
     * Live reactive stream of the user's reconciled points balance.
     */
    fun getBalance(): StateFlow<Long>

    /**
     * Live reactive stream of immutable ledger transactions.
     */
    fun getTransactions(): Flow<Result<List<Transaction>>>

    /**
     * Retrieve the list of available digital rewards.
     */
    fun getRewardCatalog(): Flow<Result<List<Reward>>>

    /**
     * Live reactive stream of user redemption requests.
     */
    fun getRedemptions(): Flow<Result<List<Redemption>>>

    /**
     * Atomically credit points to the user's ledger.
     * Enforces idempotency to prevent duplicate reward payouts.
     */
    suspend fun credit(
        userId: String = "usr_current",
        amount: Long,
        reason: TransactionReason,
        referenceId: String? = null,
        description: String,
        idempotencyKey: String
    ): Result<Transaction>

    /**
     * Atomically debit points from the user's ledger.
     * Enforces sufficient balance check and idempotency to prevent double-spending.
     */
    suspend fun debit(
        userId: String = "usr_current",
        amount: Long,
        reason: TransactionReason,
        referenceId: String? = null,
        description: String,
        idempotencyKey: String
    ): Result<Transaction>

    /**
     * Request redemption of points for a digital reward.
     * Atomically debits points with REDEMPTION_HOLD and creates record in PENDING status.
     */
    suspend fun requestRedemption(
        rewardId: String,
        payoutDetails: String,
        idempotencyKey: String
    ): Result<Redemption>

    /**
     * Authoritative approval of a redemption request.
     * Transitions status to APPROVED and stores the digital fulfillment code.
     */
    suspend fun approveRedemption(
        redemptionId: String,
        digitalCode: String
    ): Result<Redemption>

    /**
     * Authoritative rejection of a redemption request.
     * Transitions status to REJECTED with mandatory reason,
     * and automatically issues an atomic ledger points refund (REDEMPTION_REFUND).
     */
    suspend fun rejectRedemption(
        redemptionId: String,
        reason: String
    ): Result<Redemption>

    /**
     * Mathematical reconciliation check verifying that:
     * sum(CREDIT) - sum(DEBIT) == recordedBalance.
     */
    fun reconcileLedger(): LedgerReconciliationResult
}
