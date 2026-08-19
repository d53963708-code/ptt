package com.example.ui.tabs

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.VoiceMessageEntity
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryTab(
    viewModel: MainViewModel,
    messages: List<VoiceMessageEntity>,
    currentChannel: Int,
    playingMessageId: Long?,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableIntStateOf(0) } // 0: Todos, 1: Solo Voz, 2: Solo Texto

    val filteredMessages = remember(messages, filterType) {
        when (filterType) {
            1 -> messages.filter { it.messageType == "VOICE" }
            2 -> messages.filter { it.messageType == "TEXT" }
            else -> messages
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "HISTORIAL ENCRIPTADO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("Canal %02d | %d registros locales", currentChannel, messages.size),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            if (messages.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearChannelHistory() },
                    modifier = Modifier.testTag("clear_channel_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Limpiar Canal",
                        tint = Color(0xFFFF3366)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filterType == 0,
                onClick = { filterType = 0 },
                label = { Text("Todos (${messages.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E5FF),
                    selectedLabelColor = Color(0xFF0B0F19),
                    containerColor = Color(0xFF161C28),
                    labelColor = Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            FilterChip(
                selected = filterType == 1,
                onClick = { filterType = 1 },
                label = {
                    Text(
                        "Voz (${messages.count { it.messageType == "VOICE" }})",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00FF66),
                    selectedLabelColor = Color(0xFF0B0F19),
                    containerColor = Color(0xFF161C28),
                    labelColor = Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            FilterChip(
                selected = filterType == 2,
                onClick = { filterType = 2 },
                label = {
                    Text(
                        "Texto (${messages.count { it.messageType == "TEXT" }})",
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E5FF),
                    selectedLabelColor = Color(0xFF0B0F19),
                    containerColor = Color(0xFF161C28),
                    labelColor = Color(0xFF94A3B8)
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredMessages.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Sin mensajes",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin transmisiones registradas en el Canal $currentChannel",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Usa el botón PTT para hablar o el Chat para enviar texto.",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredMessages, key = { it.id }) { msg ->
                    HistoryMessageCard(
                        msg = msg,
                        isPlaying = (playingMessageId == msg.id),
                        onPlay = { viewModel.playHistoryMessage(msg) },
                        onDelete = { viewModel.deleteHistoryMessage(msg.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryMessageCard(
    msg: VoiceMessageEntity,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
    val dateStr = dateFormat.format(Date(msg.timestamp))
    val isVoice = (msg.messageType == "VOICE")

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161C28)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPlaying) Color(0xFF00FF66) else Color(0xFF263044),
                RoundedCornerShape(12.dp)
            )
            .testTag("history_msg_card_${msg.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (msg.isOutgoing) Color(0xFF00FF66).copy(alpha = 0.2f) else Color(0xFF00E5FF).copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (msg.isOutgoing) Icons.Default.CallMade else Icons.Default.CallReceived,
                        contentDescription = "Direction",
                        tint = if (msg.isOutgoing) Color(0xFF00FF66) else Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (msg.isOutgoing) "TÚ (ENVIADO)" else msg.senderName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "AES Lock",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "${msg.senderIp} | $dateStr",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = if (isVoice) Color(0xFF263044) else Color(0xFF00E5FF).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isVoice) "${msg.durationMs / 1000f}s VOZ" else "TEXTO",
                        color = if (isVoice) Color(0xFF00E5FF) else Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isVoice) {
                // Display text message body
                Surface(
                    color = Color(0xFF0B0F19),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Texto",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg.textContent,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                // Playback and Encrypted Hex Bar for Voice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = if (isPlaying) Color(0xFFFF3366) else Color(0xFF00E5FF),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onPlay() }
                            .testTag("play_msg_button_${msg.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Detener Audio" else "Reproducir Audio",
                                tint = Color(0xFF0B0F19),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPlaying) "REPRODUCIENDO AUDIO..." else "PACKET AES-256 / LYRA 2 (${msg.bitrateKbps} kbps):",
                                color = if (isPlaying) Color(0xFF00FF66) else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isPlaying) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Playing",
                                    tint = Color(0xFF00FF66).copy(alpha = alphaAnim),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = msg.encryptedHexPreview,
                            color = if (isPlaying) Color(0xFF00FF66) else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
