package com.example.crypto

import java.security.MessageDigest
import kotlin.random.Random

object ProtocolObfuscator {

    // TLS 1.3 Record Header bytes: 0x17 (Application Data), 0x03, 0x03 (TLS 1.2/1.3 Version)
    private val TLS_RECORD_HEADER = byteArrayOf(0x17.toByte(), 0x03.toByte(), 0x03.toByte())

    /**
     * Applies TLS 1.3 Application Data envelope + Dynamic XOR Header Masking + Variable Padding.
     * Hides all magic ASCII headers and VOIP signatures from Deep Packet Inspection (DPI).
     */
    fun obfuscate(payload: ByteArray, passphrase: String, channelId: Int): ByteArray {
        val keyStream = generateKeystream(passphrase, channelId, payload.size)

        // 1. XOR mask the payload with keystream
        val xorPayload = ByteArray(payload.size)
        for (i in payload.indices) {
            xorPayload[i] = (payload[i].toInt() xor keyStream[i % keyStream.size].toInt()).toByte()
        }

        // 2. Add variable random padding (16 to 48 bytes) to scramble packet lengths
        val paddingLength = Random.nextInt(16, 48)
        val paddingBytes = ByteArray(paddingLength)
        Random.nextBytes(paddingBytes)

        // Inner frame size: XOR payload + 1 byte padding length indicator + padding
        val innerSize = xorPayload.size + 1 + paddingLength

        // 3. Wrap in TLS 1.3 Record Envelope [3B Header][2B BigEndian Length][Inner Payload]
        val totalPacketSize = TLS_RECORD_HEADER.size + 2 + innerSize
        val packet = ByteArray(totalPacketSize)

        // TLS Header (0x17, 0x03, 0x03)
        System.arraycopy(TLS_RECORD_HEADER, 0, packet, 0, TLS_RECORD_HEADER.size)

        // TLS Length (2 bytes Big Endian)
        packet[3] = ((innerSize shr 8) and 0xFF).toByte()
        packet[4] = (innerSize and 0xFF).toByte()

        // Copy XOR Payload
        System.arraycopy(xorPayload, 0, packet, 5, xorPayload.size)

        // Padding metadata
        val padOffset = 5 + xorPayload.size
        packet[padOffset] = (paddingLength and 0xFF).toByte()
        System.arraycopy(paddingBytes, 0, packet, padOffset + 1, paddingLength)

        return packet
    }

    /**
     * Strips TLS 1.3 record envelope, removes variable random noise padding, and de-XORs payload.
     */
    fun deobfuscate(obfuscatedPacket: ByteArray, passphrase: String, channelId: Int): ByteArray? {
        if (obfuscatedPacket.size < 10) return null

        // Verify TLS 1.3 Header Prefix (0x17, 0x03, 0x03)
        if (obfuscatedPacket[0] != TLS_RECORD_HEADER[0] ||
            obfuscatedPacket[1] != TLS_RECORD_HEADER[1] ||
            obfuscatedPacket[2] != TLS_RECORD_HEADER[2]
        ) {
            // Not a TLS wrapped packet
            return null
        }

        val recordLength = ((obfuscatedPacket[3].toInt() and 0xFF) shl 8) or (obfuscatedPacket[4].toInt() and 0xFF)
        if (obfuscatedPacket.size < 5 + recordLength) return null

        // Extract padding length from the end
        val paddingLength = obfuscatedPacket[obfuscatedPacket.size - 1].toInt() and 0xFF
        val payloadSize = recordLength - 1 - paddingLength
        if (payloadSize <= 0 || payloadSize > obfuscatedPacket.size) return null

        val xorPayload = ByteArray(payloadSize)
        System.arraycopy(obfuscatedPacket, 5, xorPayload, 0, payloadSize)

        val keyStream = generateKeystream(passphrase, channelId, payloadSize)
        val originalPayload = ByteArray(payloadSize)
        for (i in xorPayload.indices) {
            originalPayload[i] = (xorPayload[i].toInt() xor keyStream[i % keyStream.size].toInt()).toByte()
        }

        return originalPayload
    }

    private fun generateKeystream(passphrase: String, channelId: Int, minLength: Int): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "OBFUSCATION_SALT_TLS13:$passphrase:$channelId"
        var hash = digest.digest(combined.toByteArray(Charsets.UTF_8))

        while (hash.size < minLength) {
            digest.update(hash)
            hash += digest.digest(combined.toByteArray(Charsets.UTF_8))
        }

        return hash
    }
}
