package com.voiclog.ui.screens.record.action

sealed interface RecordScreenAction {
    data class Ui(val action: UiAction): RecordScreenAction
}