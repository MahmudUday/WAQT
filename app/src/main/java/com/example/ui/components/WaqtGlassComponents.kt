package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemeMode
import com.example.ui.theme.WaqtCyan
import com.example.ui.theme.WaqtEmeraldLight
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtNavyDeep
import com.example.ui.theme.WaqtViolet

@Immutable
data class WaqtGlassNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color,
    val testTag: String
)

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
 * Colorful aurora ambient backdrop with single-pass cached gradients (minimal pixel overdraw, zero blur)
 * designed so pure crystal glass cards and the sliding glass bottom bar visibly reflect rich color underneath.
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
                colors = listOf(Color(0xFF061126), Color(0xFF0B2246), Color(0xFF081734))
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(Color(0xFFFDFBF7), Color(0xFFF1E9D7), Color(0xFFE3ECF5))
            )
        }
    }

    val goldGlow = remember(isDark) { WaqtGold.copy(alpha = if (isDark) 0.24f else 0.22f) }
    val cyanGlow = remember(isDark) { WaqtCyan.copy(alpha = if (isDark) 0.16f else 0.16f) }
    val emeraldGlow = remember(isDark) { WaqtEmeraldLight.copy(alpha = if (isDark) 0.18f else 0.15f) }
    val violetGlow = remember(isDark) { WaqtViolet.copy(alpha = if (isDark) 0.18f else 0.14f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBrush)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val topRightCenter = Offset(w * 0.86f, h * 0.11f)
                val topRightRadius = (w * 0.68f).coerceAtLeast(1f)
                val topRightBrush = Brush.radialGradient(
                    colors = listOf(goldGlow, Color.Transparent),
                    center = topRightCenter,
                    radius = topRightRadius
                )
                val topLeftCenter = Offset(w * 0.12f, h * 0.22f)
                val topLeftRadius = (w * 0.56f).coerceAtLeast(1f)
                val topLeftBrush = Brush.radialGradient(
                    colors = listOf(cyanGlow, Color.Transparent),
                    center = topLeftCenter,
                    radius = topLeftRadius
                )
                val midLeftCenter = Offset(w * 0.16f, h * 0.60f)
                val midLeftRadius = (w * 0.62f).coerceAtLeast(1f)
                val midLeftBrush = Brush.radialGradient(
                    colors = listOf(emeraldGlow, Color.Transparent),
                    center = midLeftCenter,
                    radius = midLeftRadius
                )
                val bottomRightCenter = Offset(w * 0.82f, h * 0.86f)
                val bottomRightRadius = (w * 0.64f).coerceAtLeast(1f)
                val bottomRightBrush = Brush.radialGradient(
                    colors = listOf(violetGlow, Color.Transparent),
                    center = bottomRightCenter,
                    radius = bottomRightRadius
                )
                onDrawBehind {
                    drawCircle(brush = topRightBrush, radius = topRightRadius, center = topRightCenter)
                    drawCircle(brush = topLeftBrush, radius = topLeftRadius, center = topLeftCenter)
                    drawCircle(brush = midLeftBrush, radius = midLeftRadius, center = midLeftCenter)
                    drawCircle(brush = bottomRightBrush, radius = bottomRightRadius, center = bottomRightCenter)
                }
            }
    ) {
        content()
    }
}

/**
 * Colorful translucent crystal-glass card with specular top-to-bottom luminous rim and zero runtime blur.
 */
@Composable
fun WaqtGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    accentColor: Color? = null,
    tintColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == WaqtNavyDeep

    val bgBrush = remember(isDark, accentColor, tintColor) {
        when {
            tintColor != null -> Brush.linearGradient(
                listOf(
                    tintColor,
                    tintColor.copy(alpha = (tintColor.alpha * 0.82f).coerceIn(0f, 1f))
                )
            )
            accentColor != null -> if (isDark) {
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.22f),
                        Color(0xFF152D55).copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.58f),
                        accentColor.copy(alpha = 0.13f),
                        Color.White.copy(alpha = 0.42f)
                    )
                )
            }
            else -> if (isDark) {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.13f),
                        Color(0xFF16325C).copy(alpha = 0.36f),
                        WaqtCyan.copy(alpha = 0.08f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.62f),
                        Color.White.copy(alpha = 0.42f)
                    )
                )
            }
        }
    }

    val borderStroke = remember(isDark, accentColor, borderColor, borderWidth) {
        if (borderColor != null) {
            BorderStroke(borderWidth, borderColor)
        } else {
            val topEdge = if (isDark) {
                Color.White.copy(alpha = 0.34f)
            } else {
                Color.White.copy(alpha = 0.96f)
            }
            val bottomEdge = accentColor?.copy(alpha = if (isDark) 0.32f else 0.38f)
                ?: WaqtGold.copy(alpha = if (isDark) 0.20f else 0.28f)
            BorderStroke(
                width = borderWidth,
                brush = Brush.verticalGradient(listOf(topEdge, bottomEdge))
            )
        }
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush),
            content = content
        )
    }
}

/**
 * Translucent crystal-glass surface for floating bottom navigation bar and navigation rail (zero blur).
 */
@Composable
fun WaqtGlassBarSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == WaqtNavyDeep
    val glassBg = if (isDark) {
        Color(0xFF0A1A36).copy(alpha = 0.56f)
    } else {
        Color.White.copy(alpha = 0.58f)
    }
    val topEdge = if (isDark) Color.White.copy(alpha = 0.36f) else Color.White.copy(alpha = 0.96f)
    val bottomEdge = if (isDark) WaqtGoldBright.copy(alpha = 0.26f) else WaqtGold.copy(alpha = 0.32f)
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

/**
 * Floating translucent glass bottom menu bar with a smooth GPU-only sliding liquid-glass indicator pill.
 * Animates strictly via RenderNode translationX (zero recompositions & zero layout passes per frame).
 */
@Composable
fun WaqtSlidingGlassBottomBar(
    items: List<WaqtGlassNavItem>,
    selectedIndex: Int,
    reduceMotion: Boolean,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == WaqtNavyDeep
    val layoutDirection = LocalLayoutDirection.current
    val safeIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    val activeAccent = items.getOrNull(safeIndex)?.accentColor ?: WaqtGoldBright

    val animatedIndexState = animateFloatAsState(
        targetValue = safeIndex.toFloat(),
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "glass_nav_pill_slide"
    )

    val pillBrush = remember(isDark, activeAccent) {
        if (isDark) {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.16f),
                    activeAccent.copy(alpha = 0.30f),
                    WaqtGoldBright.copy(alpha = 0.22f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.82f),
                    activeAccent.copy(alpha = 0.24f),
                    WaqtGoldBright.copy(alpha = 0.26f)
                )
            )
        }
    }
    val pillBorderBrush = remember(isDark, activeAccent) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.48f else 0.98f),
                activeAccent.copy(alpha = if (isDark) 0.42f else 0.50f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                )
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        WaqtGlassBarSurface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                val count = items.size.coerceAtLeast(1)
                val slotWidth = maxWidth / count
                val slotWidthPx = with(LocalDensity.current) { slotWidth.toPx() }
                val directionSign = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f

                // GPU-only sliding liquid-glass highlight capsule
                Box(
                    modifier = Modifier
                        .width(slotWidth)
                        .height(56.dp)
                        .graphicsLayer {
                            translationX = directionSign * slotWidthPx * animatedIndexState.value
                        }
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(pillBrush)
                        .border(BorderStroke(1.dp, pillBorderBrush), RoundedCornerShape(22.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val selected = index == safeIndex
                        val interactionSource = remember { MutableInteractionSource() }
                        val iconScale = animateFloatAsState(
                            targetValue = if (selected && !reduceMotion) 1.10f else 1f,
                            animationSpec = if (reduceMotion) {
                                snap()
                            } else {
                                spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
                            },
                            label = "nav_icon_pop"
                        )
                        val contentColor = when {
                            selected && isDark -> Color.White
                            selected && !isDark -> WaqtNavyDeep
                            isDark -> Color.White.copy(alpha = 0.72f)
                            else -> WaqtNavyDeep.copy(alpha = 0.66f)
                        }
                        val iconTint = if (selected) item.accentColor else contentColor

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .waqtPressScale(interactionSource, reduceMotion)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(bounded = true),
                                    onClick = { onSelectRoute(item.route) }
                                )
                                .testTag(item.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        val s = iconScale.value
                                        scaleX = s
                                        scaleY = s
                                    }
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = contentColor,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
