package com.voiclog.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.voiclog.data.dto.Recording

@Dao
interface RecordingDao {

    @Insert
    suspend fun createRecording(recording: Recording): Long

    @Query("UPDATE recording SET transcript = :transcript WHERE id = :id")
    suspend fun addTranscription(id: Long, transcript: String)

    @Query("UPDATE recording SET summary = :summary WHERE id = :id")
    suspend fun addSummary(id: Long, summary: List<String>)
}