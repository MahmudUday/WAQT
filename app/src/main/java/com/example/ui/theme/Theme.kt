package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.model.ThemeMode

private val WaqtLightColorScheme = lightColorScheme(
    primary = WaqtNavy,
    onPrimary = Color.White,
    primaryContainer = WaqtGoldSoft,
    onPrimaryContainer = WaqtNavyDeep,
    secondary = WaqtGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E6BC),
    onSecondaryContainer = WaqtNavyDeep,
    tertiary = WaqtEmerald,
    onTertiary = Color.White,
    background = WaqtCreamBg,
    onBackground = WaqtInkPrimary,
    surface = WaqtCreamSurface,
    onSurface = WaqtInkPrimary,
    surfaceVariant = WaqtCreamVariant,
    onSurfaceVariant = WaqtInkSecondary,
    outline = Color(0xFFD6CEBC),
    outlineVariant = Color(0xFFE7E0D0),
    error = WaqtRose,
    onError = Color.White
)

private val WaqtDarkColorScheme = darkColorScheme(
    primary = WaqtGold,
    onPrimary = WaqtNavyDeep,
    primaryContainer = WaqtNavyCard,
    onPrimaryContainer = WaqtGoldSoft,
    secondary = WaqtGoldBright,
    onSecondary = WaqtNavyDeep,
    secondaryContainer = WaqtNavyMuted,
    onSecondaryContainer = WaqtTextLight,
    tertiary = WaqtEmeraldLight,
    onTertiary = WaqtNavyDeep,
    background = WaqtNavyDeep,
    onBackground = WaqtTextLight,
    surface = WaqtNavy,
    onSurface = WaqtTextLight,
    surfaceVariant = WaqtNavySurface,
    onSurfaceVariant = WaqtTextMutedDark,
    outline = Color(0xFF2B4670),
    outlineVariant = Color(0xFF1D3459),
    error = Color(0xFFEF7A86),
    onError = WaqtNavyDeep
)

val WaqtShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun WaqtTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (useDarkTheme) WaqtDarkColorScheme else WaqtLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(useDarkTheme, view) {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WaqtTypography,
        shapes = WaqtShapes,
        content = content
    )
}
