package com.voiclog.ui.screens.record

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.voiclog.ui.theme.VoicLogTypography

@Composable
fun RecordScreenFooter() {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Your first weekly summary appears here once the week ends.",
            style = VoicLogTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}