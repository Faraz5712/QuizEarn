package com.quizearn.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quizearn_preferences")

/**
 * Production implementation of [PreferencesRepository] backed by AndroidX DataStore.
 */
class PreferencesRepositoryImpl(
    private val context: Context
) : PreferencesRepository {

    private object Keys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val LAST_ACTIVE_TIMESTAMP = longPreferencesKey("last_active_timestamp")
        val DAILY_REMINDER = booleanPreferencesKey("pref_daily_reminder")
        val TOURNAMENT_ALERTS = booleanPreferencesKey("pref_tournament_alerts")
        val STREAK_ALERTS = booleanPreferencesKey("pref_streak_alerts")
        val SOUND_EFFECTS = booleanPreferencesKey("pref_sound_effects")
        val HAPTICS = booleanPreferencesKey("pref_haptics")
        val DARK_MODE = booleanPreferencesKey("pref_dark_mode")
    }

    override val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.HAS_COMPLETED_ONBOARDING] ?: false }

    override val lastActiveTimestamp: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.LAST_ACTIVE_TIMESTAMP] ?: 0L }

    override val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.DAILY_REMINDER] ?: true }

    override val tournamentAlertsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.TOURNAMENT_ALERTS] ?: true }

    override val streakAlertsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.STREAK_ALERTS] ?: true }

    override val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.SOUND_EFFECTS] ?: true }

    override val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.HAPTICS] ?: true }

    override val darkModeEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.DARK_MODE] ?: false }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    override suspend fun updateLastActiveTimestamp(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_ACTIVE_TIMESTAMP] = timestamp }
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_REMINDER] = enabled }
    }

    override suspend fun setTournamentAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TOURNAMENT_ALERTS] = enabled }
    }

    override suspend fun setStreakAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STREAK_ALERTS] = enabled }
    }

    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    }

    override suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    override suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        @Volatile
        private var instance: PreferencesRepository? = null

        fun getInstance(context: Context? = null): PreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: run {
                    if (context != null) {
                        PreferencesRepositoryImpl(context.applicationContext)
                    } else {
                        InMemoryPreferencesRepository()
                    }
                }.also { instance = it }
            }
        }
    }
}

/**
 * In-memory fallback implementation for Compose previews, automated tests, and offline mocking.
 */
class InMemoryPreferencesRepository(
    initialOnboardingCompleted: Boolean = false
) : PreferencesRepository {

    private val _hasCompletedOnboarding = MutableStateFlow(initialOnboardingCompleted)
    private val _lastActiveTimestamp = MutableStateFlow(0L)
    private val _dailyReminder = MutableStateFlow(true)
    private val _tournamentAlerts = MutableStateFlow(true)
    private val _streakAlerts = MutableStateFlow(true)
    private val _soundEffects = MutableStateFlow(true)
    private val _haptics = MutableStateFlow(true)
    private val _darkMode = MutableStateFlow(false)

    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding.asStateFlow()
    override val lastActiveTimestamp: Flow<Long> = _lastActiveTimestamp.asStateFlow()
    override val dailyReminderEnabled: Flow<Boolean> = _dailyReminder.asStateFlow()
    override val tournamentAlertsEnabled: Flow<Boolean> = _tournamentAlerts.asStateFlow()
    override val streakAlertsEnabled: Flow<Boolean> = _streakAlerts.asStateFlow()
    override val soundEffectsEnabled: Flow<Boolean> = _soundEffects.asStateFlow()
    override val hapticsEnabled: Flow<Boolean> = _haptics.asStateFlow()
    override val darkModeEnabled: Flow<Boolean> = _darkMode.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) { _hasCompletedOnboarding.value = completed }
    override suspend fun updateLastActiveTimestamp(timestamp: Long) { _lastActiveTimestamp.value = timestamp }
    override suspend fun setDailyReminderEnabled(enabled: Boolean) { _dailyReminder.value = enabled }
    override suspend fun setTournamentAlertsEnabled(enabled: Boolean) { _tournamentAlerts.value = enabled }
    override suspend fun setStreakAlertsEnabled(enabled: Boolean) { _streakAlerts.value = enabled }
    override suspend fun setSoundEffectsEnabled(enabled: Boolean) { _soundEffects.value = enabled }
    override suspend fun setHapticsEnabled(enabled: Boolean) { _haptics.value = enabled }
    override suspend fun setDarkModeEnabled(enabled: Boolean) { _darkMode.value = enabled }

    override suspend fun clearPreferences() {
        _hasCompletedOnboarding.value = false
        _lastActiveTimestamp.value = 0L
    }
}
