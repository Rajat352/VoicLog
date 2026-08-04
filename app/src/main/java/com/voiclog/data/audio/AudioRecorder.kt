package com.voiclog.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private const val TAG = "AudioRecorder"

interface AudioRecorder {
    val amplitude: Flow<Float>
    suspend fun start(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun cancel()
}

class AudioRecorderImpl(
    private val context: Context
): AudioRecorder {

    private val mutex = Mutex()
    private var mediaRecorder: MediaRecorder? = null
    private var file: File? = null

    override suspend fun start(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (mediaRecorder != null) return@withContext Result.failure(IllegalStateException("Already recording"))

            mediaRecorder = MediaRecorder(context)
            file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")

            try {
                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setOutputFile(file)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    prepare()
                    start()
                }
                Result.success(Unit)
            } catch (e: IllegalStateException) {
                mediaRecorder?.release()
                mediaRecorder = null
                Log.e(TAG, "Failed to prepare MediaRecorder", e)
                Result.failure(e)
            } catch (e: IOException) {
                mediaRecorder?.release()
                mediaRecorder = null
                Log.e(TAG, "Failed to prepare MediaRecorder", e)
                Result.failure(e)
            }
        }
    }


    override suspend fun stop(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            Result.success(Unit)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to stop MediaRecorder", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to stop MediaRecorder", e)
            Result.failure(e)
        }
    }

    override suspend fun cancel() = withContext(Dispatchers.IO) {
        runCatching { mediaRecorder?.stop() }
        mediaRecorder?.release()
        mediaRecorder = null
        file?.delete()
        file = null
    }

    override val amplitude: Flow<Float>
        get() = TODO("Not yet implemented")
}