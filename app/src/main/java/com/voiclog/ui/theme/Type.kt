package com.voiclog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.voiclog.R

private val Sans = FontFamily(
    Font(R.font.instrument_sans_regular,  FontWeight.Normal),
    Font(R.font.instrument_sans_medium,   FontWeight.Medium),
    Font(R.font.instrument_sans_semibold, FontWeight.SemiBold),
)
private val Mono = FontFamily(
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
)

val VoicLogTypography = Typography(
    displayMedium = TextStyle(
        fontFamily =  Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize =  40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.13).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ), // buttons
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
)

// Meta — dates, counts, durations. Always uppercase at the call site.
val Meta  = TextStyle(
    fontFamily = Mono,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.96.sp
)
// Timer — the recording clock only.
val Timer = TextStyle(
    fontFamily = Mono,
    fontWeight = FontWeight.Medium,
    fontSize = 40.sp,
    lineHeight = 48.sp
)