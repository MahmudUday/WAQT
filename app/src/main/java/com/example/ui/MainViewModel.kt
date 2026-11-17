package com.example.ui

import android.app.Application
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BangladeshLocations
import com.example.data.CachedScheduleBundle
import com.example.data.ConnectivityObserver
import com.example.data.GpsLocationResult
import com.example.data.HijriCalendarHelper
import com.example.data.HijriDate
import com.example.data.LocationHelper
import com.example.data.PrayerPeriodStatus
import com.example.data.PrayerTimeCalculator
import com.example.data.WaqtPreferencesRepository
import com.example.model.AppLanguage
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.DailyPrayerSchedule
import com.example.model.DistrictLocation
import com.example.model.NotificationTone
import com.example.model.PrayerType
import com.example.model.ReminderOption
import com.example.model.TahajjudSchedule
import com.example.model.ThemeMode
import com.example.model.UserSettings
import androidx.compose.runtime.Immutable
import com.example.notifications.NotificationHelper
import com.example.notifications.PrayerAlarmScheduler
import com.example.ui.strings.WaqtStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

@Immutable
data class SehriIftarTargetCountdown(
    val isSehriNext: Boolean,
    val countdown: Duration
)

/**
 * Isolated 1-second tick state so only countdown text/bars recompose each second,
 * keeping LazyColumns and animations lag-free at 120Hz/144Hz.
 */
@Immutable
data class LiveClockState(
    val periodStatus: PrayerPeriodStatus? = null,
    val sehriIftarCountdown: SehriIftarTargetCountdown? = null,
    val isTahajjudActiveNow: Boolean = false
)

@Immutable
data class WaqtUiState(
    val settings: UserSettings = UserSettings(),
    val isOnline: Boolean = true,
    val todayDate: LocalDate = LocalDate.now(),
    val hijriDate: HijriDate = HijriCalendarHelper.fromGregorian(LocalDate.now()),
    val todaySchedule: DailyPrayerSchedule? = null,
    val tomorrowSchedule: DailyPrayerSchedule? = null,
    val tahajjudSchedule: TahajjudSchedule? = null,
    val cachedSchedules: List<DailyPrayerSchedule> = emptyList(),
    val isUsingCachedFallback: Boolean = false,
    val selectedSehriDate: LocalDate = LocalDate.now(),
    val selectedSehriSchedule: DailyPrayerSchedule? = null,
    val selectedSehriMonth: YearMonth = YearMonth.now(),
    val monthlySehriSchedules: List<DailyPrayerSchedule> = emptyList(),
    val isDetectingGps: Boolean = false,
    val gpsMessage: String? = null,
    val hasNotificationPermission: Boolean = true,
    val canScheduleExactAlarms: Boolean = true,
    val isIgnoringBatteryOptimizations: Boolean = true,
    val showSplash: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val repository = WaqtPreferencesRepository(appContext)

    private val _uiState = MutableStateFlow(createInitialUiState())
    val uiState: StateFlow<WaqtUiState> = _uiState.asStateFlow()

    private val _liveClock = MutableStateFlow(createInitialClockState(_uiState.value))
    val liveClock: StateFlow<LiveClockState> = _liveClock.asStateFlow()

    private var lastScheduleKey: String? = null
    private var lastCacheSavedKey: String? = null
    private var lastMonthlyKey: String? = null

    init {
        NotificationHelper.ensureChannels(appContext)
        refreshSystemPermissionStates()
        observeSettingsAndConnectivity()
        startClockTicker()
    }

    private fun createInitialUiState(): WaqtUiState {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)
        val today = now.toLocalDate()
        val settings = UserSettings()
        val yesterdaySched = PrayerTimeCalculator.calculateDay(
            today.minusDays(1), settings.latitude, settings.longitude,
            settings.locationNameEn, settings.locationNameBn,
            settings.calculationMethod, settings.asrJuristic, zoneId
        )
        val todaySched = PrayerTimeCalculator.calculateDay(
            today, settings.latitude, settings.longitude,
            settings.locationNameEn, settings.locationNameBn,
            settings.calculationMethod, settings.asrJuristic, zoneId
        )
        val tomorrowSched = PrayerTimeCalculator.calculateDay(
            today.plusDays(1), settings.latitude, settings.longitude,
            settings.locationNameEn, settings.locationNameBn,
            settings.calculationMethod, settings.asrJuristic, zoneId
        )
        val tahajjud = PrayerTimeCalculator.calculateTahajjud(now, yesterdaySched, todaySched, tomorrowSched)
        return WaqtUiState(
            settings = settings,
            todayDate = today,
            hijriDate = HijriCalendarHelper.fromGregorian(today),
            todaySchedule = todaySched,
            tomorrowSchedule = tomorrowSched,
            tahajjudSchedule = tahajjud,
            selectedSehriDate = today,
            selectedSehriSchedule = todaySched
        )
    }

    private fun createInitialClockState(state: WaqtUiState): LiveClockState {
        val todaySched = state.todaySchedule ?: return LiveClockState()
        val tomorrowSched = state.tomorrowSchedule ?: todaySched
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val period = PrayerTimeCalculator.evaluatePeriodStatus(now, todaySched, tomorrowSched)
        val tahajjudActive = state.tahajjudSchedule?.let {
            !now.isBefore(it.startDateTime) && now.isBefore(it.endDateTime)
        } ?: false
        return LiveClockState(
            periodStatus = period,
            sehriIftarCountdown = computeSehriIftarCountdown(
                now = now,
                today = todaySched,
                tomorrow = tomorrowSched,
                sehriBufferMinutes = state.settings.sehriBufferMinutes
            ),
            isTahajjudActiveNow = tahajjudActive
        )
    }

    fun dismissSplash() {
        _uiState.update { it.copy(showSplash = false) }
    }

    fun refreshSystemPermissionStates() {
        val hasNotif = NotificationHelper.hasNotificationPermission(appContext)
        val exactAlarm = PrayerAlarmScheduler.canScheduleExactAlarms(appContext)
        val powerManager = appContext.getSystemService(PowerManager::class.java)
        val batteryIgnored = powerManager?.isIgnoringBatteryOptimizations(appContext.packageName) ?: true
        _uiState.update {
            it.copy(
                hasNotificationPermission = hasNotif,
                canScheduleExactAlarms = exactAlarm,
                isIgnoringBatteryOptimizations = batteryIgnored
            )
        }
    }

    private fun observeSettingsAndConnectivity() {
        viewModelScope.launch {
            val cachedBundle = runCatching {
                repository.getCachedSchedulesSnapshot()
            }.getOrElse { CachedScheduleBundle(emptyList(), 0L) }

            combine(
                repository.settingsFlow.distinctUntilChanged(),
                ConnectivityObserver.observeOnlineStatus(appContext).distinctUntilChanged()
            ) { settings, isOnline ->
                settings to isOnline
            }.collect { (settings, isOnline) ->
                recomputeSchedulesIfNeeded(settings, cachedBundle, isOnline)
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                val nowMs = System.currentTimeMillis()
                val zoneId = ZoneId.systemDefault()
                val now = LocalDateTime.now(zoneId)
                val currentDate = now.toLocalDate()
                val state = _uiState.value

                if (currentDate != state.todayDate || state.todaySchedule == null) {
                    val freshBundle = CachedScheduleBundle(state.cachedSchedules, nowMs)
                    recomputeSchedulesIfNeeded(state.settings, freshBundle, state.isOnline)
                } else {
                    val todaySched = state.todaySchedule
                    val tomorrowSched = state.tomorrowSchedule ?: todaySched
                    val periodStatus = PrayerTimeCalculator.evaluatePeriodStatus(
                        now = now,
                        todaySchedule = todaySched,
                        tomorrowSchedule = tomorrowSched
                    )
                    val sehriIftarCountdown = computeSehriIftarCountdown(
                        now = now,
                        today = todaySched,
                        tomorrow = tomorrowSched,
                        sehriBufferMinutes = state.settings.sehriBufferMinutes
                    )
                    val tahajjudActive = state.tahajjudSchedule?.let {
                        !now.isBefore(it.startDateTime) && now.isBefore(it.endDateTime)
                    } ?: false

                    _liveClock.value = LiveClockState(
                        periodStatus = periodStatus,
                        sehriIftarCountdown = sehriIftarCountdown,
                        isTahajjudActiveNow = tahajjudActive
                    )
                }
                val delayMs = (1000L - (System.currentTimeMillis() % 1000L)).coerceIn(250L, 1000L)
                delay(delayMs)
            }
        }
    }

    private suspend fun recomputeSchedulesIfNeeded(
        settings: UserSettings,
        cacheBundle: CachedScheduleBundle,
        isOnline: Boolean
    ) {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)
        val today = now.toLocalDate()
        val scheduleKey = "$today|${settings.selectedDistrictId}|${settings.latitude}|${settings.longitude}|" +
            "${settings.locationNameEn}|${settings.calculationMethod.id}|${settings.asrJuristic.id}|${zoneId.id}"

        val currentState = _uiState.value
        val sehriDate = currentState.selectedSehriDate
        val sehriMonth = currentState.selectedSehriMonth
        val monthlyKey = "$sehriMonth|${settings.latitude}|${settings.longitude}|${settings.calculationMethod.id}|${settings.asrJuristic.id}|${zoneId.id}"

        val needsScheduleRecompute = scheduleKey != lastScheduleKey || currentState.todaySchedule == null
        val needsMonthlyRecompute = monthlyKey != lastMonthlyKey || currentState.monthlySehriSchedules.isEmpty()

        val todaySched: DailyPrayerSchedule
        val tomorrowSched: DailyPrayerSchedule
        val tahajjudSched: TahajjudSchedule
        val activeSevenDays: List<DailyPrayerSchedule>
        val usingCache: Boolean
        val selectedSehriSchedule: DailyPrayerSchedule

        if (needsScheduleRecompute) {
            val computed = withContext(Dispatchers.Default) {
                runCatching {
                    PrayerTimeCalculator.calculateRange(
                        startDate = today,
                        days = 7,
                        latitude = settings.latitude,
                        longitude = settings.longitude,
                        locationNameEn = settings.locationNameEn,
                        locationNameBn = settings.locationNameBn,
                        method = settings.calculationMethod,
                        asrJuristic = settings.asrJuristic,
                        zoneId = zoneId
                    )
                }.getOrNull()
            }

            val matchingCache = cacheBundle.schedules.filter {
                !it.date.isBefore(today) &&
                    it.methodId == settings.calculationMethod.id &&
                    it.asrJuristicId == settings.asrJuristic.id
            }

            if (computed != null && computed.isNotEmpty()) {
                activeSevenDays = if (!isOnline && matchingCache.any { it.date == today }) {
                    computed.map { it.copy(isFromCache = true) }
                } else {
                    computed
                }
                usingCache = !isOnline
                if (lastCacheSavedKey != scheduleKey) {
                    lastCacheSavedKey = scheduleKey
                    viewModelScope.launch(Dispatchers.IO) {
                        runCatching { repository.saveCachedSchedules(computed) }
                    }
                }
            } else {
                activeSevenDays = matchingCache
                usingCache = true
            }

            todaySched = activeSevenDays.firstOrNull { it.date == today }
                ?: calculateSingleDay(today, settings, zoneId)
            tomorrowSched = activeSevenDays.firstOrNull { it.date == today.plusDays(1) }
                ?: calculateSingleDay(today.plusDays(1), settings, zoneId)
            val yesterdaySched = calculateSingleDay(today.minusDays(1), settings, zoneId)

            tahajjudSched = PrayerTimeCalculator.calculateTahajjud(
                now = now,
                yesterdaySchedule = yesterdaySched,
                todaySchedule = todaySched,
                tomorrowSchedule = tomorrowSched
            )
            selectedSehriSchedule = calculateSingleDay(sehriDate, settings, zoneId)
            lastScheduleKey = scheduleKey
        } else {
            activeSevenDays = currentState.cachedSchedules
            usingCache = !isOnline
            todaySched = currentState.todaySchedule!!
            tomorrowSched = currentState.tomorrowSchedule ?: todaySched
            tahajjudSched = currentState.tahajjudSchedule ?: PrayerTimeCalculator.calculateTahajjud(
                now = now,
                yesterdaySchedule = todaySched,
                todaySchedule = todaySched,
                tomorrowSchedule = tomorrowSched
            )
            selectedSehriSchedule = currentState.selectedSehriSchedule ?: todaySched
        }

        val monthlySehriSchedules = if (needsMonthlyRecompute) {
            lastMonthlyKey = monthlyKey
            calculateMonth(sehriMonth, settings, zoneId)
        } else {
            currentState.monthlySehriSchedules
        }

        _uiState.update {
            it.copy(
                settings = settings,
                isOnline = isOnline,
                todayDate = today,
                hijriDate = HijriCalendarHelper.fromGregorian(today, settings.hijriOffsetDays),
                todaySchedule = todaySched,
                tomorrowSchedule = tomorrowSched,
                tahajjudSchedule = tahajjudSched,
                cachedSchedules = activeSevenDays,
                isUsingCachedFallback = usingCache,
                selectedSehriSchedule = selectedSehriSchedule,
                monthlySehriSchedules = monthlySehriSchedules,
                showSplash = if (settings.reduceMotion) false else it.showSplash
            )
        }

        val periodStatus = PrayerTimeCalculator.evaluatePeriodStatus(now, todaySched, tomorrowSched)
        val tahajjudActive = !now.isBefore(tahajjudSched.startDateTime) && now.isBefore(tahajjudSched.endDateTime)
        _liveClock.value = LiveClockState(
            periodStatus = periodStatus,
            sehriIftarCountdown = computeSehriIftarCountdown(
                now = now,
                today = todaySched,
                tomorrow = tomorrowSched,
                sehriBufferMinutes = settings.sehriBufferMinutes
            ),
            isTahajjudActiveNow = tahajjudActive
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                PrayerAlarmScheduler.scheduleIfNeeded(appContext, repository, settings, force = false)
            }
        }
    }

    private suspend fun calculateSingleDay(
        date: LocalDate,
        settings: UserSettings,
        zoneId: ZoneId
    ): DailyPrayerSchedule = withContext(Dispatchers.Default) {
        PrayerTimeCalculator.calculateDay(
            date = date,
            latitude = settings.latitude,
            longitude = settings.longitude,
            locationNameEn = settings.locationNameEn,
            locationNameBn = settings.locationNameBn,
            method = settings.calculationMethod,
            asrJuristic = settings.asrJuristic,
            zoneId = zoneId
        )
    }

    private suspend fun calculateMonth(
        month: YearMonth,
        settings: UserSettings,
        zoneId: ZoneId
    ): List<DailyPrayerSchedule> = withContext(Dispatchers.Default) {
        PrayerTimeCalculator.calculateRange(
            startDate = month.atDay(1),
            days = month.lengthOfMonth(),
            latitude = settings.latitude,
            longitude = settings.longitude,
            locationNameEn = settings.locationNameEn,
            locationNameBn = settings.locationNameBn,
            method = settings.calculationMethod,
            asrJuristic = settings.asrJuristic,
            zoneId = zoneId
        )
    }

    private fun computeSehriIftarCountdown(
        now: LocalDateTime,
        today: DailyPrayerSchedule,
        tomorrow: DailyPrayerSchedule,
        sehriBufferMinutes: Int = 0
    ): SehriIftarTargetCountdown {
        val currentTime = now.toLocalTime()
        val todaySehriCutoff = today.sehriEnd.minusMinutes(sehriBufferMinutes.toLong())
        val tomorrowSehriCutoff = tomorrow.sehriEnd.minusMinutes(sehriBufferMinutes.toLong())
        return when {
            currentTime.isBefore(todaySehriCutoff) -> {
                val target = LocalDateTime.of(today.date, todaySehriCutoff)
                SehriIftarTargetCountdown(
                    isSehriNext = true,
                    countdown = Duration.between(now, target).coerceAtLeast(Duration.ZERO)
                )
            }
            currentTime.isBefore(today.iftarStart) -> {
                val target = LocalDateTime.of(today.date, today.iftarStart)
                SehriIftarTargetCountdown(
                    isSehriNext = false,
                    countdown = Duration.between(now, target).coerceAtLeast(Duration.ZERO)
                )
            }
            else -> {
                val target = LocalDateTime.of(tomorrow.date, tomorrowSehriCutoff)
                SehriIftarTargetCountdown(
                    isSehriNext = true,
                    countdown = Duration.between(now, target).coerceAtLeast(Duration.ZERO)
                )
            }
        }
    }

    // --- User Actions ---

    fun selectDistrict(district: DistrictLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(gpsMessage = null) }
            repository.setManualDistrict(district)
        }
    }

    fun detectGpsLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingGps = true, gpsMessage = null) }
            val strings = WaqtStrings(_uiState.value.settings.language)
            when (val result = LocationHelper.getCurrentLocationSafe(appContext)) {
                is GpsLocationResult.Success -> {
                    val nearest = BangladeshLocations.findNearest(result.latitude, result.longitude)
                    repository.setGpsLocation(result.latitude, result.longitude, nearest)
                    _uiState.update { it.copy(isDetectingGps = false, gpsMessage = null) }
                }
                GpsLocationResult.PermissionDenied -> {
                    _uiState.update {
                        it.copy(isDetectingGps = false, gpsMessage = strings.gpsDeniedMessage)
                    }
                }
                GpsLocationResult.ProviderDisabled -> {
                    _uiState.update {
                        it.copy(isDetectingGps = false, gpsMessage = strings.gpsProviderDisabledMessage)
                    }
                }
                GpsLocationResult.Unavailable -> {
                    _uiState.update {
                        it.copy(isDetectingGps = false, gpsMessage = strings.gpsUnavailableMessage)
                    }
                }
            }
        }
    }

    fun onLocationPermissionDenied() {
        val strings = WaqtStrings(_uiState.value.settings.language)
        _uiState.update { it.copy(gpsMessage = strings.gpsDeniedMessage) }
    }

    fun clearGpsMessage() {
        _uiState.update { it.copy(gpsMessage = null) }
    }

    fun setSehriDate(date: LocalDate) {
        val settings = _uiState.value.settings
        val zoneId = ZoneId.systemDefault()
        viewModelScope.launch {
            val sched = calculateSingleDay(date, settings, zoneId)
            val month = YearMonth.from(date)
            val monthly = if (month != _uiState.value.selectedSehriMonth) {
                calculateMonth(month, settings, zoneId)
            } else {
                _uiState.value.monthlySehriSchedules
            }
            _uiState.update {
                it.copy(
                    selectedSehriDate = date,
                    selectedSehriSchedule = sched,
                    selectedSehriMonth = month,
                    monthlySehriSchedules = monthly
                )
            }
        }
    }

    fun setSehriMonth(month: YearMonth) {
        val settings = _uiState.value.settings
        val zoneId = ZoneId.systemDefault()
        viewModelScope.launch {
            val monthly = calculateMonth(month, settings, zoneId)
            _uiState.update {
                it.copy(
                    selectedSehriMonth = month,
                    monthlySehriSchedules = monthly
                )
            }
        }
    }

    fun togglePrayerNotification(prayer: PrayerType, enabled: Boolean) {
        viewModelScope.launch {
            repository.togglePrayerNotification(prayer, enabled)
        }
    }

    fun setReminderOption(option: ReminderOption) {
        viewModelScope.launch {
            repository.setReminderOption(option)
        }
    }

    fun toggleWaqtEndAlert(enabled: Boolean) {
        viewModelScope.launch {
            repository.setWaqtEndAlertEnabled(enabled)
        }
    }

    fun toggleTahajjudAlert(enabled: Boolean) {
        viewModelScope.launch {
            repository.setTahajjudAlertEnabled(enabled)
        }
    }

    fun setNotificationTone(tone: NotificationTone) {
        viewModelScope.launch {
            repository.setNotificationTone(tone)
        }
    }

    fun sendTestNotification() {
        val settings = _uiState.value.settings
        NotificationHelper.ensureChannels(appContext, settings.language)
        val title = if (settings.language == AppLanguage.BENGALI) {
            "ওয়াক্ত (WAQT) — অ্যালার্ম টেস্ট"
        } else {
            "WAQT — Alarm Sound Test"
        }
        val body = if (settings.language == AppLanguage.BENGALI) {
            "ওয়াক্ত শুরু হলে এমন অ্যালার্ম বাজবে • ${settings.locationNameBn}"
        } else {
            "Prayer start alarms and vibration are active • ${settings.locationNameEn}"
        }
        NotificationHelper.showPrayerNotification(
            context = appContext,
            notificationId = 9901,
            channelId = settings.notificationTone.channelId,
            title = title,
            body = body,
            playAlarmTonePulse = true
        )
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setReduceMotion(reduce: Boolean) {
        viewModelScope.launch { repository.setReduceMotion(reduce) }
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        viewModelScope.launch { repository.setUse24HourFormat(use24Hour) }
    }

    fun setHijriOffsetDays(offsetDays: Int) {
        viewModelScope.launch { repository.setHijriOffsetDays(offsetDays) }
    }

    fun setSehriBufferMinutes(minutes: Int) {
        viewModelScope.launch { repository.setSehriBufferMinutes(minutes) }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch { repository.setCalculationMethod(method) }
    }

    fun setAsrJuristic(asrJuristic: AsrJuristic) {
        viewModelScope.launch { repository.setAsrJuristic(asrJuristic) }
    }
}
