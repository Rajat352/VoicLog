package com.voiclog.data.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

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
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val recordingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var file: File? = null

    private val _state = MutableStateFlow<AudioRecorderState>(AudioRecorderState.Idle)
    override val state: StateFlow<AudioRecorderState> = _state.asStateFlow()

    override fun hasRecordPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override suspend fun start(): Result<Unit> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (audioRecord != null) return@withContext Result.failure(IllegalStateException("Already recording"))

            if (!hasRecordPermission()) return@withContext Result.failure(SecurityException("RECORD_AUDIO permission is not granted"))

            val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING)
            val bufferSizeBytes = minBufferSize * 4

            val outputDir = File(context.cacheDir, "recordings").apply { mkdirs() }
            val outputFile = File(outputDir, "rec_${System.currentTimeMillis()}.wav")


            try {

                val recorder = AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_CONFIG)
                            .setEncoding(AUDIO_ENCODING)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .build()

                if (recorder.state == AudioRecord.STATE_UNINITIALIZED) {
                    recorder.release()
                    return@withContext Result.failure(IllegalStateException("AudioRecord failed to initialize"))
                }

                audioRecord = recorder
                file = outputFile
                recorder.startRecording()
                _state.value = AudioRecorderState.Recording(0f)
                recordingJob = recordingScope.launch { captureLoop(recorder, outputFile, bufferSizeBytes) }
                Result.success(Unit)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare AudioRecord", e)
                releaseRecorderSafely()
                outputFile.delete()
                file = null
                Result.failure(e)
            }
        }
    }


    override suspend fun stop(): Result<File> = mutex.withLock {
        withContext(Dispatchers.IO) {

            val recorder = audioRecord
            val outputFile = file

            if (recorder == null || outputFile == null) {
                return@withContext Result.failure(IllegalStateException("Not currently recording"))
            }

            recordingJob?.cancelAndJoin()
            _state.value = AudioRecorderState.Idle

            try {
                recorder.stop()
                Result.success(outputFile)
            } catch (e: RuntimeException) {
                Log.e(TAG, "Failed to stop AudioRecord", e)
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
            val recorder = audioRecord
            val outputFile = file

            if (recorder == null || outputFile == null) {
                return@withContext Result.failure(IllegalStateException("Not currently recording"))
            }

            recordingJob?.cancelAndJoin()
            _state.value = AudioRecorderState.Idle

            recorder.stop()
            releaseRecorderSafely()
            outputFile.delete()
            file = null
            Log.d(TAG, "cancel(): cleanup done")
            Result.success(Unit)
        }
    }

    private suspend fun captureLoop(recorder: AudioRecord, outputFile: File, bufferSizeBytes: Int) {
        val buffer = ShortArray(bufferSizeBytes / 2)
        var pcmBytesWritten = 0L

        RandomAccessFile(outputFile, "rw").use { raf ->
            raf.write(ByteArray(WAV_HEADER_SIZE)) //placeholder, is updated once size in known

            while (currentCoroutineContext().isActive) {
                val sampleRead = recorder.read(buffer, 0, buffer.size)
                if (sampleRead > 0) {
                    val peak = (0 until sampleRead).maxOf { i -> abs(buffer[i].toInt()) }
                    _state.value = AudioRecorderState.Recording((peak / MAX_AMPLITUDE).coerceIn(0f, 1f))

                    val bytes = ByteBuffer.allocate(sampleRead * 2).order(ByteOrder.LITTLE_ENDIAN)
                        .apply { for (i in 0 until sampleRead) putShort(buffer[i]) }
                        .array()

                    raf.write(bytes)
                    pcmBytesWritten += bytes.size
                }
            }

            raf.seek(0)
            raf.write(buildWavHeader(pcmBytesWritten, SAMPLE_RATE, channels = 1, bitsPerSample = 16))
        }
    }

    private fun releaseRecorderSafely() {
        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while releasing recorder", e)
        }
        audioRecord?.release()
        audioRecord = null
    }

    companion object {
        private const val TAG = "AudioRecorderImpl"
        private const val MAX_AMPLITUDE = 32767f
        private const val SAMPLE_RATE = 16000
        private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val WAV_HEADER_SIZE = 44
    }
}