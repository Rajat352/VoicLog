package com.voiclog.data.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

interface AudioRecorder {
    val amplitude: Flow<Float>
    suspend fun start(): Result<Unit>
    suspend fun stop(): Result<File>
    suspend fun cancel(): Result<Unit>
}

class AudioRecorderImpl(
    private val context: Context
): AudioRecorder {

    private val mutex = Mutex()
    private var mediaRecorder: MediaRecorder? = null
    private var file: File? = null
    private val isRecording = MutableStateFlow(false)

    override suspend fun start(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {

            if (mediaRecorder != null) return@withContext Result.failure(IllegalStateException("Already recording"))

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return@withContext Result.failure(SecurityException("RECORD_AUDIO permission is not granted"))
            }

            val outputDir = File(context.filesDir, "recordings").apply { mkdirs() }
            val outputFile = File(outputDir, "rec_${System.currentTimeMillis()}.m4a")


            try {

                val recorder = MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setOutputFile(outputFile)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaRecorder error: what:$what extra:$extra")
                        isRecording.value = false
                    }
                    prepare()
                    start()
                }

                mediaRecorder = recorder
                file = outputFile
                isRecording.value = true
                Result.success(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare MediaRecorder", e)
                releaseRecorderSafely()
                outputFile.delete()
                file = null
                Result.failure(e)
            }
        }
    }


    override suspend fun stop(): Result<File> = mutex.withLock {
        withContext(Dispatchers.IO) {

            val recorder = mediaRecorder
            val outputFile = file

            if (recorder == null || outputFile == null) {
                return@withContext Result.failure(IllegalStateException("Not currently recording"))
            }

            isRecording.value = false

            try {
                recorder.stop()
                Result.success(outputFile)
            } catch (e: RuntimeException) {
                Log.e(TAG, "Failed to stop MediaRecorder", e)
                outputFile.delete()
                Result.failure(e)
            } finally {
                releaseRecorderSafely()
                file = null
            }
        }
    }

    override suspend fun cancel(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            isRecording.value = false

            val outputFile = file
            runCatching { mediaRecorder?.stop() }
            releaseRecorderSafely()
            outputFile?.delete()
            file = null
            Result.success(Unit)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val amplitude: Flow<Float>
        get() = isRecording
            .flatMapLatest { recording ->
                if (recording) {
                    flow {
                        while (currentCoroutineContext().isActive) {
                            val amp = mutex.withLock {
                                runCatching { mediaRecorder?.maxAmplitude }.getOrNull() ?: 0
                            }

                            emit((amp / MAX_AMPLITUDE).coerceIn(0f, 1f))
                            delay(AMPLITUDE_POLL_INTERVAL_MS)
                        }
                    }
                } else {
                    flowOf(0f)
                }
            }.flowOn(Dispatchers.IO)

    private fun releaseRecorderSafely() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while releasing recorder", e)
        } finally {
            mediaRecorder = null
        }
    }

    companion object {
        private const val TAG = "AudioRecorderImpl"
        private const val AMPLITUDE_POLL_INTERVAL_MS = 100L
        private const val MAX_AMPLITUDE = 32767f
    }
}