package com.quizearn.app.data.remote

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.ui.screens.quiz.EvaluatedQuizResult

/**
 * Remote contract for server-authoritative quiz operations.
 * Communicates with Cloud Functions / backend endpoints to ensure questions are generated server-side,
 * answer keys remain hidden on the server, sessions are bounded by strict expiration deadlines,
 * and duplicate submissions are rejected via client-generated idempotency keys.
 */
interface QuizRemoteDataSource {
    /**
     * Request the server to generate an active quiz session with randomized sanitized questions
     * (omitting correct answer keys and explanations) and an absolute expiresAt timestamp.
     */
    suspend fun createQuizSession(
        userId: String,
        categoryId: String
    ): Result<QuizSession>

    /**
     * Submit user answers for server-side evaluation, anti-fraud checks,
     * session expiration enforcement, and atomic points ledger awarding.
     */
    suspend fun submitQuizAnswers(
        submission: QuizSubmission
    ): Result<EvaluatedQuizResult>

    /**
     * Retrieve server-evaluated quiz result and review breakdown by session ID.
     */
    suspend fun getEvaluatedResult(
        sessionId: String
    ): Result<EvaluatedQuizResult>
}
