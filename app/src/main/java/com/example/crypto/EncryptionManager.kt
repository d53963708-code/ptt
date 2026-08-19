package com.example.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private val secureRandom = SecureRandom()

    /**
     * Derives a 256-bit AES key from a passphrase and optional channel ID / salt.
     */
    fun deriveKey(passphrase: String, channelId: Int = 1): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$passphrase:CometaChannel-$channelId:Salt2026"
        val hash = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(hash, ALGORITHM)
    }

    /**
     * Encrypts plaintext bytes using AES-256 GCM.
     * Output format: [12 bytes IV][Ciphertext + 16 bytes Tag]
     */
    fun encrypt(plainBytes: ByteArray, secretKey: SecretKey): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        val cipherText = cipher.doFinal(plainBytes)
        
        val result = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(cipherText, 0, result, iv.size, cipherText.size)
        return result
    }

    /**
     * Decrypts AES-256 GCM byte array.
     */
    fun decrypt(encryptedBytes: ByteArray, secretKey: SecretKey): ByteArray? {
        if (encryptedBytes.size <= GCM_IV_LENGTH) return null
        return try {
            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(encryptedBytes, 0, iv, 0, GCM_IV_LENGTH)

            val cipherTextSize = encryptedBytes.size - GCM_IV_LENGTH
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(encryptedBytes, GCM_IV_LENGTH, cipherText, 0, cipherTextSize)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Formats a short fingerprint string (e.g. "AES-256 [A4:8F:2B]") for UI status.
     */
    fun getKeyFingerprint(secretKey: SecretKey): String {
        val bytes = secretKey.encoded
        if (bytes == null || bytes.size < 3) return "AES-256 [SECURE]"
        val b1 = String.format("%02X", bytes[0])
        val b2 = String.format("%02X", bytes[1])
        val b3 = String.format("%02X", bytes[2])
        return "AES-256 [$b1:$b2:$b3]"
    }
}
