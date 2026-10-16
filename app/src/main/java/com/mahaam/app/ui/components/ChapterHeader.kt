package com.mahaam.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.ui.theme.LocalMahaamColors

@Composable
fun ChapterHeader(
    chapterNum: String,
    chapterArabic: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = chapterNum.uppercase(),
                fontFamily = FontFamily.Serif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = colors.accent
            )
            Text(
                text = "·",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Text(
                text = chapterArabic,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = colors.text,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 14.sp,
            color = colors.textSecondary
        )
    }
}
