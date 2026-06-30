package com.tmrestaurant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun TMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appColors: AppColorScheme = AppColorScheme(),
    content: @Composable () -> Unit
) {
    val lightScheme = lightColorScheme(
        primary = appColors.Primary,
        onPrimary = Color.White,
        primaryContainer = appColors.PrimaryLight,
        onPrimaryContainer = appColors.Primary,
        secondary = appColors.Orange,
        onSecondary = Color.White,
        secondaryContainer = appColors.OrangeLight,
        onSecondaryContainer = appColors.Orange,
        tertiary = appColors.Green,
        error = appColors.Danger,
        onError = Color.White,
        errorContainer = appColors.DangerLight,
        onErrorContainer = appColors.Danger,
        background = appColors.Background,
        onBackground = appColors.TextPrimary,
        surface = appColors.Surface,
        onSurface = appColors.TextPrimary,
        surfaceVariant = appColors.Background,
        onSurfaceVariant = appColors.TextSecondary,
        outline = appColors.Border,
        outlineVariant = appColors.TopBarBorder,
        inverseSurface = appColors.TextPrimary,
        inverseOnSurface = appColors.Surface,
    )

    val darkScheme = darkColorScheme(
        primary = Color(0xFFB794F4),
        onPrimary = Color(0xFF1A0033),
        primaryContainer = Color(0xFF4C1D95),
        onPrimaryContainer = Color(0xFFD8B4FE),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF1A1400),
        secondaryContainer = Color(0xFF7C5C00),
        onSecondaryContainer = Color(0xFFFDE68A),
        tertiary = Color(0xFF34D399),
        error = Color(0xFFFCA5A5),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA),
        background = Color(0xFF111827),
        onBackground = Color(0xFFF9FAFB),
        surface = Color(0xFF1F2937),
        onSurface = Color(0xFFF9FAFB),
        surfaceVariant = Color(0xFF374151),
        onSurfaceVariant = Color(0xFFD1D5DB),
        outline = Color(0xFF4B5563),
        outlineVariant = Color(0xFF374151),
    )

    val colorScheme = if (darkTheme) darkScheme else lightScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
