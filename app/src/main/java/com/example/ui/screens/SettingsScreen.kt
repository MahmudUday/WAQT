package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.model.AppLanguage
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.ThemeMode
import com.example.ui.WaqtUiState
import com.example.ui.components.WaqtGlassCard
import com.example.ui.components.waqtPressScale
import com.example.ui.components.waqtScrollItemMotion
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldSoft
import com.example.ui.theme.WaqtNavy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: WaqtUiState,
    strings: WaqtStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onToggleReduceMotion: (Boolean) -> Unit,
    onOpenLocationPicker: () -> Unit,
    onSelectMethod: (CalculationMethod) -> Unit,
    onSelectAsrJuristic: (AsrJuristic) -> Unit
) {
    val context = LocalContext.current
    val settings = uiState.settings
    val reduceMotion = settings.reduceMotion
    val locationLabel = if (settings.language == AppLanguage.BENGALI) {
        settings.locationNameBn
    } else {
        settings.locationNameEn
    }
    val listState = rememberLazyListState()
    val locationCardInteraction = remember { MutableInteractionSource() }

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
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.waqtScrollItemMotion(listState, "header", reduceMotion)
                )
            }

            item(key = "location_card") {
                WaqtGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "location_card", reduceMotion)
                        .waqtPressScale(locationCardInteraction, reduceMotion)
                        .clip(RoundedCornerShape(20.dp))
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
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = WaqtGold
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

            // Language, Theme (Dark/Light/System) & Reduce Motion
            item(key = "appearance_card") {
                WaqtGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "appearance_card", reduceMotion)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.appearanceSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
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

            // Calculation Method & Asr Juristic Rule
            item(key = "calculation_card") {
                WaqtGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "calculation_card", reduceMotion)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.calculationSection,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
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
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.calculationDocNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Preserved About Us & Developer Identity Card
            item(key = "about_card") {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqtNavy.copy(alpha = 0.78f)),
                    border = BorderStroke(1.dp, WaqtGold.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .waqtScrollItemMotion(listState, "about_card", reduceMotion)
                        .testTag("about_us_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = strings.aboutHeader,
                                tint = WaqtGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${strings.aboutHeader} — ${strings.fullBrandName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WaqtGold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.aboutDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WaqtGoldSoft.copy(alpha = 0.92f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = WaqtGold.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(10.dp))

                        AboutLinkRow(
                            icon = Icons.Rounded.Person,
                            label = strings.developerLabel,
                            value = strings.developerName,
                            onClick = null
                        )
                        AboutLinkRow(
                            icon = Icons.Rounded.Code,
                            label = strings.repositoryLabel,
                            value = "github.com/MahmudUday/namaz-app",
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
                            icon = Icons.Rounded.Info,
                            label = strings.versionLabel,
                            value = BuildConfig.VERSION_NAME,
                            onClick = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun AboutLinkRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)?
) {
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
            tint = WaqtGold,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = WaqtGoldSoft.copy(alpha = 0.72f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = WaqtGoldSoft,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
