package com.chaaru.bookbridge.screen

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val BookBridgeColorScheme = lightColorScheme(
    primary             = Burgundy,
    onPrimary           = White,
    primaryContainer    = BurgundyFaint,
    onPrimaryContainer  = BurgundyDark,
    background          = Parchment,
    onBackground        = Slate900,
    surface             = White,
    onSurface           = Slate800,
    surfaceVariant      = ParchmentLight,
    onSurfaceVariant    = Slate600,
    outline             = Slate200,
    error               = Red400,
)

val BookBridgeTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 22.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 16.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 10.sp),
)

@Composable
fun BookBridgeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookBridgeColorScheme,
        typography  = BookBridgeTypography,
        content     = content
    )
}
