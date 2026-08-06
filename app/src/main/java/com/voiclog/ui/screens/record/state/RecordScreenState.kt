package com.voiclog.ui.screens.record.state

data class RecordScreenState(
    val uiState: UiState = UiState(),
    val captureState: CaptureState = CaptureState.Idle
)
