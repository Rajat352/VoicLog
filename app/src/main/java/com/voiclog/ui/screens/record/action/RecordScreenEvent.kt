package com.voiclog.ui.screens.record.action

sealed interface RecordScreenEvent {
    data object RequestAudioPermission: RecordScreenEvent
}