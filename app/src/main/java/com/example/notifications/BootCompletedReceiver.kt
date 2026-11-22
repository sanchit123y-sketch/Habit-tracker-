package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = ReminderPreferencesRepository(appContext).getSettingsOnce()
                if (settings.enabled) {
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
