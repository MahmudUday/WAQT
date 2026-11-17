package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.PrayerAsrTintDark
import com.example.ui.theme.PrayerAsrTintLight
import com.example.ui.theme.PrayerDhuhrAccent
import com.example.ui.theme.PrayerDhuhrTintDark
import com.example.ui.theme.PrayerDhuhrTintLight
import com.example.ui.theme.PrayerFajrAccent
import com.example.ui.theme.PrayerFajrTintDark
import com.example.ui.theme.PrayerFajrTintLight
import com.example.ui.theme.PrayerIshaAccent
import com.example.ui.theme.PrayerIshaTintDark
import com.example.ui.theme.PrayerIshaTintLight
import com.example.ui.theme.PrayerMaghribAccent
import com.example.ui.theme.PrayerMaghribTintDark
import com.example.ui.theme.PrayerMaghribTintLight
import com.example.ui.theme.WaqtEmerald
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtGoldSoft
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyCard
import com.example.ui.theme.WaqtNavyDeep
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.cos
import kotlin.math.sin

private data class PrayerCardPalette(
    val accent: Color,
    val container: Color,
    val icon: ImageVector
)

@Composable
fun HomeScreen(
    uiState: WaqtUiState,
    liveClockFlow: StateFlow<LiveClockState>,
    strings: WaqtStrings,
    onOpenLocationPicker: () -> Unit,
    onTogglePrayerAlert: (PrayerType, Boolean) -> Unit,
    onToggleTahajjudAlert: (Boolean) -> Unit
) {
    val liveClock by liveClockFlow.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val isDark = when (settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val schedule = uiState.todaySchedule
    val periodStatus = liveClock.periodStatus

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("home_prayer_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Glassmorphic Animated Brand & Location Bar
            item(key = "header") {
                WaqtHeaderBar(
                    uiState = uiState,
                    strings = strings,
                    reduceMotion = settings.reduceMotion,
                    onOpenLocationPicker = onOpenLocationPicker
                )
            }

            // Offline / Cached Schedule Indicator
            item(key = "offline_pill") {
                OfflineStatusPill(
                    isOnline = uiState.isOnline,
                    isFromCache = uiState.isUsingCachedFallback,
                    isRamadan = uiState.hijriDate.isRamadan,
                    strings = strings
                )
            }

            // Modern Animated Glassmorphic WAQT Hero Section
            if (schedule != null && periodStatus != null) {
                item(key = "hero_card") {
                    HeroCountdownCard(
                        schedule = schedule,
                        currentPrayer = periodStatus.currentPrayer,
                        nextPrayer = periodStatus.nextPrayer,
                        elapsedFraction = periodStatus.elapsedFraction,
                        countdownText = BengaliFormatter.formatCountdown(
                            periodStatus.countdown,
                            settings.language
                        ),
                        dateLabel = BengaliFormatter.formatDate(uiState.todayDate, settings.language),
                        hijriLabel = uiState.hijriDate.format(settings.language),
                        reduceMotion = settings.reduceMotion,
                        strings = strings
                    )
                }

                // Five Distinct Color Glass Prayer Cards
                items(
                    items = PrayerType.entries,
                    key = { it.name }
                ) { prayer ->
                    val palette = remember(prayer, isDark) { prayerPalette(prayer, isDark) }
                    val isCurrent = periodStatus.currentPrayer == prayer
                    val isNext = periodStatus.nextPrayer == prayer
                    val isAlertEnabled = prayer in settings.enabledPrayers

                    PrayerColorCard(
                        prayer = prayer,
                        timeText = BengaliFormatter.formatTime(
                            schedule.timeFor(prayer),
                            settings.language
                        ),
                        palette = palette,
                        isCurrent = isCurrent,
                        isNext = isNext,
                        isAlertEnabled = isAlertEnabled,
                        reduceMotion = settings.reduceMotion,
                        strings = strings,
                        onToggleAlert = { onTogglePrayerAlert(prayer, !isAlertEnabled) }
                    )
                }
            }

            // Dedicated Tahajjud Prayer Section
            uiState.tahajjudSchedule?.let { tahajjud ->
                item(key = "tahajjud_section") {
                    TahajjudGlassCard(
                        tahajjud = tahajjud,
                        isActiveNow = liveClock.isTahajjudActiveNow,
                        isAlarmEnabled = settings.tahajjudAlertEnabled,
                        language = settings.language,
                        reduceMotion = settings.reduceMotion,
                        strings = strings,
                        onToggleAlarm = { onToggleTahajjudAlert(!settings.tahajjudAlertEnabled) }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun WaqtHeaderBar(
    uiState: WaqtUiState,
    strings: WaqtStrings,
    reduceMotion: Boolean,
    onOpenLocationPicker: () -> Unit
) {
    val settings = uiState.settings
    val locationLabel = if (settings.language == AppLanguage.BENGALI) {
        settings.locationNameBn
    } else {
        settings.locationNameEn
    }

    WaqtGlassCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
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
                // Mini Navy & Gold Crescent Emblem
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WaqtNavy),
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
                        color = MaterialTheme.colorScheme.onSurface
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
                    .waqtPressScale(reduceMotion = reduceMotion)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onOpenLocationPicker)
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
    strings: WaqtStrings
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            border = BorderStroke(1.dp, fgColor.copy(alpha = 0.25f)),
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
                    color = fgColor
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
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
    elapsedFraction: Float,
    countdownText: String,
    dateLabel: String,
    hijriLabel: String,
    reduceMotion: Boolean,
    strings: WaqtStrings
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_ambient")
    val pulseAlpha by if (reduceMotion) {
        remember { androidx.compose.runtime.mutableFloatStateOf(0.85f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_dot"
        )
    }

    val orbitAngle by if (reduceMotion) {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "hero_orbit"
        )
    }

    val animatedProgress by animateFloatAsState(
        targetValue = elapsedFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 500),
        label = "waqt_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_countdown_card"),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                listOf(
                    WaqtGoldBright.copy(alpha = 0.72f),
                    WaqtGold.copy(alpha = 0.25f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = WaqtNavy.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            WaqtNavyDeep.copy(alpha = 0.95f),
                            WaqtNavy.copy(alpha = 0.90f),
                            WaqtNavyCard.copy(alpha = 0.88f)
                        )
                    )
                )
        ) {
            // Animated Celestial Orbit & Islamic Arch Motif
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val orbitCenter = Offset(w * 0.86f, h * 0.26f)
                val orbitRadius = h * 0.44f

                drawCircle(
                    color = WaqtGold.copy(alpha = 0.08f),
                    radius = orbitRadius * 1.25f,
                    center = orbitCenter
                )
                drawCircle(
                    color = WaqtGold.copy(alpha = 0.16f),
                    radius = orbitRadius,
                    center = orbitCenter,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                val rad = Math.toRadians(orbitAngle.toDouble())
                val starX = orbitCenter.x + (orbitRadius * cos(rad)).toFloat()
                val starY = orbitCenter.y + (orbitRadius * sin(rad)).toFloat()
                drawCircle(
                    color = WaqtGoldBright.copy(alpha = 0.85f),
                    radius = 3.2.dp.toPx(),
                    center = Offset(starX, starY)
                )

                // Specular glass highlight arc along top-left
                val arch = Path().apply {
                    moveTo(w * 0.56f, h)
                    quadraticTo(w * 0.78f, h * 0.44f, w, h * 0.72f)
                }
                drawPath(
                    path = arch,
                    color = WaqtGoldSoft.copy(alpha = 0.12f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                            color = WaqtGoldSoft
                        )
                        Text(
                            text = hijriLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }

                    // Pulsing Live Waqt Status Pill
                    Surface(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WaqtGoldBright.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentPrayer != null) {
                                    "${strings.currentWaqtLabel}: ${strings.prayerName(currentPrayer)}"
                                } else {
                                    strings.noFardWaqtLabel
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = WaqtGoldSoft
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.nextPrayerLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.74f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AnimatedContent(
                            targetState = strings.prayerName(nextPrayer),
                            transitionSpec = {
                                fadeIn(tween(240)) togetherWith fadeOut(tween(180))
                            },
                            label = "next_prayer_title"
                        ) { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = BengaliFormatter.formatTime(
                                schedule.timeFor(nextPrayer),
                                strings.language
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = WaqtGoldBright
                        )
                    }

                    // Circular Elapsed Progress Ring + Countdown
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = strings.remainingLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.74f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(26.dp)) {
                                val stroke = 3.dp.toPx()
                                drawArc(
                                    color = Color.White.copy(alpha = 0.16f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    size = Size(size.width, size.height),
                                    style = Stroke(width = stroke)
                                )
                                drawArc(
                                    color = WaqtGoldBright,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    size = Size(size.width, size.height),
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = countdownText,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WaqtGoldBright,
                                modifier = Modifier.testTag("hero_countdown_text")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Frosted Glass Bottom Strip inside Hero
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strings.sunriseLabel}: ${BengaliFormatter.formatTime(schedule.sunrise, strings.language)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "${strings.sehriEndLabel}: ${BengaliFormatter.formatShortTime(schedule.sehriEnd, strings.language)} • ${strings.iftarStartLabel}: ${BengaliFormatter.formatShortTime(schedule.iftarStart, strings.language)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = WaqtGoldSoft
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerColorCard(
    prayer: PrayerType,
    timeText: String,
    palette: PrayerCardPalette,
    isCurrent: Boolean,
    isNext: Boolean,
    isAlertEnabled: Boolean,
    reduceMotion: Boolean,
    strings: WaqtStrings,
    onToggleAlert: () -> Unit
) {
    val borderColor = when {
        isCurrent -> palette.accent
        isNext -> WaqtGold
        else -> palette.accent.copy(alpha = 0.32f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .waqtPressScale(reduceMotion = reduceMotion)
            .testTag("prayer_card_${prayer.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.container.copy(alpha = 0.86f)),
        border = BorderStroke(if (isCurrent || isNext) 1.8.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(palette.accent.copy(alpha = 0.18f)),
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

            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.accent
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onToggleAlert,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .waqtPressScale(reduceMotion = reduceMotion)
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
    isActiveNow: Boolean,
    isAlarmEnabled: Boolean,
    language: AppLanguage,
    reduceMotion: Boolean,
    strings: WaqtStrings,
    onToggleAlarm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .waqtPressScale(reduceMotion = reduceMotion)
            .testTag("tahajjud_section_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151F42).copy(alpha = 0.90f)),
        border = BorderStroke(
            width = if (isActiveNow) 1.8.dp else 1.1.dp,
            color = if (isActiveNow) WaqtGoldBright else WaqtGold.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF141D3D).copy(alpha = 0.92f),
                            Color(0xFF232C5C).copy(alpha = 0.86f)
                        )
                    )
                )
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
                            .background(WaqtGold.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = strings.tahajjudSectionTitle,
                            tint = WaqtGoldBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.tahajjudSectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = strings.tahajjudSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WaqtGoldSoft.copy(alpha = 0.85f)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleAlarm,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .waqtPressScale(reduceMotion = reduceMotion)
                        .testTag("tahajjud_alarm_toggle")
                ) {
                    Icon(
                        imageVector = if (isAlarmEnabled) {
                            Icons.Rounded.NotificationsActive
                        } else {
                            Icons.Outlined.NotificationsOff
                        },
                        contentDescription = strings.tahajjudAlertTitle,
                        tint = if (isAlarmEnabled) WaqtGoldBright else Color.White.copy(alpha = 0.65f)
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
                    value = BengaliFormatter.formatTime(tahajjud.startTime, language),
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                TahajjudTimePill(
                    label = strings.tahajjudEndsLabel,
                    value = BengaliFormatter.formatTime(tahajjud.endTime, language),
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
                TahajjudTimePill(
                    label = strings.islamicMidnightLabel,
                    value = BengaliFormatter.formatTime(tahajjud.midnightTime, language),
                    highlight = false,
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
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = if (highlight) 0.14f else 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (highlight) WaqtGold.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = WaqtGoldSoft.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (highlight) WaqtGoldBright else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun prayerPalette(prayer: PrayerType, isDark: Boolean): PrayerCardPalette {
    return when (prayer) {
        PrayerType.FAJR -> PrayerCardPalette(
            accent = PrayerFajrAccent,
            container = if (isDark) PrayerFajrTintDark else PrayerFajrTintLight,
            icon = Icons.Rounded.WbTwilight
        )
        PrayerType.DHUHR -> PrayerCardPalette(
            accent = PrayerDhuhrAccent,
            container = if (isDark) PrayerDhuhrTintDark else PrayerDhuhrTintLight,
            icon = Icons.Rounded.WbSunny
        )
        PrayerType.ASR -> PrayerCardPalette(
            accent = PrayerAsrAccent,
            container = if (isDark) PrayerAsrTintDark else PrayerAsrTintLight,
            icon = Icons.Rounded.WbSunny
        )
        PrayerType.MAGHRIB -> PrayerCardPalette(
            accent = PrayerMaghribAccent,
            container = if (isDark) PrayerMaghribTintDark else PrayerMaghribTintLight,
            icon = Icons.Rounded.WbTwilight
        )
        PrayerType.ISHA -> PrayerCardPalette(
            accent = PrayerIshaAccent,
            container = if (isDark) PrayerIshaTintDark else PrayerIshaTintLight,
            icon = Icons.Rounded.Bedtime
        )
    }
}
