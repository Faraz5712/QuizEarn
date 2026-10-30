package com.quizearn.app.data.local.room

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.RedemptionStatus
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite verifying wallet repository behavior, accounting invariants,
 * and idempotency deduplication with the Room integration architecture.
 */
class WalletRepositoryRoomIntegrationTest {

    private lateinit var walletRepository: WalletRepositoryImpl

    @Before
    fun setUp() {
        walletRepository = WalletRepositoryImpl()
    }

    @Test
    fun initialLedger_strictlyBalances_withDoubleEntryAccounting() {
        val audit = walletRepository.reconcileLedger()
        assertTrue("Initial ledger must balance exactly", audit.isBalanced)
        assertEquals(5450L, audit.recordedBalance)
        assertEquals(5450L, audit.calculatedBalance)
        assertEquals(10450L, audit.totalCredits)
        assertEquals(5000L, audit.totalDebits)
        assertEquals(7, audit.transactionCount)
    }

    @Test
    fun credit_deduplicatesByIdempotencyKey_preventingDoubleCredit() = runBlocking {
        val key = "idem_credit_double_test"
        val firstResult = walletRepository.credit(
            userId = "user_001",
            amount = 500L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "cat_tech",
            description = "Quiz Completed",
            idempotencyKey = key
        )
        assertTrue(firstResult is Result.Success)
        val firstTx = (firstResult as Result.Success).data
        assertEquals(5950L, firstTx.balanceAfter)

        // Attempt duplicate credit with same idempotency key
        val secondResult = walletRepository.credit(
            userId = "user_001",
            amount = 500L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "cat_tech",
            description = "Duplicate Quiz Completed",
            idempotencyKey = key
        )
        assertTrue(secondResult is Result.Success)
        val secondTx = (secondResult as Result.Success).data

        // Must return identical transaction without incrementing balance again
        assertEquals(firstTx.id, secondTx.id)
        assertEquals(5950L, walletRepository.getBalance().value)

        val audit = walletRepository.reconcileLedger()
        assertTrue(audit.isBalanced)
        assertEquals(5950L, audit.calculatedBalance)
    }

    @Test
    fun debit_deduplicatesByIdempotencyKey_preventingDoubleDebit() = runBlocking {
        val key = "idem_debit_double_test"
        val firstResult = walletRepository.debit(
            userId = "user_001",
            amount = 1000L,
            reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
            referenceId = "tourn_01",
            description = "Tournament Entry",
            idempotencyKey = key
        )
        assertTrue(firstResult is Result.Success)
        val firstTx = (firstResult as Result.Success).data
        assertEquals(4450L, firstTx.balanceAfter)

        // Attempt duplicate debit with same key
        val secondResult = walletRepository.debit(
            userId = "user_001",
            amount = 1000L,
            reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
            referenceId = "tourn_01",
            description = "Duplicate Tournament Entry",
            idempotencyKey = key
        )
        assertTrue(secondResult is Result.Success)
        val secondTx = (secondResult as Result.Success).data

        assertEquals(firstTx.id, secondTx.id)
        assertEquals(4450L, walletRepository.getBalance().value)

        val audit = walletRepository.reconcileLedger()
        assertTrue(audit.isBalanced)
        assertEquals(4450L, audit.calculatedBalance)
    }

    @Test
    fun credit_rejectsReusingKeyWithDifferentPayload() = runBlocking {
        val firstResult = walletRepository.credit(
            userId = "user_001",
            amount = 100L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "session_01",
            description = "Quiz Reward",
            idempotencyKey = "idem_payload_test"
        )
        assertTrue(firstResult is Result.Success)

        val replay = walletRepository.credit(
            userId = "user_001",
            amount = 200L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "session_01",
            description = "Changed amount",
            idempotencyKey = "idem_payload_test"
        )

        assertTrue(replay is Result.Error)
        assertEquals(5550L, walletRepository.getBalance().value)
        assertTrue(walletRepository.reconcileLedger().isBalanced)
    }

    @Test
    fun creditPoints_usesStableReferenceKeyAndPersistsOnlyOneReward() = runBlocking {
        val first = walletRepository.creditPoints(
            userId = "user_001",
            amount = 100L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "session_retry_01",
            description = "Quiz Reward"
        )
        val retry = walletRepository.creditPoints(
            userId = "user_001",
            amount = 100L,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "session_retry_01",
            description = "Quiz Reward"
        )

        assertTrue(first is Result.Success)
        assertTrue(retry is Result.Success)
        assertEquals(5550L, walletRepository.getBalance().value)
        assertEquals(8, walletRepository.reconcileLedger().transactionCount)
        assertTrue(walletRepository.reconcileLedger().isBalanced)
    }

    @Test
    fun credit_rejectsBalanceOverflowWithoutMutatingLedger() = runBlocking {
        val result = walletRepository.credit(
            userId = "user_001",
            amount = Long.MAX_VALUE,
            reason = TransactionReason.QUIZ_REWARD,
            referenceId = "overflow",
            description = "Overflow attempt",
            idempotencyKey = "idem_overflow"
        )

        assertTrue(result is Result.Error)
        assertEquals(5450L, walletRepository.getBalance().value)
        assertEquals(7, walletRepository.reconcileLedger().transactionCount)
        assertTrue(walletRepository.reconcileLedger().isBalanced)
    }

    @Test
    fun redemptionLifecycle_requestApproveReject_maintainsLedgerInvariant() = runBlocking {
        // 1. Request redemption (costs 5000 points)
        val requestResult = walletRepository.requestRedemption(
            rewardId = "rew_amz_05",
            payoutDetails = "user@test.com",
            idempotencyKey = "red_lifecycle_key_01"
        )
        assertTrue(requestResult is Result.Success)
        val redemption = (requestResult as Result.Success).data
        assertEquals(RedemptionStatus.PENDING, redemption.status)
        assertEquals(450L, walletRepository.getBalance().value)

        var audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must balance after redemption request", audit.isBalanced)
        assertEquals(450L, audit.calculatedBalance)

        // 2. Reject redemption -> should auto-refund 5000 points
        val rejectResult = walletRepository.rejectRedemption(
            redemptionId = redemption.id,
            reason = "Invalid phone format provided"
        )
        assertTrue(rejectResult is Result.Success)
        val rejected = (rejectResult as Result.Success).data
        assertEquals(RedemptionStatus.REJECTED, rejected.status)
        assertEquals("Invalid phone format provided", rejected.rejectionReason)

        // Balance restored to 5450
        assertEquals(5450L, walletRepository.getBalance().value)
        audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must balance after redemption refund", audit.isBalanced)
        assertEquals(5450L, audit.calculatedBalance)
    }
}
