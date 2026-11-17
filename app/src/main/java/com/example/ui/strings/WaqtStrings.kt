package com.example.ui.strings

import androidx.compose.runtime.Immutable
import com.example.data.BengaliFormatter
import com.example.model.AppLanguage
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.NotificationTone
import com.example.model.PrayerType
import com.example.model.ReminderOption
import com.example.model.ThemeMode

@Immutable
class WaqtStrings(val language: AppLanguage) {

    private val bn: Boolean get() = language == AppLanguage.BENGALI

    val appTitle: String get() = if (bn) "ওয়াক্ত" else "WAQT"
    val appSubtitle: String get() = if (bn) "নামাজ, তাহাজ্জুদ ও সেহরি-ইফতার" else "Prayer, Tahajjud & Sehri-Iftar"
    val fullBrandName: String get() = "ওয়াক্ত (WAQT)"
    val splashTagline: String get() = if (bn) "শান্তি ও সময়ের নির্ভুল সঙ্গী" else "Your serene, accurate prayer companion"

    // Navigation Tabs
    val navToday: String get() = if (bn) "ওয়াক্ত" else "Today"
    val navSehriIftar: String get() = if (bn) "সেহরি-ইফতার" else "Sehri & Iftar"
    val navAlarms: String get() = if (bn) "অ্যালার্ম" else "Alarms"
    val navSettings: String get() = if (bn) "সেটিংস" else "Settings"

    // Status Pills
    val offlineCachedBadge: String get() = if (bn) "অফলাইন মোড • সংরক্ষিত ৭ দিনের সূচী" else "Offline Mode • 7-Day Cached Schedule"
    val onlineSyncedBadge: String get() = if (bn) "৭ দিনের সূচী অফলাইনের জন্য সংরক্ষিত" else "7-Day Schedule Saved Offline"
    val ramadanBadge: String get() = if (bn) "রমজানুল মোবারক" else "Ramadan Mubarak"

    // Hero Card
    val currentWaqtLabel: String get() = if (bn) "চলমান ওয়াক্ত" else "Current Period"
    val noFardWaqtLabel: String get() = if (bn) "ইশরাক / নফল সময়" else "Ishraq / Duha"
    val nextPrayerLabel: String get() = if (bn) "পরবর্তী ওয়াক্ত" else "Next Prayer"
    val remainingLabel: String get() = if (bn) "বাকি আছে" else "Remaining"
    val sunriseLabel: String get() = if (bn) "সূর্যোদয়" else "Sunrise"
    val currentBadge: String get() = if (bn) "চলমান" else "NOW"
    val nextBadge: String get() = if (bn) "পরবর্তী" else "NEXT"

    // Tahajjud Section
    val tahajjudSectionTitle: String get() = if (bn) "তাহাজ্জুদ নামাজ" else "Tahajjud Prayer"
    val tahajjudSubtitle: String get() = if (bn) "রাতের শেষ তৃতীয়াংশ (উত্তম সময়)" else "Last Third of the Night (Best Window)"
    val tahajjudActiveNow: String get() = if (bn) "এখন তাহাজ্জুদের সময় চলছে" else "Active Now"
    val tahajjudStartsLabel: String get() = if (bn) "শুরু" else "Starts"
    val tahajjudEndsLabel: String get() = if (bn) "শেষ (ফজর)" else "Ends (Fajr)"
    val islamicMidnightLabel: String get() = if (bn) "অর্ধরাত্রি" else "Midnight"

    // Location & GPS
    val changeLocation: String get() = if (bn) "অবস্থান পরিবর্তন" else "Change Location"
    val selectDistrictTitle: String get() = if (bn) "জেলা নির্বাচন করুন" else "Select District"
    val searchDistrictHint: String get() = if (bn) "জেলার নাম লিখুন (যেমন: কুমিল্লা, সিলেট, ঢাকা)..." else "Search district (e.g. Comilla, Sylhet, Dhaka)..."
    val detectGpsButton: String get() = if (bn) "জিপিএস (GPS) দিয়ে অবস্থান নিন" else "Detect Location via GPS"
    val detectingGps: String get() = if (bn) "অবস্থান নির্ণয় করা হচ্ছে..." else "Detecting location..."
    val gpsDeniedMessage: String get() = if (bn) "লোকেশন অনুমতি দেওয়া হয়নি। আপনি তালিকা থেকে ম্যানুয়ালি যেকোনো জেলা বেছে নিতে পারবেন।" else "Location permission denied. You can select any Bangladesh district manually below."
    val gpsProviderDisabledMessage: String get() = if (bn) "ফোনের জিপিএস/লোকেশন বন্ধ আছে। চালু করুন অথবা নিচ থেকে জেলা বেছে নিন।" else "Device GPS/Location is turned off. Enable it or select a district below."
    val gpsUnavailableMessage: String get() = if (bn) "এই মুহূর্তে জিপিএস সিগন্যাল পাওয়া যায়নি। নিচ থেকে আপনার জেলা নির্বাচন করুন।" else "GPS signal unavailable right now. Please pick your district below."

    // Sehri & Iftar Section
    val sehriIftarTitle: String get() = if (bn) "সেহরি ও ইফতারের সময়সূচী" else "Sehri & Iftar Schedule"
    val sehriIftarSubtitle: String get() = if (bn) "সারা বছরের যেকোনো তারিখ ও মাসের জন্য" else "Accurate year-round for any date & month"
    val sehriEndLabel: String get() = if (bn) "সেহরির শেষ সময়" else "Sehri Ends"
    val sehriCutoffNote: String get() = if (bn) "ফজরের ওয়াক্ত শুরুর সাথে সেহরির সময় শেষ হয়" else "Fasting cutoff coincides with Fajr start"
    val iftarStartLabel: String get() = if (bn) "ইফতারের সময়" else "Iftar Starts"
    val iftarCutoffNote: String get() = if (bn) "মাগরিবের ওয়াক্ত শুরুর সাথে ইফতারের সময় হয়" else "Iftar begins at Maghrib sunset"
    val todayButton: String get() = if (bn) "আজ" else "Today"
    val previousDayDesc: String get() = if (bn) "আগের দিন" else "Previous day"
    val nextDayDesc: String get() = if (bn) "পরের দিন" else "Next day"
    val previousMonthDesc: String get() = if (bn) "আগের মাস" else "Previous month"
    val nextMonthDesc: String get() = if (bn) "পরের মাস" else "Next month"
    val monthlyTimetableTitle: String get() = if (bn) "মাসিক সেহরি ও ইফতার তালিকা" else "Monthly Sehri & Iftar Timetable"
    val colDate: String get() = if (bn) "তারিখ" else "Date"
    val colSehri: String get() = if (bn) "সেহরি শেষ" else "Sehri End"
    val colIftar: String get() = if (bn) "ইফতার" else "Iftar"
    val sehriCountdownPrefix: String get() = if (bn) "সেহরির বাকি" else "Time to Sehri"
    val iftarCountdownPrefix: String get() = if (bn) "ইফতারের বাকি" else "Time to Iftar"

    // Alarms & Notifications Screen
    val alarmsTitle: String get() = if (bn) "নামাজের অ্যালার্ম ও রিমাইন্ডার" else "Prayer Alarms & Reminders"
    val alarmsSubtitle: String get() = if (bn) "ওয়াক্ত শুরুতে অ্যালার্ম ও শেষ হওয়ার ২০ মিনিট আগে সতর্কতা" else "Alerts at Waqt start and 20 min before Waqt ends"
    val notificationPermissionTitle: String get() = if (bn) "নোটিফিকেশন অনুমতি প্রয়োজন" else "Notification Permission Needed"
    val notificationPermissionBody: String get() = if (bn) "নামাজের সময় নোটিফিকেশন ও অ্যালার্ম পেতে অনুমতি দিন।" else "Allow notifications to receive timely prayer alerts."
    val grantPermissionButton: String get() = if (bn) "অনুমতি দিন" else "Allow"
    val exactAlarmWarningTitle: String get() = if (bn) "সঠিক সময়ে অ্যালার্মের অনুমতি" else "Exact Alarm Access"
    val exactAlarmWarningBody: String get() = if (bn) "ঠিক সময়ে আযান অ্যালার্ম পেতে অ্যান্ড্রয়েড অ্যালার্ম অনুমতি সক্রিয় করুন।" else "Enable exact alarms in system settings for punctual prayer alerts."
    val openSettingsButton: String get() = if (bn) "সেটিংস খুলুন" else "Open Settings"
    val batteryTipTitle: String get() = if (bn) "ব্যাকগ্রাউন্ড সুরক্ষা" else "Background Reliability"
    val batteryTipBody: String get() = if (bn) "অ্যাপ বন্ধ থাকলেও অ্যালার্ম চালু রাখতে ব্যাটারি অপ্টিমাইজেশন থেকে WAQT-কে আনরেস্ট্রিক্টেড রাখুন।" else "Set battery usage to Unrestricted so alarms fire reliably while closed."
    val prayerTogglesHeader: String get() = if (bn) "ওয়াক্ত ভিত্তিক অ্যালার্ম (শুরু হলেই বাজবে)" else "Prayer Start Alarms"
    val waqtEndAlertTitle: String get() = if (bn) "ওয়াক্ত শেষ হওয়ার ২০ মিনিট আগে সতর্কতা" else "20-Min Before Waqt Ends Alert"
    val waqtEndAlertDesc: String get() = if (bn) "প্রতিটি ওয়াক্ত শেষ হওয়ার ২০ মিনিট আগে আলাদা নোটিফিকেশন পাবেন" else "Sends a reminder 20 minutes before the current prayer window closes"
    val tahajjudAlertTitle: String get() = if (bn) "তাহাজ্জুদ অ্যালার্ম" else "Tahajjud Alarm"
    val tahajjudAlertDesc: String get() = if (bn) "রাতের শেষ তৃতীয়াংশ শুরু হলে জাগিয়ে দেওয়ার অ্যালার্ম" else "Alarm when the last third of the night begins"
    val reminderBeforeHeader: String get() = if (bn) "ওয়াক্ত শুরুর আগে অতিরিক্ত রিমাইন্ডার" else "Pre-Start Reminder"
    val toneHeader: String get() = if (bn) "অ্যালার্ম ও নোটিফিকেশন শব্দ" else "Alarm Sound & Vibration"
    val testNotificationButton: String get() = if (bn) "অ্যালার্ম সাউন্ড ও নোটিফিকেশন টেস্ট করুন" else "Test Alarm Sound & Notification"
    val testNotificationSent: String get() = if (bn) "টেস্ট অ্যালার্ম বাজানো হয়েছে" else "Test alarm triggered"
    val alarmScheduledSummary: String get() = if (bn) "৭ দিনের অ্যালার্ম ও ২০ মি. সমাপ্তি সতর্কতা সক্রিয়" else "7-day Waqt start & 20m closing alerts active"

    // Settings & About
    val settingsTitle: String get() = if (bn) "সেটিংস ও অ্যাপ তথ্য" else "Settings & About"
    val appearanceSection: String get() = if (bn) "ভাষা, থিম ও ডিসপ্লে" else "Language, Theme & Display"
    val languageLabel: String get() = if (bn) "ভাষা" else "Language"
    val themeLabel: String get() = if (bn) "অ্যাপ থিম (লাইট / ডার্ক / সিস্টেম)" else "App Theme (Light / Dark / System)"
    val timeFormatLabel: String get() = if (bn) "সময় প্রদর্শন ফরম্যাট" else "Clock Format"
    val timeFormat12h: String get() = if (bn) "১২ ঘণ্টা (AM/PM)" else "12-Hour (AM/PM)"
    val timeFormat24h: String get() = if (bn) "২৪ ঘণ্টা" else "24-Hour"
    val reduceMotionLabel: String get() = if (bn) "অ্যানিমেশন কমান (Reduce Motion)" else "Reduce Motion"
    val reduceMotionDesc: String get() = if (bn) "সহজ পাঠ ও ব্যাটারি সাশ্রয়ের জন্য" else "Minimal transitions for accessibility"
    val calendarPreferencesSection: String get() = if (bn) "হিজরি ও সেহরি সমন্বয়" else "Hijri & Sehri Preferences"
    val hijriAdjustLabel: String get() = if (bn) "হিজরি তারিখ সমন্বয় (চাঁদ দেখা)" else "Hijri Date Adjustment (Moon Sighting)"
    val hijriAdjustDesc: String get() = if (bn) "চাঁদ দেখার ভিত্তিতে হিজরি তারিখ ±১ বা ±২ দিন ঠিক করুন" else "Shift Hijri lunar date by ±1 or ±2 days if needed"
    val sehriBufferLabel: String get() = if (bn) "সেহরি সতর্কতামূলক সময় (ইহতিয়াত)" else "Sehri Caution Buffer"
    val sehriBufferDesc: String get() = if (bn) "ফজরের আগে নিরাপদ সতর্কতা হিসেবে সেহরির শেষ সময় এগিয়ে আনুন" else "Set how many minutes before Fajr to stop Sehri"
    val noBufferLabel: String get() = if (bn) "ফজর পর্যন্ত (০ মি.)" else "At Fajr (0m)"
    fun bufferMinutesLabel(mins: Int): String = if (bn) "${BengaliFormatter.formatInt(mins, language)} মি. আগে" else "${mins}m before"
    fun hijriOffsetLabel(offset: Int): String = when {
        offset == 0 -> if (bn) "স্বাভাবিক (০)" else "Exact (0)"
        else -> if (bn) "${BengaliFormatter.formatSignedInt(offset, language)} দিন" else "${BengaliFormatter.formatSignedInt(offset, language)}d"
    }
    val calculationSection: String get() = if (bn) "হিসাব পদ্ধতি ও ফিকহ" else "Prayer Calculation & Fiqh"
    val methodLabel: String get() = if (bn) "হিসাব পদ্ধতি" else "Calculation Method"
    val asrRuleLabel: String get() = if (bn) "আসরের ওয়াক্ত নিয়ম" else "Asr Juristic Rule"
    val calculationDocNote: String get() = if (bn) {
        "বাংলাদেশের জন্য করাচি পদ্ধতি (ফজর ১৮°, এশা ১৮°) ও হানাফী আসর (২ গুণ ছায়া) ডিফল্ট হিসেবে ব্যবহৃত হয়।"
    } else {
        "Default for Bangladesh: University of Islamic Sciences, Karachi (18° Fajr / 18° Isha) with Hanafi Asr (2x shadow)."
    }

    // Enhanced About Us (Preserving approved brand & developer identity)
    val aboutHeader: String get() = if (bn) "আমাদের সম্পর্কে" else "About Us"
    val aboutDescription: String get() = if (bn) {
        "ওয়াক্ত (WAQT) বাংলাদেশের মুসলিমদের জন্য তৈরি একটি নির্ভুল, হালকা, বিজ্ঞাপনমুক্ত এবং সম্পূর্ণ অফলাইন-সমর্থিত ইসলামিক নামাজ, তাহাজ্জুদ ও সেহরি-ইফতার সঙ্গী।"
    } else {
        "WAQT is a lightweight, accurate, ad-free, 100% offline-first Islamic prayer, Tahajjud, Sehri & Iftar companion crafted for Bangladesh."
    }
    val aboutPrivacyBadge: String get() = if (bn) "১০০% অফলাইন • বিজ্ঞাপনমুক্ত • সম্পূর্ণ প্রাইভেট" else "100% Offline • Ad-Free • Zero Tracking"
    val aboutFeatureOffline: String get() = if (bn) "৬৪ জেলা ও জিপিএস" else "64 Districts + GPS"
    val aboutFeatureTahajjud: String get() = if (bn) "তাহাজ্জুদ ও সেহরি-ইফতার" else "Tahajjud & Sehri-Iftar"
    val aboutFeatureSmartAlert: String get() = if (bn) "২০ মি. সমাপ্তি সতর্কতা" else "20m Ending Alerts"
    val shareAppLabel: String get() = if (bn) "অ্যাপটি শেয়ার করুন" else "Share WAQT App"
    val shareAppMessage: String get() = if (bn) {
        "ওয়াক্ত (WAQT) — বাংলাদেশের ৬৪ জেলার নির্ভুল অফলাইন নামাজ, তাহাজ্জুদ ও সেহরি-ইফতার অ্যাপ: https://github.com/MahmudUday/namaz-app"
    } else {
        "WAQT — Accurate offline Prayer, Tahajjud & Sehri-Iftar times for Bangladesh: https://github.com/MahmudUday/namaz-app"
    }
    val developerLabel: String get() = if (bn) "ডেভেলপার" else "Developer"
    val developerName: String get() = "Mahmud Uday"
    val repositoryLabel: String get() = if (bn) "সোর্স ও সাপোর্ট (GitHub)" else "GitHub Repository"
    val contactLabel: String get() = if (bn) "ইমেইল যোগাযোগ" else "Contact Email"
    val versionLabel: String get() = if (bn) "সংস্করণ" else "Version"

    fun prayerName(prayer: PrayerType): String = if (bn) {
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

    fun prayerSubLabel(prayer: PrayerType): String = if (bn) {
        when (prayer) {
            PrayerType.FAJR -> "ভোর ও সেহরির শেষ"
            PrayerType.DHUHR -> "দুপুরের নামাজ"
            PrayerType.ASR -> "বিকালের নামাজ"
            PrayerType.MAGHRIB -> "সূর্যাস্ত ও ইফতার"
            PrayerType.ISHA -> "রাতের নামাজ"
        }
    } else {
        when (prayer) {
            PrayerType.FAJR -> "Dawn • Sehri End"
            PrayerType.DHUHR -> "Midday Prayer"
            PrayerType.ASR -> "Afternoon Prayer"
            PrayerType.MAGHRIB -> "Sunset • Iftar"
            PrayerType.ISHA -> "Night Prayer"
        }
    }

    fun reminderLabel(option: ReminderOption): String = if (bn) {
        when (option) {
            ReminderOption.NONE -> "বন্ধ"
            ReminderOption.MIN_5 -> "৫ মিনিট আগে"
            ReminderOption.MIN_20 -> "২০ মিনিট আগে"
            ReminderOption.MIN_40 -> "৪০ মিনিট আগে"
        }
    } else {
        when (option) {
            ReminderOption.NONE -> "Off"
            ReminderOption.MIN_5 -> "5 min before"
            ReminderOption.MIN_20 -> "20 min before"
            ReminderOption.MIN_40 -> "40 min before"
        }
    }

    fun toneLabel(tone: NotificationTone): String = if (bn) {
        when (tone) {
            NotificationTone.ADHAN_TONE -> "উচ্চ অ্যালার্ম টোন ও ভাইব্রেশন"
            NotificationTone.GENTLE_CHIME -> "সৌম্য নোটিফিকেশন টোন"
            NotificationTone.SILENT_VIBRATE -> "শুধু ভাইব্রেশন"
        }
    } else {
        when (tone) {
            NotificationTone.ADHAN_TONE -> "Loud Alarm Tone + Vibration"
            NotificationTone.GENTLE_CHIME -> "Gentle Chime"
            NotificationTone.SILENT_VIBRATE -> "Vibrate Only"
        }
    }

    fun themeModeLabel(mode: ThemeMode): String = if (bn) {
        when (mode) {
            ThemeMode.SYSTEM -> "সিস্টেম"
            ThemeMode.LIGHT -> "লাইট (হালকা)"
            ThemeMode.DARK -> "ডার্ক (গাঢ়)"
        }
    } else {
        when (mode) {
            ThemeMode.SYSTEM -> "System"
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
        }
    }

    fun methodLabel(method: CalculationMethod): String = if (bn) {
        when (method) {
            CalculationMethod.KARACHI -> "করাচি / বাংলাদেশ মান (১৮° / ১৮°)"
            CalculationMethod.MWL -> "মুসলিম ওয়ার্ল্ড লিগ (১৮° / ১৭°)"
            CalculationMethod.ISNA -> "উত্তর আমেরিকা - ISNA (১৫° / ১৫°)"
            CalculationMethod.UMM_AL_QURA -> "উম্মুল কুরা (১৮.৫° / ৯০ মি.)"
        }
    } else {
        when (method) {
            CalculationMethod.KARACHI -> "Karachi / Bangladesh (18° / 18°)"
            CalculationMethod.MWL -> "Muslim World League (18° / 17°)"
            CalculationMethod.ISNA -> "ISNA (15° / 15°)"
            CalculationMethod.UMM_AL_QURA -> "Umm al-Qura (18.5° / 90m)"
        }
    }

    fun asrRuleLabel(rule: AsrJuristic): String = if (bn) {
        when (rule) {
            AsrJuristic.HANAFI -> "হানাফী (২ গুণ ছায়া)"
            AsrJuristic.SHAFI -> "শাফেয়ী / স্ট্যান্ডার্ড (১ গুণ)"
        }
    } else {
        when (rule) {
            AsrJuristic.HANAFI -> "Hanafi (2x shadow)"
            AsrJuristic.SHAFI -> "Shafi'i / Standard (1x)"
        }
    }
}
