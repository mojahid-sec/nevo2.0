package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalNivoColors = staticCompositionLocalOf { SkeuoColors }

val PureBlack: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.barBackground

val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.background

val CardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.card

val CardElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.cardElevated

val RecessedWell: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.recessedWell

val MetalTop: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.metalTop

val MetalBottom: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.metalBottom

val BevelHighlight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.bevelHighlight

val BevelShadow: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.bevelShadow

val SilverAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.silverAccent

val SilverMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.silverMuted

val PrimaryRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.primaryAccent

val DarkRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.secondaryAccent

val AccentRed: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.accent

val PrimaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.textPrimary

val SecondaryText: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.textSecondary

val SubtleGrey: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.subtle

val DividerGrey: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.outline

val OutlineGrey: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.outline

val SurfaceVariantDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalNivoColors.current.cardElevated
