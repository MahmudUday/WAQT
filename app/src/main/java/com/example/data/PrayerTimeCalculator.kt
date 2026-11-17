package com.example.data

import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerType
import com.example.model.TahajjudSchedule
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

data class PrayerPeriodStatus(
    val currentPrayer: PrayerType?,
    val nextPrayer: PrayerType,
    val nextPrayerDateTime: LocalDateTime,
    val periodStartDateTime: LocalDateTime,
    val countdown: Duration,
    val elapsedFraction: Float
)

object PrayerTimeCalculator {

    private const val SUNRISE_SUNSET_ANGLE = 0.833

    fun calculateDay(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        locationNameEn: String,
        locationNameBn: String,
        method: CalculationMethod = CalculationMethod.KARACHI,
        asrJuristic: AsrJuristic = AsrJuristic.HANAFI,
        zoneId: ZoneId = ZoneId.of("Asia/Dhaka")
    ): DailyPrayerSchedule {
        val safeLat = latitude.coerceIn(-65.0, 65.0)
        val safeLon = longitude.coerceIn(-180.0, 180.0)
        val tzHours = zoneId.rules.getOffset(date.atTime(12, 0)).totalSeconds / 3600.0

        val baseJd = julianDate(date) - safeLon / (15.0 * 24.0)

        // Initial pass at approximate solar hours
        var fajrHour = sunAngleTime(baseJd + 5.0 / 24.0, safeLat, method.fajrAngle, isMorning = true)
        var sunriseHour = sunAngleTime(baseJd + 6.0 / 24.0, safeLat, SUNRISE_SUNSET_ANGLE, isMorning = true)
        var dhuhrHour = midDay(baseJd + 12.0 / 24.0)
        var asrHour = asrTime(baseJd + 13.0 / 24.0, safeLat, asrJuristic.shadowFactor)
        var maghribHour = sunAngleTime(baseJd + 18.0 / 24.0, safeLat, SUNRISE_SUNSET_ANGLE, isMorning = false)
        var ishaHour = if (method.ishaMinutesAfterMaghrib != null) {
            maghribHour + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            sunAngleTime(baseJd + 18.0 / 24.0, safeLat, method.ishaAngle, isMorning = false)
        }

        // Second refinement pass using computed solar times for sub-minute stability
        fajrHour = sunAngleTime(baseJd + fajrHour / 24.0, safeLat, method.fajrAngle, isMorning = true)
        sunriseHour = sunAngleTime(baseJd + sunriseHour / 24.0, safeLat, SUNRISE_SUNSET_ANGLE, isMorning = true)
        dhuhrHour = midDay(baseJd + dhuhrHour / 24.0)
        asrHour = asrTime(baseJd + asrHour / 24.0, safeLat, asrJuristic.shadowFactor)
        maghribHour = sunAngleTime(baseJd + maghribHour / 24.0, safeLat, SUNRISE_SUNSET_ANGLE, isMorning = false)
        ishaHour = if (method.ishaMinutesAfterMaghrib != null) {
            maghribHour + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            sunAngleTime(baseJd + ishaHour / 24.0, safeLat, method.ishaAngle, isMorning = false)
        }

        val shift = tzHours - safeLon / 15.0
        val fajrTime = hoursToLocalTime(fajrHour + shift)
        val sunriseTime = hoursToLocalTime(sunriseHour + shift)
        // Standard 1-minute Dhuhr zawal precaution for safe Waqt entry
        val dhuhrTime = hoursToLocalTime(dhuhrHour + shift + (1.0 / 60.0))
        val asrTime = hoursToLocalTime(asrHour + shift)
        val maghribTime = hoursToLocalTime(maghribHour + shift)
        val ishaTime = hoursToLocalTime(ishaHour + shift)

        return DailyPrayerSchedule(
            date = date,
            locationNameEn = locationNameEn,
            locationNameBn = locationNameBn,
            latitude = latitude,
            longitude = longitude,
            timeZoneId = zoneId.id,
            methodId = method.id,
            asrJuristicId = asrJuristic.id,
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            isFromCache = false
        )
    }

    fun calculateRange(
        startDate: LocalDate,
        days: Int,
        latitude: Double,
        longitude: Double,
        locationNameEn: String,
        locationNameBn: String,
        method: CalculationMethod,
        asrJuristic: AsrJuristic,
        zoneId: ZoneId = ZoneId.of("Asia/Dhaka")
    ): List<DailyPrayerSchedule> {
        return (0 until days).map { offset ->
            calculateDay(
                date = startDate.plusDays(offset.toLong()),
                latitude = latitude,
                longitude = longitude,
                locationNameEn = locationNameEn,
                locationNameBn = locationNameBn,
                method = method,
                asrJuristic = asrJuristic,
                zoneId = zoneId
            )
        }
    }

    /**
     * Computes Tahajjud (last third of the Islamic night: Maghrib -> Fajr) and Islamic Midnight.
     * If `now` is before today's Fajr, evaluates the ongoing night window ending at today's Fajr.
     * Otherwise evaluates tonight's upcoming Tahajjud window ending at tomorrow's Fajr.
     */
    fun calculateTahajjud(
        now: LocalDateTime,
        yesterdaySchedule: DailyPrayerSchedule,
        todaySchedule: DailyPrayerSchedule,
        tomorrowSchedule: DailyPrayerSchedule
    ): TahajjudSchedule {
        val beforeFajr = now.toLocalTime().isBefore(todaySchedule.fajr)
        val nightStart = if (beforeFajr) {
            LocalDateTime.of(yesterdaySchedule.date, yesterdaySchedule.maghrib)
        } else {
            LocalDateTime.of(todaySchedule.date, todaySchedule.maghrib)
        }
        val nightEnd = if (beforeFajr) {
            LocalDateTime.of(todaySchedule.date, todaySchedule.fajr)
        } else {
            LocalDateTime.of(tomorrowSchedule.date, tomorrowSchedule.fajr)
        }

        val nightSeconds = Duration.between(nightStart, nightEnd).seconds.coerceAtLeast(3600L)
        val islamicMidnight = nightStart.plusSeconds(nightSeconds / 2L)
        val lastThirdStart = nightEnd.minusSeconds(nightSeconds / 3L)

        return TahajjudSchedule(
            startDateTime = lastThirdStart,
            endDateTime = nightEnd,
            islamicMidnight = islamicMidnight,
            isTonightWindow = !beforeFajr
        )
    }

    fun evaluatePeriodStatus(
        now: LocalDateTime,
        todaySchedule: DailyPrayerSchedule,
        tomorrowSchedule: DailyPrayerSchedule
    ): PrayerPeriodStatus {
        val currentTime = now.toLocalTime()
        val today = todaySchedule.date

        val (currentPrayer, nextPrayer, periodStart, nextDt) = when {
            currentTime.isBefore(todaySchedule.fajr) -> {
                PeriodQuad(
                    PrayerType.ISHA,
                    PrayerType.FAJR,
                    LocalDateTime.of(today.minusDays(1), todaySchedule.isha),
                    LocalDateTime.of(today, todaySchedule.fajr)
                )
            }
            currentTime.isBefore(todaySchedule.sunrise) -> {
                PeriodQuad(
                    PrayerType.FAJR,
                    PrayerType.DHUHR,
                    LocalDateTime.of(today, todaySchedule.fajr),
                    LocalDateTime.of(today, todaySchedule.dhuhr)
                )
            }
            currentTime.isBefore(todaySchedule.dhuhr) -> {
                PeriodQuad(
                    null,
                    PrayerType.DHUHR,
                    LocalDateTime.of(today, todaySchedule.sunrise),
                    LocalDateTime.of(today, todaySchedule.dhuhr)
                )
            }
            currentTime.isBefore(todaySchedule.asr) -> {
                PeriodQuad(
                    PrayerType.DHUHR,
                    PrayerType.ASR,
                    LocalDateTime.of(today, todaySchedule.dhuhr),
                    LocalDateTime.of(today, todaySchedule.asr)
                )
            }
            currentTime.isBefore(todaySchedule.maghrib) -> {
                PeriodQuad(
                    PrayerType.ASR,
                    PrayerType.MAGHRIB,
                    LocalDateTime.of(today, todaySchedule.asr),
                    LocalDateTime.of(today, todaySchedule.maghrib)
                )
            }
            currentTime.isBefore(todaySchedule.isha) -> {
                PeriodQuad(
                    PrayerType.MAGHRIB,
                    PrayerType.ISHA,
                    LocalDateTime.of(today, todaySchedule.maghrib),
                    LocalDateTime.of(today, todaySchedule.isha)
                )
            }
            else -> {
                PeriodQuad(
                    PrayerType.ISHA,
                    PrayerType.FAJR,
                    LocalDateTime.of(today, todaySchedule.isha),
                    LocalDateTime.of(tomorrowSchedule.date, tomorrowSchedule.fajr)
                )
            }
        }

        val totalSec = Duration.between(periodStart, nextDt).seconds.coerceAtLeast(1L)
        val elapsedSec = Duration.between(periodStart, now).seconds.coerceIn(0L, totalSec)
        val fraction = (elapsedSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f)

        return PrayerPeriodStatus(
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            nextPrayerDateTime = nextDt,
            periodStartDateTime = periodStart,
            countdown = Duration.between(now, nextDt).coerceAtLeast(Duration.ZERO),
            elapsedFraction = fraction
        )
    }

    private data class PeriodQuad(
        val current: PrayerType?,
        val next: PrayerType,
        val start: LocalDateTime,
        val end: LocalDateTime
    )

    private fun julianDate(date: LocalDate): Double {
        var year = date.year
        var month = date.monthValue
        val day = date.dayOfMonth
        if (month <= 2) {
            year -= 1
            month += 12
        }
        val a = floor(year / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dsin(g) + 0.020 * dsin(2.0 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = darcsin(dsin(e) * dsin(l))
        return decl to eqt
    }

    private fun midDay(jd: Double): Double {
        val (_, eqt) = sunPosition(jd)
        return fixHour(12.0 - eqt)
    }

    private fun sunAngleTime(jd: Double, lat: Double, angle: Double, isMorning: Boolean): Double {
        val (decl, _) = sunPosition(jd)
        val noon = midDay(jd)
        val numerator = -dsin(angle) - dsin(decl) * dsin(lat)
        val denominator = dcos(decl) * dcos(lat)
        val ratio = (numerator / denominator).coerceIn(-0.999999, 0.999999)
        val t = darccos(ratio) / 15.0
        return if (isMorning) noon - t else noon + t
    }

    private fun asrTime(jd: Double, lat: Double, shadowFactor: Double): Double {
        val (decl, _) = sunPosition(jd)
        val angle = -darccot(shadowFactor + dtan(abs(lat - decl)))
        return sunAngleTime(jd, lat, angle, isMorning = false)
    }

    private fun hoursToLocalTime(hoursInput: Double): LocalTime {
        val normalized = fixHour(hoursInput)
        val totalMinutes = (normalized * 60.0).roundToInt().coerceIn(0, 1439)
        return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
    }

    private fun dsin(d: Double) = sin(Math.toRadians(d))
    private fun dcos(d: Double) = cos(Math.toRadians(d))
    private fun dtan(d: Double) = tan(Math.toRadians(d))
    private fun darcsin(x: Double) = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun darccos(x: Double) = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun darctan2(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun darccot(x: Double) = Math.toDegrees(atan(1.0 / x))

    private fun fixAngle(a: Double): Double {
        val res = a - 360.0 * floor(a / 360.0)
        return if (res < 0) res + 360.0 else res
    }

    private fun fixHour(h: Double): Double {
        val res = h - 24.0 * floor(h / 24.0)
        return if (res < 0) res + 24.0 else res
    }
}
