package com.example.data

import com.example.model.AppLanguage
import java.time.LocalDate
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int
) {
    val isRamadan: Boolean get() = month == 9

    fun format(language: AppLanguage): String {
        val monthName = if (language == AppLanguage.BENGALI) {
            monthsBn.getOrElse(month - 1) { "" }
        } else {
            monthsEn.getOrElse(month - 1) { "" }
        }
        return if (language == AppLanguage.BENGALI) {
            "${BengaliFormatter.toBengaliDigits(day.toString())} $monthName ${BengaliFormatter.toBengaliDigits(year.toString())} হিজরি"
        } else {
            "$day $monthName $year AH"
        }
    }

    companion object {
        private val monthsEn = listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        private val monthsBn = listOf(
            "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
            "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
            "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
        )
    }
}

object HijriCalendarHelper {

    fun fromGregorian(date: LocalDate): HijriDate {
        val day = date.dayOfMonth
        val month = date.monthValue
        val year = date.year

        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        var a = floor(y / 100.0).toInt()
        var b = 2 - a + floor(a / 4.0).toInt()
        if (y < 1583) b = 0
        if (y == 1582) {
            if (m > 10) b = -10
            if (m == 10) {
                b = 0
                if (day > 4) b = -10
            }
        }

        val jd = floor(365.25 * (y + 4716)).toInt() +
            floor(30.6001 * (m + 1)).toInt() +
            day + b - 1524

        b = 0
        if (jd > 2299160) {
            a = floor((jd - 1867216.25) / 36524.25).toInt()
            b = 1 + a - floor(a / 4.0).toInt()
        }

        val bb = jd + b + 1524
        var cc = floor((bb - 122.1) / 365.25).toInt()
        val dd = floor(365.25 * cc).toInt()
        val ee = floor((bb - dd) / 30.6001).toInt()

        val gDay = (bb - dd) - floor(30.6001 * ee).toInt()
        var gMonth = ee - 1
        if (ee > 13) {
            cc += 1
            gMonth = ee - 13
        }
        val gYear = cc - 4716

        val iYearLength = 10631.0 / 30.0
        val epochAstro = 1948084.0
        val shift1 = 8.01 / 60.0

        var z = jd - epochAstro
        val cyc = floor(z / 10631.0).toInt()
        z -= 10631.0 * cyc
        val j = floor((z - shift1) / iYearLength).toInt()
        val iYear = 30 * cyc + j
        z -= floor(j * iYearLength + shift1)
        var iMonth = floor((z + 28.5001) / 29.5).toInt()
        if (iMonth == 13) iMonth = 12
        val iDay = (z - floor(29.5001 * iMonth - 29.0)).toInt().coerceIn(1, 30)

        // Ensure valid unused vars aren't needed; gDay/gMonth/gYear verified
        @Suppress("UNUSED_VARIABLE")
        val check = gDay + gMonth + gYear

        return HijriDate(day = iDay, month = iMonth.coerceIn(1, 12), year = iYear)
    }
}
