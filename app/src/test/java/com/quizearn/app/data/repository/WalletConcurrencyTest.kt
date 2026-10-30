package com.quizearn.app.data.repository

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.TransactionReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Concurrency and financial integrity test suite verifying:
 * 1. Impossibility of double-spending or negative balances under high concurrent contention.
 * 2. Mutex-protected atomic balance modifications across parallel coroutines.
 * 3. Strict mathematical double-entry reconciliation invariant: sum(CREDIT) - sum(DEBIT) == recordedBalance.
 * 4. Idempotency deduplication preserving single-execution semantics.
 * 5. Input validation blocking zero and negative mutation payloads.
 */
class WalletConcurrencyTest {

    private lateinit var walletRepository: WalletRepositoryImpl

    @Before
    fun setUp() {
        // Instantiate a fresh, isolated repository for each test
        walletRepository = WalletRepositoryImpl()
    }

    @Test
    fun concurrentDebits_preventDoubleSpending_andNeverAllowNegativeBalance() = runBlocking(Dispatchers.Default) {
        val initialBalance = walletRepository.getBalance().value // 5450 points
        val debitAmount = 1000L
        val concurrentAttempts = 10 // Total attempted: 10,000 points (exceeds 5450 points available)

        // Launch 10 simultaneous coroutines competing to debit 1000 points each
        val deferredResults = (1..concurrentAttempts).map { index ->
            async {
                walletRepository.debit(
                    userId = "user_001",
                    amount = debitAmount,
                    reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
                    referenceId = "tourn_concurrency_test",
                    description = "Concurrent debit attempt #$index",
                    idempotencyKey = "key_concurrent_debit_$index"
                )
            }
        }

        val results = deferredResults.awaitAll()

        val successes = results.filterIsInstance<Result.Success<*>>()
        val failures = results.filterIsInstance<Result.Error>()

        // 5450 points / 1000 = exactly 5 debits can succeed (total 5000 points debited)
        assertEquals("Exactly 5 debits must succeed", 5, successes.size)
        assertEquals("Remaining 5 debits must fail due to insufficient funds", 5, failures.size)

        // Verify remaining balance is exactly 450 points (never negative)
        val finalBalance = walletRepository.getBalance().value
        assertEquals(450L, finalBalance)
        assertTrue("Final balance must remain non-negative", finalBalance >= 0L)

        // Verify double-entry ledger reconciliation invariant
        val audit = walletRepository.reconcileLedger()
        assertTrue("Double-entry accounting must balance perfectly", audit.isBalanced)
        assertEquals(finalBalance, audit.recordedBalance)
        assertEquals(audit.totalCredits - audit.totalDebits, audit.calculatedBalance)
    }

    @Test
    fun concurrentInterleavedCreditsAndDebits_maintainAtomicBalanceAndDoubleEntryInvariant() = runBlocking(Dispatchers.Default) {
        val initialBalance = walletRepository.getBalance().value
        val iterations = 20

        // Launch 20 concurrent credits (+100L) and 20 concurrent debits (-50L)
        val creditJobs = (1..iterations).map { index ->
            async {
                walletRepository.credit(
                    userId = "user_001",
                    amount = 100L,
                    reason = TransactionReason.QUIZ_REWARD,
                    referenceId = "cat_science",
                    description = "Interleaved credit #$index",
                    idempotencyKey = "key_interleaved_credit_$index"
                )
            }
        }

        val debitJobs = (1..iterations).map { index ->
            async {
                walletRepository.debit(
                    userId = "user_001",
                    amount = 50L,
                    reason = TransactionReason.ADMIN_ADJUSTMENT,
                    referenceId = "adj_test",
                    description = "Interleaved debit #$index",
                    idempotencyKey = "key_interleaved_debit_$index"
                )
            }
        }

        val allResults = (creditJobs + debitJobs).awaitAll()
        val allSucceeded = allResults.all { it is Result.Success }
        assertTrue("All interleaved operations with valid funds must succeed", allSucceeded)

        // Expected delta: + (20 * 100L) - (20 * 50L) = +1000L
        val expectedBalance = initialBalance + 1000L
        val actualBalance = walletRepository.getBalance().value
        assertEquals("Final balance must match exact net delta", expectedBalance, actualBalance)

        // Verify double-entry accounting reconciliation
        val audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be strictly balanced after concurrent interleaved traffic", audit.isBalanced)
        assertEquals(expectedBalance, audit.calculatedBalance)
    }

    @Test
    fun idempotentConcurrentDebits_withIdenticalKeys_executeOnlyOnce() = runBlocking(Dispatchers.Default) {
        val initialBalance = walletRepository.getBalance().value
        val sharedIdempotencyKey = "shared_idempotent_debit_key_999"
        val debitAmount = 1000L
        val parallelCalls = 8

        // 8 parallel callers submitting the EXACT same idempotency key concurrently
        val deferredCalls = (1..parallelCalls).map {
            async {
                walletRepository.debit(
                    userId = "user_001",
                    amount = debitAmount,
                    reason = TransactionReason.REDEMPTION_HOLD,
                    referenceId = "rew_amz_05",
                    description = "Idempotent debit test",
                    idempotencyKey = sharedIdempotencyKey
                )
            }
        }

        val results = deferredCalls.awaitAll()

        // All callers must receive Result.Success
        assertTrue("All concurrent callers with same idempotency key receive success", results.all { it is Result.Success })

        // But points must only be deducted ONCE (-1000L, not -8000L)
        val expectedBalance = initialBalance - debitAmount
        val actualBalance = walletRepository.getBalance().value
        assertEquals("Points must only be debited once despite 8 concurrent calls", expectedBalance, actualBalance)

        // Verify ledger has balanced audit
        val audit = walletRepository.reconcileLedger()
        assertTrue("Ledger remains balanced with idempotency protection", audit.isBalanced)
    }

    @Test
    fun redemptionWorkflow_atomicallyHoldsPointsAndRestoresOnRejection() = runBlocking {
        val initialBalance = walletRepository.getBalance().value // 5450L
        val rewardId = "rew_amz_05" // Cost: 5000L
        val payoutEmail = "player@quizearn.com"
        val idempotencyKey = "redemption_flow_key_${UUID.randomUUID()}"

        // 1. Request Redemption: Deducts 5000L and places in PENDING status
        val requestResult = walletRepository.requestRedemption(rewardId, payoutEmail, idempotencyKey)
        assertTrue("Redemption request must succeed", requestResult is Result.Success)
        val redemption = (requestResult as Result.Success).data
        assertEquals(5000L, redemption.pointsDeducted)

        val balanceAfterHold = walletRepository.getBalance().value
        assertEquals("Balance must reflect 5000L deduction", initialBalance - 5000L, balanceAfterHold)

        // 2. Submitting duplicate redemption with same key returns cached item without double-deduction
        val duplicateResult = walletRepository.requestRedemption(rewardId, payoutEmail, idempotencyKey)
        assertTrue(duplicateResult is Result.Success)
        assertEquals(balanceAfterHold, walletRepository.getBalance().value)

        // 3. Admin rejects redemption request with mandatory reason
        val rejectionReason = "Invalid payout email domain provided by operator."
        val rejectResult = walletRepository.rejectRedemption(redemption.id, rejectionReason)
        assertTrue("Rejection must succeed", rejectResult is Result.Success)

        // 4. Automated compensatory credit (REDEMPTION_REFUND) restores balance
        val balanceAfterRefund = walletRepository.getBalance().value
        assertEquals("Balance must be fully restored after redemption rejection", initialBalance, balanceAfterRefund)

        // 5. Strict mathematical audit verification
        val audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must remain strictly balanced after redemption hold and refund", audit.isBalanced)
        assertEquals(initialBalance, audit.recordedBalance)
        assertEquals(audit.totalCredits - audit.totalDebits, audit.calculatedBalance)
    }

    @Test
    fun malformedTransactions_negativeOrZeroAmounts_failValidation() = runBlocking {
        // Credit <= 0
        val negCredit = walletRepository.credit("u1", -500L, TransactionReason.QUIZ_REWARD, null, "Neg credit", "k1")
        assertTrue("Negative credit must fail", negCredit is Result.Error)
        assertEquals("Credit amount must be greater than zero.", (negCredit as Result.Error).exception.message)

        val zeroCredit = walletRepository.credit("u1", 0L, TransactionReason.QUIZ_REWARD, null, "Zero credit", "k2")
        assertTrue("Zero credit must fail", zeroCredit is Result.Error)

        // Debit <= 0
        val negDebit = walletRepository.debit("u1", -200L, TransactionReason.ADMIN_ADJUSTMENT, null, "Neg debit", "k3")
        assertTrue("Negative debit must fail", negDebit is Result.Error)
        assertEquals("Debit amount must be greater than zero.", (negDebit as Result.Error).exception.message)

        val zeroDebit = walletRepository.debit("u1", 0L, TransactionReason.ADMIN_ADJUSTMENT, null, "Zero debit", "k4")
        assertTrue("Zero debit must fail", zeroDebit is Result.Error)
    }
}
