package com.voiclog.data.transcription

sealed interface TranscriberState {
    data object Idle: TranscriberState
    data object LoadingModel: TranscriberState
    data class Transcribing(val progress: Float): TranscriberState
}
