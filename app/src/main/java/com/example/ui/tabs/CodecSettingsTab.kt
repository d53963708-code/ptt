package com.example.ui.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel

@Composable
fun CodecSettingsTab(
    viewModel: MainViewModel,
    passphrase: String,
    encryptionFingerprint: String,
    targetBitrateKbps: Float,
    soundEffectsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isObfuscationEnabled by viewModel.isObfuscationEnabled.collectAsStateWithLifecycle()
    var passInputText by remember { mutableStateOf(passphrase) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Obfuscation and Anti-DPI Traffic Camouflage Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C28)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Camuflaje Anti-DPI",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CAMUFLAJE ANTI-DPI (TLS 1.3)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ofusca paquetes para que parezcan tráfico Web HTTPS común.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = isObfuscationEnabled,
                        onCheckedChange = { viewModel.toggleObfuscation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF0B0F19),
                            checkedTrackColor = Color(0xFF00E5FF),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.testTag("toggle_obfuscation_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isObfuscationEnabled)
                            "STATUS: ACTIVO. Envoltorio TLS 1.3 + Máscara XOR + Relleno Aleatorio. Los proveedores de Internet no pueden detectar el uso de radio PTT ni bloquear las llamadas."
                        else
                            "STATUS: DESACTIVADO. Transmisión UDP directa estándar.",
                        color = if (isObfuscationEnabled) Color(0xFF00FF66) else Color(0xFFFF9100),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // E2E Encryption Key Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C28)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Encripción",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ENCRIPCIÓN E2E (AES-256 GCM)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "La clave de 256 bits se deriva de la frase secreta de canal. Solo quienes conozcan esta clave podrán escuchar el audio.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passInputText,
                    onValueChange = { passInputText = it },
                    label = { Text("Frase Secreta de Encripción") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF66),
                        unfocusedBorderColor = Color(0xFF263044),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFF00FF66))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("encryption_passphrase_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Huella AES: $encryptionFingerprint",
                        color = Color(0xFF00FF66),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { viewModel.setPassphrase(passInputText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FF66),
                            contentColor = Color(0xFF0B0F19)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("apply_passphrase_button")
                    ) {
                        Text(text = "APLICAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lyra 2 Codec Bitrate Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C28)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Lyra 2 Codec",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CÓDEC DE VOZ LYRA 2",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Compresión de voz neuronal de ultra bajo ancho de banda para redes táctiles y P2P.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                val bitrates = listOf(
                    3.2f to "3.2 kbps (Ultra Bajo Ancho de Banda)",
                    6.0f to "6.0 kbps (Equilibrado Voz)",
                    9.6f to "9.6 kbps (Alta Fidelidad)"
                )

                bitrates.forEach { (kbps, label) ->
                    val selected = targetBitrateKbps == kbps
                    Surface(
                        color = if (selected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Color(0xFF00E5FF) else Color(0xFF263044),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.setBitrate(kbps) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) Color(0xFF00E5FF) else Color(0xFF263044),
                                    contentColor = if (selected) Color(0xFF0B0F19) else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("bitrate_option_${kbps.toInt()}")
                            ) {
                                Text(text = "$kbps kbps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radio Audio Effects Card (Nextel Squelch & Chirp)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C28)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Efectos Radio",
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EFECTOS DE SONIDO NEXTEL",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Sonido Chirp al presionar PTT y Squelch de radio al soltar.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = soundEffectsEnabled,
                    onCheckedChange = { viewModel.toggleSoundEffects() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0B0F19),
                        checkedTrackColor = Color(0xFFFF9100),
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.testTag("toggle_sound_effects_switch")
                )
            }
        }
    }
}
