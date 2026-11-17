package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode
import com.example.ui.theme.PrayerFajrAccent
import com.example.ui.theme.PrayerIshaAccent
import com.example.ui.theme.WaqtCreamBg
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyDeep
import kotlin.math.abs

/**
 * Lightweight GPU-only press-scale animation backed by Compose's existing InteractionSource.
 * Never intercepts scroll gestures or allocates raw pointerInput coroutines.
 */
fun Modifier.waqtPressScale(
    interactionSource: MutableInteractionSource,
    reduceMotion: Boolean = false
): Modifier = composed {
    if (reduceMotion) return@composed this
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "waqt_press_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * 120Hz/144Hz GPU-accelerated scroll item animation.
 * Reads LazyListState.layoutInfo exclusively inside graphicsLayer{} (render phase) so it
 * causes ZERO composition or layout passes during fast flings.
 */
fun Modifier.waqtScrollItemMotion(
    listState: LazyListState,
    itemKey: Any,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this
    return this.graphicsLayer {
        val layoutInfo = listState.layoutInfo
        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.key == itemKey }
        if (itemInfo != null) {
            val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
                .coerceAtLeast(1)
                .toFloat()
            val itemCenter = itemInfo.offset + itemInfo.size * 0.5f
            val viewportCenter = layoutInfo.viewportStartOffset + viewportHeight * 0.5f
            val normalizedDistance = (abs(itemCenter - viewportCenter) / (viewportHeight * 0.64f))
                .coerceIn(0f, 1f)
            val edgeFactor = (normalizedDistance - 0.52f).coerceAtLeast(0f) / 0.48f
            val scale = 1f - (edgeFactor * 0.04f)
            scaleX = scale
            scaleY = scale
            alpha = 1f - (edgeFactor * 0.18f)
        }
    }
}

/**
 * Ambient luminous backdrop with size-cached gradients (zero per-frame allocation, zero blur).
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
                colors = listOf(Color(0xFFFDFBF5), WaqtCreamBg, Color(0xFFEEE6D5))
            )
        }
    }

    val goldGlowColor = remember(isDark) { WaqtGold.copy(alpha = if (isDark) 0.17f else 0.18f) }
    val indigoGlowColor = remember(isDark) {
        (if (isDark) PrayerIshaAccent else PrayerFajrAccent).copy(alpha = if (isDark) 0.18f else 0.10f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBrush)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val goldCenter = Offset(w * 0.86f, h * 0.10f)
                val goldRadius = (w * 0.64f).coerceAtLeast(1f)
                val goldBrush = Brush.radialGradient(
                    colors = listOf(goldGlowColor, Color.Transparent),
                    center = goldCenter,
                    radius = goldRadius
                )
                val indigoCenter = Offset(w * 0.12f, h * 0.56f)
                val indigoRadius = (w * 0.70f).coerceAtLeast(1f)
                val indigoBrush = Brush.radialGradient(
                    colors = listOf(indigoGlowColor, Color.Transparent),
                    center = indigoCenter,
                    radius = indigoRadius
                )
                onDrawBehind {
                    drawCircle(brush = goldBrush, radius = goldRadius, center = goldCenter)
                    drawCircle(brush = indigoBrush, radius = indigoRadius, center = indigoCenter)
                }
            }
    ) {
        content()
    }
}

/**
 * Translucent glassmorphic card with specular luminous border and zero runtime blur overhead.
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
        Color(0xFF132A4E).copy(alpha = 0.62f)
    } else {
        Color.White.copy(alpha = 0.68f)
    }
    val edgeColor = borderColor ?: if (isDark) {
        WaqtGold.copy(alpha = 0.28f)
    } else {
        Color.White.copy(alpha = 0.92f)
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
