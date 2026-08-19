package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.RadioSoundEffects
import com.example.codec.LyraCodecSimulator
import com.example.crypto.EncryptionManager
import com.example.data.AppDatabase
import com.example.data.VoiceMessageEntity
import com.example.data.VoiceMessageRepository
import com.example.network.DiscoveredPeer
import com.example.network.NetworkMode
import com.example.network.P2PVoiceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PttState {
    IDLE,
    RECORDING,
    TRANSMITTING,
    RECEIVING
}

data class TextNotification(
    val senderName: String,
    val senderIp: String,
    val text: String,
    val timestamp: Long
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoiceMessageRepository
    val voiceEngine = P2PVoiceEngine()
    val audioEngine = AudioEngine()
    val radioSoundEffects = RadioSoundEffects()
    val lyraCodec = LyraCodecSimulator(targetBitrateKbps = 3.2f)

    private val _pttState = MutableStateFlow(PttState.IDLE)
    val pttState: StateFlow<PttState> = _pttState.asStateFlow()

    private val _currentChannelId = MutableStateFlow(1)
    val currentChannelId: StateFlow<Int> = _currentChannelId.asStateFlow()

    private val _passphrase = MutableStateFlow("CometaNextel2026")
    val passphrase: StateFlow<String> = _passphrase.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(true)
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _targetBitrateKbps = MutableStateFlow(3.2f)
    val targetBitrateKbps: StateFlow<Float> = _targetBitrateKbps.asStateFlow()

    private val _activeSpeakerName = MutableStateFlow<String?>(null)
    val activeSpeakerName: StateFlow<String?> = _activeSpeakerName.asStateFlow()

    private val _activeSpeakerIp = MutableStateFlow<String?>(null)
    val activeSpeakerIp: StateFlow<String?> = _activeSpeakerIp.asStateFlow()

    private val _targetPeerIp = MutableStateFlow<String?>(null)
    val targetPeerIp: StateFlow<String?> = _targetPeerIp.asStateFlow()

    private val _playingMessageId = MutableStateFlow<Long?>(null)
    val playingMessageId: StateFlow<Long?> = _playingMessageId.asStateFlow()

    private val _lastReceivedText = MutableStateFlow<TextNotification?>(null)
    val lastReceivedText: StateFlow<TextNotification?> = _lastReceivedText.asStateFlow()

    val discoveredPeers: StateFlow<Map<String, DiscoveredPeer>> = voiceEngine.discoveredPeers
    val localIpAddress: StateFlow<String> = voiceEngine.localIpAddress
    val spectrumAmplitudes: StateFlow<List<Float>> = audioEngine.spectrumAmplitudes
    val isPlayingAudio: StateFlow<Boolean> = audioEngine.isPlayingAudio

    val networkMode: StateFlow<NetworkMode> = voiceEngine.networkMode
    val isObfuscationEnabled: StateFlow<Boolean> = voiceEngine.isObfuscationEnabled
    val cloudRoomCode: StateFlow<String> = voiceEngine.cloudRoomCode
    val isRelayConnected: StateFlow<Boolean> = voiceEngine.isRelayConnected

    // Encryption Key Fingerprint
    val encryptionFingerprint: StateFlow<String> = combine(_passphrase, _currentChannelId) { pass, channel ->
        val key = EncryptionManager.deriveKey(pass, channel)
        EncryptionManager.getKeyFingerprint(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "AES-256 [SECURE]")

    // Room Database Message Log for active channel
    val channelMessages: StateFlow<List<VoiceMessageEntity>> = _currentChannelId.flatMapLatest { ch ->
        repository.getMessagesForChannel(ch)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var recordedPcmList = mutableListOf<Short>()
    private var recordStartMs: Long = 0

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VoiceMessageRepository(database.voiceMessageDao(), application)

        voiceEngine.startP2PEngine()

        // Incoming Audio Callback
        voiceEngine.setOnAudioReceivedListener { senderName, senderIp, pcmAudio, bitrate ->
            if (_pttState.value == PttState.RECORDING) return@setOnAudioReceivedListener

            viewModelScope.launch(Dispatchers.Main) {
                _pttState.value = PttState.RECEIVING
                _activeSpeakerName.value = senderName
                _activeSpeakerIp.value = senderIp

                radioSoundEffects.playPttStartChirp(_soundEffectsEnabled.value)

                val durationMs = (pcmAudio.size * 1000L) / AudioEngine.SAMPLE_RATE
                val lyraResult = lyraCodec.compressPcmToLyra(pcmAudio)

                // Save received audio message to local database
                repository.saveVoiceMessage(
                    senderName = senderName,
                    senderIp = senderIp,
                    channelId = _currentChannelId.value,
                    durationMs = maxOf(300L, durationMs),
                    isOutgoing = false,
                    bitrateKbps = bitrate,
                    pcmSamples = pcmAudio,
                    encryptedBytesPreview = lyraResult.compressedBytes
                )

                // Play received audio directly via AudioEngine
                audioEngine.playPcmAudio(pcmAudio) {
                    viewModelScope.launch(Dispatchers.Main) {
                        radioSoundEffects.playPttEndSquelch(_soundEffectsEnabled.value)
                        _pttState.value = PttState.IDLE
                        _activeSpeakerName.value = null
                        _activeSpeakerIp.value = null
                    }
                }
            }
        }

        // Incoming Encrypted Text Message Callback
        voiceEngine.setOnTextReceivedListener { senderName, senderIp, textMessage, rawEncrypted ->
            viewModelScope.launch(Dispatchers.Main) {
                radioSoundEffects.playTextMessageChirp(_soundEffectsEnabled.value)
                _lastReceivedText.value = TextNotification(
                    senderName = senderName,
                    senderIp = senderIp,
                    text = textMessage,
                    timestamp = System.currentTimeMillis()
                )

                repository.saveTextMessage(
                    senderName = senderName,
                    senderIp = senderIp,
                    channelId = _currentChannelId.value,
                    textContent = textMessage,
                    isOutgoing = false,
                    encryptedBytesPreview = rawEncrypted
                )
            }
        }

        // Incoming Alert Call
        voiceEngine.setOnAlertReceivedListener { senderName, senderIp ->
            viewModelScope.launch(Dispatchers.Main) {
                radioSoundEffects.playCallAlertBeep(_soundEffectsEnabled.value)
                _activeSpeakerName.value = "ALERTA: $senderName"
                _activeSpeakerIp.value = senderIp
                kotlinx.coroutines.delay(2000)
                if (_pttState.value == PttState.IDLE) {
                    _activeSpeakerName.value = null
                    _activeSpeakerIp.value = null
                }
            }
        }
    }

    fun setChannel(channelId: Int) {
        _currentChannelId.value = channelId
        voiceEngine.activeChannelId = channelId
    }

    fun setPassphrase(newPass: String) {
        _passphrase.value = newPass
        voiceEngine.activePassphrase = newPass
    }

    fun setBitrate(kbps: Float) {
        _targetBitrateKbps.value = kbps
        lyraCodec.targetBitrateKbps = kbps
    }

    fun toggleSoundEffects() {
        _soundEffectsEnabled.value = !_soundEffectsEnabled.value
    }

    fun setTargetPeerIp(ip: String?) {
        _targetPeerIp.value = if (ip.isNullByBlank()) null else ip
    }

    fun setNetworkMode(mode: NetworkMode) {
        voiceEngine.setNetworkMode(mode)
    }

    fun toggleObfuscation(enabled: Boolean) {
        voiceEngine.setObfuscationEnabled(enabled)
    }

    fun setCloudRoomCode(code: String) {
        voiceEngine.setCloudRoomCode(code)
    }

    fun dismissLastTextNotification() {
        _lastReceivedText.value = null
    }

    /**
     * Sends encrypted text message over P2P/Internet channel.
     */
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val encryptedBytes = voiceEngine.sendEncryptedTextMessage(
                textMessage = text.trim(),
                targetIp = _targetPeerIp.value
            ) ?: ByteArray(0)

            radioSoundEffects.playTextMessageChirp(_soundEffectsEnabled.value)

            repository.saveTextMessage(
                senderName = voiceEngine.myDeviceName,
                senderIp = if (networkMode.value == NetworkMode.INTERNET_GLOBAL) "INTERNET (${cloudRoomCode.value})" else localIpAddress.value,
                channelId = _currentChannelId.value,
                textContent = text.trim(),
                isOutgoing = true,
                encryptedBytesPreview = encryptedBytes
            )
        }
    }

    /**
     * User pressed and holds PTT button.
     */
    fun startPttTalk() {
        if (_pttState.value != PttState.IDLE) return
        _pttState.value = PttState.RECORDING
        recordedPcmList.clear()
        recordStartMs = System.currentTimeMillis()

        radioSoundEffects.playPttStartChirp(_soundEffectsEnabled.value)

        audioEngine.startRecording { frameShorts ->
            for (s in frameShorts) {
                recordedPcmList.add(s)
            }
        }
    }

    /**
     * User released PTT button.
     */
    fun stopPttTalk() {
        if (_pttState.value != PttState.RECORDING) return
        audioEngine.stopRecording()

        _pttState.value = PttState.TRANSMITTING
        val durationMs = System.currentTimeMillis() - recordStartMs
        val fullPcm = recordedPcmList.toShortArray()

        if (fullPcm.size > 800) {
            viewModelScope.launch(Dispatchers.IO) {
                val lyraResult = lyraCodec.compressPcmToLyra(fullPcm)
                voiceEngine.sendEncryptedVoicePacket(
                    lyraBytes = lyraResult.compressedBytes,
                    targetIp = _targetPeerIp.value
                )

                repository.saveVoiceMessage(
                    senderName = voiceEngine.myDeviceName,
                    senderIp = if (networkMode.value == NetworkMode.INTERNET_GLOBAL) "INTERNET (${cloudRoomCode.value})" else localIpAddress.value,
                    channelId = _currentChannelId.value,
                    durationMs = maxOf(300L, durationMs),
                    isOutgoing = true,
                    bitrateKbps = _targetBitrateKbps.value,
                    pcmSamples = fullPcm,
                    encryptedBytesPreview = lyraResult.compressedBytes
                )
            }
        }

        radioSoundEffects.playPttEndSquelch(_soundEffectsEnabled.value)
        _pttState.value = PttState.IDLE
    }

    fun sendDirectAlert() {
        radioSoundEffects.playCallAlertBeep(_soundEffectsEnabled.value)
        voiceEngine.sendAlertCall(_targetPeerIp.value)
    }

    fun playHistoryMessage(msg: VoiceMessageEntity) {
        if (_playingMessageId.value == msg.id) {
            audioEngine.stopPlayback()
            _playingMessageId.value = null
            return
        }

        viewModelScope.launch {
            val pcm = repository.loadPcmFromPath(msg.audioFilePath)
            if (pcm.isNotEmpty()) {
                _playingMessageId.value = msg.id
                radioSoundEffects.playPttStartChirp(_soundEffectsEnabled.value)
                audioEngine.playPcmAudio(pcm) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _playingMessageId.value = null
                    }
                }
            }
        }
    }

    fun stopAudioPlayback() {
        audioEngine.stopPlayback()
        _playingMessageId.value = null
    }

    fun deleteHistoryMessage(id: Long) {
        viewModelScope.launch {
            if (_playingMessageId.value == id) {
                audioEngine.stopPlayback()
                _playingMessageId.value = null
            }
            repository.deleteMessage(id)
        }
    }

    fun clearChannelHistory() {
        viewModelScope.launch {
            audioEngine.stopPlayback()
            _playingMessageId.value = null
            repository.clearChannel(_currentChannelId.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.stopP2PEngine()
        audioEngine.stopRecording()
        audioEngine.stopPlayback()
    }

    private fun String?.isNullByBlank(): Boolean {
        return this == null || this.isBlank()
    }
}
