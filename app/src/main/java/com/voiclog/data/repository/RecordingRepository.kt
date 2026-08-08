package com.voiclog.data.repository

import android.util.Log
import com.voiclog.data.dao.RecordingDao
import com.voiclog.data.dto.Recording
import com.voiclog.data.transcription.Transcriber
import java.time.LocalDate

interface RecordingRepository {
    suspend fun createRecording(date: LocalDate): Result<Long>
    suspend fun getAndAddTranscription(id: Long, audioData: FloatArray): Result<String>
}

class RecordingRepositoryImpl(
    private val recordingDao: RecordingDao,
    private val transcriber: Transcriber
) : RecordingRepository {

    override suspend fun createRecording(date: LocalDate): Result<Long> {
        return try {
            val rowId = recordingDao.createRecording(
                Recording(
                    date = date
                )
            )
            Result.success(rowId)
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while creating a recording", e)
            Result.failure(e)
        }
    }

    override suspend fun getAndAddTranscription(id: Long, audioData: FloatArray): Result<String> {
        return try {
            val result = transcriber.transcribe(audioData)

            result.onSuccess { text ->
                recordingDao.addTranscription(id, text)
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while getting transcription", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "RecordingRepository"
    }

}