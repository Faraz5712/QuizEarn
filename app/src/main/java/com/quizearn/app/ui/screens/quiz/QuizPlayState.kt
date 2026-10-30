package com.quizearn.app.ui.screens.quiz

import com.quizearn.app.domain.model.PlayerAnswer
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession

/**
 * State representing a single question during gameplay.
 */
data class QuestionPlayState(
    val question: Question,
    val selectedOptionIndex: Int? = null,
    val isLocked: Boolean = false,
    val timeSpentSeconds: Double = 0.0,
    val disabledOptionIndices: Set<Int> = emptySet(),
    val isSkipped: Boolean = false
)

/**
 * UI State container for the active QuizPlay session.
 */
data class QuizPlayUiState(
    val session: QuizSession? = null,
    val categoryName: String = "",
    val currentQuestionIndex: Int = 0,
    val remainingSeconds: Int = 15,
    val questionTimeLimit: Int = 15,
    val isFiftyFiftyUsed: Boolean = false,
    val isSkipUsed: Boolean = false,
    val recordedAnswers: List<PlayerAnswer> = emptyList(),
    val isSubmitting: Boolean = false,
    val showForfeitDialog: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val totalQuestions: Int
        get() = session?.questions?.size ?: 0

    val currentQuestion: Question?
        get() = session?.questions?.getOrNull(currentQuestionIndex)

    val progressFraction: Float
        get() = if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat() else 0f
}
