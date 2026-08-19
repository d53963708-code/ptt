package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Generates authentic Walkie-Talkie (Nextel style) radio sound effects:
 * - PTT Start Chirp: Classic high-pitched dual frequency beep when pressing PTT.
 * - End Transmission Squelch: White noise burst & roger beep when releasing PTT or receiving end.
 * - Call Alert Beep: High-priority triple alert sound.
 */
class RadioSoundEffects {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val sampleRate = 22050

    /**
     * Plays the signature Nextel Direct-Connect chirp tone when pressing down PTT button.
     */
    fun playPttStartChirp(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            try {
                // Dual tone chirp: 1400Hz (40ms) then 1800Hz (50ms)
                val durationMs1 = 40
                val durationMs2 = 50
                val samples1 = (sampleRate * durationMs1) / 1000
                val samples2 = (sampleRate * durationMs2) / 1000
                val totalSamples = samples1 + samples2

                val pcmData = ShortArray(totalSamples)

                // Tone 1: 1400 Hz
                for (i in 0 until samples1) {
                    val angle = 2.0 * Math.PI * i * 1400.0 / sampleRate
                    pcmData[i] = (sin(angle) * 24000).toInt().toShort()
                }

                // Tone 2: 1800 Hz
                for (i in 0 until samples2) {
                    val angle = 2.0 * Math.PI * i * 1800.0 / sampleRate
                    pcmData[samples1 + i] = (sin(angle) * 28000).toInt().toShort()
                }

                playToneTrack(pcmData, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Plays radio squelch white noise burst + roger beep when PTT transmission ends.
     */
    fun playPttEndSquelch(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            try {
                // 60ms white noise + 40ms 1000Hz roger beep
                val noiseDurationMs = 60
                val beepDurationMs = 40
                val noiseSamples = (sampleRate * noiseDurationMs) / 1000
                val beepSamples = (sampleRate * beepDurationMs) / 1000
                val totalSamples = noiseSamples + beepSamples

                val pcmData = ShortArray(totalSamples)

                // White noise squelch burst
                for (i in 0 until noiseSamples) {
                    val env = (1.0 - (i.toDouble() / noiseSamples)) // Fade out
                    val noise = (Math.random() * 2.0 - 1.0) * env
                    pcmData[i] = (noise * 18000).toInt().toShort()
                }

                // Roger beep 1000 Hz
                for (i in 0 until beepSamples) {
                    val angle = 2.0 * Math.PI * i * 1000.0 / sampleRate
                    pcmData[noiseSamples + i] = (sin(angle) * 20000).toInt().toShort()
                }

                playToneTrack(pcmData, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Plays Nextel Direct Connect Alert call tone.
     */
    fun playCallAlertBeep(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            try {
                // Triple pulsed alert beep: 1200Hz
                val beepMs = 80
                val pauseMs = 40
                val beepCount = 3

                val beepSamples = (sampleRate * beepMs) / 1000
                val pauseSamples = (sampleRate * pauseMs) / 1000
                val totalSamples = (beepSamples + pauseSamples) * beepCount

                val pcmData = ShortArray(totalSamples)
                var idx = 0

                for (b in 0 until beepCount) {
                    for (i in 0 until beepSamples) {
                        val angle = 2.0 * Math.PI * i * 1200.0 / sampleRate
                        pcmData[idx++] = (sin(angle) * 28000).toInt().toShort()
                    }
                    for (i in 0 until pauseSamples) {
                        pcmData[idx++] = 0
                    }
                }

                playToneTrack(pcmData, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Plays a pleasant dual blip tone for incoming text messages.
     */
    fun playTextMessageChirp(enabled: Boolean = true) {
        if (!enabled) return
        scope.launch {
            try {
                val durationMs1 = 30
                val durationMs2 = 40
                val samples1 = (sampleRate * durationMs1) / 1000
                val samples2 = (sampleRate * durationMs2) / 1000
                val totalSamples = samples1 + samples2

                val pcmData = ShortArray(totalSamples)

                // High blip 1: 1760 Hz
                for (i in 0 until samples1) {
                    val angle = 2.0 * Math.PI * i * 1760.0 / sampleRate
                    pcmData[i] = (sin(angle) * 22000).toInt().toShort()
                }

                // High blip 2: 2340 Hz
                for (i in 0 until samples2) {
                    val angle = 2.0 * Math.PI * i * 2340.0 / sampleRate
                    pcmData[samples1 + i] = (sin(angle) * 26000).toInt().toShort()
                }

                playToneTrack(pcmData, sampleRate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playToneTrack(pcmData: ShortArray, rate: Int) {
        val bufferSize = pcmData.size * 2
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(rate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcmData, 0, pcmData.size)
        audioTrack.play()

        // Release track after playback completes
        Thread.sleep((pcmData.size * 1000L / rate) + 50)
        audioTrack.stop()
        audioTrack.release()
    }
}
