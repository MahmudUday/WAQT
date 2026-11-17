package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode
import com.example.ui.theme.WaqtCreamBg
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyDeep

/**
 * Lightweight GPU-only press-scale animation backed by Compose's existing InteractionSource.
 * Reads animation state strictly in the graphicsLayer phase (zero button recomposition on press).
 */
fun Modifier.waqtPressScale(
    interactionSource: MutableInteractionSource,
    reduceMotion: Boolean = false
): Modifier = composed {
    if (reduceMotion) return@composed this
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "waqt_press_scale"
    )
    this.graphicsLayer {
        val s = scaleState.value
        scaleX = s
        scaleY = s
    }
}

/**
 * Ultra-light 120Hz/144Hz GPU-only scroll reveal animation.
 * Uses pure RenderNode translationY (no alpha saveLayer FBOs, no text resampling scale,
 * and zero per-frame layoutInfo snapshot reads during scroll).
 */
fun Modifier.waqtScrollReveal(
    reduceMotion: Boolean = false
): Modifier = composed {
    if (reduceMotion) return@composed this
    val glideOffsetPx = with(LocalDensity.current) { 14.dp.toPx() }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
        )
    }
    this.graphicsLayer {
        translationY = glideOffsetPx * progress.value
    }
}

/**
 * Ambient luminous backdrop with single-pass cached gradients (minimal pixel overdraw, zero blur).
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

    val goldGlowColor = remember(isDark) { WaqtGold.copy(alpha = if (isDark) 0.16f else 0.15f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBrush)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val goldCenter = Offset(w * 0.85f, h * 0.12f)
                val goldRadius = (w * 0.62f).coerceAtLeast(1f)
                val goldBrush = Brush.radialGradient(
                    colors = listOf(goldGlowColor, Color.Transparent),
                    center = goldCenter,
                    radius = goldRadius
                )
                onDrawBehind {
                    drawCircle(brush = goldBrush, radius = goldRadius, center = goldCenter)
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
        Color(0xFF132A4E).copy(alpha = 0.66f)
    } else {
        Color.White.copy(alpha = 0.74f)
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
