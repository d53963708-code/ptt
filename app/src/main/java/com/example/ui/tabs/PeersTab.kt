package com.example.ui.tabs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.network.DiscoveredPeer
import com.example.network.NetworkMode
import com.example.ui.MainViewModel

@Composable
fun PeersTab(
    viewModel: MainViewModel,
    localIpAddress: String,
    discoveredPeers: Map<String, DiscoveredPeer>,
    targetPeerIp: String?,
    modifier: Modifier = Modifier
) {
    val networkMode by viewModel.networkMode.collectAsStateWithLifecycle()
    val isObfuscationEnabled by viewModel.isObfuscationEnabled.collectAsStateWithLifecycle()
    val cloudRoomCode by viewModel.cloudRoomCode.collectAsStateWithLifecycle()
    val isRelayConnected by viewModel.isRelayConnected.collectAsStateWithLifecycle()

    var ipInputText by remember { mutableStateOf(targetPeerIp ?: "") }
    var roomInputText by remember { mutableStateOf(cloudRoomCode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Network Mode Switcher (Local P2P vs Global Internet)
        Surface(
            color = Color(0xFF161C28),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "SELECCIÓN DE MODO DE RED",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Button Mode 1: Local P2P
                    Surface(
                        color = if (networkMode == NetworkMode.LOCAL_P2P) Color(0xFF00E5FF) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setNetworkMode(NetworkMode.LOCAL_P2P) }
                            .border(
                                width = if (networkMode == NetworkMode.LOCAL_P2P) 2.dp else 1.dp,
                                color = if (networkMode == NetworkMode.LOCAL_P2P) Color(0xFF00E5FF) else Color(0xFF263044),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("mode_local_p2p_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (networkMode == NetworkMode.LOCAL_P2P) Color(0xFF0B0F19) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RED LOCAL (OFFLINE)",
                                color = if (networkMode == NetworkMode.LOCAL_P2P) Color(0xFF0B0F19) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Button Mode 2: Internet Global
                    Surface(
                        color = if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF00FF66) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setNetworkMode(NetworkMode.INTERNET_GLOBAL) }
                            .border(
                                width = if (networkMode == NetworkMode.INTERNET_GLOBAL) 2.dp else 1.dp,
                                color = if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF00FF66) else Color(0xFF263044),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("mode_internet_global_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF0B0F19) else Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INTERNET GLOBAL",
                                color = if (networkMode == NetworkMode.INTERNET_GLOBAL) Color(0xFF0B0F19) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (networkMode == NetworkMode.INTERNET_GLOBAL) {
            // Global Internet Room Configuration Card
            Surface(
                color = Color(0xFF161C28),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SALA GLOBAL P2P (INTERNET / 4G/5G)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permite hablar con cualquier persona en cualquier parte del mundo a través de redes celulares o redes Wi-Fi distantes.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = roomInputText,
                            onValueChange = { roomInputText = it },
                            placeholder = { Text("Ej: #SALA-GLOBAL-8899", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FF66),
                                unfocusedBorderColor = Color(0xFF263044),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cloud_room_code_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.setCloudRoomCode(roomInputText) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FF66),
                                contentColor = Color(0xFF0B0F19)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("unir_sala_button")
                        ) {
                            Text(text = "CONECTAR", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Internet & Obfuscation Status Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRelayConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (isRelayConnected) Color(0xFF00FF66) else Color(0xFFFF9100),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRelayConnected) "Conectado a Sala: $cloudRoomCode" else "Conectando a Red Global...",
                            color = if (isRelayConnected) Color(0xFF00FF66) else Color(0xFFFF9100),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isObfuscationEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Camuflaje TLS 1.3 Anti-DPI Activo (Ofuscado)",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            // Local Network Identity Banner
            Surface(
                color = Color(0xFF161C28),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF263044))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Wi-Fi local",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "TU IP LOCAL EN LA RED",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = localIpAddress,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Target IP Connection Input
        Surface(
            color = Color(0xFF161C28),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONEXIÓN UNICAST DIRECTA P2P / WAN IP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ingresa la IP local o IP pública/Dominio de un dispositivo para comunicación directa.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ipInputText,
                        onValueChange = { ipInputText = it },
                        placeholder = { Text("Ej: 192.168.1.52 o 200.55.12.33", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF263044),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("target_ip_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.setTargetPeerIp(ipInputText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF0B0F19)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("set_target_ip_button")
                    ) {
                        Text(text = "FIJAR IP", fontWeight = FontWeight.Bold)
                    }
                }

                if (!targetPeerIp.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Activo",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Transmitiendo directamente a: $targetPeerIp",
                            color = Color(0xFF00FF66),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Limpiar",
                            color = Color(0xFFFF3366),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                ipInputText = ""
                                viewModel.setTargetPeerIp(null)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Discovered Local Peers Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "DISPOSITIVOS DETECTADOS (" + discoveredPeers.size + ")",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF263044))
                    .clickable { viewModel.voiceEngine.startP2PEngine() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = "Buscar",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "RESCANEAR", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (discoveredPeers.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = "Sin peers",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (networkMode == NetworkMode.INTERNET_GLOBAL) "Esperando usuarios en la Sala Global..." else "Escaneando dispositivos locales...",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                discoveredPeers.values.forEach { peer ->
                    PeerCardItem(
                        peer = peer,
                        isSelected = targetPeerIp == peer.ipAddress,
                        onSelectPeer = {
                            ipInputText = peer.ipAddress
                            viewModel.setTargetPeerIp(peer.ipAddress)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PeerCardItem(
    peer: DiscoveredPeer,
    isSelected: Boolean,
    onSelectPeer: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF161C28)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF263044),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelectPeer() }
            .testTag("peer_item_${peer.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263044))
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = "Peer Device",
                    tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF00FF66),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "IP: ${peer.ipAddress} | Canal ${peer.channelId}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RadioButtonChecked,
                    contentDescription = "Activo",
                    tint = Color(0xFF00FF66),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "EN LÍNEA",
                    color = Color(0xFF00FF66),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
