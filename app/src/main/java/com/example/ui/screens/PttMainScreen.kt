package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AudioPermissionsHandler
import com.example.ui.MainViewModel
import com.example.ui.tabs.ChatTab
import com.example.ui.tabs.CodecSettingsTab
import com.example.ui.tabs.HistoryTab
import com.example.ui.tabs.PeersTab
import com.example.ui.tabs.RadioPttTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PttMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val pttState by viewModel.pttState.collectAsStateWithLifecycle()
    val currentChannel by viewModel.currentChannelId.collectAsStateWithLifecycle()
    val passphrase by viewModel.passphrase.collectAsStateWithLifecycle()
    val encryptionFingerprint by viewModel.encryptionFingerprint.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val targetBitrateKbps by viewModel.targetBitrateKbps.collectAsStateWithLifecycle()
    val activeSpeakerName by viewModel.activeSpeakerName.collectAsStateWithLifecycle()
    val activeSpeakerIp by viewModel.activeSpeakerIp.collectAsStateWithLifecycle()
    val targetPeerIp by viewModel.targetPeerIp.collectAsStateWithLifecycle()
    val playingMessageId by viewModel.playingMessageId.collectAsStateWithLifecycle()

    val discoveredPeers by viewModel.discoveredPeers.collectAsStateWithLifecycle()
    val localIpAddress by viewModel.localIpAddress.collectAsStateWithLifecycle()
    val spectrumAmplitudes by viewModel.spectrumAmplitudes.collectAsStateWithLifecycle()
    val channelMessages by viewModel.channelMessages.collectAsStateWithLifecycle()

    AudioPermissionsHandler(
        onPermissionGranted = {}
    ) { hasPermission, requestPermission ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "COMETA PTT",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    actions = {
                        Text(
                            text = "VOZ & TEXTO P2P",
                            color = Color(0xFF00FF66),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0B0F19)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF161C28),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Radio PTT",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("PTT", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF263044),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_radio")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat Texto",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Chat", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF263044),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_chat")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "P2P Peers",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Red", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF263044),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_peers")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Historial", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF263044),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_history")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text("Ajustes", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF263044),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            },
            containerColor = Color(0xFF0B0F19),
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF0B0F19))
            ) {
                when (selectedTab) {
                    0 -> RadioPttTab(
                        viewModel = viewModel,
                        pttState = pttState,
                        currentChannel = currentChannel,
                        encryptionFingerprint = encryptionFingerprint,
                        spectrumAmplitudes = spectrumAmplitudes,
                        discoveredPeersCount = discoveredPeers.size,
                        activeSpeakerName = activeSpeakerName,
                        activeSpeakerIp = activeSpeakerIp,
                        targetPeerIp = targetPeerIp,
                        onStartPtt = {
                            if (hasPermission) {
                                viewModel.startPttTalk()
                            } else {
                                requestPermission()
                            }
                        },
                        onStopPtt = {
                            viewModel.stopPttTalk()
                        },
                        onNavigateToChat = {
                            selectedTab = 1
                        }
                    )

                    1 -> ChatTab(
                        viewModel = viewModel,
                        messages = channelMessages,
                        currentChannel = currentChannel,
                        targetPeerIp = targetPeerIp
                    )

                    2 -> PeersTab(
                        viewModel = viewModel,
                        localIpAddress = localIpAddress,
                        discoveredPeers = discoveredPeers,
                        targetPeerIp = targetPeerIp
                    )

                    3 -> HistoryTab(
                        viewModel = viewModel,
                        messages = channelMessages,
                        currentChannel = currentChannel,
                        playingMessageId = playingMessageId
                    )

                    4 -> CodecSettingsTab(
                        viewModel = viewModel,
                        passphrase = passphrase,
                        encryptionFingerprint = encryptionFingerprint,
                        targetBitrateKbps = targetBitrateKbps,
                        soundEffectsEnabled = soundEffectsEnabled
                    )
                }
            }
        }
    }
}
