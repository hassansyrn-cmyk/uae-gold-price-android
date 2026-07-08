package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// Premium Light Luxury Theme — Warm Beige + Soft Gold + Light Emerald
// ============================================================

// Backgrounds (Light & Luxurious)
private val WarmBeige = Color(0xFFF8F4EC)           // Main background (warm off-white)
private val BeigeLight = Color(0xFFF5F0E6)          // Slightly lighter beige
private val SurfaceLight = Color(0xFFFFFFFF)        // Card surface (clean white)
private val SurfaceElevated = Color(0xFFFDF9F3)     // Slightly elevated cards

// Gold accents (rich but elegant)
private val AccentGold = Color(0xFFC9A227)          // Main gold
private val GoldLight = Color(0xFFE8D48A)           // Lighter gold highlight
private val GoldDeep = Color(0xFF8B6F1E)            // Deeper gold for contrast

// Light Emerald / Green accents
private val EmeraldSoft = Color(0xFF4A9B7E)         // Soft elegant green
private val EmeraldLight = Color(0xFF7BC9A8)        // Lighter green glow
private val EmeraldDeep = Color(0xFF2E6B55)         // Darker green for borders

// Text colors (dark warm tones for readability on light bg)
private val TextPrimaryColor = Color(0xFF2C2520)    // Main text (warm dark)
private val TextMutedColor = Color(0xFF6B6359)      // Secondary text
private val TextFaintColor = Color(0xFF9A9084)      // Very subtle text

private val PremiumLightColorScheme = lightColorScheme(
    primary = AccentGold,
    secondary = EmeraldSoft,
    tertiary = EmeraldLight,
    background = WarmBeige,
    surface = SurfaceLight,
    onPrimary = Color(0xFF2C2520),
    onSecondary = TextPrimaryColor,
    onBackground = TextPrimaryColor,
    onSurface = TextPrimaryColor,
    outline = GoldDeep,
    error = Color(0xFFB3261E)
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

/**
 * Semantic color + brush tokens used throughout the app's premium UI.
 */
object GoldColors {
    // Backgrounds
    val Background = WarmBeige
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(WarmBeige, BeigeLight)
    )
    val Surface = SurfaceLight
    val SurfaceElevated = SurfaceElevated

    // Glass / Card style (light elegant)
    val GlassCard = SurfaceElevated
    val GlassBorder = AccentGold.copy(alpha = 0.35f)
    val GlassBorderStrong = AccentGold.copy(alpha = 0.55f)

    // Gold
    val Gold = AccentGold
    val GoldLightTone = GoldLight
    val GoldDeepTone = GoldDeep
    val GoldGradient = Brush.linearGradient(colors = listOf(GoldLight, AccentGold, GoldDeep))

    // Emerald / Green
    val Emerald = EmeraldSoft
    val EmeraldGlow = EmeraldLight
    val EmeraldGradient = Brush.linearGradient(colors = listOf(EmeraldDeep, EmeraldSoft))
    val LiveGreen = EmeraldLight

    // Hero / Ounce card gradient
    val HeroGradient = Brush.linearGradient(
        colors = listOf(SurfaceElevated, BeigeLight)
    )

    // Text
    val Beige = BeigeLight
    val SecondaryCard = SurfaceElevated
    val TextPrimary = TextPrimaryColor
    val TextMuted = TextMutedColor
    val TextFaint = TextFaintColor
}