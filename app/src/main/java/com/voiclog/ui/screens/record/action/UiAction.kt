package com.voiclog.ui.screens.record.action

sealed interface UiAction {
    data object ToggleCapture: UiAction
    data class PermissionResult(val granted: Boolean): UiAction
}