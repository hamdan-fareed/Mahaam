package com.mahaam.app.ui.theme

import androidx.compose.ui.graphics.Color

// Day Palette
val DayBg = Color(0xFFFAFAF8)
val DayBgSecondary = Color(0xFFF2F0EC)
val DayText = Color(0xFF1A1A1A)
val DayTextSecondary = Color(0xFF6B6B6B)
val DayBorder = Color(0xFFE0DDD7)
val DaySurface = Color(0xFFFFFFFF)

// Night Palette
val NightBg = Color(0xFF1A1A1A)
val NightBgSecondary = Color(0xFF252525)
val NightText = Color(0xFFE8E4DE)
val NightTextSecondary = Color(0xFF9A9590)
val NightBorder = Color(0xFF3A3A3A)
val NightSurface = Color(0xFF222222)

// Inks
val InkIndigo = Color(0xFF4A5899)
val InkIndigoLight = Color(0xFF6B7AC0)
val InkIndigoFaint = Color(0x1A4A5899)

val InkCrimson = Color(0xFFA63D40)
val InkCrimsonLight = Color(0xFFC75B5E)
val InkCrimsonFaint = Color(0x1AA63D40)

val InkMatcha = Color(0xFF5B7553)
val InkMatchaLight = Color(0xFF7A9472)
val InkMatchaFaint = Color(0x1A5B7553)

data class MahaamColors(
    val bg: Color,
    val bgSecondary: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val border: Color,
    val accent: Color,
    val accentLight: Color,
    val accentFaint: Color,
    val isNight: Boolean
)

fun getMahaamColors(isNight: Boolean, ink: String): MahaamColors {
    val (accent, accentLight, accentFaint) = when (ink.lowercase()) {
        "crimson" -> Triple(InkCrimson, InkCrimsonLight, InkCrimsonFaint)
        "matcha" -> Triple(InkMatcha, InkMatchaLight, InkMatchaFaint)
        else -> Triple(InkIndigo, InkIndigoLight, InkIndigoFaint)
    }

    return if (isNight) {
        MahaamColors(
            bg = NightBg,
            bgSecondary = NightBgSecondary,
            surface = NightSurface,
            text = NightText,
            textSecondary = NightTextSecondary,
            border = NightBorder,
            accent = accent,
            accentLight = accentLight,
            accentFaint = accentFaint,
            isNight = true
        )
    } else {
        MahaamColors(
            bg = DayBg,
            bgSecondary = DayBgSecondary,
            surface = DaySurface,
            text = DayText,
            textSecondary = DayTextSecondary,
            border = DayBorder,
            accent = accent,
            accentLight = accentLight,
            accentFaint = accentFaint,
            isNight = false
        )
    }
}
