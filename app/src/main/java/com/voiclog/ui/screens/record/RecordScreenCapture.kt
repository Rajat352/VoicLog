package com.voiclog.ui.screens.record

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.voiclog.R
import com.voiclog.ui.screens.record.state.CaptureState
import com.voiclog.ui.screens.record.state.ProcessingStep
import com.voiclog.ui.theme.Timer
import com.voiclog.ui.theme.VoicLogTypography

@Composable
fun RecordScreenCapture(
    captureState: CaptureState,
    onToggleCapture: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CaptureGlyph(
            captureState = captureState,
            onToggleCapture = onToggleCapture
        )

        Text(
            text = when(captureState) {
                is CaptureState.Recording -> formatDuration(captureState.duration)
                is CaptureState.Processing -> when(captureState.step) {
                    ProcessingStep.LOADING_MODEL -> "Loading model..."
                    ProcessingStep.TRANSCRIBING -> "Transcribing..."
                    ProcessingStep.SUMMARIZING -> "Summarizing..."
                }
                else -> "Tap to say what you did today"
            },
            style = when(captureState) {
                is CaptureState.Recording -> Timer
                else -> VoicLogTypography.bodyLarge
            }
        )
    }
}

@Composable
fun CaptureGlyph(
    captureState: CaptureState,
    onToggleCapture: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }

    // Idle and Disabled appear on Home at the size of the control itself.
    // Recording and Processing appear on Record, inside the 288dp stage the
    // rings need — reserved up front so amplitude never reflows the layout.
    val stageSize = when (captureState) {
        CaptureState.Disabled, CaptureState.Idle -> ControlSize
        is CaptureState.Recording, is CaptureState.Processing -> StageSize
    }

    Box(
        modifier = Modifier
            .size(stageSize)
            .clickable(
                enabled = when (captureState) {
                    CaptureState.Idle, is CaptureState.Recording -> true
                    else -> false
                },
                onClick = { onToggleCapture() },
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        if (captureState is CaptureState.Recording) {
            AmplitudeRings(amplitude = captureState.amplitude)
        }

        when (captureState) {
            CaptureState.Idle -> CaptureControl(
                container = colorScheme.surfaceContainerLow,
                border = colorScheme.primary
            ) {
                MicGlyph(tint = colorScheme.primary)
            }

            CaptureState.Disabled -> CaptureControl(
                container = colorScheme.surfaceContainer,
                border = colorScheme.outline
            ) {
                MicGlyph(tint = colorScheme.outline)
            }

            is CaptureState.Recording -> CaptureControl(
                container = colorScheme.tertiary,
                border = null
            ) {
                StopGlyph(tint = colorScheme.onTertiary)
            }

            is CaptureState.Processing -> ProcessingRing(progress = captureState.progress)
        }
    }
}

@Composable
private fun CaptureControl(
    container: Color,
    border: Color?,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(ControlSize)
            .clip(CircleShape)
            .background(container)
            .then(
                if (border != null) Modifier.border(ControlBorderWidth, border, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun MicGlyph(tint: Color) {
    Icon(
        painter = painterResource(R.drawable.mic_icon),
        contentDescription = "Mic Icon",
        tint = tint,
        modifier = Modifier.size(MicGlyphSize)
    )
}

@Composable
private fun StopGlyph(tint: Color) {
    Box(
        modifier = Modifier
            .size(StopGlyphSize)
            .clip(RoundedCornerShape(StopGlyphRadius))
            .background(tint)
    )
}

@Composable
private fun AmplitudeRings(amplitude: Float) {
    val level = amplitude.coerceIn(0f, 1f)
    val scale by animateFloatAsState(
        targetValue = 1f + RingScaleRange * level,
        animationSpec = tween(durationMillis = RingFollowMillis, easing = LinearEasing),
        label = "ringScale"
    )
    val ringColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = Modifier.size(StageSize)) {
        val stroke = RingStrokeWidth.toPx()
        drawCircle(
            color = ringColor,
            radius = OuterRingSize.toPx() / 2f * scale - stroke / 2f,
            alpha = OuterRingAlpha,
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = ringColor,
            radius = InnerRingSize.toPx() / 2f * scale - stroke / 2f,
            alpha = InnerRingAlpha,
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun ProcessingRing(progress: Float) {
    CircularProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.size(ControlSize),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeWidth = ProcessingStrokeWidth,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp
    )
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private val StageSize = 288.dp
private val ControlSize = 132.dp
private val ControlBorderWidth = 1.5.dp

private val MicGlyphSize = 44.dp
private val StopGlyphSize = 40.dp
private val StopGlyphRadius = 2.dp

private val OuterRingSize = 288.dp
private val InnerRingSize = 212.dp
private val RingStrokeWidth = 1.dp
private const val OuterRingAlpha = 0.16f
private const val InnerRingAlpha = 0.28f
private const val RingScaleRange = 0.18f
private const val RingFollowMillis = 120

private val ProcessingStrokeWidth = 3.dp