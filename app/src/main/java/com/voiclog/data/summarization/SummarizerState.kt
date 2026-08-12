package com.voiclog.data.summarization

sealed interface SummarizerState {
    data object Idle: SummarizerState
    data object ModelNotReady: SummarizerState
    data object ModelLoading: SummarizerState
    data object Summarizing: SummarizerState
}