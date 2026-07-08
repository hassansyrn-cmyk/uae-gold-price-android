package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// Premium Light Luxury Theme — Warm Beige + Soft Gold + Light Emerald
// ============================================================

private val WarmBeige = Color(0xFFF8F4EC)
private val BeigeLight = Color(0xFFF5F0E6)
private val SurfaceLight = Color(0xFFFFFFFF)
private val SurfaceElevated = Color(0xFFFDF9F3)

private val AccentGold = Color(0xFFC9A227)
private val GoldLight = Color(0xFFE8D48A)
private val GoldDeep = Color(0xFF8B6F1E)

private val EmeraldSoft = Color(0xFF4A9B7E)
private val EmeraldLight = Color(0xFF7BC9A8)
private val EmeraldDeep = Color(0xFF2E6B55)

private val TextPrimaryColor = Color(0xFF2C2520)
private val TextMutedColor = Color(0xFF6B6359)
private val TextFaintColor = Color(0xFF9A9084)

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
fun GoldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PremiumLightColorScheme,
        content = content
    )
}

object GoldColors {
    val Background = WarmBeige
    val BackgroundGradient = Brush.verticalGradient(colors = listOf(WarmBeige, BeigeLight))
    val Surface = SurfaceLight
    val SurfaceElevated = SurfaceElevated

    val GlassCard = SurfaceElevated
    val GlassBorder = AccentGold.copy(alpha = 0.35f)
    val GlassBorderStrong = AccentGold.copy(alpha = 0.55f)

    val Gold = AccentGold
    val GoldLightTone = GoldLight
    val GoldDeepTone = GoldDeep
    val GoldGradient = Brush.linearGradient(colors = listOf(GoldLight, AccentGold, GoldDeep))

    val Emerald = EmeraldSoft
    val EmeraldGlow = EmeraldLight
    val EmeraldGradient = Brush.linearGradient(colors = listOf(EmeraldDeep, EmeraldSoft))
    val LiveGreen = EmeraldLight

    val HeroGradient = Brush.linearGradient(colors = listOf(SurfaceElevated, BeigeLight))

    val Beige = BeigeLight
    val SecondaryCard = SurfaceElevated
    val TextPrimary = TextPrimaryColor
    val TextMuted = TextMutedColor
    val TextFaint = TextFaintColor
}