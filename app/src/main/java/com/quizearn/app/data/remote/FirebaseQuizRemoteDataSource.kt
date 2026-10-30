package com.quizearn.app.data.remote

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSessionStatus
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.ui.screens.quiz.EvaluatedQuizResult
import com.quizearn.app.ui.screens.quiz.PerformanceTier
import com.quizearn.app.ui.screens.quiz.QuestionReviewItem
import com.quizearn.app.ui.screens.quiz.QuizResultSummary

class FirebaseQuizRemoteDataSource : QuizRemoteDataSource {
    override suspend fun createQuizSession(userId: String, categoryId: String): Result<QuizSession> =
        callResult("startQuizSession", mapOf("categoryId" to categoryId)) { data ->
            val questions = data.list("questions").map { item ->
                val question = item.objectMap()
                Question(
                    id = question.string("id"),
                    categoryId = question.string("categoryId"),
                    text = question.string("text"),
                    options = question.stringList("options"),
                    timeLimitSeconds = question.int("timeLimitSeconds"),
                    pointsReward = question.int("pointsReward")
                )
            }
            QuizSession(
                id = data.string("sessionId"),
                userId = data.string("userId"),
                categoryId = data.string("categoryId"),
                questions = questions,
                status = QuizSessionStatus.ACTIVE,
                timeLimitSeconds = data.int("timeLimitSeconds"),
                startedAtTimestamp = data.long("startedAt"),
                expiresAtTimestamp = data.long("expiresAt"),
                sessionToken = ""
            )
        }

    override suspend fun submitQuizAnswers(submission: QuizSubmission): Result<EvaluatedQuizResult> =
        callResult(
            "submitQuizAnswers",
            mapOf(
                "sessionId" to submission.sessionId,
                "idempotencyKey" to submission.idempotencyKey,
                "answers" to submission.answers.map {
                    mapOf(
                        "questionId" to it.questionId,
                        "selectedOption" to it.selectedOption,
                        "timeSpentSeconds" to it.timeSpentSeconds
                    )
                }
            ),
            ::toEvaluatedResult
        )

    override suspend fun getEvaluatedResult(sessionId: String): Result<EvaluatedQuizResult> =
        callResult("getQuizResult", mapOf("sessionId" to sessionId), ::toEvaluatedResult)

    private fun toEvaluatedResult(data: Map<String, Any?>): EvaluatedQuizResult {
        val summaryMap = data["summary"].objectMap()
        val summary = QuizResultSummary(
            sessionId = summaryMap.string("sessionId"),
            categoryTitle = summaryMap.string("categoryTitle"),
            score = summaryMap.int("score"),
            totalQuestions = summaryMap.int("totalQuestions"),
            correctCount = summaryMap.int("correctCount"),
            pointsEarned = summaryMap.long("pointsEarned"),
            accuracyRate = summaryMap.double("accuracyRate"),
            timeElapsedSeconds = summaryMap.int("timeElapsedSeconds"),
            performanceTier = PerformanceTier.fromAccuracy(summaryMap.double("accuracyRate"))
        )
        val reviews = data.list("reviews").map { item ->
            val review = item.objectMap()
            QuestionReviewItem(
                questionNumber = review.int("questionNumber"),
                questionText = review.string("questionText"),
                options = review.stringList("options"),
                playerSelectedOptionIndex = review.int("playerSelectedOptionIndex"),
                correctOptionIndex = review.int("correctOptionIndex"),
                isCorrect = review["isCorrect"] as? Boolean
                    ?: error("Firebase review is missing its correctness result."),
                explanationText = review.string("explanationText")
            )
        }
        return EvaluatedQuizResult(summary, reviews)
    }

    private suspend fun <T> callResult(
        name: String,
        payload: Map<String, Any?>,
        transform: (Map<String, Any?>) -> T
    ): Result<T> = try {
        Result.Success(transform(FirebaseBackend.call(name, payload)))
    } catch (exception: Exception) {
        Result.Error(exception)
    }

    private fun Any?.objectMap(): Map<String, Any?> {
        val map = this as? Map<*, *> ?: error("Invalid response object from Firebase.")
        return map.entries.associate { (key, value) ->
            (key as? String ?: error("Invalid Firebase response key.")) to value
        }
    }

    private fun Map<String, Any?>.string(key: String): String =
        this[key] as? String ?: error("Firebase response is missing '$key'.")

    private fun Map<String, Any?>.int(key: String): Int =
        (this[key] as? Number)?.toInt() ?: error("Firebase response is missing '$key'.")

    private fun Map<String, Any?>.long(key: String): Long =
        (this[key] as? Number)?.toLong() ?: error("Firebase response is missing '$key'.")

    private fun Map<String, Any?>.double(key: String): Double =
        (this[key] as? Number)?.toDouble() ?: error("Firebase response is missing '$key'.")

    private fun Map<String, Any?>.list(key: String): List<Any?> =
        this[key] as? List<Any?> ?: error("Firebase response is missing '$key'.")

    private fun Map<String, Any?>.stringList(key: String): List<String> =
        list(key).map { it as? String ?: error("Invalid '$key' item from Firebase.") }
}
