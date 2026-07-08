// File: app/src/main/java/com/uae/goldprice/Theme.kt
package com.uae.goldprice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object GoldColors {
    // Afaq-inspired Premium Palette
    val BackgroundBeige = Color(0xFFF7F5F0) // Warm beige background
    
    val SageGreen = Color(0xFF38574B)       // Elegant dark sage for headers
    val SageGreenDark = Color(0xFF284036)   // Darker green for gradients
    val SageGreenLight = Color(0xFFE9EFEA)  // Very light green for accents/pills
    val BrightGreen = Color(0xFF5BBA7E)     // For the live pulsing dot
    
    val Gold = Color(0xFFC7A153)            // Deep luxury gold
    val GoldSoft = Color(0xFFE4C88A)        // Soft light gold for highlights
    
    val SurfaceWhite = Color(0xFFFFFFFF)    // Clean white for cards
    
    // Glass & Borders
    val GlassBorder = Color(0x1A38574B)     // Subtle green tint for card borders
    
    // Typography
    val TextPrimary = Color(0xFF2B3631)     // Very dark green/charcoal for max readability
    val TextMuted = Color(0xFF74857D)       // Muted gray-green for secondary text
    val TextFaint = Color(0xFFA3B3AB)       // Faint text for disclaimers
}

private val LuxuryLightColorScheme = lightColorScheme(
    primary = GoldColors.SageGreen,
    onPrimary = Color.White,
    secondary = GoldColors.Gold,
    onSecondary = Color.White,
    background = GoldColors.BackgroundBeige,
    onBackground = GoldColors.TextPrimary,
    surface = GoldColors.SurfaceWhite,
    onSurface = GoldColors.TextPrimary,
    error = Color(0xFFD32F2F)
)

@Composable
fun GoldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Enforce the premium light theme consistently to match the "Afaq" design language,
    // overriding the system dark mode behavior as requested for the bright premium feel.
    MaterialTheme(
        colorScheme = LuxuryLightColorScheme,
        content = content
    )
}
