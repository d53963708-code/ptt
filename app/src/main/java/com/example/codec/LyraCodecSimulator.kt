package com.example.codec

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Lyra 2 Ultra-Low Bitrate Neural Voice Codec Simulator.
 * Encodes 16kHz 16-bit PCM voice speech into low-bitrate compressed frame packets (3.2 kbps - 9.6 kbps),
 * reducing audio data footprint by up to 93% for real-time P2P walkie-talkie transmission.
 */
class LyraCodecSimulator(
    var targetBitrateKbps: Float = 3.2f // 3.2 kbps, 6.0 kbps, or 9.6 kbps
) {
    companion object {
        const val MAGIC_HEADER_0: Byte = 0x4C // 'L'
        const val MAGIC_HEADER_1: Byte = 0x32 // '2'
        const val SAMPLE_RATE = 16000
        const val FRAME_DURATION_MS = 20 // 20ms per frame
        const val SAMPLES_PER_FRAME = SAMPLE_RATE * FRAME_DURATION_MS / 1000 // 320 samples per frame
    }

    data class CompressionResult(
        val compressedBytes: ByteArray,
        val rawByteCount: Int,
        val compressedByteCount: Int,
        val compressionRatio: Float,
        val bitrateKbps: Float
    )

    /**
     * Compresses 16kHz 16-bit mono PCM short array into Lyra 2 binary frames.
     */
    fun compressPcmToLyra(pcmSamples: ShortArray): CompressionResult {
        val rawByteCount = pcmSamples.size * 2
        val frameCount = max(1, pcmSamples.size / SAMPLES_PER_FRAME)
        
        // Bytes per 20ms frame depending on target bitrate:
        // 3.2 kbps = 8 bytes per 20ms frame (3200 bits / 50 frames/sec / 8 = 8 bytes)
        // 6.0 kbps = 15 bytes per frame
        // 9.6 kbps = 24 bytes per frame
        val bytesPerFrame = when {
            targetBitrateKbps <= 4.0f -> 8
            targetBitrateKbps <= 7.0f -> 15
            else -> 24
        }

        val headerSize = 4 // [MAGIC_0, MAGIC_1, BitrateCode, FrameCountHi, FrameCountLo] -> 4 bytes
        val totalCompressedSize = headerSize + (frameCount * bytesPerFrame)
        val outputBytes = ByteArray(totalCompressedSize)

        // Write header
        outputBytes[0] = MAGIC_HEADER_0
        outputBytes[1] = MAGIC_HEADER_1
        outputBytes[2] = (targetBitrateKbps * 10).toInt().toByte()
        outputBytes[3] = (frameCount and 0xFF).toByte()

        var outOffset = headerSize

        for (f in 0 until frameCount) {
            val startIdx = f * SAMPLES_PER_FRAME
            val endIdx = min(pcmSamples.size, startIdx + SAMPLES_PER_FRAME)
            
            // Extract frame metrics (RMS energy, zero-crossing, sub-band spectral energies)
            var sumSquare = 0.0
            var zeroCrossings = 0
            var maxAmplitude = 0
            
            for (i in startIdx until endIdx) {
                val sample = pcmSamples[i].toInt()
                sumSquare += sample * sample
                maxAmplitude = max(maxAmplitude, abs(sample))
                if (i > startIdx && ((pcmSamples[i] >= 0) != (pcmSamples[i - 1] >= 0))) {
                    zeroCrossings++
                }
            }

            val frameSamples = max(1, endIdx - startIdx)
            val rms = sqrt(sumSquare / frameSamples)
            
            // Quantize RMS energy to 8-bit log scale
            val quantizedRms = (min(255.0, (rms / 32767.0) * 255.0 * 2.5)).toInt().toByte()
            val quantizedZcr = min(255, zeroCrossings).toByte()
            val quantizedMaxAmp = min(255, (maxAmplitude shr 7)).toByte()

            // Write quantized frame header
            outputBytes[outOffset] = quantizedRms
            outputBytes[outOffset + 1] = quantizedZcr
            outputBytes[outOffset + 2] = quantizedMaxAmp

            // Fill sub-band spectral residual features
            for (b in 3 until bytesPerFrame) {
                val subIdx = startIdx + ((b - 3) * frameSamples / (bytesPerFrame - 3))
                val valSub = if (subIdx < endIdx) (pcmSamples[subIdx].toInt() shr 8).toByte() else 0
                outputBytes[outOffset + b] = valSub
            }

            outOffset += bytesPerFrame
        }

        val compressionRatio = if (totalCompressedSize > 0) rawByteCount.toFloat() / totalCompressedSize.toFloat() else 1.0f

        return CompressionResult(
            compressedBytes = outputBytes,
            rawByteCount = rawByteCount,
            compressedByteCount = totalCompressedSize,
            compressionRatio = compressionRatio,
            bitrateKbps = targetBitrateKbps
        )
    }

    /**
     * Decompresses Lyra 2 binary payload back to 16kHz 16-bit PCM for audio playback.
     */
    fun decompressLyraToPcm(lyraBytes: ByteArray): ShortArray {
        if (lyraBytes.size < 4 || lyraBytes[0] != MAGIC_HEADER_0 || lyraBytes[1] != MAGIC_HEADER_1) {
            // Fallback: If not valid Lyra format, decode raw shorts
            val shorts = ShortArray(lyraBytes.size / 2)
            for (i in shorts.indices) {
                val low = lyraBytes[i * 2].toInt() and 0xFF
                val high = lyraBytes[i * 2 + 1].toInt()
                shorts[i] = ((high shl 8) or low).toShort()
            }
            return shorts
        }

        val frameCount = lyraBytes[3].toInt() and 0xFF
        val headerSize = 4
        val payloadSize = lyraBytes.size - headerSize
        val bytesPerFrame = if (frameCount > 0) max(4, payloadSize / frameCount) else 8

        val totalPcmSamples = frameCount * SAMPLES_PER_FRAME
        val pcmOutput = ShortArray(max(SAMPLES_PER_FRAME, totalPcmSamples))

        var inOffset = headerSize

        for (f in 0 until frameCount) {
            if (inOffset + bytesPerFrame > lyraBytes.size) break

            val quantizedRms = lyraBytes[inOffset].toInt() and 0xFF
            val quantizedZcr = lyraBytes[inOffset + 1].toInt() and 0xFF
            val quantizedMaxAmp = lyraBytes[inOffset + 2].toInt() and 0xFF

            val rmsFactor = (quantizedRms / 255.0) * 32767.0 / 2.5
            val pitchFreq = 120.0 + (quantizedZcr * 1.5) // Reconstructed fundamental frequency

            val startSample = f * SAMPLES_PER_FRAME

            for (i in 0 until SAMPLES_PER_FRAME) {
                val t = i.toDouble() / SAMPLE_RATE
                val sineWave = sin(2.0 * Math.PI * pitchFreq * t)
                val noise = (Math.random() * 2.0 - 1.0)
                
                // Mix harmonic voice generator with noise based on zero-crossing rate
                val mixRatio = min(1.0, quantizedZcr / 100.0)
                val rawVal = (sineWave * (1.0 - mixRatio)) + (noise * mixRatio)

                // Sub-band residual interpolation from frame bytes
                val byteSubIdx = 3 + ((i * (bytesPerFrame - 3)) / SAMPLES_PER_FRAME)
                val residualByte = if (byteSubIdx < bytesPerFrame) lyraBytes[inOffset + byteSubIdx].toInt() else 0
                val residualVal = (residualByte shl 7) / 32768.0

                val finalSample = ((rawVal * rmsFactor) + (residualVal * rmsFactor * 0.5)).coerceIn(-32767.0, 32767.0)
                
                val outIdx = startSample + i
                if (outIdx < pcmOutput.size) {
                    pcmOutput[outIdx] = finalSample.toInt().toShort()
                }
            }

            inOffset += bytesPerFrame
        }

        return pcmOutput
    }
}
