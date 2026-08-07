package com.voiclog.ui.screens.record

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiclog.data.audio.AudioRecorder
import com.voiclog.data.audio.AudioRecorderState
import com.voiclog.ui.screens.record.action.RecordScreenAction
import com.voiclog.ui.screens.record.action.RecordScreenEvent
import com.voiclog.ui.screens.record.action.UiAction
import com.voiclog.ui.screens.record.state.CaptureState
import com.voiclog.ui.screens.record.state.RecordScreenState
import com.voiclog.ui.screens.record.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordScreenViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder
) : ViewModel(){

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<RecordScreenState> = combine(
        audioRecorder.state,
        _uiState
    ) { recorderState, uiState ->
        RecordScreenState(
            uiState = uiState,
            captureState = when (recorderState) {
                is AudioRecorderState.Recording -> CaptureState.Recording(recorderState.amplitude)
                AudioRecorderState.Idle -> CaptureState.Idle
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordScreenState())

    private val _events = Channel<RecordScreenEvent>(Channel.BUFFERED)
    val events: Flow<RecordScreenEvent> = _events.receiveAsFlow()

    fun onAction(action: RecordScreenAction) {
        when(action) {
            is RecordScreenAction.Ui -> handleUiActions(action.action)
        }
    }

    private fun handleUiActions(action: UiAction) {
        when(action) {
            is UiAction.ToggleCapture -> {
                viewModelScope.launch {
                    when(uiState.value.captureState) {
                        is CaptureState.Idle -> {
                            if (audioRecorder.hasRecordPermission()) {
                                audioRecorder.start()
                            } else {
                                _events.trySend(RecordScreenEvent.RequestAudioPermission)
                            }
                        }
                        is CaptureState.Recording -> audioRecorder.stop()
                        else -> {}
                    }
                }
            }

            is UiAction.PermissionResult -> {
                if (action.granted) {
                    viewModelScope.launch { audioRecorder.start() }
                } else {
                    // Todo: Handle else here to maybe show something like why permission is needed and necessary
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch(NonCancellable) {
            audioRecorder.cancel()
        }
    }

    companion object {
        private const val TAG = "RecordScreenViewModel"
    }
}