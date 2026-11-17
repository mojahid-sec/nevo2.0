package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.DarkRed
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent

@Composable
fun MotivationalCard(
    modifier: Modifier = Modifier,
    quote: String = "Small disciplines repeated with consistency every day lead to great achievements.",
    author: String = "Focus on today"
) {
    SkeuoPanelCard(
        modifier = modifier.testTag("motivational_card"),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(listOf(PrimaryRed.copy(alpha = 0.35f), DarkRed.copy(alpha = 0.25f)))
                    )
                    .border(1.dp, BevelShadow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\"$quote\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = PrimaryText,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 2.dp)
                            .background(PrimaryRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = author,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SilverAccent.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
