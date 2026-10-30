package com.quizearn.app.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quizearn.app.data.local.room.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for daily and weekly player challenges.
 */
@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: TaskEntity)

    @Query("SELECT * FROM daily_tasks WHERE userId = :userId AND type = 'DAILY'")
    fun getDailyTasksFlow(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE userId = :userId AND type = 'WEEKLY'")
    fun getWeeklyTasksFlow(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE id = :taskId AND userId = :userId LIMIT 1")
    suspend fun getTaskById(taskId: String, userId: String): TaskEntity?

    @Query("SELECT * FROM daily_tasks WHERE userId = :userId")
    suspend fun getAllTasks(userId: String): List<TaskEntity>
}
