package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Premium Theme Colors
private val IvoryBackground = Color(0xFFF8F4EA)
private val CardBackground = Color(0xFFFFFFFF)
private val SecondaryCard = Color(0xFFF1E8D2)
private val AccentGold = Color(0xFFC9A227)
private val MutedGold = Color(0xFFB8942E)
private val TextDark = Color(0xFF1E1B16)
private val TextMuted = Color(0xFF6F675A)
private val BorderColor = Color(0xFFE5D8B8)
private val LiveGreen = Color(0xFF2F7D4E)

private val PremiumLightColorScheme = lightColorScheme(
    primary = AccentGold,
    secondary = SecondaryCard,
    background = IvoryBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark,
    outline = BorderColor,
    tertiary = LiveGreen
)

@Composable
fun GoldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumLightColorScheme,
        content = content
    )
}

// Color accessors for easy use in Compose
object GoldColors {
    val TextMuted = Color(0xFF6F675A)
    val SecondaryCard = Color(0xFFF1E8D2)
    val LiveGreen = Color(0xFF2F7D4E)
    val Ivory = Color(0xFFF8F4EA)
}
