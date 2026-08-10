package com.voiclog.ui.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.voiclog.ui.theme.Meta
import com.voiclog.ui.theme.VoicLogTypography
import java.util.Calendar

@Composable
fun RecordScreenTitle() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Greeting(
            name = "Rajat"
        )
        LogStatus()
    }
}

@Composable
fun Greeting(
    name: String
) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    Text(
        text = "$greeting, $name",
        style = VoicLogTypography.titleLarge
    )
}

@Composable
fun LogStatus() {
    Box() {
        Text(
            text = "4 weeks logged",
            style = Meta,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}