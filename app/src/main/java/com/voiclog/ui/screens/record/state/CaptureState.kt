package com.voiclog.ui.screens.record.state

sealed interface CaptureState {
    data object Disabled: CaptureState
    data object Idle: CaptureState
    data class Recording(val amplitude: Float): CaptureState
    data class Processing(val step: ProcessingStep, val progress: Float): CaptureState
}

enum class ProcessingStep {
    TRANSCRIBING,
    SUMMARIZING,
    LOADING_MODEL
}
