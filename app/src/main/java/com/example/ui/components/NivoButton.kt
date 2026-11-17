package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText

@Composable
fun NivoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String = "nivo_button",
    height: Dp = 56.dp,
    containerColor: Color = PrimaryRed,
    contentColor: Color = PrimaryText
) {
    val isLight = LocalNivoColors.current.isLight
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.975f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "button_press_scale"
    )

    val isPrimaryAccent = enabled && (containerColor == PrimaryRed || containerColor == DarkRed || containerColor == AccentRed)
    val surfaceBrush = when {
        !enabled && isLight -> Brush.verticalGradient(listOf(Color(0xFFE6EAF0), Color(0xFFDCE1E9)))
        !enabled -> Brush.verticalGradient(listOf(Color(0xFF191B20), Color(0xFF111216), Color(0xFF0C0D10)))
        isPrimaryAccent && isLight -> Brush.verticalGradient(listOf(Color(0xFF3CB866), PrimaryRed, Color(0xFF2B944F)))
        isPrimaryAccent -> Brush.verticalGradient(listOf(Color(0xFFE53935), PrimaryRed, DarkRed))
        isLight -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F6F8)))
        else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
    }

    val topBevel = when {
        isLight && !enabled -> OutlineGrey
        isLight && isPrimaryAccent -> Color(0xFF6BD88E)
        isLight -> OutlineGrey
        !enabled -> Color(0xFF262932)
        isPrimaryAccent -> Color(0xFFFF7A85).copy(alpha = 0.7f)
        else -> BevelHighlight
    }

    val borderBrush = if (isLight) {
        val c = when {
            !enabled -> OutlineGrey
            isPrimaryAccent -> Color(0xFF28964E)
            else -> OutlineGrey
        }
        Brush.verticalGradient(listOf(c, c))
    } else {
        Brush.verticalGradient(
            colors = listOf(topBevel, if (enabled && isPrimaryAccent) DarkRed else BevelShadow, BevelShadow)
        )
    }

    val effectiveTextColor = when {
        !enabled -> SecondaryText
        isLight && isPrimaryAccent -> Color.White
        else -> contentColor
    }
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(surfaceBrush)
            .border(width = 1.2.dp, brush = borderBrush, shape = shape)
            .drawBehind {
                if (enabled && isPrimaryAccent && !isPressed) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(24f, 2f),
                        end = Offset(size.width - 24f, 2f),
                        strokeWidth = 2f
                    )
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = PrimaryText.copy(alpha = 0.28f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = effectiveTextColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = effectiveTextColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}
