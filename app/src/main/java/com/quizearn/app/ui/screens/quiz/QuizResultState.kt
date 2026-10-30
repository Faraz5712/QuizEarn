package com.quizearn.app.ui.screens.quiz

/**
 * Performance tier categorization based on quiz accuracy rate.
 */
enum class PerformanceTier(
    val title: String,
    val subtitle: String,
    val colorHex: String
) {
    GRANDMASTER(
        title = "Grandmaster Genius",
        subtitle = "Flawless mastery of this trivia domain!",
        colorHex = "#F5B942"
    ),
    EXPERT(
        title = "Expert Scholar",
        subtitle = "Exceptional performance with sharp intellect!",
        colorHex = "#8B5CF6"
    ),
    PROFICIENT(
        title = "Skilled Explorer",
        subtitle = "Solid knowledge! Keep leveling up.",
        colorHex = "#2DD4BF"
    ),
    APPRENTICE(
        title = "Trivia Challenger",
        subtitle = "Good effort! Practice makes permanent.",
        colorHex = "#94A3B8"
    );

    companion object {
        fun fromAccuracy(accuracy: Double): PerformanceTier {
            return when {
                accuracy >= 90.0 -> GRANDMASTER
                accuracy >= 70.0 -> EXPERT
                accuracy >= 50.0 -> PROFICIENT
                else -> APPRENTICE
            }
        }
    }
}

/**
 * Summary metrics of a finished quiz session.
 */
data class QuizResultSummary(
    val sessionId: String,
    val categoryTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int,
    val pointsEarned: Long,
    val accuracyRate: Double,
    val timeElapsedSeconds: Int,
    val performanceTier: PerformanceTier = PerformanceTier.fromAccuracy(accuracyRate)
)

/**
 * Review model for a single question post-session.
 * Reveals correct answer index and explanatory text only after server evaluation.
 */
data class QuestionReviewItem(
    val questionNumber: Int,
    val questionText: String,
    val options: List<String>,
    val playerSelectedOptionIndex: Int,
    val correctOptionIndex: Int,
    val isCorrect: Boolean,
    val explanationText: String
)

/**
 * Historical quiz session record.
 */
data class QuizHistoryItem(
    val sessionId: String,
    val categoryTitle: String,
    val timestampFormatted: String,
    val score: Int,
    val totalQuestions: Int,
    val pointsEarned: Long,
    val accuracyRate: Double
)

/**
 * Complete server-evaluated result bundle containing summary metrics
 * and individual question reviews with revealed explanations.
 */
data class EvaluatedQuizResult(
    val summary: QuizResultSummary,
    val reviews: List<QuestionReviewItem>
)
