package com.quizearn.app.data.repository

import com.quizearn.app.domain.model.AchievementBadge
import com.quizearn.app.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for managing tasks, challenges, and achievement badges.
 */
interface TaskRepository {
    fun getDailyTasks(): Flow<List<TaskItem>>
    fun getWeeklyTasks(): Flow<List<TaskItem>>
    fun getAchievements(): Flow<List<AchievementBadge>>

    suspend fun claimTaskReward(
        taskId: String,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Result<Long>

    suspend fun recordQuizProgression(
        category: String,
        score: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        streakDays: Int
    )

    suspend fun checkUtcMidnightRollover()
    suspend fun refreshTasks()
}
