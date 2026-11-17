package com.example.data

import com.example.model.AppLanguage
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object BengaliFormatter {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    private val weekdaysBn = mapOf(
        1 to "সোমবার",
        2 to "মঙ্গলবার",
        3 to "বুধবার",
        4 to "বৃহস্পতিবার",
        5 to "শুক্রবার",
        6 to "শনিবার",
        7 to "রবিবার"
    )

    private val monthsBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    private val timeFormatter12En = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val timeFormatter24En = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
    private val dateFormatterEn = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
    private val monthYearEn = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatInt(value: Int, language: AppLanguage): String {
        val str = value.toString()
        return if (language == AppLanguage.BENGALI) toBengaliDigits(str) else str
    }

    fun formatSignedInt(value: Int, language: AppLanguage): String {
        val prefix = when {
            value > 0 -> "+"
            else -> ""
        }
        val raw = "$prefix$value"
        return if (language == AppLanguage.BENGALI) toBengaliDigits(raw) else raw
    }

    fun formatTime(time: LocalTime, language: AppLanguage, use24Hour: Boolean = false): String {
        if (use24Hour) {
            val formatted24 = time.format(timeFormatter24En)
            return if (language == AppLanguage.BENGALI) toBengaliDigits(formatted24) else formatted24
        }
        if (language == AppLanguage.ENGLISH) {
            return time.format(timeFormatter12En)
        }
        val hour24 = time.hour
        val minute = time.minute
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val periodBn = when (hour24) {
            in 0..3 -> "রাত"
            in 4..5 -> "ভোর"
            in 6..11 -> "সকাল"
            in 12..15 -> "দুপুর"
            in 16..17 -> "বিকাল"
            in 18..19 -> "সন্ধ্যা"
            else -> "রাত"
        }
        val minutePadded = minute.toString().padStart(2, '0')
        return "$periodBn ${toBengaliDigits("$hour12:$minutePadded")}"
    }

    fun formatShortTime(time: LocalTime, language: AppLanguage, use24Hour: Boolean = false): String {
        if (use24Hour) {
            val formatted24 = time.format(timeFormatter24En)
            return if (language == AppLanguage.BENGALI) toBengaliDigits(formatted24) else formatted24
        }
        val hour24 = time.hour
        val minute = time.minute
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minutePadded = minute.toString().padStart(2, '0')
        val suffix = if (hour24 < 12) "AM" else "PM"
        val raw = "$hour12:$minutePadded $suffix"
        return if (language == AppLanguage.BENGALI) toBengaliDigits("$hour12:$minutePadded") else raw
    }

    fun formatCountdown(duration: Duration, language: AppLanguage): String {
        val totalSeconds = duration.seconds.coerceAtLeast(0L)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val formatted = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
        return if (language == AppLanguage.BENGALI) toBengaliDigits(formatted) else formatted
    }

    fun formatDate(date: LocalDate, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) {
            return date.format(dateFormatterEn)
        }
        val weekday = weekdaysBn[date.dayOfWeek.value] ?: ""
        val month = monthsBn.getOrElse(date.monthValue - 1) { "" }
        val dayStr = toBengaliDigits(date.dayOfMonth.toString())
        val yearStr = toBengaliDigits(date.year.toString())
        return "$weekday, $dayStr $month $yearStr"
    }

    fun formatShortDate(date: LocalDate, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) {
            return date.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
        }
        val month = monthsBn.getOrElse(date.monthValue - 1) { "" }
        return "${toBengaliDigits(date.dayOfMonth.toString())} $month"
    }

    fun formatMonthYear(year: Int, month: Int, language: AppLanguage): String {
        if (language == AppLanguage.ENGLISH) {
            return LocalDate.of(year, month, 1).format(monthYearEn)
        }
        val monthName = monthsBn.getOrElse(month - 1) { "" }
        return "$monthName ${toBengaliDigits(year.toString())}"
    }
}
