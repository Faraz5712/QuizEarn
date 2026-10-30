package com.quizearn.app.ui.screens.wallet

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.LedgerReconciliationResult
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.RedemptionStatus as DomainRedemptionStatus
import com.quizearn.app.domain.model.Reward
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying WalletViewModel reactive points balance derivation,
 * monetary equivalence calculation, transaction filtering, and redemption hold/refund workflows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeWalletRepository
    private lateinit var viewModel: WalletViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWalletRepository()
        viewModel = WalletViewModel(walletRepository = fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun observeBalance_derivesPointsAndMonetaryEquivalent() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        // Initial balance is 6000 points ($6.00, above 5000 threshold)
        val initialState = viewModel.uiState.value
        assertEquals(6000, initialState.pointsBalance)
        assertEquals("$6.00", initialState.monetaryEquivalentFormatted)
        assertTrue("Balance >= 5000 points should allow redemption", initialState.canRedeem)

        // Balance drops below threshold (e.g. 2500 points)
        fakeRepository.updateBalance(2500L)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(2500, updatedState.pointsBalance)
        assertEquals("$2.50", updatedState.monetaryEquivalentFormatted)
        assertFalse("Balance < 5000 points must not allow redemption", updatedState.canRedeem)
    }

    @Test
    fun filterTransactions_filtersAllCreditsAndDebitsCorrectly() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        // Default Filter: ALL
        assertEquals(TransactionFilter.ALL, viewModel.currentFilter.value)
        assertEquals(4, viewModel.uiState.value.recentTransactions.size)

        // Filter: CREDITS
        viewModel.setFilter(TransactionFilter.CREDITS)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TransactionFilter.CREDITS, viewModel.currentFilter.value)
        assertEquals(3, viewModel.uiState.value.recentTransactions.size)
        assertTrue(viewModel.uiState.value.recentTransactions.all { it.isCredit })

        // Filter: DEBITS
        viewModel.setFilter(TransactionFilter.DEBITS)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TransactionFilter.DEBITS, viewModel.currentFilter.value)
        assertEquals(1, viewModel.uiState.value.recentTransactions.size)
        assertTrue(viewModel.uiState.value.recentTransactions.all { !it.isCredit })
        assertEquals("Reward Redemption Hold", viewModel.uiState.value.recentTransactions[0].title)
        assertEquals(-2000, viewModel.uiState.value.recentTransactions[0].pointsDelta)
    }

    @Test
    fun redemptionHoldAndRefund_calculations_verifyBalanceIntegrity() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        // Check initial double-entry ledger reconciliation
        val initialReconciliation = fakeRepository.reconcileLedger()
        assertTrue("Double-entry ledger must balance initially", initialReconciliation.isBalanced)
        assertEquals(6000L, initialReconciliation.recordedBalance)
        assertEquals(8000L, initialReconciliation.totalCredits)
        assertEquals(2000L, initialReconciliation.totalDebits)

        // Request a new redemption hold (e.g. 1000 points)
        var redemptionResult: Result<Redemption>? = null
        viewModel.requestRedemption(
            rewardId = "rew_amazon_5",
            payoutDetails = "user@test.com",
            idempotencyKey = "key_redemption_001"
        ) { res ->
            redemptionResult = res
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull("Redemption callback must be invoked", redemptionResult)
        assertTrue("Redemption request must succeed", redemptionResult is Result.Success)

        val redemption = (redemptionResult as Result.Success).data
        assertEquals("rew_amazon_5", redemption.rewardId)
        assertEquals(DomainRedemptionStatus.PENDING, redemption.status)
        assertEquals(1000L, redemption.pointsDeducted)
    }

    /**
     * Fake WalletRepository providing controllable balance streams,
     * immutable transaction history, and double-entry accounting reconciliation.
     */
    private class FakeWalletRepository : WalletRepository {

        private val balanceFlow = MutableStateFlow(6000L)
        private val transactions = mutableListOf(
            Transaction(
                id = "txn_001",
                userId = "user_test",
                type = TransactionType.CREDIT,
                amount = 5000L,
                balanceAfter = 5000L,
                reason = TransactionReason.WELCOME_BONUS,
                description = "Welcome Signup Bonus",
                timestampIso = "2026-10-29T10:00:00Z"
            ),
            Transaction(
                id = "txn_002",
                userId = "user_test",
                type = TransactionType.CREDIT,
                amount = 1000L,
                balanceAfter = 6000L,
                reason = TransactionReason.QUIZ_REWARD,
                description = "Quiz Completion Reward",
                timestampIso = "2026-10-29T10:15:00Z"
            ),
            Transaction(
                id = "txn_003",
                userId = "user_test",
                type = TransactionType.DEBIT,
                amount = 2000L,
                balanceAfter = 4000L,
                reason = TransactionReason.REDEMPTION_HOLD,
                description = "Reward Redemption Hold",
                timestampIso = "2026-10-29T10:30:00Z"
            ),
            Transaction(
                id = "txn_004",
                userId = "user_test",
                type = TransactionType.CREDIT,
                amount = 2000L,
                balanceAfter = 6000L,
                reason = TransactionReason.REDEMPTION_REFUND,
                description = "Redemption Rejection Refund",
                timestampIso = "2026-10-29T10:45:00Z"
            )
        )

        fun updateBalance(newBalance: Long) {
            balanceFlow.value = newBalance
        }

        override fun getBalance(): StateFlow<Long> = balanceFlow.asStateFlow()

        override fun getTransactions(): Flow<Result<List<Transaction>>> {
            return flowOf(Result.Success(transactions.toList()))
        }

        override fun getRewardCatalog(): Flow<Result<List<Reward>>> {
            return flowOf(
                Result.Success(
                    listOf(
                        Reward(
                            id = "rew_amazon_5",
                            title = "$5 Amazon Gift Card",
                            description = "Amazon digital gift card voucher",
                            pointsCost = 1000L,
                            category = "GIFT_CARD",
                            stockQuantity = 50
                        )
                    )
                )
            )
        }

        override fun getRedemptions(): Flow<Result<List<Redemption>>> {
            return flowOf(Result.Success(emptyList()))
        }

        override suspend fun credit(
            userId: String,
            amount: Long,
            reason: TransactionReason,
            referenceId: String?,
            description: String,
            idempotencyKey: String
        ): Result<Transaction> {
            val newBalance = balanceFlow.value + amount
            val txn = Transaction(
                id = "txn_${System.currentTimeMillis()}",
                userId = userId,
                type = TransactionType.CREDIT,
                amount = amount,
                balanceAfter = newBalance,
                reason = reason,
                referenceId = referenceId,
                description = description,
                timestampIso = "2026-10-29T11:00:00Z",
                idempotencyKey = idempotencyKey
            )
            transactions.add(txn)
            balanceFlow.value = newBalance
            return Result.Success(txn)
        }

        override suspend fun debit(
            userId: String,
            amount: Long,
            reason: TransactionReason,
            referenceId: String?,
            description: String,
            idempotencyKey: String
        ): Result<Transaction> {
            if (balanceFlow.value < amount) {
                return Result.Error(IllegalStateException("Insufficient balance"))
            }
            val newBalance = balanceFlow.value - amount
            val txn = Transaction(
                id = "txn_${System.currentTimeMillis()}",
                userId = userId,
                type = TransactionType.DEBIT,
                amount = amount,
                balanceAfter = newBalance,
                reason = reason,
                referenceId = referenceId,
                description = description,
                timestampIso = "2026-10-29T11:00:00Z",
                idempotencyKey = idempotencyKey
            )
            transactions.add(txn)
            balanceFlow.value = newBalance
            return Result.Success(txn)
        }

        override suspend fun requestRedemption(
            rewardId: String,
            payoutDetails: String,
            idempotencyKey: String
        ): Result<Redemption> {
            val redemption = Redemption(
                id = "red_test_001",
                userId = "user_test",
                rewardId = rewardId,
                rewardTitle = "$5 Amazon Gift Card",
                pointsDeducted = 1000L,
                status = DomainRedemptionStatus.PENDING,
                payoutDetails = payoutDetails,
                createdAtIso = "2026-10-29T11:00:00Z"
            )
            return Result.Success(redemption)
        }

        override suspend fun approveRedemption(redemptionId: String, digitalCode: String): Result<Redemption> {
            return Result.Success(
                Redemption(
                    id = redemptionId,
                    userId = "user_test",
                    rewardId = "rew_amazon_5",
                    rewardTitle = "$5 Amazon Gift Card",
                    pointsDeducted = 1000L,
                    status = DomainRedemptionStatus.APPROVED,
                    payoutDetails = "user@test.com",
                    digitalCode = digitalCode,
                    createdAtIso = "2026-10-29T11:00:00Z"
                )
            )
        }

        override suspend fun rejectRedemption(redemptionId: String, reason: String): Result<Redemption> {
            return Result.Success(
                Redemption(
                    id = redemptionId,
                    userId = "user_test",
                    rewardId = "rew_amazon_5",
                    rewardTitle = "$5 Amazon Gift Card",
                    pointsDeducted = 1000L,
                    status = DomainRedemptionStatus.REJECTED,
                    payoutDetails = "user@test.com",
                    rejectionReason = reason,
                    createdAtIso = "2026-10-29T11:00:00Z"
                )
            )
        }

        override fun reconcileLedger(): LedgerReconciliationResult {
            val totalCredits = transactions.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
            val totalDebits = transactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            val calculated = totalCredits - totalDebits
            val recorded = balanceFlow.value
            return LedgerReconciliationResult(
                calculatedBalance = calculated,
                recordedBalance = recorded,
                totalCredits = totalCredits,
                totalDebits = totalDebits,
                isBalanced = calculated == recorded,
                transactionCount = transactions.size
            )
        }
    }
}
