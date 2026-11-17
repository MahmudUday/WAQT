package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.BengaliFormatter
import com.example.data.WaqtPreferencesRepository
import com.example.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: return
        val alarmKind = AlarmKind.fromId(intent.getStringExtra(PrayerAlarmScheduler.EXTRA_ALARM_KIND))
        val reminderMinutes = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_REMINDER_MINUTES, 0)
        val timeLabel = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_TIME_LABEL).orEmpty()
        val locationLabel = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_LOCATION_LABEL).orEmpty()
        val channelId = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CHANNEL_ID)
            ?: NotificationHelper.REMINDER_CHANNEL_ID
        val language = AppLanguage.fromCode(intent.getStringExtra(PrayerAlarmScheduler.EXTRA_LANGUAGE_CODE))
        val notificationId = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_NOTIFICATION_ID, 3001)

        val (title, body, playAlarmPulse) = when (alarmKind) {
            AlarmKind.WAQT_START -> {
                if (language == AppLanguage.BENGALI) {
                    Triple(
                        "$prayerName-এর ওয়াক্ত শুরু হয়েছে",
                        "সময়: $timeLabel • $locationLabel",
                        true
                    )
                } else {
                    Triple(
                        "$prayerName Waqt Has Started",
                        "Time: $timeLabel • $locationLabel",
                        true
                    )
                }
            }
            AlarmKind.PRE_START_REMINDER -> {
                if (language == AppLanguage.BENGALI) {
                    val minsBn = BengaliFormatter.toBengaliDigits(reminderMinutes.toString())
                    Triple(
                        "$prayerName শুরু হতে $minsBn মিনিট বাকি",
                        "ওয়াক্ত শুরু: $timeLabel • $locationLabel",
                        false
                    )
                } else {
                    Triple(
                        "$prayerName starts in $reminderMinutes minutes",
                        "Waqt begins: $timeLabel • $locationLabel",
                        false
                    )
                }
            }
            AlarmKind.WAQT_END_20M -> {
                if (language == AppLanguage.BENGALI) {
                    Triple(
                        "$prayerName-এর ওয়াক্ত শেষ হতে ২০ মিনিট বাকি",
                        "ওয়াক্ত শেষ: $timeLabel • $locationLabel",
                        false
                    )
                } else {
                    Triple(
                        "$prayerName Waqt ends in 20 minutes",
                        "Waqt closes at $timeLabel • $locationLabel",
                        false
                    )
                }
            }
            AlarmKind.TAHAJJUD_START -> {
                if (language == AppLanguage.BENGALI) {
                    Triple(
                        "তাহাজ্জুদ নামাজের উত্তম সময় শুরু হয়েছে",
                        "রাতের শেষ তৃতীয়াংশ: $timeLabel • $locationLabel",
                        true
                    )
                } else {
                    Triple(
                        "Tahajjud Prayer Window Started",
                        "Last third of night: $timeLabel • $locationLabel",
                        true
                    )
                }
            }
        }

        NotificationHelper.showPrayerNotification(
            context = context,
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            body = body,
            playAlarmTonePulse = playAlarmPulse
        )

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repo = WaqtPreferencesRepository(appContext)
                val settings = repo.getSettingsSnapshot()
                PrayerAlarmScheduler.scheduleIfNeeded(appContext, repo, settings, force = false)
            } catch (_: Exception) {
                // Keep receiver crash-proof
            } finally {
                pendingResult.finish()
            }
        }
    }
}
