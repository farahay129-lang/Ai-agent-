package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.model.AssistantVoiceState
import com.example.ui.theme.HoloBlue
import com.example.ui.theme.HoloIce
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink

@Composable
fun AnimatedAvatarOrb(
    voiceState: AssistantVoiceState,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_orb")

    val rotationCW by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_cw"
    )

    val rotationCCW by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_ccw"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val rapidPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rapid"
    )

    val currentScale = when (voiceState) {
        is AssistantVoiceState.Listening -> rapidPulse + (voiceState.amplitude * 0.22f)
        is AssistantVoiceState.Thinking -> rapidPulse
        is AssistantVoiceState.Speaking -> rapidPulse
        else -> breathingScale
    }

    val primaryColor = when (voiceState) {
        is AssistantVoiceState.Listening -> NeonCyan
        is AssistantVoiceState.WakeWordStandby -> HoloIce
        is AssistantVoiceState.Thinking -> HoloBlue
        is AssistantVoiceState.Confirming -> NeonAmber
        is AssistantVoiceState.Speaking -> NeonGreen
        is AssistantVoiceState.Error -> NeonPink
        else -> NeonCyan
    }

    val secondaryColor = when (voiceState) {
        is AssistantVoiceState.Listening -> NeonGreen
        is AssistantVoiceState.Thinking -> NeonCyan
        is AssistantVoiceState.Confirming -> HoloBlue
        is AssistantVoiceState.Speaking -> NeonCyan
        is AssistantVoiceState.Error -> Color(0xFFEF4444)
        else -> HoloBlue
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .scale(currentScale),
        contentAlignment = Alignment.Center
    ) {
        // JARVIS Holographic HUD Rings
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension / 2.1f
            val midRadius = outerRadius * 0.84f
            val innerRadius = outerRadius * 0.68f

            // Outer segmented HUD ring (rotates CW)
            rotate(rotationCW, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.1f),
                            primaryColor.copy(alpha = 0.9f),
                            secondaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.85f)
                        )
                    ),
                    radius = outerRadius,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 14f, 10f, 14f), 0f)
                    )
                )
            }

            // Middle counter-rotating arc ring (rotates CCW)
            rotate(rotationCCW, pivot = center) {
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.65f),
                    radius = midRadius,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 22f), 0f)
                    )
                )
            }

            // Inner glowing solid ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = innerRadius,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Orbiting telemetry nodes
            val rad = Math.toRadians(rotationCW.toDouble())
            drawCircle(
                color = NeonCyan,
                radius = 4.5.dp.toPx(),
                center = Offset(
                    center.x + (outerRadius * Math.cos(rad)).toFloat(),
                    center.y + (outerRadius * Math.sin(rad)).toFloat()
                )
            )
            val oppRad = Math.toRadians((rotationCW + 180.0))
            drawCircle(
                color = secondaryColor,
                radius = 4.dp.toPx(),
                center = Offset(
                    center.x + (outerRadius * Math.cos(oppRad)).toFloat(),
                    center.y + (outerRadius * Math.sin(oppRad)).toFloat()
                )
            )
        }

        // Arc Reactor Core
        Box(
            modifier = Modifier
                .size((sizeDp * 0.54f).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            primaryColor.copy(alpha = 0.85f),
                            secondaryColor.copy(alpha = 0.45f),
                            Color(0xFF030914).copy(alpha = 0.95f)
                        )
                    )
                )
        )
    }
}

@Composable
fun AudioWaveformVisualizer(
    amplitude: Float,
    isSpeaking: Boolean = false,
    barCount: Int = 18,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Row(
        modifier = modifier.height(34.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = Math.sin((i.toDouble() / barCount) * Math.PI + (waveOffset * 2 * Math.PI)).toFloat()
            val dynamicHeight = if (isSpeaking || amplitude > 0.05f) {
                val waveHeight = 24f * amplitude.coerceIn(0.15f, 1f) * Math.abs(factor).coerceIn(0.25f, 1f)
                (8f + waveHeight).dp
            } else {
                5.dp
            }

            val color = if (i % 2 == 0) NeonCyan else HoloBlue

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(dynamicHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            if (i < barCount - 1) {
                Box(modifier = Modifier.width(3.dp))
            }
        }
    }
}
