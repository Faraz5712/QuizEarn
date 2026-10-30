package com.quizearn.app.data.repository

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.Category
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.ui.screens.quiz.EvaluatedQuizResult
import com.quizearn.app.ui.screens.quiz.QuizHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository interface for quiz categories, session initialization, and authoritative scoring.
 */
interface QuizRepository {
    /**
     * Retrieve the list of available trivia categories.
     */
    fun getCategories(): Flow<Result<List<Category>>>

    /**
     * Request the backend to start a time-bounded quiz session with sanitized questions.
     */
    suspend fun startQuizSession(
        categoryId: String,
        idempotencyKey: String
    ): Result<QuizSession>

    /**
     * Submit user answers for server-side evaluation and reward crediting.
     */
    suspend fun submitQuizAnswers(
        submission: QuizSubmission
    ): Result<EvaluatedQuizResult>

    /**
     * Fetch the authoritatively evaluated result and review breakdown for a finished session.
     */
    suspend fun getQuizResult(
        sessionId: String
    ): Result<EvaluatedQuizResult>

    /**
     * Live reactive stream of completed quiz session history.
     */
    fun getQuizHistory(userId: String = "usr_current"): Flow<List<QuizHistoryItem>> = flowOf(emptyList())
}
