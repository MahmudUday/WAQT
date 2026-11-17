package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.NotificationTone
import com.example.model.PrayerType
import com.example.model.ReminderOption
import com.example.ui.WaqtUiState
import com.example.ui.components.WaqtGlassCard
import com.example.ui.components.waqtPressScale
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.WaqtAmber
import com.example.ui.theme.WaqtEmerald
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmsScreen(
    uiState: WaqtUiState,
    strings: WaqtStrings,
    onTogglePrayer: (PrayerType, Boolean) -> Unit,
    onToggleWaqtEndAlert: (Boolean) -> Unit,
    onToggleTahajjudAlert: (Boolean) -> Unit,
    onSelectReminder: (ReminderOption) -> Unit,
    onSelectTone: (NotificationTone) -> Unit,
    onSendTestNotification: () -> Unit,
    onRefreshPermissions: () -> Unit,
    bottomBarPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var testSentFeedback by remember { mutableStateOf(false) }
    val reduceMotion = uiState.settings.reduceMotion
    val testBtnInteraction = remember { MutableInteractionSource() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshPermissions()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
                .testTag("alarms_list"),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 18.dp + bottomBarPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header", contentType = "header") {
                Column {
                    Text(
                        text = strings.alarmsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.alarmsSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!uiState.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item(key = "notif_perm", contentType = "warning") {
                    PermissionWarningCard(
                        title = strings.notificationPermissionTitle,
                        body = strings.notificationPermissionBody,
                        buttonLabel = strings.grantPermissionButton,
                        accent = WaqtRose,
                        icon = Icons.Rounded.NotificationsActive,
                        reduceMotion = reduceMotion,
                        testTag = "grant_notification_permission_button",
                        onAction = {
                            notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }

            if (!uiState.canScheduleExactAlarms && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item(key = "exact_alarm_perm", contentType = "warning") {
                    PermissionWarningCard(
                        title = strings.exactAlarmWarningTitle,
                        body = strings.exactAlarmWarningBody,
                        buttonLabel = strings.openSettingsButton,
                        accent = WaqtAmber,
                        icon = Icons.Rounded.Schedule,
                        reduceMotion = reduceMotion,
                        testTag = "grant_exact_alarm_button",
                        onAction = {
                            runCatching {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            if (!uiState.isIgnoringBatteryOptimizations) {
                item(key = "battery_tip", contentType = "warning") {
                    PermissionWarningCard(
                        title = strings.batteryTipTitle,
                        body = strings.batteryTipBody,
                        buttonLabel = strings.openSettingsButton,
                        accent = WaqtAmber,
                        icon = Icons.Rounded.BatteryAlert,
                        reduceMotion = reduceMotion,
                        testTag = "battery_optimization_button",
                        onAction = {
                            runCatching {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            // Active 7-day summary badge
            item(key = "active_summary", contentType = "badge") {
                Surface(
                    color = WaqtEmerald.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WaqtEmerald.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AlarmOn,
                            contentDescription = null,
                            tint = WaqtEmerald
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.alarmScheduledSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WaqtEmerald
                        )
                    }
                }
            }

            // 20-Min Before Waqt Ends + Tahajjud Smart Alerts Card
            item(key = "smart_alerts_card", contentType = "card") {
                WaqtGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HourglassBottom,
                                contentDescription = null,
                                tint = WaqtGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.waqtEndAlertTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.waqtEndAlertDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.settings.waqtEndAlertEnabled,
                                onCheckedChange = onToggleWaqtEndAlert,
                                modifier = Modifier.testTag("waqt_end_alert_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = WaqtGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.tahajjudAlertTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.tahajjudAlertDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.settings.tahajjudAlertEnabled,
                                onCheckedChange = onToggleTahajjudAlert,
                                modifier = Modifier.testTag("tahajjud_alert_switch")
                            )
                        }
                    }
                }
            }

            // Individual 5 Prayer Toggles
            item(key = "prayer_toggles", contentType = "card") {
                WaqtGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.prayerTogglesHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PrayerType.entries.forEachIndexed { index, prayer ->
                            val enabled = prayer in uiState.settings.enabledPrayers
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.prayerName(prayer),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = strings.prayerSubLabel(prayer),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = { onTogglePrayer(prayer, it) },
                                    modifier = Modifier.testTag("alarm_switch_${prayer.name.lowercase()}")
                                )
                            }
                            if (index < PrayerType.entries.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Pre-Prayer Start Reminder Selector (Off, 5, 20, 40 min)
            item(key = "reminder_options", contentType = "card") {
                WaqtGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.reminderBeforeHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderOption.entries.forEach { option ->
                                val selected = uiState.settings.reminderOption == option
                                val chipInteraction = remember { MutableInteractionSource() }
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectReminder(option) },
                                    interactionSource = chipInteraction,
                                    label = { Text(strings.reminderLabel(option)) },
                                    modifier = Modifier
                                        .waqtPressScale(chipInteraction, reduceMotion)
                                        .testTag("reminder_chip_${option.minutesBefore}")
                                )
                            }
                        }
                    }
                }
            }

            // Alarm & Notification Tone Selector
            item(key = "tone_selector", contentType = "card") {
                WaqtGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.toneHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        NotificationTone.entries.forEach { tone ->
                            val selected = uiState.settings.notificationTone == tone
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clickable { onSelectTone(tone) }
                                    .testTag("tone_option_${tone.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { onSelectTone(tone) }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.toneLabel(tone),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            // Test Alarm Sound & Notification Button
            item(key = "test_notif", contentType = "button") {
                OutlinedButton(
                    onClick = {
                        if (!uiState.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onSendTestNotification()
                            testSentFeedback = true
                        }
                    },
                    interactionSource = testBtnInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .waqtPressScale(testBtnInteraction, reduceMotion)
                        .testTag("send_test_notification_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (testSentFeedback) strings.testNotificationSent else strings.testNotificationButton
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionWarningCard(
    title: String,
    body: String,
    buttonLabel: String,
    accent: Color,
    icon: ImageVector,
    reduceMotion: Boolean,
    testTag: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val btnInteraction = remember { MutableInteractionSource() }
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.14f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.44f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                interactionSource = btnInteraction,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .waqtPressScale(btnInteraction, reduceMotion)
                    .testTag(testTag)
            ) {
                Text(buttonLabel)
            }
        }
    }
}
