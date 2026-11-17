package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.strings.WaqtStrings
import com.example.ui.theme.WaqtGold
import com.example.ui.theme.WaqtGoldBright
import com.example.ui.theme.WaqtGoldSoft
import com.example.ui.theme.WaqtNavy
import com.example.ui.theme.WaqtNavyCard
import com.example.ui.theme.WaqtNavyDeep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedIntroOverlay(
    visible: Boolean,
    reduceMotion: Boolean,
    strings: WaqtStrings,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible, reduceMotion) {
        if (!visible) return@LaunchedEffect
        if (reduceMotion) {
            onDismiss()
        } else {
            delay(1350L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && !reduceMotion,
        exit = fadeOut(animationSpec = tween(320)) + scaleOut(
            targetScale = 1.04f,
            animationSpec = tween(320)
        )
    ) {
        val emblemScale = remember { Animatable(0.75f) }
        val emblemAlpha = remember { Animatable(0f) }
        val textOffsetY = remember { Animatable(24f) }
        val ringProgress = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            launch {
                emblemAlpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
            }
            launch {
                emblemScale.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
            }
            launch {
                textOffsetY.animateTo(0f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
            launch {
                ringProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WaqtNavyCard, WaqtNavy, WaqtNavyDeep)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .testTag("intro_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(152.dp)
                        .graphicsLayer {
                            scaleX = emblemScale.value
                            scaleY = emblemScale.value
                            alpha = emblemAlpha.value
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.minDimension * 0.36f

                    // Outer luminous ambient halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(WaqtGold.copy(alpha = 0.24f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r * 1.6f
                        ),
                        radius = r * 1.6f,
                        center = Offset(cx, cy)
                    )

                    // Animated golden orbital arc
                    drawArc(
                        color = WaqtGoldBright.copy(alpha = 0.65f),
                        startAngle = -90f,
                        sweepAngle = 360f * ringProgress.value,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Twinkling accent stars
                    val starAlpha = ringProgress.value
                    drawCircle(
                        color = WaqtGoldSoft.copy(alpha = starAlpha),
                        radius = 2.6.dp.toPx(),
                        center = Offset(cx + r * 0.85f, cy - r * 0.65f)
                    )
                    drawCircle(
                        color = WaqtGoldSoft.copy(alpha = starAlpha * 0.8f),
                        radius = 1.8.dp.toPx(),
                        center = Offset(cx - r * 0.92f, cy - r * 0.45f)
                    )
                    drawCircle(
                        color = WaqtGoldSoft.copy(alpha = starAlpha * 0.7f),
                        radius = 2.dp.toPx(),
                        center = Offset(cx + r * 0.62f, cy + r * 0.78f)
                    )

                    // Crescent Moon
                    drawCircle(
                        color = WaqtGold,
                        radius = r,
                        center = Offset(cx - r * 0.08f, cy)
                    )
                    drawCircle(
                        color = WaqtNavy,
                        radius = r * 0.82f,
                        center = Offset(cx + r * 0.24f, cy - r * 0.14f)
                    )

                    // Mosque Dome & Finial Silhouette
                    val domePath = Path().apply {
                        val baseY = cy + r * 0.48f
                        val domeTop = cy - r * 0.22f
                        moveTo(cx - r * 0.30f, baseY)
                        lineTo(cx - r * 0.30f, cy + r * 0.08f)
                        quadraticTo(cx - r * 0.30f, domeTop + r * 0.14f, cx, domeTop)
                        quadraticTo(cx + r * 0.30f, domeTop + r * 0.14f, cx + r * 0.30f, cy + r * 0.08f)
                        lineTo(cx + r * 0.30f, baseY)
                        close()
                    }
                    drawPath(path = domePath, color = WaqtGoldSoft)
                }

                Spacer(modifier = Modifier.height(22.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        translationY = textOffsetY.value
                        alpha = emblemAlpha.value
                    }
                ) {
                    Text(
                        text = strings.fullBrandName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = WaqtGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = strings.appSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.88f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = strings.splashTagline,
                            style = MaterialTheme.typography.labelMedium,
                            color = WaqtGoldSoft,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
