package com.mahaam.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalMahaamColors = staticCompositionLocalOf {
    getMahaamColors(isNight = false, ink = "indigo")
}

@Composable
fun MahaamTheme(
    darkTheme: Boolean = false,
    ink: String = "indigo",
    content: @Composable () -> Unit
) {
    val mahaamColors = getMahaamColors(darkTheme, ink)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = mahaamColors.accent,
            onPrimary = NightBg,
            primaryContainer = mahaamColors.accentFaint,
            onPrimaryContainer = mahaamColors.accentLight,
            secondary = mahaamColors.accentLight,
            background = mahaamColors.bg,
            onBackground = mahaamColors.text,
            surface = mahaamColors.surface,
            onSurface = mahaamColors.text,
            surfaceVariant = mahaamColors.bgSecondary,
            onSurfaceVariant = mahaamColors.textSecondary,
            outline = mahaamColors.border
        )
    } else {
        lightColorScheme(
            primary = mahaamColors.accent,
            onPrimary = DayBg,
            primaryContainer = mahaamColors.accentFaint,
            onPrimaryContainer = mahaamColors.accent,
            secondary = mahaamColors.accentLight,
            background = mahaamColors.bg,
            onBackground = mahaamColors.text,
            surface = mahaamColors.surface,
            onSurface = mahaamColors.text,
            surfaceVariant = mahaamColors.bgSecondary,
            onSurfaceVariant = mahaamColors.textSecondary,
            outline = mahaamColors.border
        )
    }

    CompositionLocalProvider(LocalMahaamColors provides mahaamColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
