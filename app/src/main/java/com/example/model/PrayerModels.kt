package com.example.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class PrayerType(val order: Int) {
    FAJR(0),
    DHUHR(1),
    ASR(2),
    MAGHRIB(3),
    ISHA(4)
}

enum class CalculationMethod(
    val id: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    KARACHI("karachi", fajrAngle = 18.0, ishaAngle = 18.0),
    MWL("mwl", fajrAngle = 18.0, ishaAngle = 17.0),
    ISNA("isna", fajrAngle = 15.0, ishaAngle = 15.0),
    UMM_AL_QURA("umm_al_qura", fajrAngle = 18.5, ishaAngle = 0.0, ishaMinutesAfterMaghrib = 90);

    companion object {
        fun fromId(id: String?): CalculationMethod =
            entries.firstOrNull { it.id == id } ?: KARACHI
    }
}

enum class AsrJuristic(val id: String, val shadowFactor: Double) {
    HANAFI("hanafi", 2.0),
    SHAFI("shafi", 1.0);

    companion object {
        fun fromId(id: String?): AsrJuristic =
            entries.firstOrNull { it.id == id } ?: HANAFI
    }
}

enum class ReminderOption(val minutesBefore: Int) {
    NONE(0),
    MIN_5(5),
    MIN_20(20),
    MIN_40(40);

    companion object {
        fun fromMinutes(minutes: Int): ReminderOption =
            entries.firstOrNull { it.minutesBefore == minutes } ?: NONE
    }
}

enum class NotificationTone(val id: String, val channelId: String) {
    ADHAN_TONE("adhan", "waqt_prayer_adhan_v2"),
    GENTLE_CHIME("gentle", "waqt_prayer_gentle_v2"),
    SILENT_VIBRATE("silent", "waqt_prayer_silent_v2");

    companion object {
        fun fromId(id: String?): NotificationTone =
            entries.firstOrNull { it.id == id } ?: ADHAN_TONE
    }
}

enum class AppLanguage(val code: String) {
    BENGALI("bn"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: BENGALI
    }
}

enum class ThemeMode(val id: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromId(id: String?): ThemeMode =
            entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

enum class LocationSource(val id: String) {
    MANUAL("manual"),
    GPS("gps");

    companion object {
        fun fromId(id: String?): LocationSource =
            entries.firstOrNull { it.id == id } ?: MANUAL
    }
}

@Immutable
data class DistrictLocation(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val divisionEn: String,
    val divisionBn: String,
    val latitude: Double,
    val longitude: Double
)

@Immutable
data class DailyPrayerSchedule(
    val date: LocalDate,
    val locationNameEn: String,
    val locationNameBn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val methodId: String,
    val asrJuristicId: String,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val isFromCache: Boolean = false
) {
    val sehriEnd: LocalTime get() = fajr
    val iftarStart: LocalTime get() = maghrib

    fun timeFor(prayer: PrayerType): LocalTime = when (prayer) {
        PrayerType.FAJR -> fajr
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }

    fun waqtEndDateTimeFor(
        prayer: PrayerType,
        tomorrowSchedule: DailyPrayerSchedule
    ): LocalDateTime = when (prayer) {
        PrayerType.FAJR -> LocalDateTime.of(date, sunrise)
        PrayerType.DHUHR -> LocalDateTime.of(date, asr)
        PrayerType.ASR -> LocalDateTime.of(date, maghrib)
        PrayerType.MAGHRIB -> LocalDateTime.of(date, isha)
        PrayerType.ISHA -> LocalDateTime.of(tomorrowSchedule.date, tomorrowSchedule.fajr)
    }
}

@Immutable
data class TahajjudSchedule(
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val islamicMidnight: LocalDateTime,
    val isTonightWindow: Boolean
) {
    val startTime: LocalTime get() = startDateTime.toLocalTime()
    val endTime: LocalTime get() = endDateTime.toLocalTime()
    val midnightTime: LocalTime get() = islamicMidnight.toLocalTime()
}

@Immutable
data class UserSettings(
    val language: AppLanguage = AppLanguage.BENGALI,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
    val calculationMethod: CalculationMethod = CalculationMethod.KARACHI,
    val asrJuristic: AsrJuristic = AsrJuristic.HANAFI,
    val locationSource: LocationSource = LocationSource.MANUAL,
    val selectedDistrictId: String = "dhaka",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val locationNameEn: String = "Dhaka",
    val locationNameBn: String = "ঢাকা",
    val enabledPrayers: Set<PrayerType> = setOf(
        PrayerType.FAJR,
        PrayerType.DHUHR,
        PrayerType.ASR,
        PrayerType.MAGHRIB,
        PrayerType.ISHA
    ),
    val reminderOption: ReminderOption = ReminderOption.MIN_20,
    val waqtEndAlertEnabled: Boolean = true,
    val tahajjudAlertEnabled: Boolean = false,
    val notificationTone: NotificationTone = NotificationTone.ADHAN_TONE
)
