package com.example.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AppLanguage
import com.example.model.NotificationTone

object NotificationHelper {

    const val REMINDER_CHANNEL_ID = "waqt_prayer_reminder_v2"
    const val WAQT_END_CHANNEL_ID = "waqt_ending_alert_v2"

    @Volatile
    private var activeRingtone: Ringtone? = null

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannels(context: Context, language: AppLanguage = AppLanguage.BENGALI) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val alarmAudioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val notifAudioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val strongVibration = longArrayOf(0, 450, 180, 450, 180, 600)
        val endingVibration = longArrayOf(0, 320, 160, 320)

        val adhanChannel = NotificationChannel(
            NotificationTone.ADHAN_TONE.channelId,
            if (language == AppLanguage.BENGALI) "নামাজের ওয়াক্ত (উচ্চ শব্দ অ্যালার্ম)" else "Prayer Time (Loud Alarm)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = if (language == AppLanguage.BENGALI) {
                "ওয়াক্ত শুরু হলে স্পষ্ট অ্যালার্ম শব্দ ও ভাইব্রেশন"
            } else {
                "Clear audible alarm sound and vibration when prayer Waqt starts"
            }
            enableVibration(true)
            vibrationPattern = strongVibration
            enableLights(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(alarmSound, alarmAudioAttrs)
        }

        val gentleChannel = NotificationChannel(
            NotificationTone.GENTLE_CHIME.channelId,
            if (language == AppLanguage.BENGALI) "নামাজের ওয়াক্ত (সৌম্য শব্দ)" else "Prayer Time (Gentle Chime)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = if (language == AppLanguage.BENGALI) {
                "মৃদু নোটিফিকেশন শব্দসহ নামাজের অ্যালার্ট"
            } else {
                "Prayer notifications with a gentle notification chime"
            }
            enableVibration(true)
            vibrationPattern = endingVibration
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(notificationSound, notifAudioAttrs)
        }

        val silentChannel = NotificationChannel(
            NotificationTone.SILENT_VIBRATE.channelId,
            if (language == AppLanguage.BENGALI) "নামাজের ওয়াক্ত (শুধু ভাইব্রেশন)" else "Prayer Time (Vibrate Only)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = if (language == AppLanguage.BENGALI) {
                "শব্দহীন শুধু ভাইব্রেশন নোটিফিকেশন"
            } else {
                "Silent vibration-only prayer reminders"
            }
            enableVibration(true)
            vibrationPattern = strongVibration
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
        }

        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            if (language == AppLanguage.BENGALI) "ওয়াক্ত শুরুর পূর্ব-রিমাইন্ডার" else "Pre-Prayer Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = if (language == AppLanguage.BENGALI) {
                "ওয়াক্ত শুরু হওয়ার ৫, ২০ বা ৪০ মিনিট আগের রিমাইন্ডার"
            } else {
                "Reminders 5, 20, or 40 minutes before prayer time"
            }
            enableVibration(true)
            vibrationPattern = endingVibration
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(notificationSound, notifAudioAttrs)
        }

        val waqtEndChannel = NotificationChannel(
            WAQT_END_CHANNEL_ID,
            if (language == AppLanguage.BENGALI) "ওয়াক্ত শেষ হওয়ার ২০ মিনিট আগের সতর্কতা" else "20-Min Before Waqt Ends",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = if (language == AppLanguage.BENGALI) {
                "প্রতিটি ওয়াক্ত শেষ হওয়ার ২০ মিনিট আগে সতর্কবার্তা"
            } else {
                "Alert 20 minutes before the current prayer window closes"
            }
            enableVibration(true)
            vibrationPattern = endingVibration
            enableLights(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(notificationSound, notifAudioAttrs)
        }

        manager.createNotificationChannels(
            listOf(adhanChannel, gentleChannel, silentChannel, reminderChannel, waqtEndChannel)
        )
    }

    fun showPrayerNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        playAlarmTonePulse: Boolean = false
    ) {
        if (!hasNotificationPermission(context)) return

        ensureChannels(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        if (channelId == NotificationTone.SILENT_VIBRATE.channelId) {
            builder.setSilent(true)
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }

        if (playAlarmTonePulse && channelId == NotificationTone.ADHAN_TONE.channelId) {
            playBriefAlarmFeedback(context)
        }
    }

    /**
     * Plays a brief, clear alarm-stream sound + haptic pulse so the user reliably hears and feels
     * Waqt start alarms even when media/notification streams are lowered, auto-stopping in 4.5s.
     */
    fun playBriefAlarmFeedback(context: Context) {
        stopAlarmFeedback()
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (uri != null) {
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.isLooping = false
                    }
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    activeRingtone = ringtone
                    ringtone.play()
                    Handler(Looper.getMainLooper()).postDelayed({
                        stopAlarmFeedback()
                    }, 4500L)
                }
            }
        }
        triggerDistinctVibration(context)
    }

    fun stopAlarmFeedback() {
        runCatching {
            activeRingtone?.let {
                if (it.isPlaying) it.stop()
            }
            activeRingtone = null
        }
    }

    private fun triggerDistinctVibration(context: Context) {
        runCatching {
            val pattern = longArrayOf(0, 380, 160, 380, 160, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vm?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        }
    }
}
