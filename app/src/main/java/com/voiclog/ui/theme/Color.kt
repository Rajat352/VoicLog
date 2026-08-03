package com.voiclog.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// dynamicColor is deliberately NOT used — the dusk palette is the brand.

// Sage is not an M3 role, so it rides alongside the scheme rather than inside it.
// One color, one job: the leading keyword of a weekly-summary bullet.
@Immutable
data class VoicLogAccents(val summaryKeyword: Color = Color(0xFF4A6A55))

val LocalAccents = staticCompositionLocalOf { VoicLogAccents() }