package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BengaliFormatter
import com.example.model.AppLanguage
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerType
import com.example.model.TahajjudSchedule
import com.example.model.ThemeMode
import com.example.ui.LiveClockState
import com.example.ui.WaqtUiState
import com.example.ui.components.WaqtGlassCard
import com.example.ui.components.waqtPressScale
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.PrayerAsrAccent
import com.example.ui.theme.PrayerAsrGlow
import com.example.ui.theme.PrayerDhuhrAccent
import com.example.ui.theme.PrayerDhuhrGlow
import com.example.ui.theme.PrayerFajrAccent
import com.example.ui.theme.PrayerFajrGlow
import com.example.ui.theme.PrayerIshaAccent
import com.example.ui.theme.PrayerIshaGlow
import com.example.ui.theme.PrayerMaghribAccent
import com.example.ui.theme.PrayerMaghribGlow
import com.example.ui.theme.WaqtCyan
import com.example.ui.theme.WaqtEmerald
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtGoldDark
import com.example.ui.theme.WaqtGoldSoft
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyDeep
import com.example.ui.theme.WaqtViolet
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Immutable
private data class PrayerCardPalette(
    val accent: Color,
    val glow: Color,
    val icon: ImageVector
)

@Immutable
private data class HeroPeriodSummary(
    val available: Boolean,
    val currentPrayer: PrayerType?,
    val nextPrayer: PrayerType
)

@Composable
fun HomeScreen(
    uiState: WaqtUiState,
    liveClockFlow: StateFlow<LiveClockState>,
    strings: WaqtStrings,
    onOpenLocationPicker: () -> Unit,
    onTogglePrayerAlert: (PrayerType, Boolean) -> Unit,
    onToggleTahajjudAlert: (Boolean) -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val initialClock = remember(liveClockFlow) { liveClockFlow.value }
    val periodSummary by remember(liveClockFlow) {
        liveClockFlow
            .map { clock ->
                clock.periodStatus?.let { HeroPeriodSummary(true, it.currentPrayer, it.nextPrayer) }
                    ?: HeroPeriodSummary(false, null, PrayerType.FAJR)
            }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(
        initialValue = initialClock.periodStatus?.let {
            HeroPeriodSummary(true, it.currentPrayer, it.nextPrayer)
        } ?: HeroPeriodSummary(false, null, PrayerType.FAJR)
    )
    val isTahajjudActive by remember(liveClockFlow) {
        liveClockFlow
            .map { it.isTahajjudActiveNow }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = initialClock.isTahajjudActiveNow)

    val settings = uiState.settings
    val isDark = when (settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val schedule = uiState.todaySchedule

    val dateLabel = remember(uiState.todayDate, settings.language) {
        BengaliFormatter.formatDate(uiState.todayDate, settings.language)
    }
    val hijriLabel = remember(uiState.hijriDate, settings.language) {
        uiState.hijriDate.format(settings.language)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("home_prayer_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 18.dp + bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Glassmorphic Brand & Location Bar
            item(key = "header", contentType = "header") {
                WaqtHeaderBar(
                    locationNameBn = settings.locationNameBn,
                    locationNameEn = settings.locationNameEn,
                    language = settings.language,
                    strings = strings,
                    reduceMotion = settings.reduceMotion,
                    onOpenLocationPicker = onOpenLocationPicker
                )
            }

            // Offline / Cached Schedule Indicator
            item(key = "offline_pill", contentType = "pill") {
                OfflineStatusPill(
                    isOnline = uiState.isOnline,
                    isFromCache = uiState.isUsingCachedFallback,
                    isRamadan = uiState.hijriDate.isRamadan,
                    strings = strings
                )
            }

            // Pure Crystal-Glass WAQT Hero Section (1-second countdown isolated inside)
            if (schedule != null && periodSummary.available) {
                item(key = "hero_card", contentType = "hero") {
                    HeroCountdownCard(
                        schedule = schedule,
                        currentPrayer = periodSummary.currentPrayer,
                        nextPrayer = periodSummary.nextPrayer,
                        liveClockFlow = liveClockFlow,
                        dateLabel = dateLabel,
                        hijriLabel = hijriLabel,
                        isDark = isDark,
                        use24HourFormat = settings.use24HourFormat,
                        sehriBufferMinutes = settings.sehriBufferMinutes,
                        strings = strings
                    )
                }

                // Five Colorful Crystal-Glass Prayer Cards (zero per-second recomposition!)
                items(
                    items = PrayerType.entries,
                    key = { it.name },
                    contentType = { "prayer_card" }
                ) { prayer ->
                    val palette = remember(prayer) { prayerPalette(prayer) }
                    val isCurrent = periodSummary.currentPrayer == prayer
                    val isNext = periodSummary.nextPrayer == prayer
                    val isAlertEnabled = prayer in settings.enabledPrayers
                    val timeText = remember(schedule, prayer, settings.language, settings.use24HourFormat) {
                        BengaliFormatter.formatTime(
                            time = schedule.timeFor(prayer),
                            language = settings.language,
                            use24Hour = settings.use24HourFormat
                        )
                    }

                    PrayerColorCard(
                        prayer = prayer,
                        timeText = timeText,
                        palette = palette,
                        isDark = isDark,
                        isCurrent = isCurrent,
                        isNext = isNext,
                        isAlertEnabled = isAlertEnabled,
                        reduceMotion = settings.reduceMotion,
                        strings = strings,
                        onToggleAlert = { onTogglePrayerAlert(prayer, !isAlertEnabled) }
                    )
                }
            }

            // Dedicated Colorful Aurora-Glass Tahajjud Prayer Section
            uiState.tahajjudSchedule?.let { tahajjud ->
                item(key = "tahajjud_section", contentType = "tahajjud") {
                    TahajjudGlassCard(
                        tahajjud = tahajjud,
                        isDark = isDark,
                        isActiveNow = isTahajjudActive,
                        isAlarmEnabled = settings.tahajjudAlertEnabled,
                        language = settings.language,
                        use24HourFormat = settings.use24HourFormat,
                        reduceMotion = settings.reduceMotion,
                        strings = strings,
                        onToggleAlarm = { onToggleTahajjudAlert(!settings.tahajjudAlertEnabled) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WaqtHeaderBar(
    locationNameBn: String,
    locationNameEn: String,
    language: AppLanguage,
    strings: WaqtStrings,
    reduceMotion: Boolean,
    onOpenLocationPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locationLabel = if (language == AppLanguage.BENGALI) locationNameBn else locationNameEn
    val locationInteraction = remember { MutableInteractionSource() }

    WaqtGlassCard(
        shape = RoundedCornerShape(22.dp),
        accentColor = WaqtGoldBright,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WaqtNavy.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NightsStay,
                        contentDescription = strings.fullBrandName,
                        tint = WaqtGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.fullBrandName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = strings.appSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = WaqtGold.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.45f)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .waqtPressScale(locationInteraction, reduceMotion)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = locationInteraction,
                        indication = androidx.compose.material3.ripple(),
                        onClick = onOpenLocationPicker
                    )
                    .testTag("location_picker_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = strings.changeLocation,
                        tint = WaqtGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineStatusPill(
    isOnline: Boolean,
    isFromCache: Boolean,
    isRamadan: Boolean,
    strings: WaqtStrings,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val offlineState = !isOnline || isFromCache
        val bgColor = if (offlineState) {
            Color(0xFFD98218).copy(alpha = 0.16f)
        } else {
            WaqtEmerald.copy(alpha = 0.14f)
        }
        val fgColor = if (offlineState) Color(0xFFB8670A) else WaqtEmerald

        Surface(
            color = bgColor,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, fgColor.copy(alpha = 0.28f)),
            modifier = Modifier.testTag("offline_status_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (offlineState) Icons.Rounded.CloudOff else Icons.Rounded.CloudDone,
                    contentDescription = null,
                    tint = fgColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (offlineState) strings.offlineCachedBadge else strings.onlineSyncedBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = fgColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isRamadan) {
            Surface(
                color = WaqtGold.copy(alpha = 0.20f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.40f))
            ) {
                Text(
                    text = strings.ramadanBadge,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HeroCountdownCard(
    schedule: DailyPrayerSchedule,
    currentPrayer: PrayerType?,
    nextPrayer: PrayerType,
    liveClockFlow: StateFlow<LiveClockState>,
    dateLabel: String,
    hijriLabel: String,
    isDark: Boolean,
    use24HourFormat: Boolean,
    sehriBufferMinutes: Int,
    strings: WaqtStrings,
    modifier: Modifier = Modifier
) {
    val borderBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.56f else 0.98f),
                WaqtGoldBright.copy(alpha = if (isDark) 0.42f else 0.55f)
            )
        )
    }
    // Pure crystal translucent glass gradient (high transparency so ambient aurora shines through cleanly)
    val heroFillBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.16f),
                    WaqtCyan.copy(alpha = 0.14f),
                    WaqtViolet.copy(alpha = 0.12f),
                    WaqtGold.copy(alpha = 0.10f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.60f),
                    WaqtCyan.copy(alpha = 0.10f),
                    WaqtGoldBright.copy(alpha = 0.14f)
                )
            )
        }
    }
    val primaryTextColor = if (isDark) Color.White else WaqtNavyDeep
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.80f) else WaqtNavy.copy(alpha = 0.78f)
    val accentTextColor = if (isDark) WaqtGoldBright else WaqtGoldDark
    val dateTextColor = if (isDark) WaqtGoldSoft else WaqtGoldDark
    val innerChipBg = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.56f)
    val innerChipBorder = if (isDark) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.92f)
    val sehriTime = remember(schedule, sehriBufferMinutes) {
        schedule.sehriEnd.minusMinutes(sehriBufferMinutes.toLong())
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_countdown_card"),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(width = 1.25.dp, brush = borderBrush),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroFillBrush)
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    val orbitCenter = Offset(w * 0.86f, h * 0.26f)
                    val orbitRadius = h * 0.44f
                    val glowStroke = Stroke(width = 1.2.dp.toPx())
                    val archStroke = Stroke(width = 1.5.dp.toPx())
                    val sheenBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.14f else 0.34f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w * 0.68f, h * 0.52f)
                    )
                    val starPos = Offset(
                        orbitCenter.x - orbitRadius * 0.72f,
                        orbitCenter.y + orbitRadius * 0.68f
                    )
                    val arch = Path().apply {
                        moveTo(w * 0.56f, h)
                        quadraticTo(w * 0.78f, h * 0.44f, w, h * 0.72f)
                    }
                    onDrawBehind {
                        // Subtle top-left glass specular reflection sheen
                        drawRect(brush = sheenBrush)
                        drawCircle(
                            color = WaqtGold.copy(alpha = if (isDark) 0.10f else 0.14f),
                            radius = orbitRadius * 1.24f,
                            center = orbitCenter
                        )
                        drawCircle(
                            color = WaqtGold.copy(alpha = if (isDark) 0.20f else 0.28f),
                            radius = orbitRadius,
                            center = orbitCenter,
                            style = glowStroke
                        )
                        drawCircle(
                            color = accentTextColor.copy(alpha = 0.85f),
                            radius = 3.dp.toPx(),
                            center = starPos
                        )
                        drawPath(
                            path = arch,
                            color = accentTextColor.copy(alpha = 0.18f),
                            style = archStroke
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = dateTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = hijriLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Current Waqt Crystal Glass Pill
                    Surface(
                        color = innerChipBg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, innerChipBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(accentTextColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentPrayer != null) {
                                    "${strings.currentWaqtLabel}: ${strings.prayerName(currentPrayer)}"
                                } else {
                                    strings.noFardWaqtLabel
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryTextColor,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.nextPrayerLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = secondaryTextColor,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AnimatedContent(
                            targetState = strings.prayerName(nextPrayer),
                            transitionSpec = {
                                fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                            },
                            label = "next_prayer_title"
                        ) { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = primaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = BengaliFormatter.formatTime(
                                time = schedule.timeFor(nextPrayer),
                                language = strings.language,
                                use24Hour = use24HourFormat
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentTextColor,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Isolated 1-second leaf composable — strict single-line compact countdown
                    HeroLiveCountdownValue(
                        liveClockFlow = liveClockFlow,
                        isDark = isDark,
                        language = strings.language,
                        strings = strings
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sleek GPU-only Horizontal Glass Waqt Progress Bar (zero layout invalidation per second)
                HeroLiveProgressBar(liveClockFlow = liveClockFlow, isDark = isDark)

                Spacer(modifier = Modifier.height(12.dp))

                // Frosted Glass Bottom Strip inside Hero
                Surface(
                    color = innerChipBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.9.dp, innerChipBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.sunriseLabel}: ${
                                BengaliFormatter.formatTime(
                                    time = schedule.sunrise,
                                    language = strings.language,
                                    use24Hour = use24HourFormat
                                )
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryTextColor.copy(alpha = 0.90f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.sehriEndLabel}: ${
                                BengaliFormatter.formatShortTime(
                                    time = sehriTime,
                                    language = strings.language,
                                    use24Hour = use24HourFormat
                                )
                            } • ${strings.iftarStartLabel}: ${
                                BengaliFormatter.formatShortTime(
                                    time = schedule.iftarStart,
                                    language = strings.language,
                                    use24Hour = use24HourFormat
                                )
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = dateTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroLiveCountdownValue(
    liveClockFlow: StateFlow<LiveClockState>,
    isDark: Boolean,
    language: AppLanguage,
    strings: WaqtStrings
) {
    val liveClock by liveClockFlow.collectAsStateWithLifecycle()
    val countdownText = liveClock.periodStatus?.let {
        BengaliFormatter.formatCountdown(it.countdown, language)
    } ?: "--:--:--"

    val labelColor = if (isDark) Color.White.copy(alpha = 0.80f) else WaqtNavy.copy(alpha = 0.78f)
    val pillColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.64f)
    val pillBorder = if (isDark) WaqtGoldBright.copy(alpha = 0.48f) else WaqtGold.copy(alpha = 0.56f)
    val valueColor = if (isDark) WaqtGoldBright else WaqtNavyDeep

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = strings.remainingLabel,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = pillColor,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, pillBorder)
        ) {
            Text(
                text = countdownText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = valueColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("hero_countdown_text")
            )
        }
    }
}

@Composable
private fun HeroLiveProgressBar(
    liveClockFlow: StateFlow<LiveClockState>,
    isDark: Boolean
) {
    val liveClock by liveClockFlow.collectAsStateWithLifecycle()
    val fraction = (liveClock.periodStatus?.elapsedFraction ?: 0f).coerceIn(0.03f, 1f)
    val trackColor = remember(isDark) {
        if (isDark) Color.White.copy(alpha = 0.16f) else WaqtNavy.copy(alpha = 0.14f)
    }
    val fillBrush = remember {
        Brush.horizontalGradient(listOf(WaqtCyan, WaqtGold, WaqtGoldBright))
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
    ) {
        val corner = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(
            color = trackColor,
            cornerRadius = corner
        )
        drawRoundRect(
            brush = fillBrush,
            size = Size(width = size.width * fraction, height = size.height),
            cornerRadius = corner
        )
    }
}

@Composable
private fun PrayerColorCard(
    prayer: PrayerType,
    timeText: String,
    palette: PrayerCardPalette,
    isDark: Boolean,
    isCurrent: Boolean,
    isNext: Boolean,
    isAlertEnabled: Boolean,
    reduceMotion: Boolean,
    strings: WaqtStrings,
    onToggleAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderBrush = remember(isDark, isCurrent, isNext, palette) {
        when {
            isCurrent -> Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f), palette.accent))
            isNext -> Brush.verticalGradient(listOf(WaqtGoldBright, WaqtGold.copy(alpha = 0.55f)))
            else -> Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (isDark) 0.34f else 0.96f),
                    palette.accent.copy(alpha = if (isDark) 0.36f else 0.40f)
                )
            )
        }
    }
    val glassFillBrush = remember(isDark, isCurrent, palette) {
        if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    palette.accent.copy(alpha = if (isCurrent) 0.30f else 0.20f),
                    palette.glow.copy(alpha = if (isCurrent) 0.18f else 0.10f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isCurrent) 0.68f else 0.58f),
                    palette.accent.copy(alpha = if (isCurrent) 0.20f else 0.12f),
                    palette.glow.copy(alpha = if (isCurrent) 0.18f else 0.10f)
                )
            )
        }
    }
    val bellInteraction = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.name.lowercase()}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(if (isCurrent || isNext) 1.5.dp else 1.dp, borderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassFillBrush)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                palette.accent.copy(alpha = 0.28f),
                                palette.glow.copy(alpha = 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = palette.icon,
                    contentDescription = strings.prayerName(prayer),
                    tint = palette.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.prayerName(prayer),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = palette.accent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = strings.currentBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = WaqtGold,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = strings.nextBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WaqtNavyDeep,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = strings.prayerSubLabel(prayer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = palette.accent.copy(alpha = if (isDark) 0.18f else 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.8.dp, palette.accent.copy(alpha = 0.34f))
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) palette.glow else palette.accent,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onToggleAlert,
                interactionSource = bellInteraction,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .waqtPressScale(bellInteraction, reduceMotion)
                    .testTag("prayer_alert_toggle_${prayer.name.lowercase()}")
            ) {
                Icon(
                    imageVector = if (isAlertEnabled) {
                        Icons.Rounded.NotificationsActive
                    } else {
                        Icons.Outlined.NotificationsOff
                    },
                    contentDescription = strings.prayerName(prayer),
                    tint = if (isAlertEnabled) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TahajjudGlassCard(
    tahajjud: TahajjudSchedule,
    isDark: Boolean,
    isActiveNow: Boolean,
    isAlarmEnabled: Boolean,
    language: AppLanguage,
    use24HourFormat: Boolean,
    reduceMotion: Boolean,
    strings: WaqtStrings,
    onToggleAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bellInteraction = remember { MutableInteractionSource() }
    val cardBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                listOf(
                    WaqtViolet.copy(alpha = 0.24f),
                    WaqtCyan.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.62f),
                    WaqtViolet.copy(alpha = 0.15f),
                    WaqtGoldBright.copy(alpha = 0.12f)
                )
            )
        }
    }
    val borderBrush = remember(isDark, isActiveNow) {
        if (isActiveNow) {
            Brush.verticalGradient(listOf(WaqtGoldBright, WaqtViolet))
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (isDark) 0.40f else 0.96f),
                    WaqtViolet.copy(alpha = if (isDark) 0.42f else 0.36f)
                )
            )
        }
    }
    val titleColor = if (isDark) Color.White else WaqtNavyDeep
    val subColor = if (isDark) WaqtGoldSoft.copy(alpha = 0.88f) else WaqtNavy.copy(alpha = 0.78f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tahajjud_section_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(if (isActiveNow) 1.6.dp else 1.1.dp, borderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(WaqtViolet.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = strings.tahajjudSectionTitle,
                            tint = if (isDark) WaqtGoldBright else WaqtViolet,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.tahajjudSectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = strings.tahajjudSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = subColor
                        )
                    }
                }

                IconButton(
                    onClick = onToggleAlarm,
                    interactionSource = bellInteraction,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .waqtPressScale(bellInteraction, reduceMotion)
                        .testTag("tahajjud_alarm_toggle")
                ) {
                    Icon(
                        imageVector = if (isAlarmEnabled) {
                            Icons.Rounded.NotificationsActive
                        } else {
                            Icons.Outlined.NotificationsOff
                        },
                        contentDescription = strings.tahajjudAlertTitle,
                        tint = if (isAlarmEnabled) {
                            if (isDark) WaqtGoldBright else WaqtViolet
                        } else {
                            subColor
                        }
                    )
                }
            }

            if (isActiveNow) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = WaqtGoldBright,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = strings.tahajjudActiveNow,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaqtNavyDeep,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TahajjudTimePill(
                    label = strings.tahajjudStartsLabel,
                    value = BengaliFormatter.formatTime(tahajjud.startTime, language, use24HourFormat),
                    highlight = true,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                TahajjudTimePill(
                    label = strings.tahajjudEndsLabel,
                    value = BengaliFormatter.formatTime(tahajjud.endTime, language, use24HourFormat),
                    highlight = false,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                TahajjudTimePill(
                    label = strings.islamicMidnightLabel,
                    value = BengaliFormatter.formatTime(tahajjud.midnightTime, language, use24HourFormat),
                    highlight = false,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TahajjudTimePill(
    label: String,
    value: String,
    highlight: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        highlight && isDark -> WaqtGold.copy(alpha = 0.18f)
        highlight && !isDark -> WaqtGold.copy(alpha = 0.22f)
        isDark -> Color.White.copy(alpha = 0.10f)
        else -> Color.White.copy(alpha = 0.52f)
    }
    val borderColor = if (highlight) {
        WaqtGold.copy(alpha = 0.56f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.White.copy(alpha = 0.90f)
    }
    val valueColor = when {
        highlight && isDark -> WaqtGoldBright
        highlight && !isDark -> WaqtGoldDark
        isDark -> Color.White
        else -> WaqtNavyDeep
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) WaqtGoldSoft.copy(alpha = 0.88f) else WaqtNavy.copy(alpha = 0.76f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun prayerPalette(prayer: PrayerType): PrayerCardPalette {
    return when (prayer) {
        PrayerType.FAJR -> PrayerCardPalette(
            accent = PrayerFajrAccent,
            glow = PrayerFajrGlow,
            icon = Icons.Rounded.WbTwilight
        )
        PrayerType.DHUHR -> PrayerCardPalette(
            accent = PrayerDhuhrAccent,
            glow = PrayerDhuhrGlow,
            icon = Icons.Rounded.WbSunny
        )
        PrayerType.ASR -> PrayerCardPalette(
            accent = PrayerAsrAccent,
            glow = PrayerAsrGlow,
            icon = Icons.Rounded.WbSunny
        )
        PrayerType.MAGHRIB -> PrayerCardPalette(
            accent = PrayerMaghribAccent,
            glow = PrayerMaghribGlow,
            icon = Icons.Rounded.WbTwilight
        )
        PrayerType.ISHA -> PrayerCardPalette(
            accent = PrayerIshaAccent,
            glow = PrayerIshaGlow,
            icon = Icons.Rounded.Bedtime
        )
    }
}
