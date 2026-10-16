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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.mahaam.app.data.model.MotivationalQuote
import com.mahaam.app.data.model.Streak
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.components.ChapterHeader
import com.mahaam.app.ui.components.TaskItemCard
import com.mahaam.app.ui.theme.LocalMahaamColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    tasks: List<Task>,
    streak: Streak?,
    quote: MotivationalQuote,
    onToggleTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onQuickAddTask: (title: String, quadrant: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current
    var quickTitle by remember { mutableStateOf("") }
    var quickQuadrant by remember { mutableStateOf(Task.QUADRANT_QUICK_WIN) }

    val totalCount = tasks.size
    val doneCount = tasks.count { it.done }
    val pendingCount = totalCount - doneCount
    val streakCount = streak?.count ?: 0

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    // Today's tasks (due today, overdue, created today, or top pending)
    val todayTasks = remember(tasks, todayStr) {
        val filtered = tasks.filter { task ->
            if (task.done) false
            else if (!task.dueDate.isNullOrBlank()) task.dueDate <= todayStr
            else task.createdAt.startsWith(todayStr)
        }
        if (filtered.isNotEmpty()) filtered
        else tasks.filter { !it.done }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .testTag("dashboard_screen")
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Chapter Header
        item {
            ChapterHeader(
                chapterNum = "No. 01",
                chapterArabic = "بداية",
                title = "Dashboard",
                subtitle = "your day, at a glance"
            )
        }

        // Daily Motivational Quote Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "“${quote.text}”",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = colors.text,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "— ${quote.author}",
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = colors.accent
                    )
                }
            }
        }

        // Quick Stats Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        count = totalCount.toString(),
                        label = "Total Tasks",
                        arabic = "المجموع",
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total"
                    )
                    StatCard(
                        count = doneCount.toString(),
                        label = "Completed",
                        arabic = "مكتمل",
                        modifier = Modifier.weight(1f),
                        testTag = "stat_done"
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        count = pendingCount.toString(),
                        label = "Pending",
                        arabic = "قيد الانتظار",
                        modifier = Modifier.weight(1f),
                        testTag = "stat_pending"
                    )
                    StatCard(
                        count = streakCount.toString(),
                        label = "Day Streak",
                        arabic = "أيام متتالية",
                        modifier = Modifier.weight(1f),
                        testTag = "stat_streak"
                    )
                }
            }
        }

        // Quick Add Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "أضف",
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        color = colors.accent
                    )
                    Text(
                        text = "Quick Add",
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quickTitle,
                    onValueChange = { quickTitle = it },
                    placeholder = { Text("What needs doing?", fontFamily = FontFamily.Serif, fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .testTag("quick_add_input")
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quadrant choices & Add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        QuickQuadrantChip("Quick Win", quickQuadrant == Task.QUADRANT_QUICK_WIN) {
                            quickQuadrant = Task.QUADRANT_QUICK_WIN
                        }
                        QuickQuadrantChip("Major", quickQuadrant == Task.QUADRANT_MAJOR_PROJECT) {
                            quickQuadrant = Task.QUADRANT_MAJOR_PROJECT
                        }
                        QuickQuadrantChip("Fill-in", quickQuadrant == Task.QUADRANT_FILL_IN) {
                            quickQuadrant = Task.QUADRANT_FILL_IN
                        }
                    }

                    Box(
                        modifier = Modifier
                            .testTag("quick_add_submit_btn")
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accent)
                            .clickable {
                                if (quickTitle.isNotBlank()) {
                                    onQuickAddTask(quickTitle, quickQuadrant)
                                    quickTitle = ""
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "Add",
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.bg
                        )
                    }
                }
            }
        }

        // Today's Tasks Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "اليوم",
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    color = colors.accent
                )
                Text(
                    text = "Today's Tasks",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks yet — add one above",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    TaskItemCard(
                        task = task,
                        onToggle = onToggleTask,
                        onEdit = onEditTask
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    count: String,
    label: String,
    arabic: String,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val colors = LocalMahaamColors.current

    Column(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        Text(
            text = count,
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = colors.accent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            color = colors.text
        )
        Text(
            text = arabic,
            fontFamily = FontFamily.Serif,
            fontSize = 11.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun QuickQuadrantChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = LocalMahaamColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) colors.accentFaint else colors.bgSecondary)
            .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontSize = 11.sp,
            color = if (isSelected) colors.accent else colors.text
        )
    }
}
