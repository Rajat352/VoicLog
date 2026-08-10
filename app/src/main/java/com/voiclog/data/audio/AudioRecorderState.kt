package com.voiclog.data.audio

sealed interface AudioRecorderState {
    data object Idle: AudioRecorderState
    data class Recording(val amplitude: Float, val duration: Long): AudioRecorderState
}
