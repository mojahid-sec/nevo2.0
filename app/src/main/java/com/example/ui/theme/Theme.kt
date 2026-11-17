package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkRedColorScheme = darkColorScheme(
    primary = DarkRedSkeuoColors.primaryAccent,
    onPrimary = DarkRedSkeuoColors.textPrimary,
    primaryContainer = DarkRedSkeuoColors.secondaryAccent,
    onPrimaryContainer = DarkRedSkeuoColors.textPrimary,
    secondary = DarkRedSkeuoColors.accent,
    onSecondary = DarkRedSkeuoColors.textPrimary,
    secondaryContainer = DarkRedSkeuoColors.cardElevated,
    onSecondaryContainer = DarkRedSkeuoColors.textPrimary,
    background = DarkRedSkeuoColors.background,
    onBackground = DarkRedSkeuoColors.textPrimary,
    surface = DarkRedSkeuoColors.card,
    onSurface = DarkRedSkeuoColors.textPrimary,
    surfaceVariant = DarkRedSkeuoColors.cardElevated,
    onSurfaceVariant = DarkRedSkeuoColors.textSecondary,
    outline = DarkRedSkeuoColors.outline,
    error = DarkRedSkeuoColors.accent,
    onError = DarkRedSkeuoColors.textPrimary
)

private val LightSkeuoColorScheme = lightColorScheme(
    primary = LightSkeuoColors.primaryAccent,
    onPrimary = Color.White,
    primaryContainer = LightSkeuoColors.secondaryAccent,
    onPrimaryContainer = Color.White,
    secondary = LightSkeuoColors.accent,
    onSecondary = Color.White,
    secondaryContainer = LightSkeuoColors.cardElevated,
    onSecondaryContainer = LightSkeuoColors.textPrimary,
    background = LightSkeuoColors.background,
    onBackground = LightSkeuoColors.textPrimary,
    surface = LightSkeuoColors.card,
    onSurface = LightSkeuoColors.textPrimary,
    surfaceVariant = LightSkeuoColors.cardElevated,
    onSurfaceVariant = LightSkeuoColors.textSecondary,
    outline = LightSkeuoColors.outline,
    error = LightSkeuoColors.error,
    onError = Color.White
)

@Suppress("DEPRECATION")
@Composable
fun MyApplicationTheme(
    theme: NivoThemeStyle = NivoThemeStyle.DARK_RED,
    content: @Composable () -> Unit
) {
    val nivoColors = when (theme) {
        NivoThemeStyle.LIGHT -> LightSkeuoColors
        NivoThemeStyle.DARK_RED -> DarkRedSkeuoColors
    }
    val materialScheme = when (theme) {
        NivoThemeStyle.LIGHT -> LightSkeuoColorScheme
        NivoThemeStyle.DARK_RED -> DarkRedColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = nivoColors.barBackground.toArgb()
                window.navigationBarColor = nivoColors.barBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = nivoColors.isLight
                insetsController.isAppearanceLightNavigationBars = nivoColors.isLight
            }
        }
    }

    CompositionLocalProvider(
        LocalNivoColors provides nivoColors
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
