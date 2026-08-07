package com.voiclog.data.repository

import com.voiclog.data.dao.RecordingDao

interface RecordingRepository {
    suspend fun getTranscription(audioData: FloatArray): String
}

class RecordingRepositoryImpl(
    private val recordingDao: RecordingDao
) : RecordingRepository {
    override suspend fun getTranscription(audioData: FloatArray): String {
        TODO("Not yet implemented")
    }

}