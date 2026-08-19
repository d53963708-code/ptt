package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class VoiceMessageRepository(
    private val dao: VoiceMessageDao,
    private val context: Context
) {
    val allMessages: Flow<List<VoiceMessageEntity>> = dao.getAllMessages()

    fun getMessagesForChannel(channelId: Int): Flow<List<VoiceMessageEntity>> {
        return dao.getMessagesForChannel(channelId)
    }

    suspend fun saveVoiceMessage(
        senderName: String,
        senderIp: String,
        channelId: Int,
        durationMs: Long,
        isOutgoing: Boolean,
        bitrateKbps: Float,
        pcmSamples: ShortArray,
        encryptedBytesPreview: ByteArray
    ) = withContext(Dispatchers.IO) {
        val fileName = "audio_${System.currentTimeMillis()}.pcm"
        val audioFile = File(context.cacheDir, fileName)

        FileOutputStream(audioFile).use { fos ->
            val byteBuffer = ByteArray(pcmSamples.size * 2)
            for (i in pcmSamples.indices) {
                val s = pcmSamples[i].toInt()
                byteBuffer[i * 2] = (s and 0xFF).toByte()
                byteBuffer[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
            }
            fos.write(byteBuffer)
        }

        // Format hex preview string (e.g., "7F:A0:C1:4E...")
        val hexPreview = encryptedBytesPreview.take(8)
            .joinToString(":") { String.format("%02X", it) } + "..."

        val entity = VoiceMessageEntity(
            senderName = senderName,
            senderIp = senderIp,
            channelId = channelId,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs,
            isOutgoing = isOutgoing,
            bitrateKbps = bitrateKbps,
            audioFilePath = audioFile.absolutePath,
            encryptedHexPreview = hexPreview,
            messageType = "VOICE",
            textContent = ""
        )

        dao.insertMessage(entity)
    }

    suspend fun saveTextMessage(
        senderName: String,
        senderIp: String,
        channelId: Int,
        textContent: String,
        isOutgoing: Boolean,
        encryptedBytesPreview: ByteArray
    ) = withContext(Dispatchers.IO) {
        val hexPreview = encryptedBytesPreview.take(8)
            .joinToString(":") { String.format("%02X", it) } + "..."

        val entity = VoiceMessageEntity(
            senderName = senderName,
            senderIp = senderIp,
            channelId = channelId,
            timestamp = System.currentTimeMillis(),
            durationMs = 0L,
            isOutgoing = isOutgoing,
            bitrateKbps = 0f,
            audioFilePath = "",
            encryptedHexPreview = hexPreview,
            messageType = "TEXT",
            textContent = textContent
        )

        dao.insertMessage(entity)
    }

    suspend fun loadPcmFromPath(path: String): ShortArray = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) return@withContext ShortArray(0)
        val bytes = file.readBytes()
        val shorts = ShortArray(bytes.size / 2)
        for (i in shorts.indices) {
            val low = bytes[i * 2].toInt() and 0xFF
            val high = bytes[i * 2 + 1].toInt()
            shorts[i] = ((high shl 8) or low).toShort()
        }
        shorts
    }

    suspend fun deleteMessage(id: Long) {
        dao.deleteMessageById(id)
    }

    suspend fun clearChannel(channelId: Int) {
        dao.clearChannelMessages(channelId)
    }
}
