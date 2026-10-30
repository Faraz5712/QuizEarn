package com.quizearn.app.data.remote

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.Question
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSessionStatus
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.ui.screens.quiz.EvaluatedQuizResult
import com.quizearn.app.ui.screens.quiz.PerformanceTier
import com.quizearn.app.ui.screens.quiz.QuestionReviewItem
import com.quizearn.app.ui.screens.quiz.QuizResultSummary
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Secret server-side question definition.
 * The correct option index and explanation are held strictly on the server
 * and are NEVER revealed to the client prior to authoritative grading.
 */
data class ServerSecretQuestion(
    val id: String,
    val categoryId: String,
    val categoryTitle: String,
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanationText: String,
    val timeLimitSeconds: Int = 15,
    val pointsReward: Int = 10
)

/**
 * Internal server record tracking an active quiz session, its secret answer keys,
 * and its strict expiration deadline.
 */
data class ServerSessionRecord(
    val session: QuizSession,
    val secretQuestions: List<ServerSecretQuestion>,
    val startedAtTimestamp: Long,
    val expiresAtTimestamp: Long,
    val sessionToken: String,
    var status: QuizSessionStatus = QuizSessionStatus.ACTIVE
)

/**
 * Mock implementation of [QuizRemoteDataSource] simulating Cloud Functions authoritative execution.
 * Enforces:
 * 1. Secret answer keys stripped from client payloads.
 * 2. Absolute server-side deadline checks (submitting late triggers expiration rejection).
 * 3. Anti-replay idempotency key verification.
 * 4. Authoritative grading and atomic double-entry wallet points crediting.
 */
class MockQuizRemoteDataSource(
    private val walletRepository: WalletRepository = WalletRepositoryImpl.getMockInstance()
) : QuizRemoteDataSource {

    // Server-side secret question bank (never transmitted in raw form to the client)
    private val serverQuestionBank = mapOf(
        "cat_science" to listOf(
            ServerSecretQuestion(
                id = "q_sci_01",
                categoryId = "cat_science",
                categoryTitle = "Science & Nature",
                text = "What is the most abundant chemical element in the observable universe?",
                options = listOf("Helium", "Hydrogen", "Oxygen", "Nitrogen"),
                correctOptionIndex = 1,
                explanationText = "Hydrogen accounts for approximately 75% of the baryonic mass of the universe."
            ),
            ServerSecretQuestion(
                id = "q_sci_02",
                categoryId = "cat_science",
                categoryTitle = "Science & Nature",
                text = "Which cellular organelle is known as the powerhouse of the cell?",
                options = listOf("Ribosome", "Nucleus", "Mitochondria", "Golgi apparatus"),
                correctOptionIndex = 2,
                explanationText = "Mitochondria generate most of the cell's supply of adenosine triphosphate (ATP) used as chemical energy."
            ),
            ServerSecretQuestion(
                id = "q_sci_03",
                categoryId = "cat_science",
                categoryTitle = "Science & Nature",
                text = "What is the hardest naturally occurring substance on Earth?",
                options = listOf("Diamond", "Corundum", "Quartz", "Topaz"),
                correctOptionIndex = 0,
                explanationText = "Diamond is the hardest known natural mineral, rated 10 on the Mohs hardness scale."
            ),
            ServerSecretQuestion(
                id = "q_sci_04",
                categoryId = "cat_science",
                categoryTitle = "Science & Nature",
                text = "Which planet has the greatest number of confirmed moons in our solar system?",
                options = listOf("Mars", "Uranus", "Neptune", "Saturn"),
                correctOptionIndex = 3,
                explanationText = "Saturn has over 140 confirmed moons, surpassing Jupiter."
            ),
            ServerSecretQuestion(
                id = "q_sci_05",
                categoryId = "cat_science",
                categoryTitle = "Science & Nature",
                text = "In the famous equation E = mc², what does 'c' represent?",
                options = listOf("Celsius", "Speed of light in vacuum", "Specific heat capacity", "Coulomb"),
                correctOptionIndex = 1,
                explanationText = "In Einstein's mass-energy equivalence, 'c' is the universal physical constant representing the speed of light (~300,000 km/s)."
            )
        ),
        "cat_tech" to listOf(
            ServerSecretQuestion(
                id = "q_tech_01",
                categoryId = "cat_tech",
                categoryTitle = "Technology & Code",
                text = "Which programming language was developed by JetBrains and is the preferred language for Android development?",
                options = listOf("Java", "Kotlin", "Swift", "Dart"),
                correctOptionIndex = 1,
                explanationText = "Google announced Kotlin as the preferred language for Android app developers at Google I/O 2019."
            ),
            ServerSecretQuestion(
                id = "q_tech_02",
                categoryId = "cat_tech",
                categoryTitle = "Technology & Code",
                text = "What does 'API' stand for in software engineering?",
                options = listOf("Application Programming Interface", "Automated Program Integration", "Advanced Processing Instruction", "Applied Peripheral Interconnect"),
                correctOptionIndex = 0,
                explanationText = "API stands for Application Programming Interface, allowing different software programs to communicate."
            ),
            ServerSecretQuestion(
                id = "q_tech_03",
                categoryId = "cat_tech",
                categoryTitle = "Technology & Code",
                text = "In computer memory hierarchy, which storage type provides the lowest latency?",
                options = listOf("L1 CPU Cache", "DDR5 RAM", "NVMe SSD", "Virtual Swap Disk"),
                correctOptionIndex = 0,
                explanationText = "L1 CPU cache resides directly inside the processor core with sub-nanosecond access latency."
            ),
            ServerSecretQuestion(
                id = "q_tech_04",
                categoryId = "cat_tech",
                categoryTitle = "Technology & Code",
                text = "Which data structure operates on a First-In, First-Out (FIFO) principle?",
                options = listOf("Stack", "Queue", "Binary Tree", "Hash Map"),
                correctOptionIndex = 1,
                explanationText = "A queue processes items in the exact order they arrive: first in, first out."
            ),
            ServerSecretQuestion(
                id = "q_tech_05",
                categoryId = "cat_tech",
                categoryTitle = "Technology & Code",
                text = "What is the fundamental port used by default for secure HTTPS web traffic?",
                options = listOf("80", "8080", "443", "22"),
                correctOptionIndex = 2,
                explanationText = "Port 443 is the standard TCP port reserved for encrypted HTTPS communication."
            )
        ),
        "cat_general" to listOf(
            ServerSecretQuestion(
                id = "q_gen_01",
                categoryId = "cat_general",
                categoryTitle = "General Knowledge",
                text = "Which is the largest ocean on Earth by surface area?",
                options = listOf("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Arctic Ocean"),
                correctOptionIndex = 2,
                explanationText = "The Pacific Ocean covers more than 30% of the Earth's surface, larger than all landmasses combined."
            ),
            ServerSecretQuestion(
                id = "q_gen_02",
                categoryId = "cat_general",
                categoryTitle = "General Knowledge",
                text = "In what year did the Apollo 11 mission land the first humans on the Moon?",
                options = listOf("1965", "1969", "1972", "1975"),
                correctOptionIndex = 1,
                explanationText = "Neil Armstrong and Buzz Aldrin landed the Apollo 11 Lunar Module Eagle on the Moon on July 20, 1969."
            ),
            ServerSecretQuestion(
                id = "q_gen_03",
                categoryId = "cat_general",
                categoryTitle = "General Knowledge",
                text = "What is the capital city of Australia?",
                options = listOf("Sydney", "Melbourne", "Canberra", "Brisbane"),
                correctOptionIndex = 2,
                explanationText = "Canberra was chosen as the capital city of Australia in 1908 as a compromise between Sydney and Melbourne."
            ),
            ServerSecretQuestion(
                id = "q_gen_04",
                categoryId = "cat_general",
                categoryTitle = "General Knowledge",
                text = "How many degrees are in the sum of the interior angles of a Euclidean triangle?",
                options = listOf("90°", "180°", "270°", "360°"),
                correctOptionIndex = 1,
                explanationText = "The sum of the three interior angles of any triangle on a flat plane always equals 180 degrees."
            ),
            ServerSecretQuestion(
                id = "q_gen_05",
                categoryId = "cat_general",
                categoryTitle = "General Knowledge",
                text = "Which famous artist painted the ceiling of the Sistine Chapel?",
                options = listOf("Leonardo da Vinci", "Michelangelo", "Raphael", "Donatello"),
                correctOptionIndex = 1,
                explanationText = "Michelangelo painted the Sistine Chapel ceiling in the Vatican between 1508 and 1512."
            )
        ),
        "cat_history" to listOf(
            ServerSecretQuestion(
                id = "q_hist_01",
                categoryId = "cat_history",
                categoryTitle = "History & Geo",
                text = "Which ancient civilization constructed the iconic pyramids at Giza?",
                options = listOf("Mesopotamians", "Ancient Egyptians", "Babylonians", "Persians"),
                correctOptionIndex = 1,
                explanationText = "The Giza pyramid complex was constructed during Egypt's Fourth Dynasty in the 26th century BC."
            ),
            ServerSecretQuestion(
                id = "q_hist_02",
                categoryId = "cat_history",
                categoryTitle = "History & Geo",
                text = "What famous trade route historically linked China to the Mediterranean world?",
                options = listOf("The Amber Road", "The Silk Road", "The Spice Route", "The Royal Road"),
                correctOptionIndex = 1,
                explanationText = "The Silk Road was a network of Eurasian trade routes active from the Han dynasty until the Ottoman boycott."
            ),
            ServerSecretQuestion(
                id = "q_hist_03",
                categoryId = "cat_history",
                categoryTitle = "History & Geo",
                text = "In which modern-day country was the ancient city-state of Sparta located?",
                options = listOf("Italy", "Greece", "Turkey", "Egypt"),
                correctOptionIndex = 1,
                explanationText = "Sparta was a prominent city-state in Laconia in ancient Greece."
            ),
            ServerSecretQuestion(
                id = "q_hist_04",
                categoryId = "cat_history",
                categoryTitle = "History & Geo",
                text = "The Magna Carta was signed by King John of England in which year?",
                options = listOf("1066", "1215", "1492", "1776"),
                correctOptionIndex = 1,
                explanationText = "The Magna Carta was agreed to by King John at Runnymede near Windsor on June 15, 1215."
            ),
            ServerSecretQuestion(
                id = "q_hist_05",
                categoryId = "cat_history",
                categoryTitle = "History & Geo",
                text = "Which mountain range naturally divides Europe from Asia?",
                options = listOf("The Alps", "The Andes", "The Ural Mountains", "The Himalayas"),
                correctOptionIndex = 2,
                explanationText = "The Ural Mountains in western Russia form the conventional boundary between Europe and Asia."
            )
        ),
        "cat_sports" to listOf(
            ServerSecretQuestion(
                id = "q_sp_01",
                categoryId = "cat_sports",
                categoryTitle = "Sports & Fitness",
                text = "How many players are on the field for one team during a regulation soccer match?",
                options = listOf("9", "10", "11", "12"),
                correctOptionIndex = 2,
                explanationText = "A standard soccer match is played between two teams of 11 players each, including the goalkeeper."
            ),
            ServerSecretQuestion(
                id = "q_sp_02",
                categoryId = "cat_sports",
                categoryTitle = "Sports & Fitness",
                text = "In tennis, what term is used to describe a score of zero points?",
                options = listOf("Fault", "Deuce", "Love", "Blank"),
                correctOptionIndex = 2,
                explanationText = "In tennis scoring, 'love' indicates a score of zero, likely derived from the French word 'l'oeuf' (egg)."
            ),
            ServerSecretQuestion(
                id = "q_sp_03",
                categoryId = "cat_sports",
                categoryTitle = "Sports & Fitness",
                text = "How long is a standard Olympic marathon race?",
                options = listOf("21.1 km", "42.195 km", "50.0 km", "10.0 km"),
                correctOptionIndex = 1,
                explanationText = "An official marathon is 42.195 kilometers (26 miles 385 yards), standardized at the 1908 London Olympics."
            ),
            ServerSecretQuestion(
                id = "q_sp_04",
                categoryId = "cat_sports",
                categoryTitle = "Sports & Fitness",
                text = "Which country has won the most FIFA Men's World Cup titles?",
                options = listOf("Germany", "Italy", "Argentina", "Brazil"),
                correctOptionIndex = 3,
                explanationText = "Brazil has won five FIFA World Cup tournaments (1958, 1962, 1970, 1994, and 2002)."
            ),
            ServerSecretQuestion(
                id = "q_sp_05",
                categoryId = "cat_sports",
                categoryTitle = "Sports & Fitness",
                text = "In basketball, how many seconds does an NBA team have to attempt a shot that hits the rim?",
                options = listOf("20 seconds", "24 seconds", "30 seconds", "35 seconds"),
                correctOptionIndex = 1,
                explanationText = "The 24-second shot clock was introduced in 1954 to maintain the pace and excitement of NBA games."
            )
        ),
        "cat_arts" to listOf(
            ServerSecretQuestion(
                id = "q_art_01",
                categoryId = "cat_arts",
                categoryTitle = "Arts & Culture",
                text = "Who authored the famous tragedy play 'Romeo and Juliet'?",
                options = listOf("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"),
                correctOptionIndex = 1,
                explanationText = "William Shakespeare wrote Romeo and Juliet early in his career, published in 1597."
            ),
            ServerSecretQuestion(
                id = "q_art_02",
                categoryId = "cat_arts",
                categoryTitle = "Arts & Culture",
                text = "In music theory, how many semitones make up a complete octave?",
                options = listOf("8", "10", "12", "16"),
                correctOptionIndex = 2,
                explanationText = "An octave consists of 12 chromatic semitones (half steps)."
            ),
            ServerSecretQuestion(
                id = "q_art_03",
                categoryId = "cat_arts",
                categoryTitle = "Arts & Culture",
                text = "Which art movement was pioneered by Pablo Picasso and Georges Braque?",
                options = listOf("Surrealism", "Cubism", "Impressionism", "Baroque"),
                correctOptionIndex = 1,
                explanationText = "Cubism was an avant-garde art movement pioneered in the early 20th century by Picasso and Braque."
            ),
            ServerSecretQuestion(
                id = "q_art_04",
                categoryId = "cat_arts",
                categoryTitle = "Arts & Culture",
                text = "What is the primary material used in traditional origami?",
                options = listOf("Clay", "Paper", "Silk", "Wood"),
                correctOptionIndex = 1,
                explanationText = "Origami is the Japanese art of paper folding ('ori' fold, 'kami' paper)."
            ),
            ServerSecretQuestion(
                id = "q_art_05",
                categoryId = "cat_arts",
                categoryTitle = "Arts & Culture",
                text = "Which musical instrument has 88 keys in standard modern layout?",
                options = listOf("Harpsichord", "Accordion", "Grand Piano", "Pipe Organ"),
                correctOptionIndex = 2,
                explanationText = "A standard modern piano has 88 keys: 52 white keys (natural notes) and 36 black keys (accidentals)."
            )
        )
    )

    private val activeSessions = ConcurrentHashMap<String, ServerSessionRecord>()
    private val processedIdempotencyKeys = ConcurrentHashMap<String, EvaluatedQuizResult>()
    private val completedResults = ConcurrentHashMap<String, EvaluatedQuizResult>()

    override suspend fun createQuizSession(
        userId: String,
        categoryId: String
    ): Result<QuizSession> {
        delay(150) // Simulate Cloud Function roundtrip latency

        val secretList = serverQuestionBank[categoryId]
            ?: serverQuestionBank["cat_general"]
            ?: emptyList()

        if (secretList.isEmpty()) {
            return Result.Error(IllegalArgumentException("Category '$categoryId' has no active trivia questions."))
        }

        // Sanitize questions: strip secret answer keys and explanation text
        val sanitizedQuestions = secretList.map { secret ->
            Question(
                id = secret.id,
                categoryId = secret.categoryId,
                text = secret.text,
                options = secret.options,
                timeLimitSeconds = secret.timeLimitSeconds,
                pointsReward = secret.pointsReward
            )
        }

        val startedAt = System.currentTimeMillis()
        val durationSeconds = sanitizedQuestions.size * 15
        val gracePeriodMillis = 15_000L // 15-second grace period for network jitter
        val expiresAt = startedAt + (durationSeconds * 1000L) + gracePeriodMillis
        val sessionId = "session_${UUID.randomUUID().toString().take(8)}"
        val sessionToken = "token_jwt_${UUID.randomUUID().toString()}"

        val session = QuizSession(
            id = sessionId,
            userId = userId,
            categoryId = categoryId,
            questions = sanitizedQuestions,
            status = QuizSessionStatus.ACTIVE,
            score = 0,
            pointsAwarded = 0,
            timeLimitSeconds = durationSeconds,
            startedAtTimestamp = startedAt,
            expiresAtTimestamp = expiresAt,
            sessionToken = sessionToken
        )

        val record = ServerSessionRecord(
            session = session,
            secretQuestions = secretList,
            startedAtTimestamp = startedAt,
            expiresAtTimestamp = expiresAt,
            sessionToken = sessionToken,
            status = QuizSessionStatus.ACTIVE
        )

        activeSessions[sessionId] = record
        return Result.Success(session)
    }

    override suspend fun submitQuizAnswers(
        submission: QuizSubmission
    ): Result<EvaluatedQuizResult> {
        delay(250) // Simulate server scoring computation

        // 1. Anti-Replay Idempotency Verification
        processedIdempotencyKeys[submission.idempotencyKey]?.let { cached ->
            return Result.Success(cached)
        }

        // 2. Active Session Lookup
        val record = activeSessions[submission.sessionId]
            ?: return Result.Error(IllegalArgumentException("Quiz session '${submission.sessionId}' was not found or has already expired."))

        // 3. Cryptographic Token Verification
        if (record.sessionToken.isNotBlank() && submission.sessionToken.isNotBlank()) {
            if (record.sessionToken != submission.sessionToken) {
                return Result.Error(SecurityException("Session authorization token mismatch. Potential request tampering detected."))
            }
        }

        // 4. Status Check
        if (record.status == QuizSessionStatus.COMPLETED) {
            completedResults[submission.sessionId]?.let { return Result.Success(it) }
            return Result.Error(IllegalStateException("Quiz session has already been completed and scored."))
        }
        if (record.status == QuizSessionStatus.EXPIRED) {
            return Result.Error(IllegalStateException("Quiz session has expired."))
        }

        // 5. Expiration Deadline Enforcement
        val now = System.currentTimeMillis()
        if (now > record.expiresAtTimestamp) {
            record.status = QuizSessionStatus.EXPIRED
            return Result.Error(IllegalStateException("Submission rejected: Time limit exceeded. Submission arrived after deadline."))
        }

        // 6. Server-Authoritative Answer Evaluation
        var correctCount = 0
        var earnedScore = 0
        val reviewItems = mutableListOf<QuestionReviewItem>()
        val answerMap = submission.answers.associateBy { it.questionId }

        record.secretQuestions.forEachIndexed { index, secret ->
            val playerAnswer = answerMap[secret.id]
            val playerSelectedOption = playerAnswer?.selectedOption ?: -1
            val isCorrect = (playerSelectedOption == secret.correctOptionIndex)

            if (isCorrect) {
                correctCount++
                earnedScore += secret.pointsReward
            }

            reviewItems.add(
                QuestionReviewItem(
                    questionNumber = index + 1,
                    questionText = secret.text,
                    options = secret.options,
                    playerSelectedOptionIndex = playerSelectedOption,
                    correctOptionIndex = secret.correctOptionIndex,
                    isCorrect = isCorrect,
                    explanationText = secret.explanationText
                )
            )
        }

        // 7. Authoritative Metric Calculations
        val totalQuestions = record.secretQuestions.size
        val accuracyRate = if (totalQuestions > 0) (correctCount.toDouble() / totalQuestions) * 100.0 else 0.0
        val pointsEarned = correctCount * 10L
        val timeElapsedSeconds = ((now - record.startedAtTimestamp) / 1000).toInt().coerceAtLeast(1)
        val performanceTier = PerformanceTier.fromAccuracy(accuracyRate)

        // 8. Atomic Double-Entry Wallet Points Credit
        if (pointsEarned > 0L) {
            val categoryTitle = record.secretQuestions.firstOrNull()?.categoryTitle ?: "Trivia"
            val creditResult = walletRepository.credit(
                userId = record.session.userId,
                amount = pointsEarned,
                reason = TransactionReason.QUIZ_REWARD,
                referenceId = record.session.id,
                description = "Quiz Reward: $categoryTitle ($correctCount/$totalQuestions correct)",
                idempotencyKey = "quiz_reward_${record.session.id}"
            )
            if (creditResult is Result.Error) {
                return Result.Error(creditResult.exception)
            }
            if (!BackendConfig.firebaseConfigured) {
                try {
                    AuthSessionManager.getInstance().recordQuizCompletion(
                        pointsEarned = pointsEarned,
                        accuracyRate = accuracyRate
                    )
                } catch (_: Exception) {
                    // In non-context test environments, silently continue.
                }
            }
            try {
                com.quizearn.app.data.repository.TaskRepositoryImpl.getInstance().recordQuizProgression(
                    category = categoryTitle,
                    score = correctCount * 10,
                    totalQuestions = totalQuestions,
                    durationSeconds = timeElapsedSeconds,
                    streakDays = 3
                )
            } catch (_: Exception) {
                // In non-context test environments, silently continue
            }
        }

        // 9. Build Evaluated Result Summary
        val summary = QuizResultSummary(
            sessionId = submission.sessionId,
            categoryTitle = record.secretQuestions.firstOrNull()?.categoryTitle ?: "Trivia Quest",
            score = earnedScore,
            totalQuestions = totalQuestions,
            correctCount = correctCount,
            pointsEarned = pointsEarned,
            accuracyRate = accuracyRate,
            timeElapsedSeconds = timeElapsedSeconds,
            performanceTier = performanceTier
        )

        val evaluatedResult = EvaluatedQuizResult(
            summary = summary,
            reviews = reviewItems
        )

        // 10. Update State and Cache
        record.status = QuizSessionStatus.COMPLETED
        completedResults[submission.sessionId] = evaluatedResult
        processedIdempotencyKeys[submission.idempotencyKey] = evaluatedResult

        return Result.Success(evaluatedResult)
    }

    override suspend fun getEvaluatedResult(
        sessionId: String
    ): Result<EvaluatedQuizResult> {
        val cached = completedResults[sessionId]
        if (cached != null) {
            return Result.Success(cached)
        }

        // Synthetic fallback for preview/demo modes
        val fallbackSummary = QuizResultSummary(
            sessionId = sessionId,
            categoryTitle = "Science & Nature",
            score = 40,
            totalQuestions = 5,
            correctCount = 4,
            pointsEarned = 50L,
            accuracyRate = 80.0,
            timeElapsedSeconds = 48
        )
        val fallbackReviews = listOf(
            QuestionReviewItem(
                questionNumber = 1,
                questionText = "What is the most abundant chemical element in the observable universe?",
                options = listOf("Helium", "Hydrogen", "Oxygen", "Nitrogen"),
                playerSelectedOptionIndex = 1,
                correctOptionIndex = 1,
                isCorrect = true,
                explanationText = "Hydrogen accounts for approximately 75% of the baryonic mass of the universe."
            ),
            QuestionReviewItem(
                questionNumber = 2,
                questionText = "Which cellular organelle is known as the powerhouse of the cell?",
                options = listOf("Ribosome", "Nucleus", "Mitochondria", "Golgi apparatus"),
                playerSelectedOptionIndex = 2,
                correctOptionIndex = 2,
                isCorrect = true,
                explanationText = "Mitochondria generate most of the cell's supply of ATP."
            ),
            QuestionReviewItem(
                questionNumber = 3,
                questionText = "What is the hardest naturally occurring substance on Earth?",
                options = listOf("Diamond", "Corundum", "Quartz", "Topaz"),
                playerSelectedOptionIndex = 0,
                correctOptionIndex = 0,
                isCorrect = true,
                explanationText = "Diamond is rated 10 on the Mohs hardness scale."
            ),
            QuestionReviewItem(
                questionNumber = 4,
                questionText = "Which planet has the greatest number of confirmed moons in our solar system?",
                options = listOf("Mars", "Uranus", "Neptune", "Saturn"),
                playerSelectedOptionIndex = 3,
                correctOptionIndex = 3,
                isCorrect = true,
                explanationText = "Saturn has over 140 confirmed moons."
            ),
            QuestionReviewItem(
                questionNumber = 5,
                questionText = "In the famous equation E = mc², what does 'c' represent?",
                options = listOf("Celsius", "Speed of light in vacuum", "Specific heat capacity", "Coulomb"),
                playerSelectedOptionIndex = 0,
                correctOptionIndex = 1,
                isCorrect = false,
                explanationText = "In Einstein's mass-energy equivalence, 'c' represents the speed of light in vacuum."
            )
        )
        return Result.Success(EvaluatedQuizResult(fallbackSummary, fallbackReviews))
    }

    companion object {
        @Volatile
        private var instance: MockQuizRemoteDataSource? = null

        fun getInstance(): MockQuizRemoteDataSource {
            return instance ?: synchronized(this) {
                instance ?: MockQuizRemoteDataSource().also { instance = it }
            }
        }
    }
}
