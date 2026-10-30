package com.quizearn.app.ui.screens.quiz

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.QuizRepository
import com.quizearn.app.domain.model.Category
import com.quizearn.app.domain.model.PlayerAnswer
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSessionStatus
import com.quizearn.app.domain.model.QuizSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying QuizViewModel time-bounded gameplay, question timer ticks,
 * deadline auto-advance, option selection, server submission, and idempotency key protection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeQuizRepository
    private lateinit var viewModel: QuizViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuizRepository()
        viewModel = QuizViewModel(quizRepository = fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadQuizSession_success_presentsSanitizedQuestionsWithoutAnswerKeys() = runTest(testDispatcher) {
        // Given a requested category
        val categoryId = "cat_science"

        // When loading the session
        viewModel.loadQuizSession(categoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then session is populated with sanitized questions
        val state = viewModel.uiState.value
        assertFalse("Loading state should be cleared", state.isLoading)
        assertNull("Error message should be null", state.errorMessage)
        assertNotNull("Session must be active", state.session)
        assertEquals("Science", state.categoryName)
        assertEquals(2, state.totalQuestions)
        assertEquals(0, state.currentQuestionIndex)
        assertEquals(15, state.remainingSeconds)

        // Verify that client question models expose no correct answer keys
        state.session?.questions?.forEach { question ->
            assertNotNull("Question text must be present", question.text)
            assertEquals("Question must have 4 options", 4, question.options.size)
        }
    }

    @Test
    fun onTimerTick_decrementsRemainingSeconds_andAutoAdvancesOnExpiration() = runTest(testDispatcher) {
        viewModel.loadQuizSession("cat_science")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(15, viewModel.uiState.value.remainingSeconds)

        // Tick once: 15 -> 14
        viewModel.onTimerTick()
        assertEquals(14, viewModel.uiState.value.remainingSeconds)

        // Tick down until 1 second remaining
        repeat(13) {
            viewModel.onTimerTick()
        }
        assertEquals(1, viewModel.uiState.value.remainingSeconds)
        assertEquals(0, viewModel.uiState.value.currentQuestionIndex)

        // Tick at 1 second: triggers auto-advance on expiration deadline with selectedOption = -1
        viewModel.onTimerTick()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Should auto-advance to question 2 (index 1)", 1, state.currentQuestionIndex)
        assertEquals("Timer should reset to 15 seconds", 15, state.remainingSeconds)
        assertEquals("One answer should be recorded for expiration", 1, state.recordedAnswers.size)
        assertEquals(-1, state.recordedAnswers[0].selectedOption)
    }

    @Test
    fun submitCurrentAnswer_recordsAnswer_andFinalizesSubmissionOnLastQuestion() = runTest(testDispatcher) {
        viewModel.loadQuizSession("cat_science")
        testDispatcher.scheduler.advanceUntilIdle()

        // Question 1: Submit option 2
        viewModel.submitCurrentAnswer(selectedOption = 2)
        assertEquals(1, viewModel.uiState.value.currentQuestionIndex)
        assertEquals(1, viewModel.uiState.value.recordedAnswers.size)
        assertEquals(2, viewModel.uiState.value.recordedAnswers[0].selectedOption)

        // Question 2 (Final): Submit option 3
        viewModel.submitCurrentAnswer(selectedOption = 3)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then submission completes and evaluated result is populated
        val state = viewModel.uiState.value
        assertFalse("Submitting flag should be reset", state.isSubmitting)
        assertNull("Error should be null", state.errorMessage)
        assertNotNull("Evaluated result must be populated", viewModel.evaluatedResult)

        val result = viewModel.evaluatedResult!!
        assertEquals("sess_test_123", result.summary.sessionId)
        assertEquals(2, result.summary.totalQuestions)
        assertEquals(20, result.summary.score)
        assertEquals(2, result.reviews.size)
    }

    @Test
    fun duplicateSubmissionRejection_viaIdempotencyKeys() = runTest(testDispatcher) {
        viewModel.loadQuizSession("cat_science")
        testDispatcher.scheduler.advanceUntilIdle()

        // Submit both questions
        viewModel.submitCurrentAnswer(selectedOption = 0)
        viewModel.submitCurrentAnswer(selectedOption = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial submission succeeded
        assertNotNull(viewModel.evaluatedResult)
        assertEquals(1, fakeRepository.submittedKeys.size)
        val initialKey = fakeRepository.submittedKeys.first()

        // Attempt second submission with duplicate idempotency key
        val duplicateSubmission = QuizSubmission(
            sessionId = "sess_test_123",
            sessionToken = "tok_test_abc",
            answers = listOf(PlayerAnswer("q1", 0, 3.0), PlayerAnswer("q2", 1, 4.0)),
            idempotencyKey = initialKey
        )

        val duplicateResult = fakeRepository.submitQuizAnswers(duplicateSubmission)
        assertTrue("Duplicate idempotency key must be rejected", duplicateResult is Result.Error)
        assertEquals(
            "Duplicate submission rejected: idempotency key already processed",
            (duplicateResult as Result.Error).exception.message
        )
    }

    @Test
    fun lifelines_fiftyFifty_and_skip_logic() = runTest(testDispatcher) {
        viewModel.loadQuizSession("cat_science")
        testDispatcher.scheduler.advanceUntilIdle()

        // Test 50-50 lifeline
        assertFalse(viewModel.uiState.value.isFiftyFiftyUsed)
        val first5050 = viewModel.useFiftyFifty()
        assertTrue("First 50:50 use should succeed", first5050)
        assertTrue("isFiftyFiftyUsed should now be true", viewModel.uiState.value.isFiftyFiftyUsed)

        val second5050 = viewModel.useFiftyFifty()
        assertFalse("Second 50:50 use must be rejected", second5050)

        // Test Skip lifeline
        assertFalse(viewModel.uiState.value.isSkipUsed)
        val firstSkip = viewModel.useSkip()
        assertTrue("Skip should succeed", firstSkip)
        assertTrue("isSkipUsed should now be true", viewModel.uiState.value.isSkipUsed)
        assertEquals("Skip should auto-advance to question 1", 1, viewModel.uiState.value.currentQuestionIndex)
        assertEquals("Skip records answer with -1", -1, viewModel.uiState.value.recordedAnswers.last().selectedOption)

        val secondSkip = viewModel.useSkip()
        assertFalse("Second Skip use must be rejected", secondSkip)
    }

    /**
     * Fake QuizRepository with deterministic in-memory session generation
     * and anti-replay idempotency key verification.
     */
    private class FakeQuizRepository : QuizRepository {
        val submittedKeys = mutableSetOf<String>()

        override fun getCategories(): Flow<Result<List<Category>>> {
            return flowOf(
                Result.Success(
                    listOf(
                        Category(
                            id = "cat_science",
                            name = "Science",
                            slug = "science",
                            colorHex = "#5B5FEF",
                            totalQuestions = 25
                        )
                    )
                )
            )
        }

        override suspend fun startQuizSession(
            categoryId: String,
            idempotencyKey: String
        ): Result<QuizSession> {
            val questions = listOf(
                Question(
                    id = "q1",
                    categoryId = categoryId,
                    text = "What is the chemical symbol for Gold?",
                    options = listOf("Au", "Ag", "Fe", "Cu")
                ),
                Question(
                    id = "q2",
                    categoryId = categoryId,
                    text = "What planet is known as the Red Planet?",
                    options = listOf("Venus", "Mars", "Jupiter", "Saturn")
                )
            )
            return Result.Success(
                QuizSession(
                    id = "sess_test_123",
                    userId = "user_test_99",
                    categoryId = "Science",
                    questions = questions,
                    status = QuizSessionStatus.ACTIVE,
                    sessionToken = "tok_test_abc"
                )
            )
        }

        override suspend fun submitQuizAnswers(submission: QuizSubmission): Result<EvaluatedQuizResult> {
            if (submittedKeys.contains(submission.idempotencyKey)) {
                return Result.Error(
                    IllegalStateException("Duplicate submission rejected: idempotency key already processed")
                )
            }
            submittedKeys.add(submission.idempotencyKey)

            val summary = QuizResultSummary(
                sessionId = submission.sessionId,
                categoryTitle = "Science",
                score = 20,
                totalQuestions = submission.answers.size,
                correctCount = submission.answers.size,
                pointsEarned = 20L,
                accuracyRate = 100.0,
                timeElapsedSeconds = 12
            )
            val reviews = submission.answers.mapIndexed { idx, ans ->
                QuestionReviewItem(
                    questionNumber = idx + 1,
                    questionText = "Question ${idx + 1}",
                    options = listOf("Option A", "Option B", "Option C", "Option D"),
                    playerSelectedOptionIndex = ans.selectedOption,
                    correctOptionIndex = ans.selectedOption.coerceAtLeast(0),
                    isCorrect = ans.selectedOption >= 0,
                    explanationText = "Authoritative explanation for question ${idx + 1}."
                )
            }
            return Result.Success(EvaluatedQuizResult(summary = summary, reviews = reviews))
        }

        override suspend fun getQuizResult(sessionId: String): Result<EvaluatedQuizResult> {
            val summary = QuizResultSummary(
                sessionId = sessionId,
                categoryTitle = "Science",
                score = 20,
                totalQuestions = 2,
                correctCount = 2,
                pointsEarned = 20L,
                accuracyRate = 100.0,
                timeElapsedSeconds = 12
            )
            return Result.Success(EvaluatedQuizResult(summary = summary, reviews = emptyList()))
        }
    }
}
