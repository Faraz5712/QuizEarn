package com.quizearn.app.ui.screens.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.QuizRepository
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.domain.model.PlayerAnswer
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.ErrorStateView
import com.quizearn.app.ui.components.LoadingStateView
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.ErrorCrimson
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.OptionShape
import com.quizearn.app.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun QuizPlayScreen(
    categoryId: String,
    onSubmitQuiz: (sessionId: String) -> Unit,
    onForfeit: () -> Unit,
    quizRepository: QuizRepository = QuizRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var session by remember { mutableStateOf<QuizSession?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isQuestionLocked by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(15) }
    val questionTimeLimit = 15

    var disabledOptions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isFiftyFiftyUsed by remember { mutableStateOf(false) }
    var isSkipUsed by remember { mutableStateOf(false) }
    val recordedAnswers = remember { mutableListOf<PlayerAnswer>() }

    var showForfeitDialog by remember { mutableStateOf(false) }
    var showSubmitErrorDialog by remember { mutableStateOf(false) }
    var submitErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Intercept back button to confirm forfeit
    BackHandler(enabled = !isSubmitting) {
        showForfeitDialog = true
    }

    // Initialize Quiz Session from repository
    LaunchedEffect(categoryId) {
        isLoading = true
        errorMessage = null
        when (val result = quizRepository.startQuizSession(categoryId, UUID.randomUUID().toString())) {
            is Result.Success -> {
                session = result.data
                isLoading = false
            }
            is Result.Error -> {
                errorMessage = result.exception.message ?: "Failed to start quiz session."
                isLoading = false
            }
            is Result.Loading -> {
                isLoading = true
            }
        }
    }

    val currentQuestion: Question? = session?.questions?.getOrNull(currentQuestionIndex)
    val totalQuestions = session?.questions?.size ?: 0

    // Advance to next question or complete quiz
    fun advanceToNextQuestion(selectedOption: Int) {
        if (isSubmitting) return
        val currentQ = currentQuestion ?: return

        val timeSpent = (questionTimeLimit - remainingSeconds).coerceAtLeast(1).toDouble()
        recordedAnswers.add(
            PlayerAnswer(
                questionId = currentQ.id,
                selectedOption = selectedOption,
                timeSpentSeconds = timeSpent
            )
        )

        if (currentQuestionIndex < totalQuestions - 1) {
            currentQuestionIndex++
            selectedOptionIndex = null
            isQuestionLocked = false
            remainingSeconds = questionTimeLimit
            disabledOptions = emptySet()
        } else {
            // Final Question - submit answers to server-authoritative scoring engine
            isSubmitting = true
            coroutineScope.launch {
                val currentSession = session
                val sessionId = currentSession?.id ?: "sess_${UUID.randomUUID().toString().take(8)}"
                val sessionToken = currentSession?.sessionToken ?: ""
                val submission = QuizSubmission(
                    sessionId = sessionId,
                    sessionToken = sessionToken,
                    answers = recordedAnswers.toList(),
                    idempotencyKey = UUID.randomUUID().toString()
                )
                when (val submitResult = quizRepository.submitQuizAnswers(submission)) {
                    is Result.Success -> {
                        isSubmitting = false
                        onSubmitQuiz(sessionId)
                    }
                    is Result.Error -> {
                        isSubmitting = false
                        submitErrorMessage = submitResult.exception.message ?: "Server rejected answer submission."
                        showSubmitErrorDialog = true
                    }
                    is Result.Loading -> {
                        isSubmitting = true
                    }
                }
            }
        }
    }

    // Timer countdown effect for active question
    LaunchedEffect(currentQuestionIndex, isQuestionLocked, isLoading) {
        if (!isLoading && !isQuestionLocked && session != null) {
            remainingSeconds = questionTimeLimit
            while (remainingSeconds > 0 && !isQuestionLocked) {
                delay(1000)
                remainingSeconds--
            }
            if (remainingSeconds == 0 && !isQuestionLocked) {
                // Time Expired
                isQuestionLocked = true
                delay(400)
                advanceToNextQuestion(selectedOption = -1) // -1 denotes unanswered/timeout
            }
        }
    }

    // Handle user selecting an option with haptic feedback
    val handleSelectOption: (Int) -> Unit = { optionIdx ->
        if (!isQuestionLocked && optionIdx !in disabledOptions) {
            isQuestionLocked = true
            selectedOptionIndex = optionIdx
            try {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {
            }
            coroutineScope.launch {
                delay(800) // Brief delay to show chosen option highlight
                advanceToNextQuestion(selectedOption = optionIdx)
            }
        }
    }

    // Handle 50:50 Lifeline with haptic feedback
    val handleUseFiftyFifty: () -> Unit = {
        if (!isFiftyFiftyUsed && !isQuestionLocked) {
            isFiftyFiftyUsed = true
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {
            }
            // Disable two options (e.g. index 2 and 3 if available, or two arbitrary non-selected options)
            disabledOptions = setOf(2, 3)
        }
    }

    // Handle Skip Lifeline with haptic feedback
    val handleUseSkip: () -> Unit = {
        if (!isSkipUsed && !isQuestionLocked) {
            isSkipUsed = true
            isQuestionLocked = true
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {
            }
            coroutineScope.launch {
                advanceToNextQuestion(selectedOption = -1)
            }
        }
    }

    if (isLoading) {
        LoadingStateView(message = "Preparing quiz questions...")
        return
    }

    if (errorMessage != null) {
        ErrorStateView(
            message = errorMessage ?: "Error loading quiz.",
            onRetry = onForfeit
        )
        return
    }

    if (isSubmitting) {
        LoadingStateView(message = "Evaluating answers server-side...")
        return
    }

    val overallProgress = remember(currentQuestionIndex, totalQuestions) {
        if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat() else 0f
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showForfeitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quit Quiz",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricIndigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = categoryId.replace("cat_", "").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Question Counter
                    Text(
                        text = "Q ${currentQuestionIndex + 1}/$totalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Overall progress bar
                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = ElectricIndigo,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Isolated Animated Circular Countdown Timer (prevents parent screen recomposition)
                QuizCountdownTimer(
                    remainingSeconds = remainingSeconds,
                    questionTimeLimit = questionTimeLimit
                )

                Spacer(modifier = Modifier.height(spacing.md))

                // Question Card
                if (currentQuestion != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentQuestion.text,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.lg))

                    // 4 Option Cards
                    val optionLetters = listOf("A", "B", "C", "D")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        currentQuestion.options.forEachIndexed { index, optionText ->
                            val isSelected = selectedOptionIndex == index
                            val isDisabled = index in disabledOptions

                            OptionCard(
                                letter = optionLetters.getOrElse(index) { "?" },
                                text = optionText,
                                isSelected = isSelected,
                                isDisabled = isDisabled,
                                onClick = { handleSelectOption(index) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            // Lifelines Row (Accessible touch targets >= 48dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 50:50 Lifeline Button
                OutlinedButton(
                    onClick = handleUseFiftyFifty,
                    enabled = !isFiftyFiftyUsed && !isQuestionLocked,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "50:50 Lifeline",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isFiftyFiftyUsed) "50:50 (Used)" else "50:50",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Skip Lifeline Button
                OutlinedButton(
                    onClick = handleUseSkip,
                    enabled = !isSkipUsed && !isQuestionLocked,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Question Lifeline",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isSkipUsed) "Skip (Used)" else "Skip",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }

    // Forfeit Confirmation Dialog
    if (showForfeitDialog) {
        AlertDialog(
            onDismissRequest = { showForfeitDialog = false },
            title = {
                Text(
                    text = "Quit Quiz?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to forfeit this session? Your current quiz progress and any earned points will be lost.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForfeitDialog = false
                        onForfeit()
                    }
                ) {
                    Text(
                        text = "Forfeit & Quit",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showForfeitDialog = false }) {
                    Text("Resume Quiz")
                }
            }
        )
    }

    // Server Submission Error Dialog (with retry or forfeit options)
    if (showSubmitErrorDialog) {
        AlertDialog(
            onDismissRequest = { /* Modal: require user action */ },
            title = {
                Text(
                    text = "Submission Issue",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = submitErrorMessage ?: "Unable to submit answers to the scoring engine. Please retry or exit.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmitErrorDialog = false
                        isSubmitting = true
                        coroutineScope.launch {
                            val currentSession = session
                            val sessionId = currentSession?.id ?: "sess_${UUID.randomUUID().toString().take(8)}"
                            val sessionToken = currentSession?.sessionToken ?: ""
                            val retrySubmission = QuizSubmission(
                                sessionId = sessionId,
                                sessionToken = sessionToken,
                                answers = recordedAnswers.toList(),
                                idempotencyKey = UUID.randomUUID().toString()
                            )
                            when (val submitResult = quizRepository.submitQuizAnswers(retrySubmission)) {
                                is Result.Success -> {
                                    isSubmitting = false
                                    onSubmitQuiz(sessionId)
                                }
                                is Result.Error -> {
                                    isSubmitting = false
                                    submitErrorMessage = submitResult.exception.message ?: "Submission failed again."
                                    showSubmitErrorDialog = true
                                }
                                is Result.Loading -> {
                                    isSubmitting = true
                                }
                            }
                        }
                    }
                ) {
                    Text("Retry Submission", fontWeight = FontWeight.SemiBold, color = ElectricIndigo)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSubmitErrorDialog = false
                        onForfeit()
                    }
                ) {
                    Text("Forfeit & Exit", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
private fun OptionCard(
    letter: String,
    text: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> ElectricIndigo
            isDisabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "optionBorder"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> ElectricIndigo.copy(alpha = 0.12f)
            isDisabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "optionBg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .alpha(if (isDisabled) 0.35f else 1.0f)
            .clickable(enabled = !isDisabled, onClick = onClick),
        shape = OptionShape,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        tonalElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            // Letter Emblem (A, B, C, D)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) ElectricIndigo else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Isolated Countdown Timer Composable.
 * Isolates second-by-second animated state changes to prevent recomposing
 * the question card, options, and scaffold during active countdowns.
 */
@Composable
private fun QuizCountdownTimer(
    remainingSeconds: Int,
    questionTimeLimit: Int,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val timerColor by animateColorAsState(
        targetValue = when {
            remainingSeconds > 5 -> MintTeal
            remainingSeconds in 3..5 -> WarningAmber
            else -> ErrorCrimson
        },
        animationSpec = tween(durationMillis = 300),
        label = "timerColor"
    )

    val timerProgress by animateFloatAsState(
        targetValue = (remainingSeconds.toFloat() / questionTimeLimit.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "timerProgress"
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .padding(spacing.xs),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { timerProgress },
            modifier = Modifier.fillMaxSize(),
            color = timerColor,
            strokeWidth = 6.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "${remainingSeconds}s",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = timerColor
        )
    }
}
