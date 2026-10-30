package com.quizearn.app.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.quizearn.app.ui.screens.quiz.QuizHistoryItem

/**
 * Room entity representing a completed quiz play session.
 */
@Entity(
    tableName = "quiz_history",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["sessionId"], unique = true)
    ]
)
data class QuizHistoryEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String,
    val categoryTitle: String,
    val timestampFormatted: String,
    val score: Int,
    val totalQuestions: Int,
    val pointsEarned: Long,
    val accuracyRate: Double,
    val timestampIso: String
) {
    fun toDomain(): QuizHistoryItem = QuizHistoryItem(
        sessionId = sessionId,
        categoryTitle = categoryTitle,
        timestampFormatted = timestampFormatted,
        score = score,
        totalQuestions = totalQuestions,
        pointsEarned = pointsEarned,
        accuracyRate = accuracyRate
    )

    companion object {
        fun fromDomain(userId: String, item: QuizHistoryItem, timestampIso: String = ""): QuizHistoryEntity =
            QuizHistoryEntity(
                sessionId = item.sessionId,
                userId = userId,
                categoryTitle = item.categoryTitle,
                timestampFormatted = item.timestampFormatted,
                score = item.score,
                totalQuestions = item.totalQuestions,
                pointsEarned = item.pointsEarned,
                accuracyRate = item.accuracyRate,
                timestampIso = timestampIso
            )
    }
}
