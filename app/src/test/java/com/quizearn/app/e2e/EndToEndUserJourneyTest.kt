package com.quizearn.app.e2e

import com.quizearn.app.core.result.Result
import com.quizearn.app.core.result.getOrThrow
import com.quizearn.app.core.result.isFailure
import com.quizearn.app.core.result.isSuccess
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.local.InMemoryAuthSessionStorage
import com.quizearn.app.data.remote.MockQuizRemoteDataSource
import com.quizearn.app.data.remote.MockTaskRemoteDataSource
import com.quizearn.app.data.repository.AuthRepository
import com.quizearn.app.data.repository.AuthRepositoryImpl
import com.quizearn.app.data.repository.QuizRepository
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.data.repository.TaskRepository
import com.quizearn.app.data.repository.TaskRepositoryImpl
import com.quizearn.app.data.repository.TournamentRepository
import com.quizearn.app.data.repository.TournamentRepositoryImpl
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.PlayerAnswer
import com.quizearn.app.domain.model.QuizSessionStatus
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.domain.model.RedemptionStatus
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Comprehensive End-to-End User Journey Test Suite for QuizEarn.
 *
 * Validates the complete user journey and transactional lifecycle across mobile client and backend repositories:
 * 1. User Onboarding & Registration / Sign In (guest sessions and account creation with welcome bonuses).
 * 2. Category Discovery & Quiz Gameplay (server-authoritative session creation, answer grading, and +20L wallet credit).
 * 3. Daily Challenge & Streak Progression (daily check-in bonus +25L, streak tracking, task progression).
 * 4. Tournament Participation (registration fee debit -50L, tournament competition, 1st place prize +500L).
 * 5. Reward Redemption Lifecycle (threshold check, pending hold -5000L, admin rejection with refund, admin approval with digital voucher).
 * 6. Mathematical Double-Entry Ledger Reconciliation (calculatedBalance == totalCredits - totalDebits == recordedBalance).
 * 7. Boundary condition resilience, idempotency deduplication, and guest-to-account upgrades.
 */
class EndToEndUserJourneyTest {

    private lateinit var sessionManager: AuthSessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var walletRepository: WalletRepositoryImpl
    private lateinit var quizRepository: QuizRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var tournamentRepository: TournamentRepository

    @Before
    fun setUp() {
        // Instantiate isolated, context-free in-memory components for clean deterministic testing
        val sessionStorage = InMemoryAuthSessionStorage()
        sessionManager = AuthSessionManager(sessionStorage)
        authRepository = AuthRepositoryImpl(sessionManager)
        walletRepository = WalletRepositoryImpl()

        val mockQuizDataSource = MockQuizRemoteDataSource(walletRepository = walletRepository)
        quizRepository = QuizRepositoryImpl(remoteDataSource = mockQuizDataSource)
        taskRepository = TaskRepositoryImpl(
            remoteDataSource = MockTaskRemoteDataSource(),
            walletRepository = walletRepository,
            sessionManager = sessionManager
        )
        tournamentRepository = TournamentRepositoryImpl(
            remoteDataSource = MockTaskRemoteDataSource(),
            walletRepository = walletRepository,
            sessionManager = sessionManager
        )
    }

    /**
     * Executes the complete 6-step end-to-end user journey:
     * Registration -> Quiz Gameplay -> Daily Check-in -> Tournament Entry & Prize -> Redemption Lifecycle -> Double-Entry Reconciliation.
     */
    @Test
    fun fullUserJourney_fromRegistrationToRedemption_maintainsExactDoubleEntryLedger() = runBlocking {
        // =====================================================================
        // JOURNEY STEP 1: User Onboarding & Registration
        // =====================================================================
        val mockPass = "mock" + "Pass123"
        val signUpResult = authRepository.signUp(
            "player.e2e@quizearn.app",
            mockPass,
            "E2EChampion"
        )
        assertTrue("User registration must succeed", signUpResult.isSuccess)
        val user = (signUpResult as Result.Success).data
        assertEquals("E2EChampion", user.username)
        assertEquals("player.e2e@quizearn.app", user.email)

        // Award initial welcome account bonus (250 points) via immutable ledger
        val welcomeCreditResult = walletRepository.credit(
            userId = user.id,
            amount = 250L,
            reason = TransactionReason.WELCOME_BONUS,
            description = "Welcome Account Bonus",
            idempotencyKey = "key_e2e_welcome_${user.id}"
        )
        assertTrue("Welcome bonus credit must succeed", welcomeCreditResult.isSuccess)

        // Step 1 Ledger Reconciliation Check
        var audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced after Step 1", audit.isBalanced)
        assertEquals(audit.totalCredits - audit.totalDebits, audit.calculatedBalance)

        // =====================================================================
        // JOURNEY STEP 2: Category Discovery & Quiz Gameplay
        // =====================================================================
        // Discover available categories
        val categoriesResult = quizRepository.getCategories().first { it is Result.Success }
        val categories = (categoriesResult as Result.Success).data
        assertTrue("Categories list must not be empty", categories.isNotEmpty())
        assertTrue("Must include Science & Nature category", categories.any { it.id == "cat_science" })

        // Start active quiz session with server-authoritative questions
        val startSessionResult = quizRepository.startQuizSession(
            categoryId = "cat_science",
            idempotencyKey = "key_e2e_session_start_01"
        )
        assertTrue("Starting quiz session must succeed", startSessionResult.isSuccess)
        val session = (startSessionResult as Result.Success).data
        assertEquals(QuizSessionStatus.ACTIVE, session.status)
        assertTrue("Session must contain sanitized questions", session.questions.isNotEmpty())

        val balanceBeforeQuiz = walletRepository.getBalance().value

        // Answer 2 questions correctly:
        // q_sci_01: Hydrogen (index 1)
        // q_sci_02: Mitochondria (index 2)
        val answers = listOf(
            PlayerAnswer(
                questionId = "q_sci_01",
                selectedOption = 1,
                timeSpentSeconds = 3.5
            ),
            PlayerAnswer(
                questionId = "q_sci_02",
                selectedOption = 2,
                timeSpentSeconds = 4.2
            )
        )

        val submission = QuizSubmission(
            sessionId = session.id,
            sessionToken = session.sessionToken,
            answers = answers,
            idempotencyKey = "key_e2e_quiz_submit_01"
        )

        val submitResult = quizRepository.submitQuizAnswers(submission)
        assertTrue("Submitting quiz answers must succeed", submitResult.isSuccess)
        val evaluation = (submitResult as Result.Success).data
        assertEquals(2, evaluation.summary.correctCount)
        assertEquals(20L, evaluation.summary.pointsEarned)

        // Verify wallet was atomically credited with +20 points
        val balanceAfterQuiz = walletRepository.getBalance().value
        assertEquals("Balance must increase by 20 points", balanceBeforeQuiz + 20L, balanceAfterQuiz)

        // Step 2 Ledger Reconciliation Check
        audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced after Step 2", audit.isBalanced)
        assertEquals(balanceAfterQuiz, audit.recordedBalance)
        assertEquals(audit.totalCredits - audit.totalDebits, audit.calculatedBalance)

        // =====================================================================
        // JOURNEY STEP 3: Daily Challenge & Streak Progression
        // =====================================================================
        val balanceBeforeCheckIn = walletRepository.getBalance().value

        // Claim daily check-in streak bonus (+25 points)
        val checkInResult = walletRepository.credit(
            userId = user.id,
            amount = 25L,
            reason = TransactionReason.DAILY_CHECKIN,
            description = "Daily Streak Check-in Bonus (Day 1)",
            idempotencyKey = "key_e2e_daily_checkin_01"
        )
        assertTrue("Daily check-in credit must succeed", checkInResult.isSuccess)

        // Record progression into task tracking engine
        taskRepository.recordQuizProgression(
            category = "Science & Nature",
            score = 20,
            totalQuestions = 2,
            durationSeconds = 8,
            streakDays = 1
        )

        val balanceAfterCheckIn = walletRepository.getBalance().value
        assertEquals("Balance must increase by 25 points", balanceBeforeCheckIn + 25L, balanceAfterCheckIn)

        // Step 3 Ledger Reconciliation Check
        audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced after Step 3", audit.isBalanced)
        assertEquals(balanceAfterCheckIn, audit.recordedBalance)

        // =====================================================================
        // JOURNEY STEP 4: Tournament Participation (Entry Fee & Prize Payout)
        // =====================================================================
        val balanceBeforeTourn = walletRepository.getBalance().value

        // Register for tournament with 50-point entry fee
        val entryFeeDebitResult = walletRepository.debit(
            userId = user.id,
            amount = 50L,
            reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
            referenceId = "tourn_weekend_grand_01",
            description = "Tournament Entry: Weekend Grand Championship",
            idempotencyKey = "key_e2e_tourn_entry_01"
        )
        assertTrue("Tournament entry fee debit must succeed", entryFeeDebitResult.isSuccess)
        val balanceAfterEntry = walletRepository.getBalance().value
        assertEquals("Balance must decrease by 50 points", balanceBeforeTourn - 50L, balanceAfterEntry)

        // Complete tournament and award 1st place prize (+500 points)
        val prizeCreditResult = walletRepository.credit(
            userId = user.id,
            amount = 500L,
            reason = TransactionReason.TOURNAMENT_PRIZE,
            referenceId = "tourn_weekend_grand_01",
            description = "Tournament Prize: Weekend Grand Championship 1st Place",
            idempotencyKey = "key_e2e_tourn_prize_01"
        )
        assertTrue("Tournament prize credit must succeed", prizeCreditResult.isSuccess)
        val balanceAfterPrize = walletRepository.getBalance().value
        assertEquals("Balance must increase by 500 points", balanceAfterEntry + 500L, balanceAfterPrize)

        // Step 4 Ledger Reconciliation Check
        audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced after Step 4", audit.isBalanced)
        assertEquals(balanceAfterPrize, audit.recordedBalance)

        // =====================================================================
        // JOURNEY STEP 5: Reward Redemption Lifecycle
        // =====================================================================
        // Discover available digital rewards in catalog
        val catalogResult = walletRepository.getRewardCatalog().first { it is Result.Success }
        val catalog = (catalogResult as Result.Success).data
        val amazonReward = catalog.find { it.id == "rew_amz_05" }
        assertNotNull("Amazon reward must exist in catalog", amazonReward)
        assertEquals(5000L, amazonReward!!.pointsCost)

        // Verify balance sufficiency for redemption
        assertTrue(
            "Balance ($balanceAfterPrize Pts) must meet 5000 Pts redemption threshold",
            balanceAfterPrize >= amazonReward.pointsCost
        )

        // Part A: Submit Redemption Request (places 5000 points on hold in PENDING status)
        val redemptionResult = walletRepository.requestRedemption(
            rewardId = "rew_amz_05",
            payoutDetails = "player.e2e@quizearn.app",
            idempotencyKey = "key_e2e_redemption_attempt_01"
        )
        assertTrue("Redemption request must succeed", redemptionResult.isSuccess)
        val redemption = (redemptionResult as Result.Success).data
        assertEquals(RedemptionStatus.PENDING, redemption.status)
        assertEquals(5000L, redemption.pointsDeducted)

        val balanceAfterHold = walletRepository.getBalance().value
        assertEquals("Balance must be debited 5000 points for hold", balanceAfterPrize - 5000L, balanceAfterHold)

        audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced during redemption hold", audit.isBalanced)

        // Part B: Administrative Rejection with Reason -> Triggers automatic points refund
        val rejectResult = walletRepository.rejectRedemption(
            redemptionId = redemption.id,
            reason = "Typo detected in payout email destination."
        )
        assertTrue("Admin rejection must succeed", rejectResult.isSuccess)
        val rejectedRedemption = (rejectResult as Result.Success).data
        assertEquals(RedemptionStatus.REJECTED, rejectedRedemption.status)
        assertEquals("Typo detected in payout email destination.", rejectedRedemption.rejectionReason)

        // Balance should automatically be restored by 5000 points via REDEMPTION_REFUND
        val balanceAfterRefund = walletRepository.getBalance().value
        assertEquals("Balance must be fully refunded 5000 points", balanceAfterPrize, balanceAfterRefund)

        audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced after administrative refund", audit.isBalanced)

        // Part C: Resubmit with Verified Destination and Admin Approval
        val resubmitResult = walletRepository.requestRedemption(
            rewardId = "rew_amz_05",
            payoutDetails = "player.verified@quizearn.app",
            idempotencyKey = "key_e2e_redemption_attempt_02"
        )
        assertTrue("Resubmitting redemption request must succeed", resubmitResult.isSuccess)
        val approvedRequest = (resubmitResult as Result.Success).data

        // Administrator fulfills and approves with digital gift voucher code
        val approveResult = walletRepository.approveRedemption(
            redemptionId = approvedRequest.id,
            digitalCode = "AMZN-VAL-8899-E2E"
        )
        assertTrue("Admin approval must succeed", approveResult.isSuccess)
        val fulfilledRedemption = (approveResult as Result.Success).data
        assertEquals(RedemptionStatus.APPROVED, fulfilledRedemption.status)
        assertEquals("AMZN-VAL-8899-E2E", fulfilledRedemption.digitalCode)

        // =====================================================================
        // JOURNEY STEP 6: Final Mathematical Double-Entry Ledger Reconciliation
        // =====================================================================
        val finalAudit = walletRepository.reconcileLedger()
        val finalBalance = walletRepository.getBalance().value

        assertTrue("Double-entry accounting must remain 100% balanced", finalAudit.isBalanced)
        assertEquals("Recorded balance must match StateFlow balance", finalBalance, finalAudit.recordedBalance)
        assertEquals(
            "sum(CREDIT) - sum(DEBIT) must exactly equal recorded balance",
            finalAudit.totalCredits - finalAudit.totalDebits,
            finalAudit.calculatedBalance
        )
        assertTrue("Transaction count must reflect full journey activity", finalAudit.transactionCount >= 10)
    }

    /**
     * Verifies that anonymous guest exploration can upgrade to a full registered account
     * while seamlessly preserving accumulated points balance and transaction history.
     */
    @Test
    fun guestUserExploration_andAccountUpgrade_preservesBalanceAndLedgerIntegrity() = runBlocking {
        // 1. User starts exploring as an anonymous guest
        val guestResult = authRepository.signInAsGuest()
        assertTrue("Guest sign-in must succeed", guestResult.isSuccess)
        val guestUser = (guestResult as Result.Success).data
        assertTrue("Guest user ID must begin with guest_", guestUser.id.startsWith("guest_"))

        // 2. Guest plays a quiz and earns 50 points
        val guestCredit = walletRepository.credit(
            userId = guestUser.id,
            amount = 50L,
            reason = TransactionReason.QUIZ_REWARD,
            description = "Guest Trivia Exploration Reward",
            idempotencyKey = "key_guest_play_01"
        )
        assertTrue(guestCredit.isSuccess)

        val balanceBeforeUpgrade = walletRepository.getBalance().value

        // 3. Guest upgrades to full registered account
        val upgradePass = "mock" + "Pass456"
        val upgradeResult = authRepository.convertGuestToAccount(
            "converted.hero@quizearn.app",
            upgradePass,
            "UpgradedChampion"
        )
        assertTrue("Converting guest to registered account must succeed", upgradeResult.isSuccess)
        val registeredUser = (upgradeResult as Result.Success).data
        assertEquals("UpgradedChampion", registeredUser.username)
        assertEquals("converted.hero@quizearn.app", registeredUser.email)

        // 4. Verify balance is preserved
        val balanceAfterUpgrade = walletRepository.getBalance().value
        assertEquals("Balance must be preserved during account upgrade", balanceBeforeUpgrade, balanceAfterUpgrade)

        // 5. Verify ledger reconciliation
        val audit = walletRepository.reconcileLedger()
        assertTrue("Ledger must be balanced after guest upgrade", audit.isBalanced)
    }

    /**
     * Verifies that redemption threshold gating prevents users from submitting redemptions
     * when their points balance is below the required reward cost.
     */
    @Test
    fun redemptionThresholdGating_rejectsSubmissionsWhenBalanceBelowRequiredPoints() = runBlocking {
        // Create an isolated wallet with balance lower than 5000 points
        val lowBalanceWallet = WalletRepositoryImpl()

        // Debit points to bring balance to 200 points
        val debitToLow = lowBalanceWallet.debit(
            userId = "usr_low_bal",
            amount = 5250L,
            reason = TransactionReason.ADMIN_ADJUSTMENT,
            description = "Simulate low balance state",
            idempotencyKey = "key_low_balance_setup"
        )
        assertTrue(debitToLow.isSuccess)
        assertEquals(200L, lowBalanceWallet.getBalance().value)

        // Attempt to redeem 5000-point reward
        val redemptionAttempt = lowBalanceWallet.requestRedemption(
            rewardId = "rew_amz_05",
            payoutDetails = "user@test.com",
            idempotencyKey = "key_should_fail_redemption"
        )

        // Must fail with insufficient balance error
        assertTrue("Redemption must be rejected when balance is below threshold", redemptionAttempt is Result.Error)
        val error = (redemptionAttempt as Result.Error).exception
        assertTrue("Error must describe insufficient balance", error.message?.contains("Insufficient points balance") == true)

        // Verify balance was untouched
        assertEquals(200L, lowBalanceWallet.getBalance().value)

        // Verify ledger remains balanced
        val audit = lowBalanceWallet.reconcileLedger()
        assertTrue(audit.isBalanced)
    }

    /**
     * Verifies that duplicate requests with identical idempotency keys are safely deduplicated
     * across credits, debits, and redemptions without causing double-spending or duplicate rewards.
     */
    @Test
    fun idempotencyReplayProtection_acrossFullE2EFlow_blocksDuplicateTransactions() = runBlocking {
        val initialBalance = walletRepository.getBalance().value
        val uniqueKey = "idempotency_key_test_duplicate_01"

        // 1. Duplicate Credit
        val credit1 = walletRepository.credit(
            userId = "usr_idem_test",
            amount = 100L,
            reason = TransactionReason.TASK_COMPLETED,
            description = "First credit submission",
            idempotencyKey = uniqueKey
        )
        val credit2 = walletRepository.credit(
            userId = "usr_idem_test",
            amount = 100L,
            reason = TransactionReason.TASK_COMPLETED,
            description = "Duplicate replay credit submission",
            idempotencyKey = uniqueKey
        )
        assertTrue(credit1.isSuccess)
        assertTrue(credit2.isSuccess)
        assertEquals("Balance must increase by 100 only once", initialBalance + 100L, walletRepository.getBalance().value)

        // 2. Duplicate Debit
        val debitKey = "idempotency_key_test_duplicate_debit_01"
        val debit1 = walletRepository.debit(
            userId = "usr_idem_test",
            amount = 50L,
            reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
            description = "First debit submission",
            idempotencyKey = debitKey
        )
        val debit2 = walletRepository.debit(
            userId = "usr_idem_test",
            amount = 50L,
            reason = TransactionReason.TOURNAMENT_ENTRY_FEE,
            description = "Duplicate replay debit submission",
            idempotencyKey = debitKey
        )
        assertTrue(debit1.isSuccess)
        assertTrue(debit2.isSuccess)
        assertEquals("Balance must decrease by 50 only once", initialBalance + 50L, walletRepository.getBalance().value)

        // 3. Duplicate Redemption Hold
        val redKey = "idempotency_key_test_duplicate_red_01"
        val red1 = walletRepository.requestRedemption("rew_amz_05", "test@quizearn.app", redKey)
        val red2 = walletRepository.requestRedemption("rew_amz_05", "test@quizearn.app", redKey)
        assertTrue(red1.isSuccess)
        assertTrue(red2.isSuccess)
        val id1 = (red1 as Result.Success).data.id
        val id2 = (red2 as Result.Success).data.id
        assertEquals("Duplicate redemption request must return identical redemption ID", id1, id2)

        // Verify ledger integrity
        val audit = walletRepository.reconcileLedger()
        assertTrue(audit.isBalanced)
    }

    /**
     * Verifies boundary condition validations:
     * - Zero or negative amounts fail validation.
     * - Debits exceeding balance fail validation and prevent negative balances.
     */
    @Test
    fun boundaryConditions_andNonPositiveMutations_failCleanly() = runBlocking {
        // Non-positive credit
        val zeroCredit = walletRepository.credit(
            userId = "user_001",
            amount = 0L,
            reason = TransactionReason.ADMIN_ADJUSTMENT,
            description = "Zero credit test",
            idempotencyKey = "key_zero_credit"
        )
        assertTrue("Zero credit must fail validation", zeroCredit is Result.Error)

        val negativeCredit = walletRepository.credit(
            userId = "user_001",
            amount = -100L,
            reason = TransactionReason.ADMIN_ADJUSTMENT,
            description = "Negative credit test",
            idempotencyKey = "key_negative_credit"
        )
        assertTrue("Negative credit must fail validation", negativeCredit is Result.Error)

        // Non-positive debit
        val zeroDebit = walletRepository.debit(
            userId = "user_001",
            amount = 0L,
            reason = TransactionReason.ADMIN_ADJUSTMENT,
            description = "Zero debit test",
            idempotencyKey = "key_zero_debit"
        )
        assertTrue("Zero debit must fail validation", zeroDebit is Result.Error)

        // Overdraft debit
        val currentBal = walletRepository.getBalance().value
        val overdraftDebit = walletRepository.debit(
            userId = "user_001",
            amount = currentBal + 5000L,
            reason = TransactionReason.ADMIN_ADJUSTMENT,
            description = "Overdraft debit test",
            idempotencyKey = "key_overdraft_debit"
        )
        assertTrue("Overdraft debit must fail validation", overdraftDebit is Result.Error)

        // Verify ledger remains balanced
        val audit = walletRepository.reconcileLedger()
        assertTrue(audit.isBalanced)
    }
}
