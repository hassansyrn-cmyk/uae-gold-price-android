package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremiumLightColorScheme = lightColorScheme(
    primary = Color(0xFF7A9382),
    secondary = Color(0xFFC7A556),

    background = Color(0xFFF9F7F2),
    surface = Color(0xFFFFFFFF),

    onPrimary = Color.White,
    onSecondary = Color.White,

    onBackground = Color(0xFF2C2A28),
    onSurface = Color(0xFF2C2A28),

    error = Color(0xFFD32F2F)
)

@Composable
fun GoldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumLightColorScheme,
        typography = Typography(),
        content = content
    )
}
