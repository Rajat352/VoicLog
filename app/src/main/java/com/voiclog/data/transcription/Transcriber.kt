package com.voiclog.data.transcription

import android.content.Context
import android.util.Log
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

interface Transcriber {
    val state: StateFlow<TranscriberState>
    suspend fun warmUp()
    suspend fun transcribe(audioData: FloatArray): Result<String>
}

class TranscriberImpl(
    private val context: Context
): Transcriber {

    private val mutex = Mutex()
    private var whisperPtr: Long = 0
    private val whisperDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val _state = MutableStateFlow<TranscriberState>(TranscriberState.Idle)
    override val state: StateFlow<TranscriberState> = _state.asStateFlow()

    override suspend fun warmUp() = ensureLoaded()

    override suspend fun transcribe(audioData: FloatArray): Result<String> = withContext(whisperDispatcher) {
        try {
            ensureLoaded()
            _state.value = TranscriberState.Transcribing(0f)
            val text = WhisperLib.fullTranscribe(whisperPtr, THREAD_COUNT, audioData) { progress ->
                Log.d(TAG, "transcribe progress: $progress")
                _state.value = TranscriberState.Transcribing(progress / 100f)
            }
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while transcribing", e)
            Result.failure(e)
        } finally {
            _state.value = TranscriberState.Idle
        }
    }

    private suspend fun ensureLoaded() = mutex.withLock {
        if (whisperPtr != 0L) return@withLock
        _state.value = TranscriberState.LoadingModel
        whisperPtr = withContext(whisperDispatcher) {
            WhisperLib.initContextFromAsset(context.assets, MODEL_ASSET_PATH)
        }
        check(whisperPtr != 0L) { "Failed to load $MODEL_ASSET_PATH" }
        Log.d(TAG, "Whisper model loaded successfully")
    }

    companion object {
        private const val TAG = "Transcriber"
        private const val MODEL_ASSET_PATH = "ggml-tiny.en.bin"
        private const val THREAD_COUNT = 4
    }

}