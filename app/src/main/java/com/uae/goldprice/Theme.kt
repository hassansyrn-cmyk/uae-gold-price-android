package com.uae.goldprice

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// UAE Gold Price
// Afaq Inspired Luxury Theme
// Sage Green + Warm Beige + Soft Gold
// ============================================================

// Background
private val BackgroundTop = Color(0xFFF7F4ED)
private val BackgroundBottom = Color(0xFFF0EBE2)

// Cards
private val CardWhite = Color(0xFFFFFFFF)
private val CardBeige = Color(0xFFF6F2EA)

// Sage Green
private val SageDark = Color(0xFF516B65)
private val Sage = Color(0xFF647E78)
private val SageLight = Color(0xFF8FA19D)

// Gold
private val Gold = Color(0xFFD4B07A)
private val GoldLight = Color(0xFFE6C89A)
private val GoldDeep = Color(0xFFBB945C)

// Text
private val TextDark = Color(0xFF263632)
private val TextSecondary = Color(0xFF68716E)
private val TextMuted = Color(0xFF8C9591)

private val PremiumColorScheme = darkColorScheme(
    primary = Sage,
    secondary = Gold,
    tertiary = SageLight,

    background = BackgroundTop,
    surface = CardWhite,

    onPrimary = Color.White,
    onSecondary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun GoldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumColorScheme,
        content = content
    )
}

object GoldColors {

    // Main Background
    val Background = BackgroundTop

    val BackgroundGradient = Brush.verticalGradient(
        colors = listOf(
            BackgroundTop,
            BackgroundBottom
        )
    )

    // Cards
    val Surface = CardWhite

    val SurfaceElevated =
        CardBeige

    // Glass Effect
    val GlassCard =
        Color.White.copy(alpha = 0.90f)

    val GlassCardPremium =
        CardBeige.copy(alpha = 0.95f)

    val GlassBorder =
        Sage.copy(alpha = 0.18f)

    val GlassBorderStrong =
        Gold.copy(alpha = 0.35f)

    val GlassGlow =
        GoldLight.copy(alpha = 0.10f)

    // Gold
    val Gold = Gold
    val GoldLightTone = GoldLight
    val GoldDeepTone = GoldDeep

    val GoldGradient = Brush.linearGradient(
        colors = listOf(
            GoldLight,
            Gold,
            GoldDeep
        )
    )

    // Green
    val Emerald = Sage
    val EmeraldGlow = SageDark

    val EmeraldGradient = Brush.linearGradient(
        colors = listOf(
            SageLight,
            Sage,
            SageDark
        )
    )

    // Hero Card
    val HeroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6E8680),
            Color(0xFF4F6963)
        )
    )

    // Premium
    val PremiumCard =
        CardBeige

    val PremiumCardBorder =
        Gold.copy(alpha = 0.35f)

    // Live
    val LiveGreen =
        Sage

    // Beige
    val Beige =
        BackgroundBottom

    val SecondaryCard =
        CardBeige

    // Text
    val TextPrimary =
        TextDark

    val TextMuted =
        TextSecondary

    val TextFaint =
        TextMuted
}
