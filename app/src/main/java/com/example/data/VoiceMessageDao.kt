package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceMessageDao {
    @Query("SELECT * FROM voice_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<VoiceMessageEntity>>

    @Query("SELECT * FROM voice_messages WHERE channelId = :channelId ORDER BY timestamp DESC")
    fun getMessagesForChannel(channelId: Int): Flow<List<VoiceMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: VoiceMessageEntity): Long

    @Query("DELETE FROM voice_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM voice_messages WHERE channelId = :channelId")
    suspend fun clearChannelMessages(channelId: Int)
}
