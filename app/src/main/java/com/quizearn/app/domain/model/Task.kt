package com.quizearn.app.domain.model

/**
 * Type of challenge task.
 */
enum class TaskType {
    DAILY,
    WEEKLY
}

/**
 * Representation of a gamified challenge task.
 */
data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Long,
    val currentProgress: Int,
    val targetGoal: Int,
    val isClaimed: Boolean = false,
    val type: TaskType = TaskType.DAILY,
    val categoryKey: String? = null,
    val claimedAtTimestamp: Long? = null
) {
    val isCompleted: Boolean
        get() = currentProgress >= targetGoal

    val progressFraction: Float
        get() = if (targetGoal > 0) (currentProgress.toFloat() / targetGoal.toFloat()).coerceIn(0f, 1f) else 0f
}

/**
 * Representation of an unlockable achievement badge.
 */
data class AchievementBadge(
    val id: String,
    val name: String,
    val description: String,
    val categoryKey: String,
    val isUnlocked: Boolean,
    val unlockedDateFormatted: String? = null,
    val progress: Int,
    val targetGoal: Int,
    val rewardPoints: Long = 50L,
    val isClaimed: Boolean = false
) {
    val progressFraction: Float
        get() = if (targetGoal > 0) (progress.toFloat() / targetGoal.toFloat()).coerceIn(0f, 1f) else 0f
}
