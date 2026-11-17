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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.components.WaqtGlassBarSurface
import com.example.ui.components.WaqtGlassNavItem
import com.example.ui.components.WaqtSlidingGlassBottomBar
import com.example.ui.components.waqtPressScale
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SehriIftarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.PrayerAsrAccent
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtTeal
import com.example.ui.theme.WaqtTheme
import com.example.ui.theme.WaqtViolet

private enum class WaqtDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color
) {
    TODAY("today", Icons.Rounded.AccessTimeFilled, Icons.Outlined.AccessTime, WaqtGoldBright),
    SEHRI_IFTAR("sehri_iftar", Icons.Rounded.NightsStay, Icons.Outlined.NightsStay, WaqtTeal),
    ALARMS("alarms", Icons.Rounded.Notifications, Icons.Outlined.Notifications, PrayerAsrAccent),
    SETTINGS("settings", Icons.Rounded.Settings, Icons.Outlined.Settings, WaqtViolet);

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

    val navigateTo: (String) -> Unit = remember(navController) {
        { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val openLocationPicker: () -> Unit = remember(viewModel) {
        {
            viewModel.clearGpsMessage()
            showLocationSheet = true
        }
    }

    val navItems = remember(strings) {
        WaqtDestination.entries.map { dest ->
            WaqtGlassNavItem(
                route = dest.route,
                label = dest.label(strings),
                selectedIcon = dest.selectedIcon,
                unselectedIcon = dest.unselectedIcon,
                accentColor = dest.accentColor,
                testTag = "nav_${dest.route}"
            )
        }
    }
    val selectedIndex = remember(currentRoute) {
        WaqtDestination.entries.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    WaqtTheme(themeMode = uiState.settings.themeMode) {
        WaqtAmbientBackground(themeMode = uiState.settings.themeMode) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavRail = maxWidth >= 680.dp
                val layoutDirection = LocalLayoutDirection.current

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (!useNavRail) {
                            WaqtSlidingGlassBottomBar(
                                items = navItems,
                                selectedIndex = selectedIndex,
                                reduceMotion = reduceMotion,
                                onSelectRoute = navigateTo
                            )
                        }
                    }
                ) { innerPadding ->
                    val bottomBarPadding = if (useNavRail) 0.dp else innerPadding.calculateBottomPadding()
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = innerPadding.calculateStartPadding(layoutDirection),
                                top = innerPadding.calculateTopPadding(),
                                end = innerPadding.calculateEndPadding(layoutDirection)
                            )
                    ) {
                        if (useNavRail) {
                            WaqtGlassBarSurface(
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 10.dp)
                            ) {
                                NavigationRail(
                                    containerColor = Color.Transparent
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
                                            label = {
                                                Text(
                                                    text = label,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = dest.accentColor,
                                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                indicatorColor = dest.accentColor.copy(alpha = 0.22f)
                                            ),
                                            modifier = Modifier
                                                .waqtPressScale(railInteraction, reduceMotion)
                                                .testTag("rail_${dest.route}")
                                        )
                                    }
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
                                    fadeIn(tween(180, easing = FastOutSlowInEasing)) +
                                        slideInHorizontally(tween(180, easing = FastOutSlowInEasing)) { it / 20 }
                                }
                            },
                            exitTransition = {
                                if (reduceMotion) {
                                    ExitTransition.None
                                } else {
                                    fadeOut(tween(140)) +
                                        slideOutHorizontally(tween(140)) { -it / 28 }
                                }
                            },
                            popEnterTransition = {
                                if (reduceMotion) {
                                    EnterTransition.None
                                } else {
                                    fadeIn(tween(180, easing = FastOutSlowInEasing))
                                }
                            },
                            popExitTransition = {
                                if (reduceMotion) {
                                    ExitTransition.None
                                } else {
                                    fadeOut(tween(140))
                                }
                            }
                        ) {
                            composable(WaqtDestination.TODAY.route) {
                                HomeScreen(
                                    uiState = uiState,
                                    liveClockFlow = viewModel.liveClock,
                                    strings = strings,
                                    onOpenLocationPicker = openLocationPicker,
                                    onTogglePrayerAlert = viewModel::togglePrayerNotification,
                                    onToggleTahajjudAlert = viewModel::toggleTahajjudAlert,
                                    bottomBarPadding = bottomBarPadding
                                )
                            }
                            composable(WaqtDestination.SEHRI_IFTAR.route) {
                                SehriIftarScreen(
                                    uiState = uiState,
                                    liveClockFlow = viewModel.liveClock,
                                    strings = strings,
                                    onSelectDate = viewModel::setSehriDate,
                                    onSelectMonth = viewModel::setSehriMonth,
                                    onOpenLocationPicker = openLocationPicker,
                                    bottomBarPadding = bottomBarPadding
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
                                    onRefreshPermissions = viewModel::refreshSystemPermissionStates,
                                    bottomBarPadding = bottomBarPadding
                                )
                            }
                            composable(WaqtDestination.SETTINGS.route) {
                                SettingsScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onSelectLanguage = viewModel::setLanguage,
                                    onSelectThemeMode = viewModel::setThemeMode,
                                    onToggleReduceMotion = viewModel::setReduceMotion,
                                    onToggle24HourFormat = viewModel::setUse24HourFormat,
                                    onSelectHijriOffset = viewModel::setHijriOffsetDays,
                                    onSelectSehriBuffer = viewModel::setSehriBufferMinutes,
                                    onOpenLocationPicker = openLocationPicker,
                                    onSelectMethod = viewModel::setCalculationMethod,
                                    onSelectAsrJuristic = viewModel::setAsrJuristic,
                                    bottomBarPadding = bottomBarPadding
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
