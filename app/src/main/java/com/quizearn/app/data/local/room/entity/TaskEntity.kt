package com.quizearn.app.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TaskType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Room entity representing a daily or weekly challenge task.
 */
@Entity(
    tableName = "daily_tasks",
    primaryKeys = ["id", "userId"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["id", "userId"], unique = true)
    ]
)
data class TaskEntity(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val rewardPoints: Long,
    val currentProgress: Int,
    val targetGoal: Int,
    val isClaimed: Boolean,
    val type: String, // "DAILY" or "WEEKLY"
    val categoryKey: String?,
    val claimedAtTimestamp: Long?,
    val periodKey: String
) {
    fun toDomain(): TaskItem = TaskItem(
        id = id,
        title = title,
        description = description,
        rewardPoints = rewardPoints,
        currentProgress = currentProgress,
        targetGoal = targetGoal,
        isClaimed = isClaimed,
        type = try { TaskType.valueOf(type) } catch (_: Exception) { TaskType.DAILY },
        categoryKey = categoryKey,
        claimedAtTimestamp = claimedAtTimestamp
    )

    companion object {
        fun currentPeriodKey(type: String): String {
            if (type != TaskType.DAILY.name) return "WEEKLY"
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.format(Date())
        }

        fun fromDomain(userId: String, domain: TaskItem): TaskEntity = TaskEntity(
            id = domain.id,
            userId = userId,
            title = domain.title,
            description = domain.description,
            rewardPoints = domain.rewardPoints,
            currentProgress = domain.currentProgress,
            targetGoal = domain.targetGoal,
            isClaimed = domain.isClaimed,
            type = domain.type.name,
            categoryKey = domain.categoryKey,
            claimedAtTimestamp = domain.claimedAtTimestamp,
            periodKey = currentPeriodKey(domain.type.name)
        )
    }
}
