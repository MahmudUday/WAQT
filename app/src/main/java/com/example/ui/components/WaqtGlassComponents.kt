package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode
import com.example.ui.theme.PrayerFajrAccent
import com.example.ui.theme.PrayerIshaAccent
import com.example.ui.theme.WaqtCreamBg
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyDeep

/**
 * Lightweight GPU-accelerated press-scale feedback for buttons and interactive cards.
 * Uses graphicsLayer to avoid layout passes on 90Hz/120Hz displays.
 */
fun Modifier.waqtPressScale(
    enabled: Boolean = true,
    reduceMotion: Boolean = false
): Modifier = composed {
    if (!enabled || reduceMotion) return@composed this
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "waqt_press_scale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
        }
}

/**
 * Subtle ambient luminous backdrop so translucent glass cards show depth and contrast
 * without using expensive runtime blur filters.
 */
@Composable
fun WaqtAmbientBackground(
    themeMode: ThemeMode,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val baseBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(WaqtNavyDeep, WaqtNavy, Color(0xFF091830))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFFDFBF5), WaqtCreamBg, Color(0xFFF1EBDC))
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WaqtGold.copy(alpha = if (isDark) 0.14f else 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.10f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.85f, h * 0.10f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isDark) PrayerIshaAccent else PrayerFajrAccent).copy(alpha = if (isDark) 0.18f else 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.12f, h * 0.52f),
                    radius = w * 0.70f
                ),
                radius = w * 0.70f,
                center = Offset(w * 0.12f, h * 0.52f)
            )
        }
        content()
    }
}

/**
 * Translucent glassmorphic card with specular luminous edge border.
 */
@Composable
fun WaqtGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    tintColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == WaqtNavyDeep
    val glassBg = tintColor ?: if (isDark) {
        Color(0xFF132A4E).copy(alpha = 0.74f)
    } else {
        Color.White.copy(alpha = 0.78f)
    }
    val edgeColor = borderColor ?: if (isDark) {
        WaqtGold.copy(alpha = 0.24f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = glassBg),
        border = BorderStroke(borderWidth, edgeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}
