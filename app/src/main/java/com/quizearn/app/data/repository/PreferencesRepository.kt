package com.quizearn.app.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing persistent user preferences and application flags.
 */
interface PreferencesRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    val lastActiveTimestamp: Flow<Long>

    // Notification & alert preferences
    val dailyReminderEnabled: Flow<Boolean>
    val tournamentAlertsEnabled: Flow<Boolean>
    val streakAlertsEnabled: Flow<Boolean>

    // Gameplay effects preferences
    val soundEffectsEnabled: Flow<Boolean>
    val hapticsEnabled: Flow<Boolean>
    val darkModeEnabled: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean = true)
    suspend fun updateLastActiveTimestamp(timestamp: Long = System.currentTimeMillis())

    suspend fun setDailyReminderEnabled(enabled: Boolean)
    suspend fun setTournamentAlertsEnabled(enabled: Boolean)
    suspend fun setStreakAlertsEnabled(enabled: Boolean)
    suspend fun setSoundEffectsEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setDarkModeEnabled(enabled: Boolean)

    suspend fun clearPreferences()
}
