package com.voiclog.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

interface AudioRecorder {
    val state: StateFlow<AudioRecorderState>
    fun hasRecordPermission(): Boolean
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

    private val _state = MutableStateFlow<AudioRecorderState>(AudioRecorderState.Idle)
    override val state: StateFlow<AudioRecorderState> = _state.asStateFlow()

    private val pollingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null

    override fun hasRecordPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

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
                        pollingJob?.cancel()
                        _state.value = AudioRecorderState.Idle
                    }
                    prepare()
                    start()
                }

                mediaRecorder = recorder
                file = outputFile
                _state.value = AudioRecorderState.Recording(0f)
                pollingJob = pollingScope.launch { pollAmplitude() }
                Result.success(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare MediaRecorder", e)
                pollingJob?.cancel()
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

            pollingJob?.cancel()
            _state.value = AudioRecorderState.Idle

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
            pollingJob?.cancel()
            _state.value = AudioRecorderState.Idle

            val outputFile = file
            runCatching { mediaRecorder?.stop() }
            releaseRecorderSafely()
            outputFile?.delete()
            file = null
            Result.success(Unit)
        }
    }

    private suspend fun pollAmplitude() {
        while (currentCoroutineContext().isActive) {
            val amp = mutex.withLock {
                runCatching { mediaRecorder?.maxAmplitude }.getOrNull() ?: 0
            }

            _state.value = AudioRecorderState.Recording((amp/MAX_AMPLITUDE).coerceIn(0f, 1f))
            delay(AMPLITUDE_POLL_INTERVAL_MS)
        }
    }

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