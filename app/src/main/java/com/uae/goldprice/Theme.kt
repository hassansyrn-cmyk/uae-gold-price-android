package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// Premium Luxury Dark Theme — Gold / Emerald / Warm Beige
// ============================================================

private val DeepBackground = Color(0xFF0F0D0A)
private val DeepBackgroundEnd = Color(0xFF171310)
private val SurfaceDarkColor = Color(0xFF1C1712)
private val SurfaceElevatedColor = Color(0xFF241E17)

private val AccentGold = Color(0xFFD4AF37)
private val GoldLight = Color(0xFFF0D97D)
private val GoldDeep = Color(0xFF9C7A1E)
private val GoldHairline = Color(0xFF6B5215)

private val EmeraldDeep = Color(0xFF0E4A38)
private val EmeraldMid = Color(0xFF1E8F6B)
private val EmeraldLight = Color(0xFF5FD3A8)

private val BeigeWarm = Color(0xFFE8DCC0)
private val TextPrimaryColor = Color(0xFFF5EFE0)
private val TextMutedColor = Color(0xFFB3A98E)
private val TextFaintColor = Color(0xFF7C7566)

private val PremiumDarkColorScheme = darkColorScheme(
    primary = AccentGold,
    secondary = EmeraldMid,
    tertiary = EmeraldLight,
    background = DeepBackground,
    surface = SurfaceDarkColor,
    onPrimary = Color(0xFF1A1408),
    onSecondary = TextPrimaryColor,
    onBackground = TextPrimaryColor,
    onSurface = TextPrimaryColor,
    outline = GoldHairline,
    error = Color(0xFFCF6679)
)

@Composable
fun GoldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        content = content
    )
}

object GoldColors {
    val Background = DeepBackground
    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(DeepBackground, DeepBackgroundEnd, EmeraldDeep.copy(alpha = 0.15f))
    )
    val Surface = SurfaceDarkColor
    val SurfaceElevated = SurfaceElevatedColor

    val GlassCard = SurfaceElevatedColor.copy(alpha = 0.72f)
    val GlassBorder = AccentGold.copy(alpha = 0.28f)
    val GlassBorderStrong = AccentGold.copy(alpha = 0.55f)

    val Gold = AccentGold
    val GoldLightTone = GoldLight
    val GoldDeepTone = GoldDeep
    val GoldGradient = Brush.linearGradient(colors = listOf(GoldLight, AccentGold, GoldDeep))

    val Emerald = EmeraldMid
    val EmeraldGlow = EmeraldLight
    val EmeraldGradient = Brush.linearGradient(colors = listOf(EmeraldDeep, EmeraldMid))
    val LiveGreen = EmeraldLight

    val HeroGradient = Brush.linearGradient(
        colors = listOf(SurfaceElevatedColor, EmeraldDeep.copy(alpha = 0.55f), SurfaceElevatedColor)
    )

    val Beige = BeigeWarm
    val SecondaryCard = SurfaceElevatedColor
    val TextPrimary = TextPrimaryColor
    val TextMuted = TextMutedColor
    val TextFaint = TextFaintColor
}