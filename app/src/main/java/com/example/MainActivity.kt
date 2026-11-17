package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notifications.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.components.AnimatedIntroOverlay
import com.example.ui.components.LocationPickerSheet
import com.example.ui.components.WaqtAmbientBackground
import com.example.ui.components.waqtPressScale
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SehriIftarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtTheme

private enum class WaqtDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TODAY("today", Icons.Rounded.AccessTimeFilled, Icons.Outlined.AccessTime),
    SEHRI_IFTAR("sehri_iftar", Icons.Rounded.NightsStay, Icons.Outlined.NightsStay),
    ALARMS("alarms", Icons.Rounded.Notifications, Icons.Outlined.Notifications),
    SETTINGS("settings", Icons.Rounded.Settings, Icons.Outlined.Settings);

    fun label(strings: WaqtStrings): String = when (this) {
        TODAY -> strings.navToday
        SEHRI_IFTAR -> strings.navSehriIftar
        ALARMS -> strings.navAlarms
        SETTINGS -> strings.navSettings
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableHighestRefreshRate()
        NotificationHelper.stopAlarmFeedback()
        setContent {
            val viewModel: MainViewModel = viewModel()
            WaqtApp(viewModel = viewModel)
        }
    }

    @Suppress("DEPRECATION")
    private fun enableHighestRefreshRate() {
        runCatching {
            val displayObj = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                windowManager?.defaultDisplay
            } ?: return
            val currentMode = displayObj.mode
            val sameResolutionModes = displayObj.supportedModes.filter { mode ->
                mode.physicalWidth == currentMode.physicalWidth &&
                    mode.physicalHeight == currentMode.physicalHeight
            }
            val bestMode = (if (sameResolutionModes.isNotEmpty()) {
                sameResolutionModes
            } else {
                displayObj.supportedModes.toList()
            }).maxByOrNull { it.refreshRate } ?: return

            val params = window.attributes
            params.preferredDisplayModeId = bestMode.modeId
            params.preferredRefreshRate = bestMode.refreshRate
            window.attributes = params
        }
    }
}

@Composable
fun WaqtApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = remember(uiState.settings.language) {
        WaqtStrings(uiState.settings.language)
    }
    val reduceMotion = uiState.settings.reduceMotion
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: WaqtDestination.TODAY.route
    var showLocationSheet by remember { mutableStateOf(false) }

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    WaqtTheme(themeMode = uiState.settings.themeMode) {
        WaqtAmbientBackground(themeMode = uiState.settings.themeMode) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 680.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (!useNavRail) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                            ) {
                                WaqtDestination.entries.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    val label = dest.label(strings)
                                    val itemInteraction = remember { MutableInteractionSource() }
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest.route) },
                                        interactionSource = itemInteraction,
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = { Text(label, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = WaqtGold.copy(alpha = 0.28f)
                                        ),
                                        modifier = Modifier
                                            .waqtPressScale(itemInteraction, reduceMotion)
                                            .testTag("nav_${dest.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                            ) {
                                WaqtDestination.entries.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    val label = dest.label(strings)
                                    val railInteraction = remember { MutableInteractionSource() }
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest.route) },
                                        interactionSource = railInteraction,
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = { Text(label, maxLines = 1) },
                                        colors = NavigationRailItemDefaults.colors(
                                            indicatorColor = WaqtGold.copy(alpha = 0.28f)
                                        ),
                                        modifier = Modifier
                                            .waqtPressScale(railInteraction, reduceMotion)
                                            .testTag("rail_${dest.route}")
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = WaqtDestination.TODAY.route,
                            modifier = Modifier.weight(1f),
                            enterTransition = {
                                if (reduceMotion) {
                                    EnterTransition.None
                                } else {
                                    fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                                        slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { it / 16 }
                                }
                            },
                            exitTransition = {
                                if (reduceMotion) {
                                    ExitTransition.None
                                } else {
                                    fadeOut(tween(170)) +
                                        slideOutHorizontally(tween(170)) { -it / 24 }
                                }
                            },
                            popEnterTransition = {
                                if (reduceMotion) {
                                    EnterTransition.None
                                } else {
                                    fadeIn(tween(220, easing = FastOutSlowInEasing))
                                }
                            },
                            popExitTransition = {
                                if (reduceMotion) {
                                    ExitTransition.None
                                } else {
                                    fadeOut(tween(170))
                                }
                            }
                        ) {
                            composable(WaqtDestination.TODAY.route) {
                                HomeScreen(
                                    uiState = uiState,
                                    liveClockFlow = viewModel.liveClock,
                                    strings = strings,
                                    onOpenLocationPicker = {
                                        viewModel.clearGpsMessage()
                                        showLocationSheet = true
                                    },
                                    onTogglePrayerAlert = viewModel::togglePrayerNotification,
                                    onToggleTahajjudAlert = viewModel::toggleTahajjudAlert
                                )
                            }
                            composable(WaqtDestination.SEHRI_IFTAR.route) {
                                SehriIftarScreen(
                                    uiState = uiState,
                                    liveClockFlow = viewModel.liveClock,
                                    strings = strings,
                                    onSelectDate = viewModel::setSehriDate,
                                    onSelectMonth = viewModel::setSehriMonth,
                                    onOpenLocationPicker = {
                                        viewModel.clearGpsMessage()
                                        showLocationSheet = true
                                    }
                                )
                            }
                            composable(WaqtDestination.ALARMS.route) {
                                AlarmsScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onTogglePrayer = viewModel::togglePrayerNotification,
                                    onToggleWaqtEndAlert = viewModel::toggleWaqtEndAlert,
                                    onToggleTahajjudAlert = viewModel::toggleTahajjudAlert,
                                    onSelectReminder = viewModel::setReminderOption,
                                    onSelectTone = viewModel::setNotificationTone,
                                    onSendTestNotification = viewModel::sendTestNotification,
                                    onRefreshPermissions = viewModel::refreshSystemPermissionStates
                                )
                            }
                            composable(WaqtDestination.SETTINGS.route) {
                                SettingsScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onSelectLanguage = viewModel::setLanguage,
                                    onSelectThemeMode = viewModel::setThemeMode,
                                    onToggleReduceMotion = viewModel::setReduceMotion,
                                    onOpenLocationPicker = {
                                        viewModel.clearGpsMessage()
                                        showLocationSheet = true
                                    },
                                    onSelectMethod = viewModel::setCalculationMethod,
                                    onSelectAsrJuristic = viewModel::setAsrJuristic
                                )
                            }
                        }
                    }
                }
            }

            if (showLocationSheet) {
                LocationPickerSheet(
                    selectedDistrictId = uiState.settings.selectedDistrictId,
                    isDetectingGps = uiState.isDetectingGps,
                    gpsMessage = uiState.gpsMessage,
                    strings = strings,
                    onSelectDistrict = viewModel::selectDistrict,
                    onRequestGps = viewModel::detectGpsLocation,
                    onPermissionDenied = viewModel::onLocationPermissionDenied,
                    onDismiss = { showLocationSheet = false }
                )
            }

            AnimatedIntroOverlay(
                visible = uiState.showSplash,
                reduceMotion = reduceMotion,
                strings = strings,
                onDismiss = viewModel::dismissSplash
            )
        }
    }
}
