package com.mahaam.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.ui.theme.InkCrimson
import com.mahaam.app.ui.theme.InkIndigo
import com.mahaam.app.ui.theme.InkMatcha
import com.mahaam.app.ui.theme.LocalMahaamColors

@Composable
fun MahaamTopAppBar(
    isNight: Boolean,
    currentInk: String,
    onToggleTheme: () -> Unit,
    onSelectInk: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("app_logo")
            ) {
                Text(
                    text = "مهام",
                    fontFamily = FontFamily.Serif,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Text(
                    text = "Mahaam",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    color = colors.textSecondary
                )
            }

            // Controls (Theme & Inks)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Theme Toggle
                Box(
                    modifier = Modifier
                        .testTag("theme_toggle_btn")
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .clickable { onToggleTheme() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isNight) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = colors.text,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isNight) "Day" else "Night",
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            color = colors.text
                        )
                    }
                }

                // Ink selector dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    InkDot(
                        inkName = "indigo",
                        color = InkIndigo,
                        isSelected = currentInk.equals("indigo", ignoreCase = true),
                        onSelect = { onSelectInk("indigo") }
                    )
                    InkDot(
                        inkName = "crimson",
                        color = InkCrimson,
                        isSelected = currentInk.equals("crimson", ignoreCase = true),
                        onSelect = { onSelectInk("crimson") }
                    )
                    InkDot(
                        inkName = "matcha",
                        color = InkMatcha,
                        isSelected = currentInk.equals("matcha", ignoreCase = true),
                        onSelect = { onSelectInk("matcha") }
                    )
                }
            }
        }

        // Editorial subtle border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.border)
        )
    }
}

@Composable
private fun InkDot(
    inkName: String,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag("ink_btn_$inkName")
            .size(20.dp)
            .clip(CircleShape)
            .background(if (isSelected) color.copy(alpha = 0.25f) else Color.Transparent)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 12.dp else 10.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}
