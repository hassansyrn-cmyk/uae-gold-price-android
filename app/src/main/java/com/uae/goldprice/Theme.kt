package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomColorScheme = darkColorScheme(
    primary = Color(0xFFC7A556),       // Luxury Gold
    secondary = Color(0xFF1E2D42),     // Deep Premium Navy
    background = Color(0xFF0A1424),    // Dark Blue Luxury Base
    surface = Color(0xFFFDFBF7),       // Soft Ivory Card Surface
    onPrimary = Color(0xFF0A1424),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A1424)
)

@Composable
fun GoldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CustomColorScheme,
        content = content
    )
}
