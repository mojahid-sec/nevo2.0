package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SilverAccent

@Composable
fun NivoLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 68.dp,
    showTagline: Boolean = true,
    language: AppLanguage = AppLanguage.ENGLISH
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .testTag("nivo_brand_logo"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NivoRibbonSymbol(size = iconSize)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "N I V O",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText,
            letterSpacing = 5.sp,
            textAlign = TextAlign.Center
        )

        if (showTagline) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = NivoStrings.appTagline(language),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SilverAccent.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
        }
    }
}
