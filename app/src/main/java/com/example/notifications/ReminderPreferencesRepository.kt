package com.example.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val Context.reminderDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "study_reminder_prefs"
)

data class ReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = 17,
    val minute: Int = 0,
    val skipIfGoalMet: Boolean = true
) {
    fun formattedTime(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
    }
}

class ReminderPreferencesRepository(private val context: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("reminder_enabled")
        val HOUR = intPreferencesKey("reminder_hour")
        val MINUTE = intPreferencesKey("reminder_minute")
        val SKIP_IF_GOAL_MET = booleanPreferencesKey("skip_if_goal_met")
    }

    val settingsFlow: Flow<ReminderSettings> = context.reminderDataStore.data.map { prefs ->
        ReminderSettings(
            enabled = prefs[Keys.ENABLED] ?: false,
            hour = prefs[Keys.HOUR] ?: 17,
            minute = prefs[Keys.MINUTE] ?: 0,
            skipIfGoalMet = prefs[Keys.SKIP_IF_GOAL_MET] ?: true
        )
    }

    suspend fun getSettingsOnce(): ReminderSettings = settingsFlow.first()

    suspend fun updateEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.ENABLED] = enabled
        }
    }

    suspend fun updateTime(hour: Int, minute: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun updateSkipIfGoalMet(skip: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.SKIP_IF_GOAL_MET] = skip
        }
    }
}
