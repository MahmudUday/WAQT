package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BengaliFormatter
import com.example.data.HijriCalendarHelper
import com.example.model.AppLanguage
import com.example.model.DailyPrayerSchedule
import com.example.ui.LiveClockState
import com.example.ui.WaqtUiState
import com.example.ui.components.WaqtGlassCard
import com.example.ui.components.waqtPressScale
import com.example.ui.components.waqtScrollItemMotion
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.PrayerFajrAccent
import com.example.ui.theme.PrayerMaghribAccent
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldSoft
import com.example.ui.theme.WaqtNavy
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
    onOpenLocationPicker: () -> Unit
) {
    val lang = uiState.settings.language
    val reduceMotion = uiState.settings.reduceMotion
    val selectedSchedule = uiState.selectedSehriSchedule ?: uiState.todaySchedule
    val locationLabel = if (lang == AppLanguage.BENGALI) {
        uiState.settings.locationNameBn
    } else {
        uiState.settings.locationNameEn
    }
    val selectedHijri = remember(uiState.selectedSehriDate) {
        HijriCalendarHelper.fromGregorian(uiState.selectedSehriDate)
    }
    val listState = rememberLazyListState()

    val locationInteraction = remember { MutableInteractionSource() }
    val prevDayInteraction = remember { MutableInteractionSource() }
    val todayInteraction = remember { MutableInteractionSource() }
    val nextDayInteraction = remember { MutableInteractionSource() }
    val prevMonthInteraction = remember { MutableInteractionSource() }
    val nextMonthInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "header", reduceMotion),
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
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                        border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.45f)),
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
                                tint = WaqtGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationLabel,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            item(key = "countdown_banner") {
                SehriCountdownBanner(
                    liveClockFlow = liveClockFlow,
                    language = lang,
                    strings = strings,
                    modifier = Modifier.waqtScrollItemMotion(listState, "countdown_banner", reduceMotion)
                )
            }

            item(key = "date_nav") {
                WaqtGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "date_nav", reduceMotion)
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
                item(key = "sehri_iftar_cards") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .waqtScrollItemMotion(listState, "sehri_iftar_cards", reduceMotion),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FastingBoundaryCard(
                            title = strings.sehriEndLabel,
                            time = BengaliFormatter.formatTime(selectedSchedule.sehriEnd, lang),
                            note = strings.sehriCutoffNote,
                            accent = PrayerFajrAccent,
                            icon = Icons.Rounded.Bedtime,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sehri_time_card")
                        )
                        FastingBoundaryCard(
                            title = strings.iftarStartLabel,
                            time = BengaliFormatter.formatTime(selectedSchedule.iftarStart, lang),
                            note = strings.iftarCutoffNote,
                            accent = PrayerMaghribAccent,
                            icon = Icons.Rounded.WbTwilight,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("iftar_time_card")
                        )
                    }
                }
            }

            item(key = "month_header") {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "month_header", reduceMotion),
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

            item(key = "table_columns") {
                Surface(
                    color = WaqtNavy.copy(alpha = 0.80f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.30f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "table_columns", reduceMotion)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.colDate,
                            style = MaterialTheme.typography.labelLarge,
                            color = WaqtGoldSoft,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = strings.colSehri,
                            style = MaterialTheme.typography.labelLarge,
                            color = WaqtGoldSoft,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = strings.colIftar,
                            style = MaterialTheme.typography.labelLarge,
                            color = WaqtGoldSoft,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            items(
                items = uiState.monthlySehriSchedules,
                key = { it.date.toString() }
            ) { daySchedule ->
                val dateKey = daySchedule.date.toString()
                MonthlySehriRow(
                    schedule = daySchedule,
                    isToday = daySchedule.date == uiState.todayDate,
                    isSelected = daySchedule.date == uiState.selectedSehriDate,
                    language = lang,
                    onClick = { onSelectDate(daySchedule.date) },
                    modifier = Modifier.waqtScrollItemMotion(listState, dateKey, reduceMotion)
                )
            }

            item(key = "bottom_space") {
                Spacer(modifier = Modifier.height(14.dp))
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
    strings: WaqtStrings,
    modifier: Modifier = Modifier
) {
    val liveClock by liveClockFlow.collectAsStateWithLifecycle()
    val target = liveClock.sehriIftarCountdown ?: return

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WaqtNavy.copy(alpha = 0.78f)),
        border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.50f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                color = WaqtGoldSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = BengaliFormatter.formatCountdown(target.countdown, language),
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = WaqtGold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.testTag("sehri_iftar_countdown")
            )
        }
    }
}

@Composable
private fun FastingBoundaryCard(
    title: String,
    time: String,
    note: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    WaqtGlassCard(
        modifier = modifier,
        borderColor = accent.copy(alpha = 0.45f),
        borderWidth = 1.3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        isSelected -> WaqtGold.copy(alpha = 0.22f)
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
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
                text = BengaliFormatter.formatShortTime(schedule.sehriEnd, language),
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                color = PrayerFajrAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = BengaliFormatter.formatShortTime(schedule.iftarStart, language),
                style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                color = PrayerMaghribAccent,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f))
    }
}
