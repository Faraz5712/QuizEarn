package com.quizearn.app.data.repository

import com.quizearn.app.core.result.exceptionOrNull
import com.quizearn.app.core.result.isFailure
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.local.room.QuizEarnDatabase
import com.quizearn.app.data.local.room.entity.TaskEntity
import com.quizearn.app.data.remote.MockTaskRemoteDataSource
import com.quizearn.app.data.remote.FirebaseTaskRemoteDataSource
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.data.remote.TaskRemoteDataSource
import com.quizearn.app.domain.model.AchievementBadge
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TransactionReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Production implementation of [TaskRepository] coordinating task progression,
 * UTC midnight scheduling, and atomic double-entry ledger reward claims.
 */
class TaskRepositoryImpl(
    private val remoteDataSource: TaskRemoteDataSource = MockTaskRemoteDataSource(),
    private val walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
    val database: QuizEarnDatabase? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : TaskRepository {

    private val _dailyTasks = MutableStateFlow<List<TaskItem>>(emptyList())
    private val _weeklyTasks = MutableStateFlow<List<TaskItem>>(emptyList())
    private val _achievements = MutableStateFlow<List<AchievementBadge>>(emptyList())

    init {
        coroutineScope.launch {
            refreshTasks()
        }
    }

    private suspend fun getActiveUserId(): String {
        if (remoteDataSource is FirebaseTaskRemoteDataSource) {
            return FirebaseBackend.auth.currentUser?.uid
                ?: throw IllegalStateException("Sign-in is required to load Firebase task progress.")
        }
        return sessionManager.currentSession.firstOrNull()?.userId ?: "mock_user_123"
    }

    override fun getDailyTasks(): Flow<List<TaskItem>> = _dailyTasks.asStateFlow()

    override fun getWeeklyTasks(): Flow<List<TaskItem>> = _weeklyTasks.asStateFlow()

    override fun getAchievements(): Flow<List<AchievementBadge>> = _achievements.asStateFlow()

    override suspend fun refreshTasks() {
        val userId = getActiveUserId()
        remoteDataSource.checkUtcMidnightRollover(userId)
        val remoteDaily = remoteDataSource.getDailyTasks(userId)
        val remoteWeekly = remoteDataSource.getWeeklyTasks(userId)
        val persistedTasks = database?.taskDao()?.getAllTasks(userId)
            ?.filter { it.periodKey == TaskEntity.currentPeriodKey(it.type) }
            ?.associateBy { it.id }
            .orEmpty()
        fun restoreProgress(task: TaskItem): TaskItem {
            if (remoteDataSource is FirebaseTaskRemoteDataSource) return task
            val persisted = persistedTasks[task.id]?.toDomain() ?: return task
            return task.copy(
                currentProgress = maxOf(task.currentProgress, persisted.currentProgress).coerceAtMost(task.targetGoal),
                isClaimed = task.isClaimed || persisted.isClaimed,
                claimedAtTimestamp = task.claimedAtTimestamp ?: persisted.claimedAtTimestamp
            )
        }
        val daily = remoteDaily.map(::restoreProgress)
        val weekly = remoteWeekly.map(::restoreProgress)
        val achs = remoteDataSource.getAchievements(userId)

        if (database != null) {
            val entities = (daily + weekly).map { TaskEntity.fromDomain(userId, it) }
            database.taskDao().insertOrUpdateTasks(entities)
        }
        _dailyTasks.value = daily
        _weeklyTasks.value = weekly
        _achievements.value = achs
    }

    override suspend fun claimTaskReward(taskId: String, idempotencyKey: String): Result<Long> {
        val userId = getActiveUserId()

        if (remoteDataSource !is FirebaseTaskRemoteDataSource &&
            database?.taskDao()?.getTaskById(taskId, userId)?.isClaimed == true) {
            return Result.failure(IllegalStateException("Task '$taskId' has already been claimed."))
        }

        // 1. Authoritatively claim task on server/remote data source
        val claimResult = remoteDataSource.claimTaskReward(userId, taskId, idempotencyKey)
        if (claimResult.isFailure) {
            return claimResult
        }
        val rewardPoints = claimResult.getOrThrow()

        if (remoteDataSource is FirebaseTaskRemoteDataSource) {
            refreshTasks()
            return Result.success(rewardPoints)
        }

        // 2. Authoritatively credit user's double-entry wallet ledger
        val creditResult = walletRepository.credit(
            userId = userId,
            amount = rewardPoints,
            reason = TransactionReason.TASK_COMPLETED,
            description = "Task Claim: $taskId",
            referenceId = taskId,
            idempotencyKey = idempotencyKey
        )

        if (creditResult.isFailure) {
            return Result.failure(creditResult.exceptionOrNull() ?: Exception("Failed to credit wallet."))
        }

        // 3. Refresh in-memory task states
        refreshTasks()
        return Result.success(rewardPoints)
    }

    override suspend fun recordQuizProgression(
        category: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        streakDays: Int
    ) {
        val userId = getActiveUserId()
        remoteDataSource.recordQuizProgression(
            userId = userId,
            category = category,
            score = score,
            totalQuestions = totalQuestions,
            durationSeconds = durationSeconds,
            streakDays = streakDays
        )
        refreshTasks()
    }

    override suspend fun checkUtcMidnightRollover() {
        val userId = getActiveUserId()
        remoteDataSource.checkUtcMidnightRollover(userId)
        refreshTasks()
    }

    companion object {
        @Volatile
        private var instance: TaskRepositoryImpl? = null

        fun getInstance(
            remoteDataSource: TaskRemoteDataSource = MockTaskRemoteDataSource(),
            walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
            sessionManager: AuthSessionManager = AuthSessionManager.getInstance(),
            database: QuizEarnDatabase? = null
        ): TaskRepositoryImpl {
            val existing = instance
            if (existing != null) {
                if (database != null && existing.database == null) {
                    synchronized(this) {
                        if (instance?.database == null) {
                            instance = TaskRepositoryImpl(
                                remoteDataSource = remoteDataSource,
                                walletRepository = walletRepository,
                                sessionManager = sessionManager,
                                database = database
                            )
                        }
                    }
                }
                return instance!!
            }
            return synchronized(this) {
                instance ?: TaskRepositoryImpl(
                    remoteDataSource = remoteDataSource,
                    walletRepository = walletRepository,
                    sessionManager = sessionManager,
                    database = database
                ).also { instance = it }
            }
        }
    }
}
