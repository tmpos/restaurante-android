package com.tmrestaurant.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColorScheme(
    // Primary colors - derived from selected hex
    val Primary: Color = Color(0xFF8758F2),
    val PrimaryHover: Color = Color(0xFF7548E8),
    val PrimaryLight: Color = Color(0xFFF0EBFF),

    // Fixed colors - never change with primary
    val Background: Color = Color(0xFFF5F6FA),
    val Surface: Color = Color(0xFFFFFFFF),
    val Danger: Color = Color(0xFFEF4444),
    val DangerLight: Color = Color(0xFFFEE2E2),
    val Orange: Color = Color(0xFFE68A00),
    val OrangeLight: Color = Color(0xFFFFF3E0),
    val TextPrimary: Color = Color(0xFF111827),
    val TextSecondary: Color = Color(0xFF6B7280),
    val Border: Color = Color(0xFFE5E7EB),
    val Success: Color = Color(0xFF10B981),
    val SuccessLight: Color = Color(0xFFD1FAE5),
    val Info: Color = Color(0xFF3B82F6),
    val InfoLight: Color = Color(0xFFDBEAFE),
    val Green: Color = Color(0xFF22C55E),
    val Gray: Color = Color(0xFF9CA3AF),
    val StarYellow: Color = Color(0xFFFBBF24),
    val BadgeRed: Color = Color(0xFFEF4444),
    val BadgeBlue: Color = Color(0xFF3B82F6),
    val BadgeOrange: Color = Color(0xFFE68A00),
    val BadgeGreen: Color = Color(0xFF10B981),
    val CardShadow: Color = Color(0x08000000),
    val IconGray: Color = Color(0xFF6B7280),
    val PlaceholderBg: Color = Color(0xFFF3F4F6),
    val TopBarBorder: Color = Color(0xFFE5E7EB),
    val NotificationDot: Color = Color(0xFFEF4444),
    val CartItemBg: Color = Color(0xFFF9FAFB),
    val DividerColor: Color = Color(0xFFF3F4F6),
)

val LocalAppColors = staticCompositionLocalOf { AppColorScheme() }

/**
 * Proxy object so existing `AppColors.Primary` references keep working.
 * Each property delegates to [LocalAppColors] via composable getters.
 */
object AppColors {
    val Primary: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Primary
    val PrimaryHover: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.PrimaryHover
    val PrimaryLight: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.PrimaryLight
    val Background: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Background
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Surface
    val Danger: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Danger
    val DangerLight: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.DangerLight
    val Orange: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Orange
    val OrangeLight: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.OrangeLight
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.TextPrimary
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.TextSecondary
    val Border: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Border
    val Success: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Success
    val SuccessLight: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.SuccessLight
    val Info: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Info
    val InfoLight: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.InfoLight
    val Green: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Green
    val Gray: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.Gray
    val StarYellow: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.StarYellow
    val BadgeRed: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.BadgeRed
    val BadgeBlue: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.BadgeBlue
    val BadgeOrange: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.BadgeOrange
    val BadgeGreen: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.BadgeGreen
    val CardShadow: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.CardShadow
    val IconGray: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.IconGray
    val PlaceholderBg: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.PlaceholderBg
    val TopBarBorder: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.TopBarBorder
    val NotificationDot: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.NotificationDot
    val CartItemBg: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.CartItemBg
    val DividerColor: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.DividerColor
}

/** Parse a hex color string like "#8758F2" into a Compose [Color]. */
fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val argb = when (clean.length) {
        6 -> (0xFF000000 or clean.toLong(16))
        8 -> clean.toLong(16)
        else -> 0xFF8758F2 // fallback to default purple
    }
    return Color(argb.toInt())
}

/**
 * Build an [AppColorScheme] with primary colors derived from [primaryHex].
 * Non-primary colors keep their defaults.
 */
fun buildColorScheme(primaryHex: String, darkTheme: Boolean = false): AppColorScheme {
    val primary = parseHexColor(primaryHex)
    // Darken for hover by blending with black
    val hoverR = (primary.red * 0.88f)
    val hoverG = (primary.green * 0.88f)
    val hoverB = (primary.blue * 0.88f)
    val primaryHover = Color(hoverR, hoverG, hoverB, primary.alpha)
    // Lighten for light variant by blending with white
    val lightR = primary.red * 0.15f + 0.85f
    val lightG = primary.green * 0.15f + 0.85f
    val lightB = primary.blue * 0.15f + 0.85f
    val primaryLight = Color(lightR, lightG, lightB, 1f)

    return if (darkTheme) {
        AppColorScheme(
            Primary = Color(
                red = primary.red * 0.55f + 0.45f,
                green = primary.green * 0.55f + 0.45f,
                blue = primary.blue * 0.55f + 0.45f,
                alpha = 1f
            ),
            PrimaryHover = Color(
                red = primaryHover.red * 0.60f + 0.40f,
                green = primaryHover.green * 0.60f + 0.40f,
                blue = primaryHover.blue * 0.60f + 0.40f,
                alpha = 1f
            ),
            PrimaryLight = Color(0xFF2F2552),
            Background = Color(0xFF0F172A),
            Surface = Color(0xFF111827),
            Danger = Color(0xFFF87171),
            DangerLight = Color(0xFF3F1D24),
            Orange = Color(0xFFFBBF24),
            OrangeLight = Color(0xFF3D2C0A),
            TextPrimary = Color(0xFFF9FAFB),
            TextSecondary = Color(0xFFCBD5E1),
            Border = Color(0xFF334155),
            Success = Color(0xFF34D399),
            SuccessLight = Color(0xFF123B31),
            Info = Color(0xFF60A5FA),
            InfoLight = Color(0xFF172A46),
            Green = Color(0xFF34D399),
            Gray = Color(0xFF94A3B8),
            StarYellow = Color(0xFFFBBF24),
            BadgeRed = Color(0xFFF87171),
            BadgeBlue = Color(0xFF60A5FA),
            BadgeOrange = Color(0xFFFBBF24),
            BadgeGreen = Color(0xFF34D399),
            CardShadow = Color(0x40000000),
            IconGray = Color(0xFFCBD5E1),
            PlaceholderBg = Color(0xFF1E293B),
            TopBarBorder = Color(0xFF334155),
            NotificationDot = Color(0xFFF87171),
            CartItemBg = Color(0xFF1E293B),
            DividerColor = Color(0xFF334155),
        )
    } else {
        AppColorScheme(
            Primary = primary,
            PrimaryHover = primaryHover,
            PrimaryLight = primaryLight,
        )
    }
}
