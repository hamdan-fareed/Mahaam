package com.mahaam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.PomoStats
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.components.ChapterHeader
import com.mahaam.app.ui.theme.LocalMahaamColors

@Composable
fun FocusScreen(
    timerRunning: Boolean,
    timerMode: String,
    timerSeconds: Int,
    timerSessionCount: Int,
    tasks: List<Task>,
    selectedTaskId: String?,
    pomoStats: PomoStats?,
    settings: AppSettings,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectTask: (String?) -> Unit,
    onOpenSettingsDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    val mins = timerSeconds / 60
    val secs = timerSeconds % 60
    val timeFormatted = "%02d:%02d".format(mins, secs)

    val modeTitle = when (timerMode) {
        "short-break" -> "Short Break"
        "long-break" -> "Long Break"
        else -> "Work Session"
    }

    val pendingTasks = remember(tasks) { tasks.filter { !it.done } }
    val selectedTask = remember(tasks, selectedTaskId) {
        tasks.find { it.id == selectedTaskId }
    }

    var isTaskMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .testTag("focus_screen")
            .fillMaxSize()
            .background(colors.bg),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ChapterHeader(
                chapterNum = "No. 04",
                chapterArabic = "تركيز",
                title = "Focus",
                subtitle = "deep work, deliberate rest"
            )
        }

        // Timer Dial / Display Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode indicator tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accentFaint)
                        .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = modeTitle,
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Circular Frame for Time
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(colors.bgSecondary)
                        .border(2.dp, if (timerRunning) colors.accent else colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeFormatted,
                        fontFamily = FontFamily.Serif,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.text,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Focused task selector dropdown
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                        .clickable { isTaskMenuExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (selectedTask != null) "Focusing on: ${selectedTask.title}" else "— Select a task to focus on —",
                        fontFamily = FontFamily.Serif,
                        fontStyle = if (selectedTask == null) FontStyle.Italic else FontStyle.Normal,
                        fontSize = 13.sp,
                        color = if (selectedTask != null) colors.text else colors.textSecondary,
                        maxLines = 1
                    )

                    DropdownMenu(
                        expanded = isTaskMenuExpanded,
                        onDismissRequest = { isTaskMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Free Focus)", fontFamily = FontFamily.Serif) },
                            onClick = {
                                onSelectTask(null)
                                isTaskMenuExpanded = false
                            }
                        )
                        pendingTasks.forEach { task ->
                            DropdownMenuItem(
                                text = { Text(task.title, fontFamily = FontFamily.Serif) },
                                onClick = {
                                    onSelectTask(task.id)
                                    isTaskMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control Buttons: Start/Pause/Resume & Reset
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    Box(
                        modifier = Modifier
                            .testTag("timer_reset_btn")
                            .clip(CircleShape)
                            .background(colors.bgSecondary)
                            .border(1.dp, colors.border, CircleShape)
                            .clickable { onResetTimer() }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Reset Timer",
                            tint = colors.text,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main Start/Pause Button
                    Box(
                        modifier = Modifier
                            .testTag("timer_toggle_btn")
                            .clip(RoundedCornerShape(26.dp))
                            .background(colors.accent)
                            .clickable {
                                if (timerRunning) onPauseTimer() else onStartTimer()
                            }
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (timerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (timerRunning) "Pause" else "Start",
                                tint = colors.bg,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (timerRunning) "Pause" else "Start",
                                fontFamily = FontFamily.Serif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.bg
                            )
                        }
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .testTag("timer_settings_btn")
                            .clip(CircleShape)
                            .background(colors.bgSecondary)
                            .border(1.dp, colors.border, CircleShape)
                            .clickable { onOpenSettingsDialog() }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Timer Settings",
                            tint = colors.text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Focus Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FocusStatCard(
                    title = "${pomoStats?.todaySessions ?: 0}",
                    subtitle = "Today's Sessions",
                    arabic = "جلسات اليوم",
                    modifier = Modifier.weight(1f)
                )
                FocusStatCard(
                    title = "${pomoStats?.totalSessions ?: 0}",
                    subtitle = "Total Sessions",
                    arabic = "المجموع",
                    modifier = Modifier.weight(1f)
                )
                FocusStatCard(
                    title = "$timerSessionCount / ${settings.targetSessions}",
                    subtitle = "Session Cycle",
                    arabic = "دورة العمل",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FocusStatCard(
    title: String,
    subtitle: String,
    arabic: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = colors.accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontFamily = FontFamily.Serif,
            fontSize = 11.sp,
            color = colors.text
        )
        Text(
            text = arabic,
            fontFamily = FontFamily.Serif,
            fontSize = 10.sp,
            color = colors.textSecondary
        )
    }
}
