package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class NivoThemeStyle(val id: String) {
    LIGHT("light"),
    DARK_RED("dark_red");

    companion object {
        fun fromId(id: String?): NivoThemeStyle {
            return when (id?.lowercase()) {
                "light" -> LIGHT
                else -> DARK_RED
            }
        }
    }
}

/**
 * Skeuomorphic Color System for NIVO supporting Light & Dark Red themes.
 */
data class NivoColors(
    val background: Color,
    val barBackground: Color,
    val card: Color,
    val cardElevated: Color,
    val recessedWell: Color,
    val metalTop: Color,
    val metalBottom: Color,
    val bevelHighlight: Color,
    val bevelShadow: Color,
    val silverAccent: Color,
    val silverMuted: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    val subtle: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val disabled: Color,
    val isLight: Boolean = false
)

val DarkRedSkeuoColors = NivoColors(
    background = Color(0xFF0B0B0D),      // Deep Matte Black
    barBackground = Color(0xFF070709),   // Obsidian Hardware Dock
    card = Color(0xFF17181D),            // Dark Charcoal Chassis
    cardElevated = Color(0xFF22242B),    // Raised Brushed Charcoal
    recessedWell = Color(0xFF0D0E12),    // Inset Display Bay
    metalTop = Color(0xFF292C34),        // Brushed Metal Top Specular
    metalBottom = Color(0xFF121317),     // Brushed Metal Bottom Depth
    bevelHighlight = Color(0xFF3E424F),  // Top Machined Rim Highlight
    bevelShadow = Color(0xFF040405),     // Deep Cast Bevel Shadow
    silverAccent = Color(0xFFC8CDD6),    // Metallic Silver
    silverMuted = Color(0xFF838A98),     // Brushed Steel
    primaryAccent = Color(0xFFD32F2F),   // Deep Crimson Red
    secondaryAccent = Color(0xFF7F0000), // Oxblood Shadow Red
    accent = Color(0xFFFF334B),          // Glowing Crimson LED / Specular Red
    textPrimary = Color(0xFFF5F6F8),     // Crisp Matte White
    textSecondary = Color(0xFF9EA4B0),   // Soft Metallic Gray
    outline = Color(0xFF2C2F38),         // Machined Seam Border
    subtle = Color(0xFF4F5563),          // Muted Gunmetal
    success = Color(0xFF22C55E),         // Subtle Green
    warning = Color(0xFFF59E0B),         // Amber
    error = Color(0xFFEF4444),           // Muted Red
    info = Color(0xFF3B82F6),            // Soft Blue
    disabled = Color(0xFF4B5563),        // Neutral Gray
    isLight = false
)

val LightSkeuoColors = NivoColors(
    background = Color(0xFFF7F8FA),      // Soft Off-White Main Background
    barBackground = Color(0xFFF1F3F6),   // Very Light Gray Dock / Bar Surface
    card = Color(0xFFFFFFFF),            // Clean Pure White Card
    cardElevated = Color(0xFFF5F6F8),    // Elevated Soft Gray Surface
    recessedWell = Color(0xFFF5F6F8),    // Soft Gray Secondary Well
    metalTop = Color(0xFFFFFFFF),        // Natural Light Top Highlight
    metalBottom = Color(0xFFF1F3F6),     // Subtle Light Bottom Surface
    bevelHighlight = Color(0xFFFFFFFF),  // Crisp White Rim Highlight
    bevelShadow = Color(0xFFE4E8ED),     // Very Light Gray Soft Shadow / Border
    silverAccent = Color(0xFF475569),    // Clean Dark Slate Icon / Label
    silverMuted = Color(0xFF8996A8),     // Muted Blue Gray
    primaryAccent = Color(0xFF32A85A),   // Soft Green Primary Button & Active Progress
    secondaryAccent = Color(0xFF248848), // Deeper Leaf Green Accent
    accent = Color(0xFF32A85A),          // Soft Green Active Indicator
    textPrimary = Color(0xFF202B40),     // Dark Navy Primary Text
    textSecondary = Color(0xFF8996A8),   // Muted Blue Gray Secondary Text
    outline = Color(0xFFE4E8ED),         // Very Light Gray Border
    subtle = Color(0xFFA8B3C2),          // Muted Gray
    success = Color(0xFF32A85A),         // Soft Green
    warning = Color(0xFFD97706),         // Warm Amber
    error = Color(0xFFDC2626),           // Crisp Red
    info = Color(0xFF2B8CBE),            // Soft Blue
    disabled = Color(0xFFD5DCE4),        // Soft Light Disabled Gray
    isLight = true
)

/**
 * Balanced pastel palette for individual habit cards in Light Theme.
 */
data class HabitPastelPalette(
    val container: Color,
    val accent: Color
)

object LightHabitPastels {
    val palette = listOf(
        HabitPastelPalette(container = Color(0xFFDDF3FF), accent = Color(0xFF2B8CBE)), // Pastel Blue
        HabitPastelPalette(container = Color(0xFFE2F8E9), accent = Color(0xFF32A85A)), // Pastel Green
        HabitPastelPalette(container = Color(0xFFFFF7DF), accent = Color(0xFFD49A1A)), // Pastel Yellow
        HabitPastelPalette(container = Color(0xFFFFE5E5), accent = Color(0xFFD9536F)), // Pastel Pink
        HabitPastelPalette(container = Color(0xFFF1E6FF), accent = Color(0xFF8B5CF6))  // Pastel Purple
    )

    fun forTitle(title: String): HabitPastelPalette {
        val lower = title.lowercase()
        val index = when {
            lower.contains("read") || lower.contains("book") || lower.contains("study") || lower.contains("learn") -> 0
            lower.contains("run") || lower.contains("walk") || lower.contains("workout") || lower.contains("health") || lower.contains("gym") || lower.contains("water") -> 1
            lower.contains("focus") || lower.contains("work") || lower.contains("code") || lower.contains("finance") -> 2
            lower.contains("meditat") || lower.contains("gratitude") || lower.contains("journal") || lower.contains("sleep") -> 4
            else -> kotlin.math.abs(title.hashCode()) % palette.size
        }
        return palette[index]
    }
}

val SkeuoColors = DarkRedSkeuoColors
