package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.WaqtPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootAndTimeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val relevant = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED

        if (!relevant) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = WaqtPreferencesRepository(appContext)
                val settings = repo.getSettingsSnapshot()
                NotificationHelper.ensureChannels(appContext, settings.language)
                // Force reschedule on reboot, app update, date, or timezone change
                PrayerAlarmScheduler.scheduleIfNeeded(appContext, repo, settings, force = true)
            } catch (_: Exception) {
                // Keep boot receiver crash-proof
            } finally {
                pendingResult.finish()
            }
        }
    }
}
