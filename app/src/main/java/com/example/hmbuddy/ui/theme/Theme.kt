package com.example.hmbuddy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = White,
    primaryContainer = TealSurface,
    onPrimaryContainer = Teal700,
    secondary = Blue500,
    onSecondary = White,
    secondaryContainer = BlueLight,
    onSecondaryContainer = Gray900,
    tertiary = Orange500,
    onTertiary = White,
    tertiaryContainer = OrangeLight,
    onTertiaryContainer = Gray900,
    background = White,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = LightGray,
    onSurfaceVariant = Gray700,
    outline = Gray200,
    outlineVariant = Gray100
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal500,
    onPrimary = Gray900,
    primaryContainer = Teal700,
    onPrimaryContainer = TealLight,
    secondary = Blue500,
    onSecondary = Gray900,
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = BlueLight,
    tertiary = Orange500,
    onTertiary = Gray900,
    tertiaryContainer = Color(0xFF5D4037),
    onTertiaryContainer = OrangeLight,
    background = DarkSurface,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Gray200,
    outline = Gray700,
    outlineVariant = Gray700
)

@Composable
fun HmBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
