package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PttState

@Composable
fun PttButton(
    pttState: PttState,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeColor = when (pttState) {
        PttState.RECORDING -> Color(0xFF00FF66)
        PttState.TRANSMITTING -> Color(0xFFFF9100)
        PttState.RECEIVING -> Color(0xFF00E5FF)
        PttState.IDLE -> Color(0xFF00E5FF)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(230.dp)
    ) {
        // Glowing animated outer pulse ring when active or pressed
        if (isPressed || pttState != PttState.IDLE) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
            ) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.25f)
                )
            }
        }

        // Inner Tactical Walkie-Talkie Button Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isPressed) {
                            listOf(activeColor.copy(alpha = 0.35f), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        }
                    )
                )
                .border(
                    width = if (isPressed) 4.dp else 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            activeColor,
                            activeColor.copy(alpha = 0.4f)
                        )
                    ),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            onPressStart()
                            tryAwaitRelease()
                            isPressed = false
                            onPressEnd()
                        }
                    )
                }
                .testTag("ptt_talk_button")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (pttState == PttState.RECEIVING) Icons.Default.Radio else Icons.Default.Mic,
                    contentDescription = "Push To Talk Mic",
                    tint = activeColor,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (pttState) {
                        PttState.RECORDING -> "TRANSMITIENDO..."
                        PttState.TRANSMITTING -> "ENVIANDO..."
                        PttState.RECEIVING -> "ESCUCHANDO..."
                        PttState.IDLE -> "PRESIONA PTT"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = if (pttState == PttState.IDLE) "Mantén para hablar" else "Soltar para enviar",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}
