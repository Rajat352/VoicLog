package com.voiclog.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

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

@Composable
fun VoicLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> DuskLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoicLogTypography,
        content = content
    )
}