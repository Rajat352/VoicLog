package com.voiclog.ui.screens.record.state

data class UiState(
    val weeklySummary: List<String> = emptyList(),
    val areModelsDownloaded: Boolean = false
)
