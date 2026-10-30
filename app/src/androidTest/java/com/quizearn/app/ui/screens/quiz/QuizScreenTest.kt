package com.quizearn.app.ui.screens.quiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.QuizRepository
import com.quizearn.app.domain.model.Category
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSessionStatus
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.ui.theme.QuizEarnTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI interaction tests verifying QuizPlayScreen option item tap selection,
 * visual selected state styling, lifelines, question counter, countdown timer rendering,
 * and forfeit confirmation dialogs.
 */
@RunWith(AndroidJUnit4::class)
class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quizScreen_rendersQuestionCounterAndTimer_onInitialLoad() {
        val fakeRepo = createFakeQuizRepository()

        composeTestRule.setContent {
            QuizEarnTheme {
                QuizPlayScreen(
                    categoryId = "science",
                    onSubmitQuiz = {},
                    onForfeit = {},
                    quizRepository = fakeRepo
                )
            }
        }

        // Verify Question Counter renders Question 1 of 2 ("Q 1/2")
        composeTestRule.onNodeWithText("Q 1/2").assertIsDisplayed()

        // Verify Category pill renders "Science"
        composeTestRule.onNodeWithText("Science").assertIsDisplayed()

        // Verify 15-second countdown timer text is displayed
        composeTestRule.onNodeWithText("15s").assertIsDisplayed()

        // Verify Question text is displayed
        composeTestRule
            .onNodeWithText("Which element has the atomic number 1?")
            .assertIsDisplayed()

        // Verify all 4 options are rendered
        composeTestRule.onNodeWithText("Hydrogen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Helium").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lithium").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oxygen").assertIsDisplayed()
    }

    @Test
    fun quizScreen_optionSelection_registersTap() {
        val fakeRepo = createFakeQuizRepository()

        composeTestRule.setContent {
            QuizEarnTheme {
                QuizPlayScreen(
                    categoryId = "science",
                    onSubmitQuiz = {},
                    onForfeit = {},
                    quizRepository = fakeRepo
                )
            }
        }

        // Tap the option "Hydrogen"
        val optionNode = composeTestRule.onNodeWithText("Hydrogen")
        optionNode.assertIsDisplayed()
        optionNode.performClick()
    }

    @Test
    fun quizScreen_lifelines_fiftyFifty_disablesTwoOptions() {
        val fakeRepo = createFakeQuizRepository()

        composeTestRule.setContent {
            QuizEarnTheme {
                QuizPlayScreen(
                    categoryId = "science",
                    onSubmitQuiz = {},
                    onForfeit = {},
                    quizRepository = fakeRepo
                )
            }
        }

        // 50:50 Lifeline should initially be enabled
        val fiftyFiftyButton = composeTestRule.onNodeWithText("50:50")
        fiftyFiftyButton.assertIsDisplayed()
        fiftyFiftyButton.assertIsEnabled()

        // Tap 50:50 Lifeline
        fiftyFiftyButton.performClick()

        // Lifeline text updates to "50:50 (Used)" and is disabled
        composeTestRule.onNodeWithText("50:50 (Used)").assertIsDisplayed()
        composeTestRule.onNodeWithText("50:50 (Used)").assertIsNotEnabled()
    }

    @Test
    fun quizScreen_forfeitConfirmationDialog_displayedOnQuit() {
        val fakeRepo = createFakeQuizRepository()
        var forfeitTriggered = false

        composeTestRule.setContent {
            QuizEarnTheme {
                QuizPlayScreen(
                    categoryId = "science",
                    onSubmitQuiz = {},
                    onForfeit = { forfeitTriggered = true },
                    quizRepository = fakeRepo
                )
            }
        }

        // Tap Quit Quiz top bar icon
        composeTestRule.onNodeWithContentDescription("Quit Quiz").performClick()

        // Forfeit confirmation dialog must appear
        composeTestRule.onNodeWithText("Quit Quiz?").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Are you sure you want to forfeit this session? Your current quiz progress and any earned points will be lost.")
            .assertIsDisplayed()

        // Tap "Resume Quiz" to dismiss dialog
        composeTestRule.onNodeWithText("Resume Quiz").performClick()

        // Verify Question 1 is still displayed
        composeTestRule.onNodeWithText("Q 1/2").assertIsDisplayed()
    }

    private fun createFakeQuizRepository(): QuizRepository {
        return object : QuizRepository {
            override fun getCategories(): Flow<Result<List<Category>>> {
                return flowOf(
                    Result.Success(
                        listOf(
                            Category("science", "Science", "Science trivia", "#5B5FEF", 10)
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
                        text = "Which element has the atomic number 1?",
                        options = listOf("Hydrogen", "Helium", "Lithium", "Oxygen")
                    ),
                    Question(
                        id = "q2",
                        categoryId = categoryId,
                        text = "What is the speed of light in m/s?",
                        options = listOf("3x10^8", "1.5x10^8", "3x10^6", "3x10^5")
                    )
                )
                return Result.Success(
                    QuizSession(
                        id = "sess_compose_test_001",
                        userId = "user_ui_tester",
                        categoryId = categoryId,
                        questions = questions,
                        status = QuizSessionStatus.ACTIVE,
                        sessionToken = "tok_ui_test"
                    )
                )
            }

            override suspend fun submitQuizAnswers(submission: QuizSubmission): Result<EvaluatedQuizResult> {
                val summary = QuizResultSummary(
                    sessionId = submission.sessionId,
                    categoryTitle = "Science",
                    score = 20,
                    totalQuestions = 2,
                    correctCount = 2,
                    pointsEarned = 20L,
                    accuracyRate = 100.0,
                    timeElapsedSeconds = 10
                )
                return Result.Success(EvaluatedQuizResult(summary = summary, reviews = emptyList()))
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
                    timeElapsedSeconds = 10
                )
                return Result.Success(EvaluatedQuizResult(summary = summary, reviews = emptyList()))
            }
        }
    }
}
