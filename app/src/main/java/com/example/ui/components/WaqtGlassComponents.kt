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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode
import com.example.ui.theme.WaqtCreamBg
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyDeep

/**
 * Lightweight GPU-only press-scale animation backed by Compose's existing InteractionSource.
 * Implemented directly as a @Composable Modifier (zero Modifier.composed overhead) and reads
 * animation state strictly in the graphicsLayer phase.
 */
@Composable
fun Modifier.waqtPressScale(
    interactionSource: MutableInteractionSource,
    reduceMotion: Boolean = false
): Modifier {
    if (reduceMotion) return this
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "waqt_press_scale"
    )
    return this.graphicsLayer {
        val s = scaleState.value
        scaleX = s
        scaleY = s
    }
}

/**
 * Ambient luminous backdrop with single-pass cached gradients (minimal pixel overdraw, zero blur)
 * designed so translucent glass cards and glass bottom navigation bar reflect rich depth underneath.
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
                colors = listOf(Color(0xFF061124), WaqtNavy, Color(0xFF07162E))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFFDFBF5), WaqtCreamBg, Color(0xFFE7DEC9))
            )
        }
    }

    val goldGlowColor = remember(isDark) { WaqtGold.copy(alpha = if (isDark) 0.18f else 0.16f) }
    val bottomGlowColor = remember(isDark) {
        if (isDark) Color(0xFF195E59).copy(alpha = 0.22f) else WaqtGold.copy(alpha = 0.10f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBrush)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val goldCenter = Offset(w * 0.85f, h * 0.12f)
                val goldRadius = (w * 0.64f).coerceAtLeast(1f)
                val goldBrush = Brush.radialGradient(
                    colors = listOf(goldGlowColor, Color.Transparent),
                    center = goldCenter,
                    radius = goldRadius
                )
                val lowerCenter = Offset(w * 0.18f, h * 0.88f)
                val lowerRadius = (w * 0.68f).coerceAtLeast(1f)
                val lowerBrush = Brush.radialGradient(
                    colors = listOf(bottomGlowColor, Color.Transparent),
                    center = lowerCenter,
                    radius = lowerRadius
                )
                onDrawBehind {
                    drawCircle(brush = goldBrush, radius = goldRadius, center = goldCenter)
                    drawCircle(brush = lowerBrush, radius = lowerRadius, center = lowerCenter)
                }
            }
    ) {
        content()
    }
}

/**
 * Translucent glassmorphic card with specular top-to-bottom luminous border and zero runtime blur overhead.
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
        Color(0xFF132B52).copy(alpha = 0.54f)
    } else {
        Color.White.copy(alpha = 0.62f)
    }
    val borderStroke = if (borderColor != null) {
        BorderStroke(borderWidth, borderColor)
    } else {
        val topEdge = if (isDark) Color.White.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.94f)
        val bottomEdge = if (isDark) WaqtGold.copy(alpha = 0.14f) else WaqtGold.copy(alpha = 0.24f)
        BorderStroke(
            width = borderWidth,
            brush = Brush.verticalGradient(listOf(topEdge, bottomEdge))
        )
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = glassBg),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

/**
 * Translucent frosted-glass surface for floating bottom navigation bar and navigation rail (zero blur).
 */
@Composable
fun WaqtGlassBarSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == WaqtNavyDeep
    val glassBg = if (isDark) {
        Color(0xFF0C1E3A).copy(alpha = 0.64f)
    } else {
        Color.White.copy(alpha = 0.70f)
    }
    val topEdge = if (isDark) WaqtGoldBright.copy(alpha = 0.34f) else Color.White.copy(alpha = 0.96f)
    val bottomEdge = if (isDark) Color.White.copy(alpha = 0.12f) else WaqtGold.copy(alpha = 0.28f)
    val borderBrush = remember(isDark) {
        Brush.verticalGradient(listOf(topEdge, bottomEdge))
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = glassBg,
        border = BorderStroke(1.1.dp, borderBrush),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        content = content
    )
}
