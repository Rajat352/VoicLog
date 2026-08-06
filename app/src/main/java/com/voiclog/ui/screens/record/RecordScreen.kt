package com.voiclog.ui.screens.record

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voiclog.ui.screens.record.action.RecordScreenAction
import com.voiclog.ui.screens.record.action.RecordScreenEvent
import com.voiclog.ui.screens.record.action.UiAction

@Composable
fun RecordScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: RecordScreenViewModel = viewModel()
    val uiState by remember { viewModel.uiState }.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onAction(RecordScreenAction.Ui(UiAction.PermissionResult(granted)))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RecordScreenEvent.RequestAudioPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        RecordScreenTitle()
        RecordScreenCapture(
            captureState = uiState.captureState,
            onToggleCapture = {
                viewModel.onAction(RecordScreenAction.Ui(UiAction.ToggleCapture))
            }
        )
        RecordScreenFooter()
    }
}