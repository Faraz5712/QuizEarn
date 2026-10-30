package com.quizearn.app.domain.model

/**
 * Domain representation of an active or concluded quiz session.
 * Created server-side with time boundaries and token attestation.
 */
data class QuizSession(
    val id: String,
    val userId: String,
    val categoryId: String,
    val questions: List<Question>,
    val status: QuizSessionStatus = QuizSessionStatus.ACTIVE,
    val score: Int = 0,
    val pointsAwarded: Int = 0,
    val timeLimitSeconds: Int = 90,
    val startedAtTimestamp: Long = System.currentTimeMillis(),
    val expiresAtTimestamp: Long = System.currentTimeMillis() + 90_000L,
    val sessionToken: String = ""
)

/**
 * Lifecycle statuses of a quiz session.
 */
enum class QuizSessionStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED,
    ABANDONED
}

/**
 * Client-side recorded answer for a single question.
 * Contains no correct answer indication.
 */
data class PlayerAnswer(
    val questionId: String,
    val selectedOption: Int,
    val timeSpentSeconds: Double
)

/**
 * Payload sent to the server for authoritative answer evaluation.
 * Includes session authorization token and anti-replay idempotency key.
 */
data class QuizSubmission(
    val sessionId: String,
    val sessionToken: String,
    val answers: List<PlayerAnswer>,
    val idempotencyKey: String,
    val submittedAtTimestamp: Long = System.currentTimeMillis()
)
