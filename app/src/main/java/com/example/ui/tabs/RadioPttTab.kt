package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.NetworkMode
import com.example.ui.MainViewModel
import com.example.ui.PttState
import com.example.ui.components.AudioWaveformCanvas
import com.example.ui.components.PttButton

@Composable
fun RadioPttTab(
    viewModel: MainViewModel,
    pttState: PttState,
    currentChannel: Int,
    encryptionFingerprint: String,
    spectrumAmplitudes: List<Float>,
    discoveredPeersCount: Int,
    activeSpeakerName: String?,
    activeSpeakerIp: String?,
    targetPeerIp: String?,
    onStartPtt: () -> Unit,
    onStopPtt: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val networkMode by viewModel.networkMode.collectAsStateWithLifecycle()
    val isObfuscationEnabled by viewModel.isObfuscationEnabled.collectAsStateWithLifecycle()
    val cloudRoomCode by viewModel.cloudRoomCode.collectAsStateWithLifecycle()
    val lastReceivedText by viewModel.lastReceivedText.collectAsStateWithLifecycle()

    var channelDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Tactical Channel & Security Bar
        Surface(
            color = Color(0xFF161C28),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                // Channel Selector Dropdown
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF263044))
                            .clickable { channelDropdownExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("channel_selector_dropdown")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = "Canal Radio",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("CH %02d", currentChannel),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    DropdownMenu(
                        expanded = channelDropdownExpanded,
                        onDismissRequest = { channelDropdownExpanded = false }
                    ) {
                        for (ch in 1..16) {
                            DropdownMenuItem(
                                text = { Text(text = String.format("Canal %02d (Frecuencia P2P)", ch)) },
                                onClick = {
                                    viewModel.setChannel(ch)
                                    channelDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Network Mode Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF00FF66).copy(alpha = 0.2f) else Color(0xFF263044))
                        .border(
                            1.dp,
                            if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF00FF66) else Color(0xFF00E5FF),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (networkMode == NetworkMode.INTERNET_GLOBAL) Icons.Default.Language else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF00FF66) else Color(0xFF00E5FF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (networkMode == NetworkMode.INTERNET_GLOBAL) "INTERNET" else "P2P LOCAL",
                        color = if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF00FF66) else Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // E2E Security Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF00FF66).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encripción E2E",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = encryptionFingerprint,
                        color = Color(0xFF00FF66),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Incoming Text Notification Toast Banner
        AnimatedVisibility(
            visible = lastReceivedText != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            lastReceivedText?.let { notification ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14243B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.dismissLastTextNotification()
                            onNavigateToChat()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Nuevo Mensaje",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MENSAJE DE ${notification.senderName}:",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notification.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissLastTextNotification() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Digital Radio Display Terminal Screen
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF263044), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Display Header Status Line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (pttState) {
                                        PttState.RECORDING -> Color(0xFF00FF66)
                                        PttState.TRANSMITTING -> Color(0xFFFF9100)
                                        PttState.RECEIVING -> Color(0xFF00E5FF)
                                        PttState.IDLE -> Color(0xFF64748B)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (pttState) {
                                PttState.RECORDING -> "TRANSMITIENDO EN VIVO"
                                PttState.TRANSMITTING -> "PROCESANDO PACKET LYRA 2"
                                PttState.RECEIVING -> "REPRODUCIENDO VOZ ENTRANTE"
                                PttState.IDLE -> "CANAL EN ESPERA"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = if (isObfuscationEnabled) "TLS 1.3 OFUSCADO" else "LYRA 2 / 3.2k",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Speaker Banner
                if (!activeSpeakerName.isNullOrBlank()) {
                    Text(
                        text = "VOZ DE: $activeSpeakerName ($activeSpeakerIp)",
                        color = Color(0xFF00FF66),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else if (networkMode == NetworkMode.INTERNET_GLOBAL) {
                    Text(
                        text = "MODO GLOBAL INTERNET (SALA: $cloudRoomCode)",
                        color = Color(0xFF00FF66),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                } else if (!targetPeerIp.isNullOrBlank()) {
                    Text(
                        text = "DESTINO DIRECTO: $targetPeerIp",
                        color = Color(0xFFFF9100),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "MODO: TRANSMISIÓN MULTICAST P2P / INTERNET",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Audio Spectrum Visualizer
                AudioWaveformCanvas(
                    amplitudes = spectrumAmplitudes,
                    pttState = pttState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Large Central Push-To-Talk Button
        PttButton(
            pttState = pttState,
            onPressStart = onStartPtt,
            onPressEnd = onStopPtt
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons Row (Direct Alert + Open Chat)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Button(
                onClick = { viewModel.sendDirectAlert() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF263044),
                    contentColor = Color(0xFFFF9100)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("send_direct_alert_button")
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alert Call",
                    tint = Color(0xFFFF9100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ALERTA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onNavigateToChat,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF161C28),
                    contentColor = Color(0xFF00E5FF)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("quick_open_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Abrir Chat",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CHAT TEXTO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
