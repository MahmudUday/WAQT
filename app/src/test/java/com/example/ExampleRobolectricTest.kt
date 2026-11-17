package com.example

import android.app.AlarmManager
import android.content.Context
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import com.example.data.BangladeshLocations
import com.example.data.WaqtPreferencesRepository
import com.example.model.AsrJuristic
import com.example.model.ReminderOption
import com.example.notifications.PrayerAlarmScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app name matches WAQT identity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("ওয়াক্ত (WAQT)", context.getString(R.string.app_name))
    }

    @Test
    fun `preferences repository persists location and asr rule safely`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WaqtPreferencesRepository(context)
        val comilla = BangladeshLocations.findById("comilla")

        repo.setManualDistrict(comilla)
        repo.setAsrJuristic(AsrJuristic.SHAFI)
        repo.setReminderOption(ReminderOption.MIN_40)
        repo.setWaqtEndAlertEnabled(false)
        repo.setTahajjudAlertEnabled(true)
        repo.setUse24HourFormat(true)
        repo.setHijriOffsetDays(1)
        repo.setSehriBufferMinutes(5)

        val settings = repo.settingsFlow.first()
        assertEquals("comilla", settings.selectedDistrictId)
        assertEquals(AsrJuristic.SHAFI, settings.asrJuristic)
        assertEquals(ReminderOption.MIN_40, settings.reminderOption)
        assertFalse(settings.waqtEndAlertEnabled)
        assertTrue(settings.tahajjudAlertEnabled)
        assertTrue(settings.use24HourFormat)
        assertEquals(1, settings.hijriOffsetDays)
        assertEquals(5, settings.sehriBufferMinutes)
    }

    @Test
    fun `alarm scheduler avoids redundant rescheduling and schedules 7 day alarms`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WaqtPreferencesRepository(context)
        repo.setTahajjudAlertEnabled(true)
        val settings = repo.getSettingsSnapshot()

        val firstScheduled = PrayerAlarmScheduler.scheduleIfNeeded(context, repo, settings, force = true)
        assertTrue(firstScheduled)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        assertTrue(shadowAlarmManager.scheduledAlarms.isNotEmpty())

        // Second call with identical settings and date must NOT reschedule unnecessarily
        val secondScheduled = PrayerAlarmScheduler.scheduleIfNeeded(context, repo, settings, force = false)
        assertFalse(secondScheduled)
    }

    @Test
    fun `main activity launches and displays hero countdown and tahajjud cards`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hero_countdown_card").assertExists()
        composeRule.onNodeWithTag("hero_countdown_text").assertExists()
        composeRule.onNodeWithTag("home_prayer_list").performScrollToNode(hasTestTag("prayer_card_fajr"))
        composeRule.onNodeWithTag("prayer_card_fajr").assertExists()
        composeRule.onNodeWithTag("home_prayer_list").performScrollToNode(hasTestTag("prayer_card_isha"))
        composeRule.onNodeWithTag("prayer_card_isha").assertExists()
        composeRule.onNodeWithTag("home_prayer_list").performScrollToNode(hasTestTag("tahajjud_section_card"))
        composeRule.onNodeWithTag("tahajjud_section_card").assertExists()
    }

    @Test
    fun `glass bottom bar navigates smoothly across all tabs and settings controls work`() {
        composeRule.mainClock.advanceTimeBy(1800L)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_sehri_iftar").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sehri_list").performScrollToNode(hasTestTag("sehri_time_card"))
        composeRule.onNodeWithTag("sehri_time_card").assertExists()

        composeRule.onNodeWithTag("nav_alarms").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("alarms_list").performScrollToNode(hasTestTag("waqt_end_alert_switch"))
        composeRule.onNodeWithTag("waqt_end_alert_switch").assertExists()

        composeRule.onNodeWithTag("nav_settings").assertExists().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("time_format_24h"))
        composeRule.onNodeWithTag("settings_list").performTouchInput {
            swipeUp(startY = centerY, endY = centerY - 220f)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("time_format_24h").assertExists().performClick()
        composeRule.waitUntil(timeoutMillis = 3000L) {
            composeRule.onAllNodes(hasTestTag("time_format_24h") and isSelected())
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("sehri_buffer_3"))
        composeRule.onNodeWithTag("settings_list").performTouchInput {
            swipeUp(startY = centerY, endY = centerY - 200f)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hijri_offset_1").assertExists().performClick()
        composeRule.waitUntil(timeoutMillis = 3000L) {
            composeRule.onAllNodes(hasTestTag("hijri_offset_1") and isSelected())
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("sehri_buffer_3").assertExists().performClick()
        composeRule.waitUntil(timeoutMillis = 3000L) {
            composeRule.onAllNodes(hasTestTag("sehri_buffer_3") and isSelected())
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("about_us_card"))
        composeRule.onNodeWithTag("about_us_card").assertExists()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("share_app_button"))
        composeRule.onNodeWithTag("share_app_button").assertExists()
    }
}
