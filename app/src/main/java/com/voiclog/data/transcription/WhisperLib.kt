package com.voiclog.data.transcription

import android.content.res.AssetManager

fun interface WhisperProgressListener {
    fun onProgress(progress: Int)
}

internal object WhisperLib {
    init {
        System.loadLibrary("voiclog_whisper")
    }

    external fun initContextFromAsset(assetManager: AssetManager, assetPath: String): Long

    // TranscriberImpl is designed to be a Singleton and meant to stay alive for the process lifetime and not load model on each recording
    external fun freeContext(contextPtr: Long)
    external fun fullTranscribe(contextPtr: Long, numThreads: Int, audioData: FloatArray, listener: WhisperProgressListener?)
    external fun getTextSegmentCount(contextPtr: Long): Int
    external fun getTextSegment(contextPtr: Long, index: Int): String

    fun fullTranscribe(contextPtr: Long, numThreads: Int, audioData: FloatArray, onProgress: (Int) -> Unit): String {
        fullTranscribe(contextPtr, numThreads, audioData, WhisperProgressListener(onProgress))
        return buildString {
            for (i in 0 until getTextSegmentCount(contextPtr)) append(getTextSegment(contextPtr, i))
        }
    }
}