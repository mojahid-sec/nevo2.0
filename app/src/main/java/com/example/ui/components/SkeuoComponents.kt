package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent
import com.example.ui.theme.SilverMuted

/**
 * Raised panel card: brushed charcoal chassis in Dark Red Theme,
 * clean soft-shadow white/pastel surface in Light Theme.
 */
@Composable
fun SkeuoPanelCard(
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    containerColorOverride: Color? = null,
    borderColorOverride: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = LocalNivoColors.current.isLight
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "panel_press_scale"
    )

    val surfaceBrush = if (isLight) {
        val baseColor = containerColorOverride ?: CardBackground
        Brush.verticalGradient(listOf(baseColor, baseColor))
    } else if (containerColorOverride != null) {
        Brush.verticalGradient(listOf(containerColorOverride, containerColorOverride))
    } else if (isHighlighted) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF281B1E), Color(0xFF1A1618), Color(0xFF121114))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(MetalTop.copy(alpha = 0.72f), CardBackground, MetalBottom)
        )
    }

    val bevelShadowColor = BevelShadow
    val topRimColor = if (isHighlighted) AccentRed.copy(alpha = 0.55f) else BevelHighlight
    val borderBrush = if (isLight) {
        val bColor = borderColorOverride ?: if (isHighlighted) PrimaryRed.copy(alpha = 0.65f) else OutlineGrey
        Brush.verticalGradient(listOf(bColor, bColor))
    } else {
        Brush.verticalGradient(
            colors = listOf(
                topRimColor,
                if (isHighlighted) PrimaryRed.copy(alpha = 0.4f) else OutlineGrey,
                bevelShadowColor
            )
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = PrimaryRed.copy(alpha = 0.25f)),
            onClick = onClick
        )
    } else {
        Modifier
    }

    val shadowModifier = if (isLight) {
        Modifier.shadow(
            elevation = if (isHighlighted) 4.dp else 2.dp,
            shape = shape,
            ambientColor = Color(0x18202B40),
            spotColor = Color(0x18202B40)
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .then(shadowModifier)
            .clip(shape)
            .background(surfaceBrush)
            .border(width = if (isLight && isHighlighted) 1.4.dp else 1.2.dp, brush = borderBrush, shape = shape)
            .drawBehind {
                if (!isLight) {
                    // Inner bottom specular groove for dark skeuomorphic chassis
                    drawLine(
                        color = bevelShadowColor.copy(alpha = 0.85f),
                        start = Offset(12f, size.height - 1f),
                        end = Offset(size.width - 12f, size.height - 1f),
                        strokeWidth = 2f
                    )
                }
            }
            .then(clickModifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Secondary inset well: concave dark display bay in Dark Red Theme,
 * soft gray #F5F6F8 rounded container in Light Theme.
 */
@Composable
fun SkeuoRecessedWell(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    accentGlow: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isLight = LocalNivoColors.current.isLight
    val shape = RoundedCornerShape(cornerRadius)
    val wellBrush = if (isLight) {
        Brush.verticalGradient(listOf(RecessedWell, RecessedWell))
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF060608),
                RecessedWell,
                Color(0xFF12141A)
            )
        )
    }
    val borderBrush = if (isLight) {
        val c = if (accentGlow) PrimaryRed.copy(alpha = 0.35f) else OutlineGrey
        Brush.verticalGradient(listOf(c, c))
    } else {
        Brush.verticalGradient(
            colors = listOf(
                BevelShadow,
                if (accentGlow) PrimaryRed.copy(alpha = 0.35f) else Color(0xFF1B1D24),
                BevelHighlight.copy(alpha = 0.5f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(wellBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content
    )
}

/**
 * Status dot / LED indicator: glowing crimson LED in Dark Red Theme,
 * crisp soft green status dot in Light Theme.
 */
@Composable
fun SkeuoLedIndicator(
    isOn: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp
) {
    val isLight = LocalNivoColors.current.isLight
    val coreColor by animateColorAsState(
        targetValue = if (isOn) AccentRed else if (isLight) Color(0xFFCBD5E1) else Color(0xFF333640),
        label = "led_core"
    )
    val haloColor by animateColorAsState(
        targetValue = if (isOn) PrimaryRed.copy(alpha = if (isLight) 0.18f else 0.35f) else Color.Transparent,
        label = "led_halo"
    )

    Box(
        modifier = modifier
            .size(size * 1.8f)
            .clip(CircleShape)
            .background(haloColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    if (isLight) {
                        Brush.radialGradient(listOf(coreColor, coreColor))
                    } else if (isOn) {
                        Brush.radialGradient(listOf(Color(0xFFFF9EA8), coreColor, DarkRed))
                    } else {
                        Brush.radialGradient(listOf(Color(0xFF454954), Color(0xFF22242B)))
                    }
                )
                .border(0.6.dp, if (isLight) OutlineGrey else BevelShadow, CircleShape)
        )
    }
}

/**
 * Physical hardware toggle switch in Dark Red Theme,
 * clean soft-green/gray pill toggle switch in Light Theme.
 * Maintains a >= 48dp touch target for accessibility.
 */
@Composable
fun SkeuoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val isLight = LocalNivoColors.current.isLight
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switch_thumb_offset"
    )
    val trackColor by animateColorAsState(
        targetValue = when {
            isLight && checked -> PrimaryRed
            isLight && !checked -> Color(0xFFD5DCE4)
            checked -> DarkRed.copy(alpha = 0.85f)
            else -> Color(0xFF0A0B0E)
        },
        label = "switch_track"
    )
    val ledDotColor by animateColorAsState(
        targetValue = when {
            isLight -> if (checked) PrimaryRed else Color(0xFFB8C2CE)
            checked -> AccentRed
            else -> Color(0xFF464A54)
        },
        label = "switch_led"
    )

    val taggedModifier = if (testTag.isNotBlank()) modifier.testTag(testTag) else modifier

    Box(
        modifier = taggedModifier
            .size(width = 54.dp, height = 48.dp)
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 26.dp, color = PrimaryRed.copy(alpha = 0.3f)),
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Track
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isLight) {
                        Brush.verticalGradient(listOf(trackColor, trackColor))
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF050507), trackColor, Color(0xFF15171D))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = if (isLight) {
                        val b = if (checked) PrimaryRed else OutlineGrey
                        Brush.verticalGradient(listOf(b, b))
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                BevelShadow,
                                if (checked) PrimaryRed.copy(alpha = 0.7f) else BevelHighlight.copy(alpha = 0.45f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            // Sliding Knob
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .then(if (isLight) Modifier.shadow(2.dp, CircleShape) else Modifier)
                    .clip(CircleShape)
                    .background(
                        if (isLight) {
                            Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                        } else {
                            Brush.verticalGradient(
                                colors = if (checked) {
                                    listOf(Color(0xFFE6E9EF), SilverAccent, Color(0xFF7D8390))
                                } else {
                                    listOf(Color(0xFF565B68), Color(0xFF2E313A), Color(0xFF1D1F25))
                                }
                            )
                        }
                    )
                    .border(0.8.dp, if (isLight) OutlineGrey else BevelShadow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ledDotColor)
                )
            }
        }
    }
}

/**
 * Single unified linear progress gauge per Habit.
 */
@Composable
fun SkeuoLinearGauge(
    progressFraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    fillColorOverride: Color? = null
) {
    val isLight = LocalNivoColors.current.isLight
    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "linear_gauge_progress"
    )

    val trackBrush = if (isLight) {
        Brush.verticalGradient(listOf(Color(0xFFE6EBF0), Color(0xFFEDF1F5)))
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF050507), Color(0xFF111216), Color(0xFF1A1C22))
        )
    }

    val fillBrush = if (fillColorOverride != null) {
        Brush.verticalGradient(listOf(fillColorOverride, fillColorOverride))
    } else if (isLight) {
        Brush.verticalGradient(listOf(Color(0xFF42B86A), PrimaryRed))
    } else {
        Brush.verticalGradient(colors = listOf(AccentRed, PrimaryRed, DarkRed))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackBrush)
            .border(
                width = 0.8.dp,
                brush = if (isLight) {
                    Brush.verticalGradient(listOf(OutlineGrey, OutlineGrey))
                } else {
                    Brush.verticalGradient(listOf(BevelShadow, BevelHighlight.copy(alpha = 0.5f)))
                },
                shape = RoundedCornerShape(height / 2)
            )
            .padding(if (isLight) 0.dp else 1.5.dp)
    ) {
        if (animatedFraction > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(height / 2))
                    .background(fillBrush)
                    .drawBehind {
                        if (!isLight) {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.25f),
                                topLeft = Offset(2f, 1f),
                                size = androidx.compose.ui.geometry.Size(
                                    width = (size.width - 4f).coerceAtLeast(0f),
                                    height = (size.height * 0.28f).coerceAtLeast(1f)
                                ),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
            )
        }
    }
}

/**
 * Section label header with indicator dot.
 */
@Composable
fun SkeuoSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            SkeuoLedIndicator(isOn = true, size = 6.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SilverAccent,
                letterSpacing = 1.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailing,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        }
    }
}

/**
 * Icon key button (48dp touch target) for Back, Edit, Delete, Nav arrows.
 */
@Composable
fun SkeuoIconKeyButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = SilverAccent,
    isAccent: Boolean = false,
    keySize: Dp = 40.dp,
    testTag: String = ""
) {
    val isLight = LocalNivoColors.current.isLight
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "icon_key_scale"
    )
    val taggedModifier = if (testTag.isNotBlank()) modifier.testTag(testTag) else modifier

    val keyBrush = when {
        isLight && isAccent -> Brush.verticalGradient(listOf(PrimaryRed, PrimaryRed))
        isLight -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F6F8)))
        isAccent -> Brush.verticalGradient(listOf(AccentRed, PrimaryRed, DarkRed))
        else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
    }

    val borderBrush = if (isLight) {
        val c = if (isAccent) PrimaryRed else OutlineGrey
        Brush.verticalGradient(listOf(c, c))
    } else {
        Brush.verticalGradient(
            listOf(
                if (isAccent) Color(0xFFFF8A96) else BevelHighlight,
                BevelShadow
            )
        )
    }

    val iconTint = when {
        isAccent -> Color.White
        else -> tint
    }

    Box(
        modifier = taggedModifier
            .size(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp, color = PrimaryRed.copy(alpha = 0.3f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(keySize)
                .clip(RoundedCornerShape(12.dp))
                .background(keyBrush)
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Tactile chip button with indicator dot when selected.
 */
@Composable
fun SkeuoDetentChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = ""
) {
    val isLight = LocalNivoColors.current.isLight
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chip_scale"
    )
    val taggedModifier = if (testTag.isNotBlank()) modifier.testTag(testTag) else modifier

    val bgBrush = when {
        isLight && selected -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color(0xFFD6F3DF)))
        isLight -> Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F6F8)))
        selected -> Brush.verticalGradient(listOf(Color(0xFF3E191E), Color(0xFF281115), Color(0xFF1A0D10)))
        else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
    }

    val borderBrush = if (isLight) {
        val c = if (selected) PrimaryRed.copy(alpha = 0.7f) else OutlineGrey
        Brush.verticalGradient(listOf(c, c))
    } else {
        Brush.verticalGradient(
            listOf(
                if (selected) AccentRed.copy(alpha = 0.8f) else BevelHighlight,
                BevelShadow
            )
        )
    }

    Box(
        modifier = taggedModifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush)
            .border(
                width = if (selected) 1.2.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = PrimaryRed.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) AccentRed else SilverMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                SkeuoLedIndicator(isOn = selected, size = 5.dp)
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isLight && selected -> DarkRed
                    selected -> PrimaryText
                    else -> SecondaryText
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Horizontal divider: carved bevel groove in Dark Red, clean 1dp hairline in Light Theme.
 */
@Composable
fun SkeuoCarvedDivider(modifier: Modifier = Modifier) {
    val isLight = LocalNivoColors.current.isLight
    Column(modifier = modifier.fillMaxWidth()) {
        if (isLight) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OutlineGrey)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BevelShadow)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(BevelHighlight.copy(alpha = 0.4f))
            )
        }
    }
}

/**
 * Compact stat bay cell used in Overview consoles.
 */
@Composable
fun SkeuoTelemetryCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightAccent: Boolean = false
) {
    SkeuoRecessedWell(
        modifier = modifier,
        cornerRadius = 12.dp,
        accentGlow = highlightAccent,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (highlightAccent) AccentRed else PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
