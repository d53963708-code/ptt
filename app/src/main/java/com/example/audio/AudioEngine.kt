package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Handles low-latency PCM microphone recording during Push-To-Talk hold
 * and low-latency audio playback for walkie-talkie audio packets.
 */
class AudioEngine {
    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null

    private val _spectrumAmplitudes = MutableStateFlow(List(16) { 0f })
    val spectrumAmplitudes: StateFlow<List<Float>> = _spectrumAmplitudes.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private var currentPlayingTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    fun startRecording(onFrameRecorded: (ShortArray) -> Unit) {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG_IN,
            AUDIO_FORMAT
        )
        val bufferSize = maxOf(minBufferSize, 1024)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT,
                bufferSize
            )

            audioRecord?.startRecording()
            isRecording = true

            recordingJob = scope.launch {
                val buffer = ShortArray(320) // 20ms frame at 16kHz
                val spectrumBuffer = FloatArray(16)

                while (isRecording && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readCount > 0) {
                        val frameData = buffer.copyOf(readCount)
                        onFrameRecorded(frameData)

                        // Calculate visual audio spectrum amplitudes for Compose Waveform
                        var maxAmp = 0
                        for (i in 0 until readCount) {
                            maxAmp = maxOf(maxAmp, abs(buffer[i].toInt()))
                        }
                        val normalizedAmp = (maxAmp / 32768f).coerceIn(0f, 1f)

                        for (i in 0 until 15) {
                            spectrumBuffer[i] = spectrumBuffer[i + 1] * 0.85f
                        }
                        spectrumBuffer[15] = normalizedAmp
                        _spectrumAmplitudes.value = spectrumBuffer.toList()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
        }
    }

    fun stopRecording(): List<Float> {
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
        _spectrumAmplitudes.value = List(16) { 0f }
        return emptyList()
    }

    fun playPcmAudio(pcmSamples: ShortArray, onComplete: (() -> Unit)? = null) {
        if (pcmSamples.isEmpty()) {
            onComplete?.invoke()
            return
        }

        stopPlayback()

        playbackJob = scope.launch {
            try {
                _isPlayingAudio.value = true
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIG_OUT,
                    AUDIO_FORMAT
                )
                val bufferSize = maxOf(minBufferSize, pcmSamples.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_CONFIG_OUT)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                currentPlayingTrack = track
                track.play()

                // Stream PCM samples in 20ms chunks (320 shorts)
                val chunkSize = 320
                var offset = 0
                val spectrumBuffer = FloatArray(16)

                while (offset < pcmSamples.size && _isPlayingAudio.value) {
                    val length = minOf(chunkSize, pcmSamples.size - offset)
                    track.write(pcmSamples, offset, length)

                    // Calculate real-time amplitude for chunk
                    var maxAmp = 0
                    for (i in 0 until length) {
                        maxAmp = maxOf(maxAmp, abs(pcmSamples[offset + i].toInt()))
                    }
                    val normalized = (maxAmp / 32768f).coerceIn(0.05f, 1f)

                    for (i in 0 until 15) {
                        spectrumBuffer[i] = spectrumBuffer[i + 1] * 0.85f
                    }
                    spectrumBuffer[15] = normalized
                    _spectrumAmplitudes.value = spectrumBuffer.toList()

                    offset += length
                    kotlinx.coroutines.delay(18) // ~20ms per frame
                }

                // Brief tail delay to flush output buffer
                kotlinx.coroutines.delay(100)
                try {
                    track.stop()
                    track.release()
                } catch (ignored: Exception) {}
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                currentPlayingTrack = null
                _isPlayingAudio.value = false
                _spectrumAmplitudes.value = List(16) { 0f }
                onComplete?.invoke()
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            currentPlayingTrack?.stop()
            currentPlayingTrack?.release()
        } catch (ignored: Exception) {}
        currentPlayingTrack = null
        _isPlayingAudio.value = false
        _spectrumAmplitudes.value = List(16) { 0f }
    }
}
