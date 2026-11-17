package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverMuted
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularProgressRing(
    progressFraction: Float,
    completedDays: Int,
    remainingDays: Int,
    totalDays: Int,
    modifier: Modifier = Modifier,
    title: String? = null,
    size: Dp = 230.dp,
    strokeWidth: Dp = 16.dp,
    primaryColor: Color = PrimaryRed,
    secondaryColor: Color = DarkRed,
    testTag: String = "circular_progress_ring",
    language: AppLanguage = AppLanguage.ENGLISH
) {
    val isLight = LocalNivoColors.current.isLight
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    val progressPercent = (animatedProgress * 100).toInt()
    val isCompact = size < 180.dp
    val accentRedColor = AccentRed
    val tickColor = if (isLight) OutlineGrey else SilverMuted.copy(alpha = 0.35f)
    val lightCardColor = CardBackground
    val lightWellColor = RecessedWell
    val lightBorderColor = OutlineGrey

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w / 2f, h / 2f)
            val strokePx = strokeWidth.toPx()
            val outerBezelRadius = (minOf(w, h) / 2f) - 2f

            // 1. Outer bezel ring
            if (isLight) {
                drawCircle(
                    color = lightBorderColor,
                    radius = outerBezelRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            } else {
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3A3E4A), Color(0xFF14151A), Color(0xFF070709))
                    ),
                    radius = outerBezelRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 2. Inner dial face
            val innerWellRadius = outerBezelRadius - strokePx * 0.9f
            if (isLight) {
                drawCircle(
                    color = lightCardColor,
                    radius = innerWellRadius.coerceAtLeast(10f),
                    center = center
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF13151B), Color(0xFF0B0C0F), Color(0xFF060608)),
                        center = center,
                        radius = innerWellRadius.coerceAtLeast(10f)
                    ),
                    radius = innerWellRadius.coerceAtLeast(10f),
                    center = center
                )
            }

            // 3. Subtle calibration ticks
            val tickOuter = outerBezelRadius - 5.dp.toPx()
            val tickInner = outerBezelRadius - 9.dp.toPx()
            for (i in 0 until 24) {
                val angleRad = Math.toRadians((i * 15.0) - 90.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()
                val isMajor = i % 6 == 0
                drawLine(
                    color = if (isMajor) accentRedColor.copy(alpha = if (isLight) 0.45f else 0.65f) else tickColor,
                    start = Offset(center.x + tickInner * cosA, center.y + tickInner * sinA),
                    end = Offset(center.x + tickOuter * cosA, center.y + tickOuter * sinA),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Track channel
            val arcInset = strokePx / 2f + 10.dp.toPx()
            val arcSize = Size((w - arcInset * 2f).coerceAtLeast(4f), (h - arcInset * 2f).coerceAtLeast(4f))
            val topLeft = Offset(arcInset, arcInset)

            if (isLight) {
                drawArc(
                    color = lightWellColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    color = Color(0xFF050507),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx + 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color(0xFF1B1D24),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx - 1.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 5. Active progress arc
            if (animatedProgress > 0.005f) {
                val sweep = 360f * animatedProgress
                drawArc(
                    color = primaryColor.copy(alpha = if (isLight) 0.14f else 0.25f),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx + 5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = if (isLight) {
                        Brush.sweepGradient(
                            colors = listOf(secondaryColor, primaryColor, Color(0xFF45BD6E), primaryColor, secondaryColor)
                        )
                    } else {
                        Brush.sweepGradient(
                            colors = listOf(secondaryColor, primaryColor, accentRedColor, primaryColor, secondaryColor)
                        )
                    },
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Telemetry Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            if (title != null && isCompact) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Text(
                text = "${progressPercent.toLocalizedDigits(language)}%",
                style = TextStyle(
                    fontSize = if (isCompact) 26.sp else 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    fontFeatureSettings = "tnum"
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))

            Text(
                text = if (isCompact) {
                    NivoStrings.daysRatio(language, completedDays, totalDays)
                } else {
                    NivoStrings.ofDays(language, completedDays, totalDays)
                },
                fontSize = if (isCompact) 11.sp else 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentRed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isCompact) 1.dp else 2.dp))

            Text(
                text = NivoStrings.daysRemaining(language, remainingDays),
                fontSize = if (isCompact) 10.sp else 11.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}
