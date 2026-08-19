package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.VoiceMessageEntity
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatTab(
    viewModel: MainViewModel,
    messages: List<VoiceMessageEntity>,
    currentChannel: Int,
    targetPeerIp: String?,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Filter to text messages
    val textMessages = remember(messages) {
        messages.filter { it.messageType == "TEXT" }.reversed()
    }

    LaunchedEffect(textMessages.size) {
        if (textMessages.isNotEmpty()) {
            listState.animateScrollToItem(textMessages.size - 1)
        }
    }

    val quickPhrases = listOf(
        "10-4 Copiado",
        "Afirmativo",
        "Negativo",
        "En posición",
        "¿Me copias?",
        "Cambio y fuera"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Channel and Security Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C28)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF263044), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("CANAL %02d | CHAT P2P", currentChannel),
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "AES-256 GCM",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = if (targetPeerIp.isNullOrBlank()) "Modo: Broadcast (A todos los peers)" else "Destino Directo: $targetPeerIp",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = Color(0xFF00FF66).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Anti-DPI",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "ANTI-DPI",
                            color = Color(0xFF00FF66),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Messages List or Empty View
        if (textMessages.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Sin mensajes de texto",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sin mensajes de texto en Canal $currentChannel",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escribe un mensaje o usa las frases rápidas tácticas.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                items(textMessages, key = { it.id }) { msg ->
                    ChatBubble(
                        msg = msg,
                        onDelete = { viewModel.deleteHistoryMessage(msg.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Tactical Phrases
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            quickPhrases.forEach { phrase ->
                FilterChip(
                    selected = false,
                    onClick = {
                        viewModel.sendTextMessage(phrase)
                    },
                    label = {
                        Text(
                            text = phrase,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E5FF)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF161C28)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color(0xFF263044),
                        enabled = true,
                        selected = false
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Text Input Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Mensaje encriptado (Canal $currentChannel)...",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF263044),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF161C28),
                    unfocusedContainerColor = Color(0xFF161C28)
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = if (textInput.isNotBlank()) Color(0xFF00E5FF) else Color(0xFF263044),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendTextMessage(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank(),
                    modifier = Modifier.testTag("send_text_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar Mensaje",
                        tint = if (textInput.isNotBlank()) Color(0xFF0B0F19) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    msg: VoiceMessageEntity,
    onDelete: () -> Unit
) {
    val isMe = msg.isOutgoing
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(msg.timestamp))

    Column(
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Sender info header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isMe) "Tú" else msg.senderName,
                color = if (isMe) Color(0xFF00FF66) else Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$timeStr • ${msg.senderIp}",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }

        // Bubble Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) Color(0xFF0A2E20) else Color(0xFF14243B)
            ),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
            ),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .border(
                    1.dp,
                    if (isMe) Color(0xFF00FF66).copy(alpha = 0.3f) else Color(0xFF00E5FF).copy(alpha = 0.3f),
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isMe) 14.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 14.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                Text(
                    text = msg.textContent,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "AES-256",
                            tint = if (isMe) Color(0xFF00FF66) else Color(0xFF00E5FF),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = msg.encryptedHexPreview.take(12),
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar mensaje",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
