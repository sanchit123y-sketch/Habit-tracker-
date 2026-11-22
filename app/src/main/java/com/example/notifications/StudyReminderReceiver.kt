package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.DateUtils
import com.example.data.StudyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = ReminderPreferencesRepository(appContext)
                val settings = prefsRepo.getSettingsOnce()
                if (settings.enabled) {
                    val todayMinutes = StudyDatabase.getInstance(appContext)
                        .studyDao()
                        .getMinutesForDate(DateUtils.todayDateKey())

                    val shouldNotify = !settings.skipIfGoalMet || todayMinutes < 60
                    if (shouldNotify) {
                        StudyNotificationHelper.showStudyReminderNotification(
                            context = appContext,
                            todayStudyMinutes = todayMinutes,
                            isTest = false
                        )
                    }

                    // Schedule next day's reminder cleanly
                    StudyReminderScheduler.scheduleDailyReminder(
                        context = appContext,
                        hour = settings.hour,
                        minute = settings.minute
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
