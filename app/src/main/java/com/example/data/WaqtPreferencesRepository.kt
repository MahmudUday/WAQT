package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.DailyPrayerSchedule
import com.example.model.DistrictLocation
import com.example.model.LocationSource
import com.example.model.NotificationTone
import com.example.model.PrayerType
import com.example.model.ReminderOption
import com.example.model.ThemeMode
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

private val Context.waqtDataStore: DataStore<Preferences> by preferencesDataStore(name = "waqt_preferences")

data class CachedScheduleBundle(
    val schedules: List<DailyPrayerSchedule>,
    val updatedAtEpochMs: Long
)

class WaqtPreferencesRepository(context: Context) {

    private val dataStore = context.applicationContext.waqtDataStore

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val ASR_JURISTIC = stringPreferencesKey("asr_juristic")
        val LOCATION_SOURCE = stringPreferencesKey("location_source")
        val DISTRICT_ID = stringPreferencesKey("district_id")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val LOCATION_NAME_EN = stringPreferencesKey("location_name_en")
        val LOCATION_NAME_BN = stringPreferencesKey("location_name_bn")
        val ENABLED_PRAYERS = stringSetPreferencesKey("enabled_prayers")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
        val WAQT_END_ALERT_ENABLED = booleanPreferencesKey("waqt_end_alert_enabled")
        val TAHAJJUD_ALERT_ENABLED = booleanPreferencesKey("tahajjud_alert_enabled")
        val NOTIFICATION_TONE = stringPreferencesKey("notification_tone")
        val CACHED_SCHEDULES_JSON = stringPreferencesKey("cached_schedules_json")
        val CACHE_UPDATED_AT = longPreferencesKey("cache_updated_at")
        val LAST_ALARM_SIGNATURE = stringPreferencesKey("last_alarm_signature")
    }

    val settingsFlow: Flow<UserSettings> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> parseSettings(prefs) }

    val cachedSchedulesFlow: Flow<CachedScheduleBundle> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val rawJson = prefs[Keys.CACHED_SCHEDULES_JSON].orEmpty()
            val updatedAt = prefs[Keys.CACHE_UPDATED_AT] ?: 0L
            CachedScheduleBundle(
                schedules = decodeSchedules(rawJson),
                updatedAtEpochMs = updatedAt
            )
        }

    suspend fun getSettingsSnapshot(): UserSettings = settingsFlow.first()

    suspend fun getLastAlarmSignature(): String? {
        return dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { it[Keys.LAST_ALARM_SIGNATURE] }
            .first()
    }

    suspend fun setLastAlarmSignature(signature: String) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_ALARM_SIGNATURE] = signature
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = language.code }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = themeMode.id }
    }

    suspend fun setReduceMotion(reduce: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.REDUCE_MOTION] = reduce }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        dataStore.edit { prefs -> prefs[Keys.CALCULATION_METHOD] = method.id }
    }

    suspend fun setAsrJuristic(asrJuristic: AsrJuristic) {
        dataStore.edit { prefs -> prefs[Keys.ASR_JURISTIC] = asrJuristic.id }
    }

    suspend fun setManualDistrict(district: DistrictLocation) {
        dataStore.edit { prefs ->
            prefs[Keys.LOCATION_SOURCE] = LocationSource.MANUAL.id
            prefs[Keys.DISTRICT_ID] = district.id
            prefs[Keys.LATITUDE] = district.latitude
            prefs[Keys.LONGITUDE] = district.longitude
            prefs[Keys.LOCATION_NAME_EN] = district.nameEn
            prefs[Keys.LOCATION_NAME_BN] = district.nameBn
        }
    }

    suspend fun setGpsLocation(
        latitude: Double,
        longitude: Double,
        nearestDistrict: DistrictLocation
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.LOCATION_SOURCE] = LocationSource.GPS.id
            prefs[Keys.DISTRICT_ID] = nearestDistrict.id
            prefs[Keys.LATITUDE] = latitude
            prefs[Keys.LONGITUDE] = longitude
            prefs[Keys.LOCATION_NAME_EN] = "${nearestDistrict.nameEn} (GPS)"
            prefs[Keys.LOCATION_NAME_BN] = "${nearestDistrict.nameBn} (জিপিএস)"
        }
    }

    suspend fun togglePrayerNotification(prayer: PrayerType, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_PRAYERS]
                ?: PrayerType.entries.map { it.name }.toSet()
            val updated = current.toMutableSet()
            if (enabled) updated.add(prayer.name) else updated.remove(prayer.name)
            prefs[Keys.ENABLED_PRAYERS] = updated
        }
    }

    suspend fun setReminderOption(option: ReminderOption) {
        dataStore.edit { prefs -> prefs[Keys.REMINDER_MINUTES] = option.minutesBefore }
    }

    suspend fun setWaqtEndAlertEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.WAQT_END_ALERT_ENABLED] = enabled }
    }

    suspend fun setTahajjudAlertEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.TAHAJJUD_ALERT_ENABLED] = enabled }
    }

    suspend fun setNotificationTone(tone: NotificationTone) {
        dataStore.edit { prefs -> prefs[Keys.NOTIFICATION_TONE] = tone.id }
    }

    suspend fun saveCachedSchedules(schedules: List<DailyPrayerSchedule>) {
        if (schedules.isEmpty()) return
        val encoded = encodeSchedules(schedules)
        dataStore.edit { prefs ->
            prefs[Keys.CACHED_SCHEDULES_JSON] = encoded
            prefs[Keys.CACHE_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    private fun parseSettings(prefs: Preferences): UserSettings {
        val defaultDistrict = BangladeshLocations.defaultDistrict
        val savedPrayers = prefs[Keys.ENABLED_PRAYERS]?.mapNotNull { name ->
            runCatching { PrayerType.valueOf(name) }.getOrNull()
        }?.toSet() ?: PrayerType.entries.toSet()

        return UserSettings(
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            themeMode = ThemeMode.fromId(prefs[Keys.THEME_MODE]),
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            calculationMethod = CalculationMethod.fromId(prefs[Keys.CALCULATION_METHOD]),
            asrJuristic = AsrJuristic.fromId(prefs[Keys.ASR_JURISTIC]),
            locationSource = LocationSource.fromId(prefs[Keys.LOCATION_SOURCE]),
            selectedDistrictId = prefs[Keys.DISTRICT_ID] ?: defaultDistrict.id,
            latitude = prefs[Keys.LATITUDE] ?: defaultDistrict.latitude,
            longitude = prefs[Keys.LONGITUDE] ?: defaultDistrict.longitude,
            locationNameEn = prefs[Keys.LOCATION_NAME_EN] ?: defaultDistrict.nameEn,
            locationNameBn = prefs[Keys.LOCATION_NAME_BN] ?: defaultDistrict.nameBn,
            enabledPrayers = savedPrayers,
            reminderOption = ReminderOption.fromMinutes(prefs[Keys.REMINDER_MINUTES] ?: 20),
            waqtEndAlertEnabled = prefs[Keys.WAQT_END_ALERT_ENABLED] ?: true,
            tahajjudAlertEnabled = prefs[Keys.TAHAJJUD_ALERT_ENABLED] ?: false,
            notificationTone = NotificationTone.fromId(prefs[Keys.NOTIFICATION_TONE])
        )
    }

    private fun encodeSchedules(schedules: List<DailyPrayerSchedule>): String {
        val arr = JSONArray()
        schedules.forEach { s ->
            val obj = JSONObject().apply {
                put("date", s.date.toString())
                put("locEn", s.locationNameEn)
                put("locBn", s.locationNameBn)
                put("lat", s.latitude)
                put("lon", s.longitude)
                put("tz", s.timeZoneId)
                put("method", s.methodId)
                put("asrRule", s.asrJuristicId)
                put("fajr", s.fajr.toString())
                put("sunrise", s.sunrise.toString())
                put("dhuhr", s.dhuhr.toString())
                put("asr", s.asr.toString())
                put("maghrib", s.maghrib.toString())
                put("isha", s.isha.toString())
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decodeSchedules(raw: String): List<DailyPrayerSchedule> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        DailyPrayerSchedule(
                            date = LocalDate.parse(obj.getString("date")),
                            locationNameEn = obj.getString("locEn"),
                            locationNameBn = obj.getString("locBn"),
                            latitude = obj.getDouble("lat"),
                            longitude = obj.getDouble("lon"),
                            timeZoneId = obj.getString("tz"),
                            methodId = obj.getString("method"),
                            asrJuristicId = obj.getString("asrRule"),
                            fajr = LocalTime.parse(obj.getString("fajr")),
                            sunrise = LocalTime.parse(obj.getString("sunrise")),
                            dhuhr = LocalTime.parse(obj.getString("dhuhr")),
                            asr = LocalTime.parse(obj.getString("asr")),
                            maghrib = LocalTime.parse(obj.getString("maghrib")),
                            isha = LocalTime.parse(obj.getString("isha")),
                            isFromCache = true
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }
}
