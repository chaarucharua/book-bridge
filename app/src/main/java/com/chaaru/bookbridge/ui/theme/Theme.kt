package com.chaaru.bookbridge.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Burgundy,
    onPrimary = Cream,
    secondary = Cream,
    onSecondary = Burgundy,
    background = Color(0xFF1A0005),
    surface = DarkBurgundy,
    onBackground = Cream,
    onSurface = Cream
)

private val LightColorScheme = lightColorScheme(
    primary = Burgundy,
    onPrimary = Cream,
    primaryContainer = Burgundy,
    onPrimaryContainer = Cream,
    secondary = Cream,
    onSecondary = Burgundy,
    secondaryContainer = Cream,
    onSecondaryContainer = Burgundy,
    background = Cream,
    onBackground = Burgundy,
    surface = LightCream,
    onSurface = Burgundy,
    surfaceVariant = Cream,
    onSurfaceVariant = Burgundy,
    outline = Burgundy
)

@Composable
fun BookBridgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Burgundy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
