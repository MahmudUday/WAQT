package com.example

import com.example.data.BangladeshLocations
import com.example.data.BengaliFormatter
import com.example.data.HijriCalendarHelper
import com.example.data.PrayerTimeCalculator
import com.example.model.AppLanguage
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.PrayerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ExampleUnitTest {

    @Test
    fun `dhaka comilla and sylhet prayer times follow accurate east-west solar order`() {
        val date = LocalDate.of(2026, 3, 15)
        val dhakaZone = ZoneId.of("Asia/Dhaka")

        val dhaka = BangladeshLocations.findById("dhaka")
        val comilla = BangladeshLocations.findById("comilla")
        val sylhet = BangladeshLocations.findById("sylhet")

        val dhakaSchedule = PrayerTimeCalculator.calculateDay(
            date = date,
            latitude = dhaka.latitude,
            longitude = dhaka.longitude,
            locationNameEn = dhaka.nameEn,
            locationNameBn = dhaka.nameBn,
            method = CalculationMethod.KARACHI,
            asrJuristic = AsrJuristic.HANAFI,
            zoneId = dhakaZone
        )
        val comillaSchedule = PrayerTimeCalculator.calculateDay(
            date = date,
            latitude = comilla.latitude,
            longitude = comilla.longitude,
            locationNameEn = comilla.nameEn,
            locationNameBn = comilla.nameBn,
            method = CalculationMethod.KARACHI,
            asrJuristic = AsrJuristic.HANAFI,
            zoneId = dhakaZone
        )
        val sylhetSchedule = PrayerTimeCalculator.calculateDay(
            date = date,
            latitude = sylhet.latitude,
            longitude = sylhet.longitude,
            locationNameEn = sylhet.nameEn,
            locationNameBn = sylhet.nameBn,
            method = CalculationMethod.KARACHI,
            asrJuristic = AsrJuristic.HANAFI,
            zoneId = dhakaZone
        )

        // Strict daily chronological order for each city
        listOf(dhakaSchedule, comillaSchedule, sylhetSchedule).forEach { s ->
            assertTrue(s.fajr.isBefore(s.sunrise))
            assertTrue(s.sunrise.isBefore(s.dhuhr))
            assertTrue(s.dhuhr.isBefore(s.asr))
            assertTrue(s.asr.isBefore(s.maghrib))
            assertTrue(s.maghrib.isBefore(s.isha))
            assertEquals(s.fajr, s.sehriEnd)
            assertEquals(s.maghrib, s.iftarStart)
        }

        // Sylhet (91.87E) and Comilla (91.18E) have solar noon earlier than Dhaka (90.41E)
        assertTrue(sylhetSchedule.dhuhr.isBefore(comillaSchedule.dhuhr))
        assertTrue(comillaSchedule.dhuhr.isBefore(dhakaSchedule.dhuhr))
    }

    @Test
    fun `hanafi asr is later than shafi asr`() {
        val date = LocalDate.of(2026, 6, 15)
        val dhaka = BangladeshLocations.defaultDistrict
        val hanafi = PrayerTimeCalculator.calculateDay(
            date, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.HANAFI, ZoneId.of("Asia/Dhaka")
        )
        val shafi = PrayerTimeCalculator.calculateDay(
            date, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.SHAFI, ZoneId.of("Asia/Dhaka")
        )

        assertTrue(hanafi.asr.isAfter(shafi.asr))
    }

    @Test
    fun `all 64 districts exist and support bengali and english search`() {
        assertEquals(64, BangladeshLocations.allDistricts.size)
        assertTrue(BangladeshLocations.search("কুমিল্লা").any { it.id == "comilla" })
        assertTrue(BangladeshLocations.search("sylhet").any { it.id == "sylhet" })
        val nearestToSylhet = BangladeshLocations.findNearest(24.90, 91.87)
        assertEquals("sylhet", nearestToSylhet.id)
    }

    @Test
    fun `period evaluation transitions cleanly after isha across midnight`() {
        val dhaka = BangladeshLocations.defaultDistrict
        val today = LocalDate.of(2026, 11, 15)
        val tomorrow = today.plusDays(1)
        val zone = ZoneId.of("Asia/Dhaka")

        val todaySched = PrayerTimeCalculator.calculateDay(
            today, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.HANAFI, zone
        )
        val tomorrowSched = PrayerTimeCalculator.calculateDay(
            tomorrow, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.HANAFI, zone
        )

        val lateNight = LocalDateTime.of(today, todaySched.isha.plusMinutes(30))
        val status = PrayerTimeCalculator.evaluatePeriodStatus(lateNight, todaySched, tomorrowSched)
        assertEquals(PrayerType.ISHA, status.currentPrayer)
        assertEquals(PrayerType.FAJR, status.nextPrayer)
        assertEquals(tomorrow, status.nextPrayerDateTime.toLocalDate())
    }

    @Test
    fun `bengali formatter and hijri helper format cleanly without literal escapes`() {
        val date = LocalDate.of(2026, 11, 15)
        val formattedDate = BengaliFormatter.formatDate(date, AppLanguage.BENGALI)
        assertTrue(formattedDate.contains("২০২৬"))
        val hijri = HijriCalendarHelper.fromGregorian(date).format(AppLanguage.BENGALI)
        assertTrue(hijri.contains("হিজরি"))
    }

    @Test
    fun `tahajjud last third and waqt end boundaries are astronomically consistent`() {
        val dhaka = BangladeshLocations.defaultDistrict
        val zone = ZoneId.of("Asia/Dhaka")
        val yesterday = LocalDate.of(2026, 11, 14)
        val today = LocalDate.of(2026, 11, 15)
        val tomorrow = LocalDate.of(2026, 11, 16)

        val ySched = PrayerTimeCalculator.calculateDay(
            yesterday, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.HANAFI, zone
        )
        val tSched = PrayerTimeCalculator.calculateDay(
            today, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.HANAFI, zone
        )
        val tmSched = PrayerTimeCalculator.calculateDay(
            tomorrow, dhaka.latitude, dhaka.longitude, dhaka.nameEn, dhaka.nameBn,
            CalculationMethod.KARACHI, AsrJuristic.HANAFI, zone
        )

        val nightCheck = LocalDateTime.of(today, tSched.isha.plusMinutes(15))
        val tahajjud = PrayerTimeCalculator.calculateTahajjud(nightCheck, ySched, tSched, tmSched)
        val maghribDt = LocalDateTime.of(today, tSched.maghrib)

        assertTrue(maghribDt.isBefore(tahajjud.islamicMidnight))
        assertTrue(tahajjud.islamicMidnight.isBefore(tahajjud.startDateTime))
        assertTrue(tahajjud.startDateTime.isBefore(tahajjud.endDateTime))

        // Fajr ends at Sunrise, Dhuhr ends at Asr, Isha ends at tomorrow Fajr
        assertEquals(LocalDateTime.of(today, tSched.sunrise), tSched.waqtEndDateTimeFor(PrayerType.FAJR, tmSched))
        assertEquals(LocalDateTime.of(today, tSched.asr), tSched.waqtEndDateTimeFor(PrayerType.DHUHR, tmSched))
        assertEquals(LocalDateTime.of(tomorrow, tmSched.fajr), tSched.waqtEndDateTimeFor(PrayerType.ISHA, tmSched))
    }
}
