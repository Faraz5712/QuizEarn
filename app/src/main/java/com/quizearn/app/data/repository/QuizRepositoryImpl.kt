package com.quizearn.app.data.repository

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.local.room.QuizEarnDatabase
import com.quizearn.app.data.local.room.entity.QuizHistoryEntity
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.data.remote.FirebaseQuizRemoteDataSource
import com.quizearn.app.data.remote.MockQuizRemoteDataSource
import com.quizearn.app.data.remote.QuizRemoteDataSource
import com.quizearn.app.domain.model.Category
import com.quizearn.app.domain.model.QuizSession
import com.quizearn.app.domain.model.QuizSubmission
import com.quizearn.app.ui.screens.home.CategoryIconKey
import com.quizearn.app.ui.screens.home.DailyChallengeProgress
import com.quizearn.app.ui.screens.home.FeaturedQuiz
import com.quizearn.app.ui.screens.home.LiveTournamentPreview
import com.quizearn.app.ui.screens.home.QuizCategoryDisplay
import com.quizearn.app.ui.screens.quiz.EvaluatedQuizResult
import com.quizearn.app.ui.screens.quiz.QuizHistoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Repository implementation managing quiz categories, sessions, and dashboard content.
 * Delegates quiz creation, time-bounded expiration, and authoritative grading to [QuizRemoteDataSource].
 */
class QuizRepositoryImpl(
    private val remoteDataSource: QuizRemoteDataSource = MockQuizRemoteDataSource.getInstance(),
    val database: QuizEarnDatabase? = null
) : QuizRepository {

    private val defaultCategories = listOf(
        Category(
            id = "cat_general",
            name = "General Knowledge",
            slug = "general-knowledge",
            colorHex = "#5B5FEF",
            displayOrder = 1,
            totalQuestions = 150
        ),
        Category(
            id = "cat_science",
            name = "Science & Nature",
            slug = "science-nature",
            colorHex = "#2DD4BF",
            displayOrder = 2,
            totalQuestions = 120
        ),
        Category(
            id = "cat_tech",
            name = "Technology & Code",
            slug = "technology-code",
            colorHex = "#8B5CF6",
            displayOrder = 3,
            totalQuestions = 95
        ),
        Category(
            id = "cat_history",
            name = "History & Geo",
            slug = "history-geography",
            colorHex = "#F59E0B",
            displayOrder = 4,
            totalQuestions = 110
        ),
        Category(
            id = "cat_sports",
            name = "Sports & Fitness",
            slug = "sports-fitness",
            colorHex = "#10B981",
            displayOrder = 5,
            totalQuestions = 80
        ),
        Category(
            id = "cat_arts",
            name = "Arts & Culture",
            slug = "arts-culture",
            colorHex = "#EC4899",
            displayOrder = 6,
            totalQuestions = 90
        )
    )

    override fun getCategories(): Flow<Result<List<Category>>> = flow {
        emit(Result.Loading)
        delay(150)
        emit(Result.Success(defaultCategories))
    }

    fun getCategoryDisplays(): List<QuizCategoryDisplay> {
        return listOf(
            QuizCategoryDisplay(
                id = "cat_general",
                name = "General Knowledge",
                slug = "general-knowledge",
                totalQuestions = 150,
                difficulty = "Casual",
                accentColorHex = "#5B5FEF",
                iconKey = CategoryIconKey.GENERAL
            ),
            QuizCategoryDisplay(
                id = "cat_science",
                name = "Science & Nature",
                slug = "science-nature",
                totalQuestions = 120,
                difficulty = "Challenger",
                accentColorHex = "#2DD4BF",
                iconKey = CategoryIconKey.SCIENCE
            ),
            QuizCategoryDisplay(
                id = "cat_tech",
                name = "Technology & Code",
                slug = "technology-code",
                totalQuestions = 95,
                difficulty = "Master",
                accentColorHex = "#8B5CF6",
                iconKey = CategoryIconKey.TECH
            ),
            QuizCategoryDisplay(
                id = "cat_history",
                name = "History & Geo",
                slug = "history-geography",
                totalQuestions = 110,
                difficulty = "Challenger",
                accentColorHex = "#F59E0B",
                iconKey = CategoryIconKey.HISTORY
            ),
            QuizCategoryDisplay(
                id = "cat_sports",
                name = "Sports & Fitness",
                slug = "sports-fitness",
                totalQuestions = 80,
                difficulty = "Casual",
                accentColorHex = "#10B981",
                iconKey = CategoryIconKey.SPORTS
            ),
            QuizCategoryDisplay(
                id = "cat_arts",
                name = "Arts & Culture",
                slug = "arts-culture",
                totalQuestions = 90,
                difficulty = "Casual",
                accentColorHex = "#EC4899",
                iconKey = CategoryIconKey.ARTS
            )
        )
    }

    fun getFeaturedQuizzes(): List<FeaturedQuiz> {
        return listOf(
            FeaturedQuiz(
                id = "feat_01",
                title = "Cosmic Universe Marathon",
                subtitle = "Explore black holes, supernovas, and exoplanets.",
                categoryId = "cat_science",
                rewardPoints = 75L,
                questionCount = 10,
                durationMinutes = 3,
                tag = "DOUBLE REWARDS",
                accentColorHex = "#2DD4BF"
            ),
            FeaturedQuiz(
                id = "feat_02",
                title = "Ancient Empires & Relics",
                subtitle = "From Roman colosseums to Mayan pyramids.",
                categoryId = "cat_history",
                rewardPoints = 50L,
                questionCount = 8,
                durationMinutes = 2,
                tag = "DAILY SPECIAL",
                accentColorHex = "#F59E0B"
            ),
            FeaturedQuiz(
                id = "feat_03",
                title = "Silicon Valley Innovators",
                subtitle = "Test your knowledge of groundbreaking tech giants.",
                categoryId = "cat_tech",
                rewardPoints = 60L,
                questionCount = 8,
                durationMinutes = 2,
                tag = "HOT EVENT",
                accentColorHex = "#8B5CF6"
            )
        )
    }

    fun getActiveTournament(): LiveTournamentPreview {
        return LiveTournamentPreview(
            id = "tourn_weekly_01",
            title = "Weekend Grand Championship",
            prizePool = 5000L,
            remainingTimeFormatted = "03h 45m",
            activeContenders = 312
        )
    }

    fun getDailyChallenge(): DailyChallengeProgress {
        return DailyChallengeProgress(
            id = "chal_daily_01",
            title = "Daily Knowledge Quest",
            description = "Complete 2 quizzes across any category today",
            currentProgress = 1,
            targetGoal = 2,
            rewardBonus = 50L
        )
    }

    override suspend fun startQuizSession(categoryId: String, idempotencyKey: String): Result<QuizSession> {
        return remoteDataSource.createQuizSession("usr_current", categoryId)
    }

    override suspend fun submitQuizAnswers(submission: QuizSubmission): Result<EvaluatedQuizResult> {
        val result = remoteDataSource.submitQuizAnswers(submission)
        if (result is Result.Success && database != null) {
                val historyUserId = if (FirebaseBackend.isConfigured) {
                    FirebaseBackend.auth.currentUser?.uid
                        ?: return Result.Error(IllegalStateException("Firebase session ended before quiz history was saved."))
                } else {
                    "user_001"
                }
                val summary = result.data.summary
                val historyItem = QuizHistoryItem(
                    sessionId = summary.sessionId,
                    categoryTitle = summary.categoryTitle,
                    timestampFormatted = "Just now",
                    score = summary.score,
                    totalQuestions = summary.totalQuestions,
                    pointsEarned = summary.pointsEarned,
                    accuracyRate = summary.accuracyRate
                )
                database.quizHistoryDao().insertQuizHistory(
                    QuizHistoryEntity.fromDomain(historyUserId, historyItem, "2026-10-29T12:55:00Z")
                )
        }
        return result
    }

    override suspend fun getQuizResult(sessionId: String): Result<EvaluatedQuizResult> {
        return remoteDataSource.getEvaluatedResult(sessionId)
    }

    override fun getQuizHistory(userId: String): Flow<List<QuizHistoryItem>> {
        return if (database != null) {
            database.quizHistoryDao().getQuizHistoryFlow(userId).map { list ->
                list.map { it.toDomain() }
            }
        } else {
            flowOf(emptyList())
        }
    }

    companion object {
        @Volatile
        private var instance: QuizRepositoryImpl? = null

        fun getInstance(database: QuizEarnDatabase? = null): QuizRepositoryImpl {
            if (FirebaseBackend.isConfigured) {
                return synchronized(this) {
                    val existing = instance
                    if (existing?.remoteDataSource is FirebaseQuizRemoteDataSource) {
                        existing
                    } else {
                        QuizRepositoryImpl(
                            remoteDataSource = FirebaseQuizRemoteDataSource(),
                            database = database ?: existing?.database
                        ).also { instance = it }
                    }
                }
            }
            val existing = instance
            if (existing != null) {
                if (database != null && existing.database == null) {
                    synchronized(this) {
                        if (instance?.database == null) {
                            instance = QuizRepositoryImpl(database = database)
                        }
                    }
                }
                return instance!!
            }
            return synchronized(this) {
                instance ?: QuizRepositoryImpl(database = database).also { instance = it }
            }
        }
    }
}
