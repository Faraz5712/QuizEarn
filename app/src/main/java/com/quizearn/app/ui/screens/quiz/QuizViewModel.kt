package com.quizearn.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.QuizRepository
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.domain.model.PlayerAnswer
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSubmission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel governing time-bounded quiz gameplay, question timer countdowns,
 * lifelines (50-50, skip), and server-authoritative submission with idempotency protection.
 */
class QuizViewModel(
    private val quizRepository: QuizRepository = QuizRepositoryImpl.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizPlayUiState())
    val uiState: StateFlow<QuizPlayUiState> = _uiState.asStateFlow()

    private var submissionIdempotencyKey: String? = null
    var evaluatedResult: EvaluatedQuizResult? = null
        private set

    fun loadQuizSession(
        categoryId: String,
        idempotencyKey: String = UUID.randomUUID().toString()
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = quizRepository.startQuizSession(categoryId, idempotencyKey)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            session = result.data,
                            categoryName = result.data.categoryId,
                            isLoading = false,
                            errorMessage = null,
                            currentQuestionIndex = 0,
                            remainingSeconds = 15,
                            isFiftyFiftyUsed = false,
                            isSkipUsed = false,
                            recordedAnswers = emptyList()
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to start quiz session."
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        val state = _uiState.value
        if (state.isSubmitting) return
        _uiState.update { it.copy() }
    }

    fun onTimerTick() {
        val state = _uiState.value
        if (state.session == null || state.isSubmitting) return

        if (state.remainingSeconds > 1) {
            _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
        } else {
            // Auto-advance on expiration deadline with no answer selected (-1)
            submitCurrentAnswer(selectedOption = -1)
        }
    }

    fun submitCurrentAnswer(selectedOption: Int) {
        val state = _uiState.value
        val session = state.session ?: return
        if (state.isSubmitting) return

        val currentQ = state.currentQuestion ?: return
        val timeSpent = (state.questionTimeLimit - state.remainingSeconds).coerceAtLeast(1).toDouble()

        val answer = PlayerAnswer(
            questionId = currentQ.id,
            selectedOption = selectedOption,
            timeSpentSeconds = timeSpent
        )

        val updatedAnswers = state.recordedAnswers + answer

        if (state.currentQuestionIndex < state.totalQuestions - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    remainingSeconds = it.questionTimeLimit,
                    recordedAnswers = updatedAnswers
                )
            }
        } else {
            // Final question complete: trigger server-authoritative submission
            _uiState.update { it.copy(recordedAnswers = updatedAnswers, isSubmitting = true) }
            finalizeQuizSubmission(session, updatedAnswers)
        }
    }

    fun useFiftyFifty(): Boolean {
        val state = _uiState.value
        if (state.isFiftyFiftyUsed || state.isSubmitting) return false
        _uiState.update { it.copy(isFiftyFiftyUsed = true) }
        return true
    }

    fun useSkip(): Boolean {
        val state = _uiState.value
        if (state.isSkipUsed || state.isSubmitting) return false
        _uiState.update { it.copy(isSkipUsed = true) }
        submitCurrentAnswer(selectedOption = -1)
        return true
    }

    private fun finalizeQuizSubmission(
        session: QuizSession,
        answers: List<PlayerAnswer>,
        forcedKey: String? = null
    ) {
        val key = forcedKey ?: submissionIdempotencyKey ?: UUID.randomUUID().toString().also {
            submissionIdempotencyKey = it
        }

        viewModelScope.launch {
            val submission = QuizSubmission(
                sessionId = session.id,
                sessionToken = session.sessionToken,
                answers = answers,
                idempotencyKey = key
            )

            when (val result = quizRepository.submitQuizAnswers(submission)) {
                is Result.Success -> {
                    evaluatedResult = result.data
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.exception.message ?: "Failed to evaluate quiz answers."
                        )
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isSubmitting = true) }
                }
            }
        }
    }
}
