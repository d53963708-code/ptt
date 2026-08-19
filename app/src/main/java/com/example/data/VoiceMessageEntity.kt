package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_messages")
data class VoiceMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderIp: String,
    val channelId: Int,
    val timestamp: Long,
    val durationMs: Long = 0L,
    val isOutgoing: Boolean,
    val bitrateKbps: Float = 0f,
    val audioFilePath: String = "",
    val encryptedHexPreview: String = "",
    val messageType: String = "VOICE", // "VOICE" or "TEXT"
    val textContent: String = ""
)

