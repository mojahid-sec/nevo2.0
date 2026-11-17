package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PureBlack

@Composable
fun NivoRibbonSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    containerBackground: Color = PureBlack,
    borderColor: Color = OutlineGrey
) {
    val isLight = LocalNivoColors.current.isLight
    val shape = RoundedCornerShape(size * 0.28f)
    val rimBrush = if (isLight) {
        Brush.verticalGradient(listOf(OutlineGrey, OutlineGrey))
    } else {
        Brush.verticalGradient(listOf(BevelHighlight, borderColor, BevelShadow))
    }
    val housingBrush = if (isLight) {
        Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color.White))
    } else {
        Brush.verticalGradient(listOf(MetalTop, containerBackground, MetalBottom))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(housingBrush)
            .border(1.4.dp, rimBrush, shape),
        contentAlignment = Alignment.Center
    ) {
        val primaryRedColor = PrimaryRed
        val accentRedColor = AccentRed
        val darkRedColor = DarkRed
        val depthDarkColor = DarkRed.copy(alpha = 0.65f)

        Canvas(modifier = Modifier.size(size * 0.76f)) {
            val w = this.size.width
            val h = this.size.height

            val ribbonBrush = Brush.linearGradient(
                colors = listOf(accentRedColor, primaryRedColor, darkRedColor),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            val ribbonPath = Path().apply {
                moveTo(w * 0.22f, h * 0.85f)
                cubicTo(w * 0.12f, h * 0.65f, w * 0.16f, h * 0.25f, w * 0.32f, h * 0.15f)
                cubicTo(w * 0.45f, h * 0.08f, w * 0.52f, h * 0.22f, w * 0.45f, h * 0.40f)
                cubicTo(w * 0.40f, h * 0.52f, w * 0.32f, h * 0.72f, w * 0.55f, h * 0.82f)
                cubicTo(w * 0.68f, h * 0.88f, w * 0.75f, h * 0.75f, w * 0.78f, h * 0.60f)
                lineTo(w * 0.84f, h * 0.18f)
                cubicTo(w * 0.88f, h * 0.10f, w * 0.94f, h * 0.14f, w * 0.90f, h * 0.28f)
                lineTo(w * 0.76f, h * 0.72f)
                cubicTo(w * 0.72f, h * 0.88f, w * 0.58f, h * 0.95f, w * 0.42f, h * 0.88f)
                cubicTo(w * 0.28f, h * 0.80f, w * 0.35f, h * 0.60f, w * 0.40f, h * 0.48f)
                cubicTo(w * 0.45f, h * 0.35f, w * 0.38f, h * 0.25f, w * 0.30f, h * 0.28f)
                cubicTo(w * 0.24f, h * 0.32f, w * 0.22f, h * 0.60f, w * 0.30f, h * 0.82f)
                close()
            }

            drawPath(
                path = ribbonPath,
                brush = Brush.verticalGradient(
                    colors = listOf(darkRedColor, depthDarkColor),
                    startY = h * 0.3f,
                    endY = h
                ),
                style = Fill
            )

            drawPath(
                path = ribbonPath,
                brush = ribbonBrush,
                style = Stroke(
                    width = w * 0.14f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            val frontHighlight = Path().apply {
                moveTo(w * 0.32f, h * 0.22f)
                cubicTo(w * 0.40f, h * 0.38f, w * 0.50f, h * 0.65f, w * 0.68f, h * 0.82f)
            }
            drawPath(
                path = frontHighlight,
                color = accentRedColor,
                style = Stroke(
                    width = w * 0.11f,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
