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

private val DuskLight = lightColorScheme(
    primary              = Color(0xFF2E4A6B),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFD3E4F8),
    onPrimaryContainer   = Color(0xFF12283F),
    secondary            = Color(0xFF51606F),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFD5E4F6),
    onSecondaryContainer = Color(0xFF0E1D2A),
    tertiary             = Color(0xFFB07D3F),  // live mic only
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFFFDDB0),
    onTertiaryContainer  = Color(0xFF2C1A00),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
    surface              = Color(0xFFFBFAF8),
    surfaceContainerLow  = Color(0xFFF6F4F1),
    surfaceContainer     = Color(0xFFF1EFEC),
    surfaceContainerHigh = Color(0xFFEBE8E4),
    onSurface            = Color(0xFF1A1C1E),
    onSurfaceVariant     = Color(0xFF5A6068),
    outline              = Color(0xFFC9C6C2),
    outlineVariant       = Color(0xFFE3E0DC),
    inverseSurface       = Color(0xFF2F3133),
    inverseOnSurface     = Color(0xFFF1F0F4),
)

// dynamicColor is deliberately NOT used — the dusk palette is the brand.

// Sage is not an M3 role, so it rides alongside the scheme rather than inside it.
// One color, one job: the leading keyword of a weekly-summary bullet.
@Immutable
data class VoicLogAccents(val summaryKeyword: Color = Color(0xFF4A6A55))

val LocalAccents = staticCompositionLocalOf { VoicLogAccents() }