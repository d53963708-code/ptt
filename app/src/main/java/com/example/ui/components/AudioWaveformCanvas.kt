package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.PttState

@Composable
fun AudioWaveformCanvas(
    amplitudes: List<Float>,
    pttState: PttState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeColor = when (pttState) {
        PttState.RECORDING -> Color(0xFF00FF66) // Signal Lime
        PttState.TRANSMITTING -> Color(0xFFFF9100) // Warning Orange
        PttState.RECEIVING -> Color(0xFF00E5FF) // Electric Cyan
        PttState.IDLE -> Color(0xFF334155) // Idle Muted
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = 24
        val barWidth = (width / barCount) * 0.55f
        val gap = (width / barCount) * 0.45f

        val active = pttState != PttState.IDLE

        for (i in 0 until barCount) {
            val ampIdx = i % amplitudes.size.coerceAtLeast(1)
            val baseAmp = if (amplitudes.isNotEmpty()) amplitudes[ampIdx] else 0.1f

            val animatedHeight = if (active) {
                val factor = if (i % 2 == 0) pulseAnim else (1.2f - pulseAnim)
                (height * (baseAmp * 0.85f + 0.15f) * factor).coerceIn(8f, height)
            } else {
                (height * 0.08f)
            }

            val x = i * (barWidth + gap) + gap / 2
            val y = (height - animatedHeight) / 2

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        activeColor.copy(alpha = if (active) 0.9f else 0.4f),
                        activeColor.copy(alpha = if (active) 0.4f else 0.15f)
                    )
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, animatedHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}
