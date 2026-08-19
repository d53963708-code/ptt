package com.example.network

import android.os.Build
import com.example.codec.LyraCodecSimulator
import com.example.crypto.EncryptionManager
import com.example.crypto.ProtocolObfuscator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

enum class NetworkMode {
    LOCAL_P2P,        // Wi-Fi / Hotspot Local (Sin Internet)
    INTERNET_GLOBAL   // Red Celular 4G/5G / WAN / Servidor Relay / IP Pública
}

data class DiscoveredPeer(
    val id: String,
    val name: String,
    val ipAddress: String,
    val channelId: Int,
    val lastSeenMs: Long = System.currentTimeMillis()
)

class P2PVoiceEngine {
    companion object {
        const val UDP_PORT = 8888
        const val MAGIC_AUDIO_HEADER = "CMTA_AUD"
        const val MAGIC_BEACON_HEADER = "CMTA_BCN"
        const val MAGIC_ALERT_HEADER = "CMTA_ALT"
        const val MAGIC_TEXT_HEADER = "CMTA_TXT"
    }

    private var socket: DatagramSocket? = null
    private var isListening = false
    private var listenJob: Job? = null
    private var beaconJob: Job? = null

    private val cloudRelayClient = CloudRelayClient()
    private val scope = CoroutineScope(Dispatchers.IO)

    val myDeviceId: String = "PEER-" + (1000..9999).random()
    val myDeviceName: String = "Android " + Build.MODEL.take(10)

    private val _discoveredPeers = MutableStateFlow<Map<String, DiscoveredPeer>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _localIpAddress = MutableStateFlow("127.0.0.1")
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    private val _networkMode = MutableStateFlow(NetworkMode.LOCAL_P2P)
    val networkMode: StateFlow<NetworkMode> = _networkMode.asStateFlow()

    private val _isObfuscationEnabled = MutableStateFlow(true)
    val isObfuscationEnabled: StateFlow<Boolean> = _isObfuscationEnabled.asStateFlow()

    private val _cloudRoomCode = MutableStateFlow("#SALA-GLOBAL-8899")
    val cloudRoomCode: StateFlow<String> = _cloudRoomCode.asStateFlow()

    private val _isRelayConnected = MutableStateFlow(false)
    val isRelayConnected: StateFlow<Boolean> = _isRelayConnected.asStateFlow()

    var activeChannelId: Int = 1
    var activePassphrase: String = "CometaNextel2026"
    var customWanServerUrl: String = "wss://ws.postman-echo.com/raw"

    private var onAudioReceivedListener: ((senderName: String, senderIp: String, pcmAudio: ShortArray, bitrateKbps: Float) -> Unit)? = null
    private var onAlertReceivedListener: ((senderName: String, senderIp: String) -> Unit)? = null
    private var onTextReceivedListener: ((senderName: String, senderIp: String, textMessage: String, rawEncrypted: ByteArray) -> Unit)? = null

    fun setOnAudioReceivedListener(listener: (senderName: String, senderIp: String, pcmAudio: ShortArray, bitrateKbps: Float) -> Unit) {
        onAudioReceivedListener = listener
    }

    fun setOnAlertReceivedListener(listener: (senderName: String, senderIp: String) -> Unit) {
        onAlertReceivedListener = listener
    }

    fun setOnTextReceivedListener(listener: (senderName: String, senderIp: String, textMessage: String, rawEncrypted: ByteArray) -> Unit) {
        onTextReceivedListener = listener
    }

    fun setNetworkMode(mode: NetworkMode) {
        _networkMode.value = mode
        if (mode == NetworkMode.INTERNET_GLOBAL) {
            connectCloudRelay()
        } else {
            cloudRelayClient.disconnect()
            _isRelayConnected.value = false
        }
    }

    fun setObfuscationEnabled(enabled: Boolean) {
        _isObfuscationEnabled.value = enabled
    }

    fun setCloudRoomCode(code: String) {
        _cloudRoomCode.value = if (code.startsWith("#")) code else "#$code"
        if (_networkMode.value == NetworkMode.INTERNET_GLOBAL) {
            connectCloudRelay()
        }
    }

    private fun connectCloudRelay() {
        cloudRelayClient.setOnDataReceivedListener { rawData ->
            processRawDataBytes(rawData, "CLOUD-RELAY")
        }
        cloudRelayClient.connectToRoom(customWanServerUrl, _cloudRoomCode.value)
        _isRelayConnected.value = cloudRelayClient.isRelayConnected()
    }

    fun startP2PEngine() {
        if (isListening) return
        _localIpAddress.value = detectLocalIpAddress()

        try {
            socket = DatagramSocket(UDP_PORT).apply {
                broadcast = true
                reuseAddress = true
            }
            isListening = true

            // Start UDP Receiver Listener
            listenJob = scope.launch {
                val receiveBuffer = ByteArray(16384)
                while (isListening && socket?.isClosed == false) {
                    try {
                        val packet = DatagramPacket(receiveBuffer, receiveBuffer.size)
                        socket?.receive(packet)
                        val senderIp = packet.address?.hostAddress ?: "LOCAL"
                        if (senderIp != _localIpAddress.value) {
                            val receivedData = ByteArray(packet.length)
                            System.arraycopy(packet.data, 0, receivedData, 0, packet.length)
                            processRawDataBytes(receivedData, senderIp)
                        }
                    } catch (e: Exception) {
                        if (!isListening) break
                    }
                }
            }

            // Start Beacon Heartbeat
            beaconJob = scope.launch {
                while (isListening) {
                    sendBeaconPacket()
                    cleanStalePeers()
                    delay(3000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopP2PEngine() {
        isListening = false
        listenJob?.cancel()
        beaconJob?.cancel()
        socket?.close()
        socket = null
        cloudRelayClient.disconnect()
        _isRelayConnected.value = false
    }

    /**
     * Sends encrypted (and optionally obfuscated) Lyra voice packet over UDP and Cloud Relay.
     */
    fun sendEncryptedVoicePacket(
        lyraBytes: ByteArray,
        targetIp: String? = null
    ) {
        scope.launch {
            try {
                val secretKey = EncryptionManager.deriveKey(activePassphrase, activeChannelId)
                val encryptedData = EncryptionManager.encrypt(lyraBytes, secretKey)

                val nameBytes = myDeviceName.padEnd(16, ' ').take(16).toByteArray(Charsets.UTF_8)
                val headerBytes = MAGIC_AUDIO_HEADER.toByteArray(Charsets.UTF_8)

                val packetData = ByteArray(headerBytes.size + 4 + nameBytes.size + encryptedData.size)
                System.arraycopy(headerBytes, 0, packetData, 0, headerBytes.size)

                var offset = headerBytes.size
                packetData[offset] = (activeChannelId and 0xFF).toByte()
                offset += 4

                System.arraycopy(nameBytes, 0, packetData, offset, nameBytes.size)
                offset += nameBytes.size

                System.arraycopy(encryptedData, 0, packetData, offset, encryptedData.size)

                // Apply TLS 1.3 Obfuscation if enabled
                val finalTransmissionBytes = if (_isObfuscationEnabled.value) {
                    ProtocolObfuscator.obfuscate(packetData, activePassphrase, activeChannelId)
                } else {
                    packetData
                }

                // Send via Cloud Relay if in Internet Global Mode
                if (_networkMode.value == NetworkMode.INTERNET_GLOBAL) {
                    cloudRelayClient.sendBinaryData(finalTransmissionBytes)
                }

                // Send via Local UDP Unicast or Broadcast
                if (!targetIp.isNullOrBlank() && targetIp != "127.0.0.1") {
                    sendUdpData(finalTransmissionBytes, targetIp)
                } else {
                    sendUdpData(finalTransmissionBytes, "255.255.255.255")
                    _discoveredPeers.value.values.forEach { peer ->
                        if (peer.channelId == activeChannelId && peer.ipAddress != _localIpAddress.value) {
                            sendUdpData(finalTransmissionBytes, peer.ipAddress)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sends high priority Nextel Direct Alert beep.
     */
    fun sendAlertCall(targetIp: String? = null) {
        scope.launch {
            try {
                val headerBytes = MAGIC_ALERT_HEADER.toByteArray(Charsets.UTF_8)
                val nameBytes = myDeviceName.padEnd(16, ' ').take(16).toByteArray(Charsets.UTF_8)
                val packetData = ByteArray(headerBytes.size + 4 + nameBytes.size)

                System.arraycopy(headerBytes, 0, packetData, 0, headerBytes.size)
                packetData[headerBytes.size] = (activeChannelId and 0xFF).toByte()
                System.arraycopy(nameBytes, 0, packetData, headerBytes.size + 4, nameBytes.size)

                val finalTransmissionBytes = if (_isObfuscationEnabled.value) {
                    ProtocolObfuscator.obfuscate(packetData, activePassphrase, activeChannelId)
                } else {
                    packetData
                }

                if (_networkMode.value == NetworkMode.INTERNET_GLOBAL) {
                    cloudRelayClient.sendBinaryData(finalTransmissionBytes)
                }

                if (!targetIp.isNullOrBlank()) {
                    sendUdpData(finalTransmissionBytes, targetIp)
                } else {
                    sendUdpData(finalTransmissionBytes, "255.255.255.255")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sends encrypted text message over active channel (P2P UDP and Cloud Relay)
     */
    fun sendEncryptedTextMessage(
        textMessage: String,
        targetIp: String? = null
    ): ByteArray? {
        try {
            val textBytes = textMessage.toByteArray(Charsets.UTF_8)
            val secretKey = EncryptionManager.deriveKey(activePassphrase, activeChannelId)
            val encryptedData = EncryptionManager.encrypt(textBytes, secretKey)

            val nameBytes = myDeviceName.padEnd(16, ' ').take(16).toByteArray(Charsets.UTF_8)
            val headerBytes = MAGIC_TEXT_HEADER.toByteArray(Charsets.UTF_8)

            val packetData = ByteArray(headerBytes.size + 4 + nameBytes.size + encryptedData.size)
            System.arraycopy(headerBytes, 0, packetData, 0, headerBytes.size)

            var offset = headerBytes.size
            packetData[offset] = (activeChannelId and 0xFF).toByte()
            offset += 4

            System.arraycopy(nameBytes, 0, packetData, offset, nameBytes.size)
            offset += nameBytes.size

            System.arraycopy(encryptedData, 0, packetData, offset, encryptedData.size)

            val finalTransmissionBytes = if (_isObfuscationEnabled.value) {
                ProtocolObfuscator.obfuscate(packetData, activePassphrase, activeChannelId)
            } else {
                packetData
            }

            scope.launch {
                try {
                    if (_networkMode.value == NetworkMode.INTERNET_GLOBAL) {
                        cloudRelayClient.sendBinaryData(finalTransmissionBytes)
                    }

                    if (!targetIp.isNullOrBlank() && targetIp != "127.0.0.1") {
                        sendUdpData(finalTransmissionBytes, targetIp)
                    } else {
                        sendUdpData(finalTransmissionBytes, "255.255.255.255")
                        _discoveredPeers.value.values.forEach { peer ->
                            if (peer.channelId == activeChannelId && peer.ipAddress != _localIpAddress.value) {
                                sendUdpData(finalTransmissionBytes, peer.ipAddress)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return encryptedData
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun sendBeaconPacket() {
        try {
            val headerBytes = MAGIC_BEACON_HEADER.toByteArray(Charsets.UTF_8)
            val nameBytes = myDeviceName.padEnd(16, ' ').take(16).toByteArray(Charsets.UTF_8)
            val idBytes = myDeviceId.padEnd(12, ' ').take(12).toByteArray(Charsets.UTF_8)

            val packetData = ByteArray(headerBytes.size + 4 + nameBytes.size + idBytes.size)
            System.arraycopy(headerBytes, 0, packetData, 0, headerBytes.size)

            var offset = headerBytes.size
            packetData[offset] = (activeChannelId and 0xFF).toByte()
            offset += 4

            System.arraycopy(nameBytes, 0, packetData, offset, nameBytes.size)
            offset += nameBytes.size

            System.arraycopy(idBytes, 0, packetData, offset, idBytes.size)

            val finalTransmissionBytes = if (_isObfuscationEnabled.value) {
                ProtocolObfuscator.obfuscate(packetData, activePassphrase, activeChannelId)
            } else {
                packetData
            }

            if (_networkMode.value == NetworkMode.INTERNET_GLOBAL) {
                cloudRelayClient.sendBinaryData(finalTransmissionBytes)
            } else {
                sendUdpData(finalTransmissionBytes, "255.255.255.255")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processRawDataBytes(rawData: ByteArray, senderIp: String) {
        // Try deobfuscating first if obfuscated
        val payload = ProtocolObfuscator.deobfuscate(rawData, activePassphrase, activeChannelId) ?: rawData

        if (payload.size < 8) return
        val headerStr = String(payload, 0, 8, Charsets.UTF_8)

        when {
            headerStr.startsWith(MAGIC_AUDIO_HEADER) -> {
                val channelId = payload[8].toInt() and 0xFF
                if (channelId != activeChannelId) return

                val nameStr = String(payload, 12, 16, Charsets.UTF_8).trim()
                val encryptedSize = payload.size - 28
                if (encryptedSize <= 12) return

                val encryptedBytes = ByteArray(encryptedSize)
                System.arraycopy(payload, 28, encryptedBytes, 0, encryptedSize)

                // Decrypt AES-256
                val secretKey = EncryptionManager.deriveKey(activePassphrase, activeChannelId)
                val decryptedLyra = EncryptionManager.decrypt(encryptedBytes, secretKey) ?: return

                // Decompress Lyra 2
                val lyraDecoder = LyraCodecSimulator()
                val pcmAudio = lyraDecoder.decompressLyraToPcm(decryptedLyra)

                onAudioReceivedListener?.invoke(
                    nameStr.ifBlank { "Radio Peer" },
                    senderIp,
                    pcmAudio,
                    3.2f
                )
            }

            headerStr.startsWith(MAGIC_BEACON_HEADER) -> {
                val channelId = payload[8].toInt() and 0xFF
                val nameStr = String(payload, 12, 16, Charsets.UTF_8).trim()
                val peerIdStr = String(payload, 28, 12, Charsets.UTF_8).trim()

                val newPeer = DiscoveredPeer(
                    id = peerIdStr,
                    name = nameStr,
                    ipAddress = senderIp,
                    channelId = channelId
                )

                val currentMap = _discoveredPeers.value.toMutableMap()
                currentMap[peerIdStr] = newPeer
                _discoveredPeers.value = currentMap
            }

            headerStr.startsWith(MAGIC_ALERT_HEADER) -> {
                val channelId = payload[8].toInt() and 0xFF
                if (channelId != activeChannelId) return
                val nameStr = String(payload, 12, 16, Charsets.UTF_8).trim()

                onAlertReceivedListener?.invoke(nameStr.ifBlank { "Radio Peer" }, senderIp)
            }

            headerStr.startsWith(MAGIC_TEXT_HEADER) -> {
                val channelId = payload[8].toInt() and 0xFF
                if (channelId != activeChannelId) return

                val nameStr = String(payload, 12, 16, Charsets.UTF_8).trim()
                val encryptedSize = payload.size - 28
                if (encryptedSize <= 12) return

                val encryptedBytes = ByteArray(encryptedSize)
                System.arraycopy(payload, 28, encryptedBytes, 0, encryptedSize)

                // Decrypt AES-256
                val secretKey = EncryptionManager.deriveKey(activePassphrase, activeChannelId)
                val decryptedBytes = EncryptionManager.decrypt(encryptedBytes, secretKey) ?: return
                val textMessage = String(decryptedBytes, Charsets.UTF_8)

                onTextReceivedListener?.invoke(
                    nameStr.ifBlank { "Radio Peer" },
                    senderIp,
                    textMessage,
                    encryptedBytes
                )
            }
        }
    }

    private fun sendUdpData(bytes: ByteArray, ipStr: String) {
        try {
            val address = InetAddress.getByName(ipStr)
            val packet = DatagramPacket(bytes, bytes.size, address, UDP_PORT)
            socket?.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanStalePeers() {
        val now = System.currentTimeMillis()
        val updated = _discoveredPeers.value.filterValues { now - it.lastSeenMs < 12000 }
        if (updated.size != _discoveredPeers.value.size) {
            _discoveredPeers.value = updated
        }
    }

    private fun detectLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress.hostAddress?.contains(":") == false) {
                        return inetAddress.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "192.168.1.100"
    }
}
