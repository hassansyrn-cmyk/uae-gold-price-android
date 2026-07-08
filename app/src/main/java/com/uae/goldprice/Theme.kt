package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// UAE Gold Price — Premium Glass Luxury Theme
// Gold + Emerald + Warm Beige
// ============================================================

// Backgrounds
private val DeepBackground = Color(0xFF0D0B08)
private val DeepBackgroundEnd = Color(0xFF17120E)

private val SurfaceDarkColor = Color(0xFF1B1712)
private val SurfaceElevatedColor = Color(0xFF231C15)

// Gold
private val AccentGold = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFF3DD8A)
private val GoldDeep = Color(0xFF9C7718)
private val GoldHairline = Color(0xFF70551A)

// Emerald
private val EmeraldDeep = Color(0xFF0F3B2D)
private val EmeraldMid = Color(0xFF1C8A66)
private val EmeraldLight = Color(0xFF67D8B0)

// Beige
private val BeigeWarm = Color(0xFFE9DDC0)
private val BeigeLight = Color(0xFFF6F0E3)

// Text
private val TextPrimaryColor = BeigeLight
private val TextMutedColor = Color(0xFFC5B89B)
private val TextFaintColor = Color(0xFF8A816F)

private val PremiumDarkColorScheme = darkColorScheme(
    primary = AccentGold,
    secondary = EmeraldMid,
    tertiary = EmeraldLight,

    background = DeepBackground,
    surface = SurfaceDarkColor,
    surfaceVariant = SurfaceElevatedColor,

    onPrimary = Color(0xFF1A1408),
    onSecondary = BeigeLight,
    onBackground = BeigeLight,
    onSurface = BeigeLight,

    outline = GoldHairline,
    error = Color(0xFFCF6679)
)

@Composable
fun GoldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        content = content
    )
}

object GoldColors {

    // Main Background
    val Background = DeepBackground

    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            DeepBackground,
            DeepBackgroundEnd,
            EmeraldDeep.copy(alpha = 0.18f)
        )
    )

    // Glass Cards
    val Surface = SurfaceDarkColor
    val SurfaceElevated = SurfaceElevatedColor

    val GlassCard =
        Color(0xFF2A211A).copy(alpha = 0.72f)

    val GlassCardPremium =
        Color(0xFF31261B).copy(alpha = 0.82f)

    val GlassBorder =
        AccentGold.copy(alpha = 0.28f)

    val GlassBorderStrong =
        AccentGold.copy(alpha = 0.55f)

    val GlassGlow =
        GoldLight.copy(alpha = 0.12f)

    // Gold
    val Gold = AccentGold
    val GoldLightTone = GoldLight
    val GoldDeepTone = GoldDeep

    val GoldGradient = Brush.linearGradient(
        colors = listOf(
            GoldLight,
            AccentGold,
            GoldDeep
        )
    )

    // Emerald
    val Emerald = EmeraldMid
    val EmeraldGlow = EmeraldLight

    val EmeraldGradient = Brush.linearGradient(
        colors = listOf(
            EmeraldDeep,
            EmeraldMid,
            EmeraldLight
        )
    )

    // Hero Section Premium
    val HeroGradient = Brush.linearGradient(
        colors = listOf(
            GoldDeep.copy(alpha = 0.18f),
            EmeraldDeep.copy(alpha = 0.35f),
            SurfaceElevatedColor
        )
    )

    // Premium Highlight Card
    val PremiumCard =
        Color(0xFF2A2117)

    val PremiumCardBorder =
        AccentGold.copy(alpha = 0.45f)

    // Live Price Green
    val LiveGreen =
        EmeraldLight

    // Beige
    val Beige = BeigeWarm
    val SecondaryCard = SurfaceElevatedColor

    // Text
    val TextPrimary = TextPrimaryColor
    val TextMuted = TextMutedColor
    val TextFaint = TextFaintColor
}
