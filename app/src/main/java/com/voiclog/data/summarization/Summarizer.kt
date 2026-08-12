package com.voiclog.data.summarization

import android.content.Context
import android.util.Log
import androidx.compose.material3.Badge
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.voiclog.data.repository.ModelDownloadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

interface Summarizer {
    val state: StateFlow<SummarizerState>
    suspend fun summarize(transcript: String): Result<List<String>>
    fun release()
}

class SummarizerImpl(
    private val context: Context,
    private val modelDownloadRepo: ModelDownloadRepository
): Summarizer {

    private var engine: Engine? = null
    private val mutex = Mutex()

    private val _state = MutableStateFlow<SummarizerState>(SummarizerState.Idle)
    override val state = _state.asStateFlow()

    override suspend fun summarize(transcript: String): Result<List<String>> = withContext(Dispatchers.Default) {
        try {
            ensureInitialized()

            val engine = engine ?: return@withContext Result.failure(IllegalStateException("Model not initialized"))

            val conversation = engine.createConversation(
                ConversationConfig(
                    samplerConfig = SamplerConfig(topK = 32, topP = 0.9, temperature = 0.3),
                    systemInstruction = Contents.of(listOf(Content.Text(SUMMARIZATION_SYSTEM_INSTRUCTION)))
                )
            )

            _state.value = SummarizerState.Summarizing
            val bullets = runSingleTurn(conversation, transcript)

            if (bullets.isNotEmpty()) Result.success(parseBulletPoints(bullets))
            else Result.failure(Exception("Summarization produced no valid bullet points"))

        } catch (e: Exception) {
            Log.e(TAG, "An error occurred while summarizing", e)
            Result.failure(e)
        } finally {
            _state.value = SummarizerState.Idle
        }
    }

    override fun release() {
        engine?.close()
        engine = null
        _state.value = SummarizerState.Idle
    }

    private suspend fun ensureInitialized() = mutex.withLock {
        withContext(Dispatchers.IO) {

            if (engine != null) return@withContext

            val modelPath = modelDownloadRepo.modelFile()?.absolutePath

            if (modelPath == null) {
                _state.value = SummarizerState.ModelNotReady
                error("Gemma model not downloaded")
            }

            _state.value = SummarizerState.ModelLoading
            val config = EngineConfig(
                modelPath = modelPath,
                backend = Backend.CPU(),
                visionBackend = null,
                audioBackend = null,
                maxNumTokens = 512,
                cacheDir = context.cacheDir.absolutePath
            )
            val newEngine = Engine(config).also { it.initialize() }

            engine = newEngine
            Log.d(TAG, "Engine created successfully!")

        }
    }

    private suspend fun runSingleTurn(convo: Conversation, transcript: String): String = withContext(Dispatchers.Default + NonCancellable) {
        val buffer = StringBuilder()

        convo.use {
            it.sendMessageAsync(Contents.of(listOf(Content.Text(transcript))))
                .catch { e ->
                    Log.e(TAG, "An error occurred while running summary on model", e)
                }
                .collect { message ->
                    buffer.append(message.toString())
                }
        }

        return@withContext buffer.toString()
    }

    companion object {
        private const val TAG = "SummarizerImpl"
    }
}