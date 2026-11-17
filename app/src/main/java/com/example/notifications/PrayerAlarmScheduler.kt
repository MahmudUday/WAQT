package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.BengaliFormatter
import com.example.data.PrayerTimeCalculator
import com.example.data.WaqtPreferencesRepository
import com.example.model.AppLanguage
import com.example.model.PrayerType
import com.example.model.ReminderOption
import com.example.model.UserSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

enum class AlarmKind(val id: String) {
    WAQT_START("waqt_start"),
    PRE_START_REMINDER("pre_start_reminder"),
    WAQT_END_20M("waqt_end_20m"),
    TAHAJJUD_START("tahajjud_start");

    companion object {
        fun fromId(id: String?): AlarmKind =
            entries.firstOrNull { it.id == id } ?: WAQT_START
    }
}

object PrayerAlarmScheduler {

    private const val SCHEDULING_WINDOW_DAYS = 7
    private const val EXACT_ALARM_BASE_CODE = 1000
    private const val REMINDER_ALARM_BASE_CODE = 2000
    private const val WAQT_END_ALARM_BASE_CODE = 3000
    private const val TAHAJJUD_ALARM_BASE_CODE = 4000

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_ALARM_KIND = "extra_alarm_kind"
    const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"
    const val EXTRA_TIME_LABEL = "extra_time_label"
    const val EXTRA_LOCATION_LABEL = "extra_location_label"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_LANGUAGE_CODE = "extra_language_code"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { alarmManager.canScheduleExactAlarms() }.getOrDefault(false)
        } else {
            true
        }
    }

    fun buildScheduleSignature(settings: UserSettings, today: LocalDate, zoneId: ZoneId): String {
        val enabledSorted = settings.enabledPrayers.map { it.name }.sorted().joinToString(",")
        return listOf(
            today.toString(),
            zoneId.id,
            "%.4f".format(settings.latitude),
            "%.4f".format(settings.longitude),
            settings.calculationMethod.id,
            settings.asrJuristic.id,
            enabledSorted,
            settings.reminderOption.minutesBefore.toString(),
            settings.waqtEndAlertEnabled.toString(),
            settings.tahajjudAlertEnabled.toString(),
            settings.notificationTone.id,
            settings.language.code
        ).joinToString("|")
    }

    suspend fun scheduleIfNeeded(
        context: Context,
        repository: WaqtPreferencesRepository,
        settings: UserSettings,
        force: Boolean = false
    ): Boolean {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val signature = buildScheduleSignature(settings, today, zoneId)
        val previousSignature = repository.getLastAlarmSignature()

        if (!force && signature == previousSignature) {
            return false
        }

        cancelAllAlarms(context)
        scheduleSevenDayWindow(context, settings, today, zoneId)
        repository.setLastAlarmSignature(signature)
        return true
    }

    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (dayIdx in 0 until SCHEDULING_WINDOW_DAYS) {
            for (prayer in PrayerType.entries) {
                cancelPendingIntent(context, alarmManager, EXACT_ALARM_BASE_CODE + dayIdx * 10 + prayer.order)
                cancelPendingIntent(context, alarmManager, REMINDER_ALARM_BASE_CODE + dayIdx * 10 + prayer.order)
                cancelPendingIntent(context, alarmManager, WAQT_END_ALARM_BASE_CODE + dayIdx * 10 + prayer.order)
            }
            cancelPendingIntent(context, alarmManager, TAHAJJUD_ALARM_BASE_CODE + dayIdx)
        }
    }

    private fun cancelPendingIntent(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val existing = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (existing != null) {
            runCatching { alarmManager.cancel(existing) }
            runCatching { existing.cancel() }
        }
    }

    private fun scheduleSevenDayWindow(
        context: Context,
        settings: UserSettings,
        startDate: LocalDate,
        zoneId: ZoneId
    ) {
        if (settings.enabledPrayers.isEmpty() && !settings.tahajjudAlertEnabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        NotificationHelper.ensureChannels(context, settings.language)

        val nowMs = System.currentTimeMillis()
        val locationLabel = if (settings.language == AppLanguage.BENGALI) {
            settings.locationNameBn
        } else {
            settings.locationNameEn
        }
        val useExact = canScheduleExactAlarms(context)

        // Precompute 8 days (day 0..7) so day 6 can look up day 7 Fajr for Isha Waqt end & Tahajjud
        val daysRange = PrayerTimeCalculator.calculateRange(
            startDate = startDate,
            days = SCHEDULING_WINDOW_DAYS + 1,
            latitude = settings.latitude,
            longitude = settings.longitude,
            locationNameEn = settings.locationNameEn,
            locationNameBn = settings.locationNameBn,
            method = settings.calculationMethod,
            asrJuristic = settings.asrJuristic,
            zoneId = zoneId
        )

        for (dayIdx in 0 until SCHEDULING_WINDOW_DAYS) {
            val schedule = daysRange[dayIdx]
            val nextSchedule = daysRange[dayIdx + 1]
            val targetDate = schedule.date

            for (prayer in PrayerType.entries) {
                if (prayer !in settings.enabledPrayers) continue

                val prayerTime = schedule.timeFor(prayer)
                val prayerDateTime = LocalDateTime.of(targetDate, prayerTime)
                val triggerAtMs = prayerDateTime.atZone(zoneId).toInstant().toEpochMilli()
                val timeLabel = BengaliFormatter.formatTime(prayerTime, settings.language)
                val prayerLabel = prayerLabel(prayer, settings.language)

                // 1. Alarm right when Waqt starts
                if (triggerAtMs > nowMs + 2_000L) {
                    val reqCode = EXACT_ALARM_BASE_CODE + dayIdx * 10 + prayer.order
                    val pendingIntent = buildAlarmPendingIntent(
                        context = context,
                        requestCode = reqCode,
                        prayerName = prayerLabel,
                        alarmKind = AlarmKind.WAQT_START,
                        reminderMinutes = 0,
                        timeLabel = timeLabel,
                        locationLabel = locationLabel,
                        channelId = settings.notificationTone.channelId,
                        languageCode = settings.language.code,
                        notificationId = reqCode
                    )
                    scheduleSingleAlarm(alarmManager, triggerAtMs, pendingIntent, useExact)
                }

                // 2. Optional Pre-Prayer Start Reminder (5, 20, or 40 min before start)
                if (settings.reminderOption != ReminderOption.NONE) {
                    val minsBefore = settings.reminderOption.minutesBefore
                    val reminderTriggerMs = triggerAtMs - minsBefore * 60_000L
                    if (reminderTriggerMs > nowMs + 2_000L) {
                        val remCode = REMINDER_ALARM_BASE_CODE + dayIdx * 10 + prayer.order
                        val remPendingIntent = buildAlarmPendingIntent(
                            context = context,
                            requestCode = remCode,
                            prayerName = prayerLabel,
                            alarmKind = AlarmKind.PRE_START_REMINDER,
                            reminderMinutes = minsBefore,
                            timeLabel = timeLabel,
                            locationLabel = locationLabel,
                            channelId = NotificationHelper.REMINDER_CHANNEL_ID,
                            languageCode = settings.language.code,
                            notificationId = remCode
                        )
                        scheduleSingleAlarm(alarmManager, reminderTriggerMs, remPendingIntent, useExact)
                    }
                }

                // 3. 20 Minutes Before Waqt Ends Alert
                if (settings.waqtEndAlertEnabled) {
                    val endDateTime = schedule.waqtEndDateTimeFor(prayer, nextSchedule)
                    val endAlertDateTime = endDateTime.minusMinutes(20)
                    val endAlertMs = endAlertDateTime.atZone(zoneId).toInstant().toEpochMilli()
                    if (endAlertMs > nowMs + 2_000L) {
                        val endCode = WAQT_END_ALARM_BASE_CODE + dayIdx * 10 + prayer.order
                        val endTimeLabel = BengaliFormatter.formatTime(endDateTime.toLocalTime(), settings.language)
                        val endPendingIntent = buildAlarmPendingIntent(
                            context = context,
                            requestCode = endCode,
                            prayerName = prayerLabel,
                            alarmKind = AlarmKind.WAQT_END_20M,
                            reminderMinutes = 20,
                            timeLabel = endTimeLabel,
                            locationLabel = locationLabel,
                            channelId = NotificationHelper.WAQT_END_CHANNEL_ID,
                            languageCode = settings.language.code,
                            notificationId = endCode
                        )
                        scheduleSingleAlarm(alarmManager, endAlertMs, endPendingIntent, useExact)
                    }
                }
            }

            // 4. Optional Tahajjud Alarm (Last third of night)
            if (settings.tahajjudAlertEnabled) {
                val tahajjud = PrayerTimeCalculator.calculateTahajjud(
                    now = LocalDateTime.of(targetDate, schedule.maghrib.plusMinutes(5)),
                    yesterdaySchedule = schedule,
                    todaySchedule = schedule,
                    tomorrowSchedule = nextSchedule
                )
                val tahajjudMs = tahajjud.startDateTime.atZone(zoneId).toInstant().toEpochMilli()
                if (tahajjudMs > nowMs + 2_000L) {
                    val tCode = TAHAJJUD_ALARM_BASE_CODE + dayIdx
                    val tLabel = if (settings.language == AppLanguage.BENGALI) "তাহাজ্জুদ" else "Tahajjud"
                    val tTimeLabel = BengaliFormatter.formatTime(tahajjud.startTime, settings.language)
                    val tPendingIntent = buildAlarmPendingIntent(
                        context = context,
                        requestCode = tCode,
                        prayerName = tLabel,
                        alarmKind = AlarmKind.TAHAJJUD_START,
                        reminderMinutes = 0,
                        timeLabel = tTimeLabel,
                        locationLabel = locationLabel,
                        channelId = settings.notificationTone.channelId,
                        languageCode = settings.language.code,
                        notificationId = tCode
                    )
                    scheduleSingleAlarm(alarmManager, tahajjudMs, tPendingIntent, useExact)
                }
            }
        }
    }

    private fun buildAlarmPendingIntent(
        context: Context,
        requestCode: Int,
        prayerName: String,
        alarmKind: AlarmKind,
        reminderMinutes: Int,
        timeLabel: String,
        locationLabel: String,
        channelId: String,
        languageCode: String,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_ALARM_KIND, alarmKind.id)
            putExtra(EXTRA_REMINDER_MINUTES, reminderMinutes)
            putExtra(EXTRA_TIME_LABEL, timeLabel)
            putExtra(EXTRA_LOCATION_LABEL, locationLabel)
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_LANGUAGE_CODE, languageCode)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleSingleAlarm(
        alarmManager: AlarmManager,
        triggerAtMs: Long,
        pendingIntent: PendingIntent,
        useExact: Boolean
    ) {
        try {
            if (useExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMs,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMs,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            runCatching {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMs,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Never crash on manufacturer alarm quota restrictions
        }
    }

    private fun prayerLabel(prayer: PrayerType, language: AppLanguage): String {
        return if (language == AppLanguage.BENGALI) {
            when (prayer) {
                PrayerType.FAJR -> "ফজর"
                PrayerType.DHUHR -> "যোহর"
                PrayerType.ASR -> "আসর"
                PrayerType.MAGHRIB -> "মাগরিব"
                PrayerType.ISHA -> "এশা"
            }
        } else {
            when (prayer) {
                PrayerType.FAJR -> "Fajr"
                PrayerType.DHUHR -> "Dhuhr"
                PrayerType.ASR -> "Asr"
                PrayerType.MAGHRIB -> "Maghrib"
                PrayerType.ISHA -> "Isha"
            }
        }
    }
}
