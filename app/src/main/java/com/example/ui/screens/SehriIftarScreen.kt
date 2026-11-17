package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BengaliFormatter
import com.example.data.HijriCalendarHelper
import com.example.model.AppLanguage
import com.example.model.DailyPrayerSchedule
import com.example.model.ThemeMode
import com.example.ui.LiveClockState
import com.example.ui.WaqtUiState
import com.example.ui.components.WaqtGlassCard
import com.example.ui.components.waqtPressScale
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.PrayerFajrAccent
import com.example.ui.theme.PrayerFajrGlow
import com.example.ui.theme.PrayerMaghribAccent
import com.example.ui.theme.PrayerMaghribGlow
import com.example.ui.theme.WaqtCyan
import com.example.ui.theme.WaqtEmerald
import com.example.ui.theme.WaqtEmeraldLight
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtGoldDark
import com.example.ui.theme.WaqtNavyDeep
import com.example.ui.theme.WaqtTeal
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun SehriIftarScreen(
    uiState: WaqtUiState,
    liveClockFlow: StateFlow<LiveClockState>,
    strings: WaqtStrings,
    onSelectDate: (LocalDate) -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onOpenLocationPicker: () -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val settings = uiState.settings
    val lang = settings.language
    val reduceMotion = settings.reduceMotion
    val use24Hour = settings.use24HourFormat
    val sehriBufferMins = settings.sehriBufferMinutes
    val isDark = when (settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val selectedSchedule = uiState.selectedSehriSchedule ?: uiState.todaySchedule
    val locationLabel = if (lang == AppLanguage.BENGALI) {
        settings.locationNameBn
    } else {
        settings.locationNameEn
    }
    val selectedHijri = remember(uiState.selectedSehriDate, settings.hijriOffsetDays) {
        HijriCalendarHelper.fromGregorian(uiState.selectedSehriDate, settings.hijriOffsetDays)
    }

    val locationInteraction = remember { MutableInteractionSource() }
    val prevDayInteraction = remember { MutableInteractionSource() }
    val todayInteraction = remember { MutableInteractionSource() }
    val nextDayInteraction = remember { MutableInteractionSource() }
    val prevMonthInteraction = remember { MutableInteractionSource() }
    val nextMonthInteraction = remember { MutableInteractionSource() }

    val tableHeaderBrush = remember(isDark) {
        if (isDark) {
            Brush.linearGradient(
                listOf(
                    WaqtTeal.copy(alpha = 0.24f),
                    WaqtGoldBright.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.68f),
                    WaqtTeal.copy(alpha = 0.16f),
                    WaqtGoldBright.copy(alpha = 0.18f)
                )
            )
        }
    }
    val tableHeaderTextColor = if (isDark) WaqtGoldBright else WaqtNavyDeep

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("sehri_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 18.dp + bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header", contentType = "header") {
                WaqtGlassCard(
                    accentColor = WaqtTeal,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.sehriIftarTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.sehriIftarSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = WaqtTeal.copy(alpha = if (isDark) 0.20f else 0.14f),
                            border = BorderStroke(1.dp, WaqtTeal.copy(alpha = 0.50f)),
                            modifier = Modifier
                                .waqtPressScale(locationInteraction, reduceMotion)
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = locationInteraction,
                                    indication = ripple(),
                                    onClick = onOpenLocationPicker
                                )
                                .testTag("sehri_location_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = strings.changeLocation,
                                    tint = if (isDark) WaqtTeal else WaqtEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = locationLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item(key = "countdown_banner", contentType = "banner") {
                SehriCountdownBanner(
                    liveClockFlow = liveClockFlow,
                    language = lang,
                    isDark = isDark,
                    strings = strings
                )
            }

            item(key = "date_nav", contentType = "date_nav") {
                WaqtGlassCard(
                    accentColor = WaqtCyan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { onSelectDate(uiState.selectedSehriDate.minusDays(1)) },
                            interactionSource = prevDayInteraction,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .waqtPressScale(prevDayInteraction, reduceMotion)
                                .testTag("sehri_prev_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = strings.previousDayDesc
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = BengaliFormatter.formatDate(uiState.selectedSehriDate, lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = selectedHijri.format(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (uiState.selectedSehriDate != uiState.todayDate) {
                            OutlinedButton(
                                onClick = { onSelectDate(uiState.todayDate) },
                                interactionSource = todayInteraction,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .waqtPressScale(todayInteraction, reduceMotion)
                                    .testTag("sehri_today_button")
                            ) {
                                Text(strings.todayButton, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        IconButton(
                            onClick = { onSelectDate(uiState.selectedSehriDate.plusDays(1)) },
                            interactionSource = nextDayInteraction,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .waqtPressScale(nextDayInteraction, reduceMotion)
                                .testTag("sehri_next_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = strings.nextDayDesc
                            )
                        }
                    }
                }
            }

            if (selectedSchedule != null) {
                item(key = "sehri_iftar_cards", contentType = "boundary_cards") {
                    val sehriCutoff = remember(selectedSchedule, sehriBufferMins) {
                        selectedSchedule.sehriEnd.minusMinutes(sehriBufferMins.toLong())
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FastingBoundaryCard(
                            title = strings.sehriEndLabel,
                            time = BengaliFormatter.formatTime(sehriCutoff, lang, use24Hour),
                            note = if (sehriBufferMins > 0) {
                                "${strings.sehriCutoffNote} (${strings.bufferMinutesLabel(sehriBufferMins)})"
                            } else {
                                strings.sehriCutoffNote
                            },
                            accent = PrayerFajrAccent,
                            glow = PrayerFajrGlow,
                            icon = Icons.Rounded.Bedtime,
                            isDark = isDark,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sehri_time_card")
                        )
                        FastingBoundaryCard(
                            title = strings.iftarStartLabel,
                            time = BengaliFormatter.formatTime(selectedSchedule.iftarStart, lang, use24Hour),
                            note = strings.iftarCutoffNote,
                            accent = PrayerMaghribAccent,
                            glow = PrayerMaghribGlow,
                            icon = Icons.Rounded.WbTwilight,
                            isDark = isDark,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("iftar_time_card")
                        )
                    }
                }
            }

            item(key = "month_header", contentType = "month_header") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.monthlyTimetableTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onSelectMonth(uiState.selectedSehriMonth.minusMonths(1)) },
                            interactionSource = prevMonthInteraction,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .waqtPressScale(prevMonthInteraction, reduceMotion)
                                .testTag("sehri_prev_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = strings.previousMonthDesc
                            )
                        }
                        Text(
                            text = BengaliFormatter.formatMonthYear(
                                uiState.selectedSehriMonth.year,
                                uiState.selectedSehriMonth.monthValue,
                                lang
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = { onSelectMonth(uiState.selectedSehriMonth.plusMonths(1)) },
                            interactionSource = nextMonthInteraction,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .waqtPressScale(nextMonthInteraction, reduceMotion)
                                .testTag("sehri_next_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = strings.nextMonthDesc
                            )
                        }
                    }
                }
            }

            item(key = "table_columns", contentType = "table_header") {
                Card(
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isDark) 0.40f else 0.96f),
                                WaqtTeal.copy(alpha = 0.42f)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tableHeaderBrush)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.colDate,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = tableHeaderTextColor,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = strings.colSehri,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) PrayerFajrGlow else PrayerFajrAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = strings.colIftar,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) PrayerMaghribGlow else PrayerMaghribAccent,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            items(
                items = uiState.monthlySehriSchedules,
                key = { it.date.toString() },
                contentType = { "monthly_row" }
            ) { daySchedule ->
                MonthlySehriRow(
                    schedule = daySchedule,
                    isToday = daySchedule.date == uiState.todayDate,
                    isSelected = daySchedule.date == uiState.selectedSehriDate,
                    isDark = isDark,
                    language = lang,
                    use24HourFormat = use24Hour,
                    sehriBufferMinutes = sehriBufferMins,
                    onClick = { onSelectDate(daySchedule.date) }
                )
            }
        }
    }
}

/**
 * Isolated 1-second observer so the 30-day timetable never recomposes every second.
 */
@Composable
private fun SehriCountdownBanner(
    liveClockFlow: StateFlow<LiveClockState>,
    language: AppLanguage,
    isDark: Boolean,
    strings: WaqtStrings,
    modifier: Modifier = Modifier
) {
    val liveClock by liveClockFlow.collectAsStateWithLifecycle()
    val target = liveClock.sehriIftarCountdown ?: return

    val bannerBrush = remember(isDark, target.isSehriNext) {
        val accent = if (target.isSehriNext) WaqtTeal else PrayerMaghribAccent
        if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.14f),
                    accent.copy(alpha = 0.24f),
                    WaqtGold.copy(alpha = 0.14f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.64f),
                    accent.copy(alpha = 0.16f),
                    WaqtGoldBright.copy(alpha = 0.18f)
                )
            )
        }
    }
    val borderBrush = remember(isDark, target.isSehriNext) {
        val accent = if (target.isSehriNext) WaqtTeal else PrayerMaghribAccent
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.50f else 0.98f),
                accent.copy(alpha = 0.50f)
            )
        )
    }
    val labelColor = if (isDark) Color.White else WaqtNavyDeep
    val countdownColor = if (isDark) WaqtGoldBright else WaqtGoldDark
    val pillColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.72f)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, borderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBrush)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (target.isSehriNext) {
                    strings.sehriCountdownPrefix
                } else {
                    strings.iftarCountdownPrefix
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = pillColor,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.45f))
            ) {
                Text(
                    text = BengaliFormatter.formatCountdown(target.countdown, language),
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = countdownColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("sehri_iftar_countdown")
                )
            }
        }
    }
}

@Composable
private fun FastingBoundaryCard(
    title: String,
    time: String,
    note: String,
    accent: Color,
    glow: Color,
    icon: ImageVector,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    WaqtGlassCard(
        modifier = modifier,
        accentColor = accent,
        borderWidth = 1.25.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    accent.copy(alpha = 0.28f),
                                    glow.copy(alpha = 0.16f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDark) glow else accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) glow else accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthlySehriRow(
    schedule: DailyPrayerSchedule,
    isToday: Boolean,
    isSelected: Boolean,
    isDark: Boolean,
    language: AppLanguage,
    use24HourFormat: Boolean,
    sehriBufferMinutes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        isSelected -> Brush.linearGradient(
            listOf(
                WaqtGold.copy(alpha = if (isDark) 0.24f else 0.26f),
                WaqtTeal.copy(alpha = if (isDark) 0.14f else 0.12f)
            )
        )
        isToday -> Brush.linearGradient(
            listOf(
                WaqtEmeraldLight.copy(alpha = if (isDark) 0.22f else 0.18f),
                Color.White.copy(alpha = if (isDark) 0.08f else 0.50f)
            )
        )
        else -> Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.08f else 0.50f),
                Color.White.copy(alpha = if (isDark) 0.04f else 0.36f)
            )
        )
    }
    val sehriTime = remember(schedule, sehriBufferMinutes) {
        schedule.sehriEnd.minusMinutes(sehriBufferMinutes.toLong())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = BengaliFormatter.formatShortDate(schedule.date, language),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = BengaliFormatter.formatShortTime(sehriTime, language, use24HourFormat),
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) PrayerFajrGlow else PrayerFajrAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = BengaliFormatter.formatShortTime(schedule.iftarStart, language, use24HourFormat),
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) PrayerMaghribGlow else PrayerMaghribAccent,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f))
    }
}
