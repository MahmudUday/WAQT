package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.model.AppLanguage
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.ThemeMode
import com.example.ui.WaqtUiState
import com.example.ui.components.WaqtGlassCard
import com.example.ui.components.waqtPressScale
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.WaqtCyan
import com.example.ui.theme.WaqtEmerald
import com.example.ui.theme.WaqtEmeraldLight
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtGoldDark
import com.example.ui.theme.WaqtGoldSoft
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyDeep
import com.example.ui.theme.WaqtTeal
import com.example.ui.theme.WaqtViolet

private val HIJRI_OFFSETS = listOf(-2, -1, 0, 1, 2)
private val SEHRI_BUFFERS = listOf(0, 3, 5)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: WaqtUiState,
    strings: WaqtStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleReduceMotion: (Boolean) -> Unit,
    onToggle24HourFormat: (Boolean) -> Unit = {},
    onSelectHijriOffset: (Int) -> Unit = {},
    onSelectSehriBuffer: (Int) -> Unit = {},
    onOpenLocationPicker: () -> Unit,
    onSelectMethod: (CalculationMethod) -> Unit,
    onSelectAsrJuristic: (AsrJuristic) -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val settings = uiState.settings
    val reduceMotion = settings.reduceMotion
    val isDark = when (settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val locationLabel = if (settings.language == AppLanguage.BENGALI) {
        settings.locationNameBn
    } else {
        settings.locationNameEn
    }
    val locationCardInteraction = remember { MutableInteractionSource() }
    val shareBtnInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("settings_list"),
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
                    accentColor = WaqtViolet,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.settingsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
            }

            item(key = "location_card", contentType = "card") {
                WaqtGlassCard(
                    accentColor = WaqtEmeraldLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtPressScale(locationCardInteraction, reduceMotion)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(
                            interactionSource = locationCardInteraction,
                            indication = ripple(),
                            onClick = onOpenLocationPicker
                        )
                        .testTag("settings_location_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsSectionOrb(
                            icon = Icons.Rounded.LocationOn,
                            accent = if (isDark) WaqtEmeraldLight else WaqtEmerald
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.changeLocation,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = locationLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Language, Theme (Dark/Light/System), 12h/24h Clock & Reduce Motion
            item(key = "appearance_card", contentType = "card") {
                WaqtGlassCard(
                    accentColor = WaqtCyan,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionOrb(
                                icon = Icons.Rounded.Palette,
                                accent = WaqtCyan
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.appearanceSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = strings.languageLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val bnInteraction = remember { MutableInteractionSource() }
                            val enInteraction = remember { MutableInteractionSource() }
                            FilterChip(
                                selected = settings.language == AppLanguage.BENGALI,
                                onClick = { onSelectLanguage(AppLanguage.BENGALI) },
                                interactionSource = bnInteraction,
                                label = { Text("বাংলা") },
                                modifier = Modifier
                                    .waqtPressScale(bnInteraction, reduceMotion)
                                    .testTag("lang_chip_bn")
                            )
                            FilterChip(
                                selected = settings.language == AppLanguage.ENGLISH,
                                onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                                interactionSource = enInteraction,
                                label = { Text("English") },
                                modifier = Modifier
                                    .waqtPressScale(enInteraction, reduceMotion)
                                    .testTag("lang_chip_en")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.themeLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val icon = when (mode) {
                                    ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                                    ThemeMode.LIGHT -> Icons.Rounded.LightMode
                                    ThemeMode.DARK -> Icons.Rounded.DarkMode
                                }
                                val modeInteraction = remember { MutableInteractionSource() }
                                FilterChip(
                                    selected = settings.themeMode == mode,
                                    onClick = { onSelectThemeMode(mode) },
                                    interactionSource = modeInteraction,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text(strings.themeModeLabel(mode)) },
                                    modifier = Modifier
                                        .waqtPressScale(modeInteraction, reduceMotion)
                                        .testTag("theme_chip_${mode.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.timeFormatLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val chip12Interaction = remember { MutableInteractionSource() }
                            val chip24Interaction = remember { MutableInteractionSource() }
                            FilterChip(
                                selected = !settings.use24HourFormat,
                                onClick = { onToggle24HourFormat(false) },
                                interactionSource = chip12Interaction,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(strings.timeFormat12h) },
                                modifier = Modifier
                                    .waqtPressScale(chip12Interaction, reduceMotion)
                                    .testTag("time_format_12h")
                            )
                            FilterChip(
                                selected = settings.use24HourFormat,
                                onClick = { onToggle24HourFormat(true) },
                                interactionSource = chip24Interaction,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(strings.timeFormat24h) },
                                modifier = Modifier
                                    .waqtPressScale(chip24Interaction, reduceMotion)
                                    .testTag("time_format_24h")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.reduceMotionLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = strings.reduceMotionDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.reduceMotion,
                                onCheckedChange = onToggleReduceMotion,
                                modifier = Modifier.testTag("reduce_motion_switch")
                            )
                        }
                    }
                }
            }

            // Hijri Moon-Sighting Adjustment & Sehri Caution Buffer Preferences
            item(key = "calendar_prefs_card", contentType = "card") {
                WaqtGlassCard(
                    accentColor = WaqtViolet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionOrb(
                                icon = Icons.Rounded.CalendarMonth,
                                accent = WaqtViolet
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.calendarPreferencesSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = strings.hijriAdjustLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = strings.hijriAdjustDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HIJRI_OFFSETS.forEach { offset ->
                                val chipInteraction = remember { MutableInteractionSource() }
                                FilterChip(
                                    selected = settings.hijriOffsetDays == offset,
                                    onClick = { onSelectHijriOffset(offset) },
                                    interactionSource = chipInteraction,
                                    label = { Text(strings.hijriOffsetLabel(offset)) },
                                    modifier = Modifier
                                        .waqtPressScale(chipInteraction, reduceMotion)
                                        .testTag("hijri_offset_$offset")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = strings.sehriBufferLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = strings.sehriBufferDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SEHRI_BUFFERS.forEach { mins ->
                                val chipInteraction = remember { MutableInteractionSource() }
                                FilterChip(
                                    selected = settings.sehriBufferMinutes == mins,
                                    onClick = { onSelectSehriBuffer(mins) },
                                    interactionSource = chipInteraction,
                                    label = {
                                        Text(
                                            if (mins == 0) strings.noBufferLabel else strings.bufferMinutesLabel(mins)
                                        )
                                    },
                                    modifier = Modifier
                                        .waqtPressScale(chipInteraction, reduceMotion)
                                        .testTag("sehri_buffer_$mins")
                                )
                            }
                        }
                    }
                }
            }

            // Calculation Method & Asr Juristic Rule
            item(key = "calculation_card", contentType = "card") {
                WaqtGlassCard(
                    accentColor = WaqtGold,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SettingsSectionOrb(
                                icon = Icons.Rounded.Explore,
                                accent = WaqtGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.calculationSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = strings.asrRuleLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AsrJuristic.entries.forEach { rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { onSelectAsrJuristic(rule) }
                                    .testTag("asr_rule_${rule.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.asrJuristic == rule,
                                    onClick = { onSelectAsrJuristic(rule) }
                                )
                                Text(
                                    text = strings.asrRuleLabel(rule),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = strings.methodLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CalculationMethod.entries.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { onSelectMethod(method) }
                                    .testTag("calc_method_${method.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.calculationMethod == method,
                                    onClick = { onSelectMethod(method) }
                                )
                                Text(
                                    text = strings.methodLabel(method),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = WaqtGold.copy(alpha = if (isDark) 0.14f else 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.8.dp, WaqtGold.copy(alpha = 0.30f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.calculationDocNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Enhanced Aurora Crystal-Glass About Us & Developer Identity Card
            item(key = "about_card", contentType = "card") {
                val aboutBrush = remember(isDark) {
                    if (isDark) {
                        Brush.linearGradient(
                            listOf(
                                WaqtViolet.copy(alpha = 0.24f),
                                WaqtCyan.copy(alpha = 0.16f),
                                WaqtGold.copy(alpha = 0.14f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.68f),
                                WaqtViolet.copy(alpha = 0.14f),
                                WaqtCyan.copy(alpha = 0.12f),
                                WaqtGoldBright.copy(alpha = 0.16f)
                            )
                        )
                    }
                }
                val aboutBorder = remember(isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isDark) 0.52f else 0.98f),
                            WaqtGoldBright.copy(alpha = 0.55f)
                        )
                    )
                }
                val titleColor = if (isDark) WaqtGoldBright else WaqtNavyDeep
                val bodyColor = if (isDark) WaqtGoldSoft.copy(alpha = 0.92f) else WaqtNavy.copy(alpha = 0.86f)
                val mutedColor = if (isDark) WaqtGoldSoft.copy(alpha = 0.74f) else WaqtNavy.copy(alpha = 0.68f)

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.25.dp, aboutBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("about_us_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(aboutBrush)
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(WaqtGoldBright, WaqtViolet)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NightsStay,
                                        contentDescription = strings.aboutHeader,
                                        tint = WaqtNavyDeep,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.fullBrandName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor
                                    )
                                    Text(
                                        text = strings.aboutHeader,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = mutedColor
                                    )
                                }
                            }

                            Surface(
                                color = WaqtGold.copy(alpha = if (isDark) 0.24f else 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) WaqtGoldBright else WaqtGoldDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.aboutDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = bodyColor
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = WaqtEmerald.copy(alpha = if (isDark) 0.22f else 0.14f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, WaqtEmerald.copy(alpha = 0.42f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = if (isDark) WaqtEmeraldLight else WaqtEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.aboutPrivacyBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) WaqtEmeraldLight else WaqtEmerald
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AboutFeatureChip(
                                icon = Icons.Rounded.LocationOn,
                                label = strings.aboutFeatureOffline,
                                accent = WaqtCyan,
                                isDark = isDark
                            )
                            AboutFeatureChip(
                                icon = Icons.Rounded.Bedtime,
                                label = strings.aboutFeatureTahajjud,
                                accent = WaqtViolet,
                                isDark = isDark
                            )
                            AboutFeatureChip(
                                icon = Icons.Rounded.NotificationsActive,
                                label = strings.aboutFeatureSmartAlert,
                                accent = WaqtTeal,
                                isDark = isDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = WaqtGold.copy(alpha = 0.28f))
                        Spacer(modifier = Modifier.height(10.dp))

                        AboutLinkRow(
                            icon = Icons.Rounded.Person,
                            label = strings.developerLabel,
                            value = strings.developerName,
                            isDark = isDark,
                            onClick = null
                        )
                        AboutLinkRow(
                            icon = Icons.Rounded.Code,
                            label = strings.repositoryLabel,
                            value = "github.com/MahmudUday/namaz-app",
                            isDark = isDark,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/MahmudUday/namaz-app")
                                        )
                                    )
                                }
                            }
                        )
                        AboutLinkRow(
                            icon = Icons.Rounded.Email,
                            label = strings.contactLabel,
                            value = "mahmududay.info@gmail.com",
                            isDark = isDark,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_SENDTO,
                                            Uri.parse("mailto:mahmududay.info@gmail.com")
                                        )
                                    )
                                }
                            }
                        )
                        AboutLinkRow(
                            icon = Icons.Rounded.AutoAwesome,
                            label = strings.versionLabel,
                            value = BuildConfig.VERSION_NAME,
                            isDark = isDark,
                            onClick = null
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.72f),
                            border = BorderStroke(
                                1.1.dp,
                                Brush.linearGradient(listOf(WaqtGoldBright, WaqtCyan))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .waqtPressScale(shareBtnInteraction, reduceMotion)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = shareBtnInteraction,
                                    indication = ripple(),
                                    onClick = {
                                        runCatching {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, strings.fullBrandName)
                                                putExtra(Intent.EXTRA_TEXT, strings.shareAppMessage)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(sendIntent, strings.shareAppLabel)
                                            )
                                        }
                                    }
                                )
                                .testTag("share_app_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = strings.shareAppLabel,
                                    tint = if (isDark) WaqtGoldBright else WaqtNavyDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.shareAppLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else WaqtNavyDeep
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionOrb(
    icon: ImageVector,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AboutFeatureChip(
    icon: ImageVector,
    label: String,
    accent: Color,
    isDark: Boolean
) {
    Surface(
        color = accent.copy(alpha = if (isDark) 0.18f else 0.12f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(0.9.dp, accent.copy(alpha = 0.40f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) WaqtGoldBright else accent,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else WaqtNavyDeep
            )
        }
    }
}

@Composable
private fun AboutLinkRow(
    icon: ImageVector,
    label: String,
    value: String,
    isDark: Boolean,
    onClick: (() -> Unit)?
) {
    val labelColor = if (isDark) WaqtGoldSoft.copy(alpha = 0.72f) else WaqtNavy.copy(alpha = 0.70f)
    val valueColor = if (isDark) Color.White else WaqtNavyDeep
    val iconColor = if (isDark) WaqtGoldBright else WaqtGoldDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .let { mod ->
                if (onClick != null) mod.clickable(onClick = onClick) else mod
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
