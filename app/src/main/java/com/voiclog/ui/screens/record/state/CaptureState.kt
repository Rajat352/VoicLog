package com.voiclog.ui.screens.record.state

sealed interface CaptureState {
    data object Disabled: CaptureState
    data object Idle: CaptureState
    data class Recording(val amplitude: Float, val duration: Long): CaptureState
    data class Processing(val step: ProcessingStep, val progress: Float): CaptureState
}

enum class ProcessingStep {
    TRANSCRIBING,
    SUMMARIZING,
    LOADING_MODEL
}
