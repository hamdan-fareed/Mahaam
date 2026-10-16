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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.components.ChapterHeader
import com.mahaam.app.ui.theme.LocalMahaamColors

@Composable
fun MatrixScreen(
    tasks: List<Task>,
    onToggleTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onAddTaskToQuadrant: (String) -> Unit,
    onMoveTaskQuadrant: (id: String, quadrant: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    val q1Tasks = remember(tasks) { tasks.filter { it.quadrant == Task.QUADRANT_QUICK_WIN } }
    val q2Tasks = remember(tasks) { tasks.filter { it.quadrant == Task.QUADRANT_MAJOR_PROJECT } }
    val q3Tasks = remember(tasks) { tasks.filter { it.quadrant == Task.QUADRANT_FILL_IN } }
    val q4Tasks = remember(tasks) { tasks.filter { it.quadrant == Task.QUADRANT_THANKLESS } }

    LazyColumn(
        modifier = modifier
            .testTag("matrix_screen")
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ChapterHeader(
                chapterNum = "No. 02",
                chapterArabic = "نظام",
                title = "The Matrix",
                subtitle = "prioritise with clarity"
            )
        }

        // Matrix Axis Legend
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "High Impact ↑",
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        color = colors.accent
                    )
                    Text(
                        text = "Low Effort ←",
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        color = colors.accent
                    )
                }
            }
        }

        // Quadrants: Q1 (Quick Wins) and Q2 (Major Projects)
        item {
            QuadrantCard(
                title = "Quick Wins",
                arabic = "إنجاز سريع",
                desc = "High Impact · Low Effort",
                quadrantKey = Task.QUADRANT_QUICK_WIN,
                tasks = q1Tasks,
                onToggle = onToggleTask,
                onEdit = onEditTask,
                onAdd = { onAddTaskToQuadrant(Task.QUADRANT_QUICK_WIN) },
                onMove = onMoveTaskQuadrant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        item {
            QuadrantCard(
                title = "Major Projects",
                arabic = "مشاريع كبيرة",
                desc = "High Impact · High Effort",
                quadrantKey = Task.QUADRANT_MAJOR_PROJECT,
                tasks = q2Tasks,
                onToggle = onToggleTask,
                onEdit = onEditTask,
                onAdd = { onAddTaskToQuadrant(Task.QUADRANT_MAJOR_PROJECT) },
                onMove = onMoveTaskQuadrant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Axis divider
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Low Impact ↓",
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "High Effort →",
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Quadrants: Q3 (Fill-ins) and Q4 (Thankless Tasks)
        item {
            QuadrantCard(
                title = "Fill-in",
                arabic = "مهام جانبية",
                desc = "Low Impact · Low Effort",
                quadrantKey = Task.QUADRANT_FILL_IN,
                tasks = q3Tasks,
                onToggle = onToggleTask,
                onEdit = onEditTask,
                onAdd = { onAddTaskToQuadrant(Task.QUADRANT_FILL_IN) },
                onMove = onMoveTaskQuadrant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        item {
            QuadrantCard(
                title = "Thankless Task",
                arabic = "مهام شاقة",
                desc = "Low Impact · High Effort",
                quadrantKey = Task.QUADRANT_THANKLESS,
                tasks = q4Tasks,
                onToggle = onToggleTask,
                onEdit = onEditTask,
                onAdd = { onAddTaskToQuadrant(Task.QUADRANT_THANKLESS) },
                onMove = onMoveTaskQuadrant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    arabic: String,
    desc: String,
    quadrantKey: String,
    tasks: List<Task>,
    onToggle: (String) -> Unit,
    onEdit: (Task) -> Unit,
    onAdd: () -> Unit,
    onMove: (id: String, quadrant: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    Column(
        modifier = modifier
            .testTag("quadrant_card_$quadrantKey")
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        // Quadrant Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = arabic,
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = desc,
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .testTag("add_to_quadrant_${quadrantKey}_btn")
                    .clip(CircleShape)
                    .background(colors.bgSecondary)
                    .border(1.dp, colors.border, CircleShape)
                    .clickable { onAdd() }
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task to $title",
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quadrant Chips List
        if (tasks.isEmpty()) {
            Text(
                text = "No tasks in this quadrant",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tasks.forEach { task ->
                    MatrixTaskChip(
                        task = task,
                        onToggle = onToggle,
                        onEdit = onEdit,
                        onMove = onMove
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixTaskChip(
    task: Task,
    onToggle: (String) -> Unit,
    onEdit: (Task) -> Unit,
    onMove: (id: String, quadrant: String) -> Unit
) {
    val colors = LocalMahaamColors.current
    var showMoveMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .testTag("matrix_chip_${task.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (task.done) colors.bgSecondary.copy(alpha = 0.5f) else colors.bgSecondary)
            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
            .clickable { onEdit(task) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chip Checkbox
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (task.done) colors.accent else Color.Transparent)
                .border(1.dp, if (task.done) colors.accent else colors.border, CircleShape)
                .clickable { onToggle(task.id) },
            contentAlignment = Alignment.Center
        ) {
            if (task.done) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = colors.bg,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Title
        Text(
            text = task.title,
            fontFamily = FontFamily.Serif,
            fontSize = 13.sp,
            color = if (task.done) colors.textSecondary else colors.text,
            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )

        // Move quadrant selector button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(3.dp))
                .clickable { showMoveMenu = !showMoveMenu }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Move",
                fontFamily = FontFamily.Serif,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }

    if (showMoveMenu) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (task.quadrant != Task.QUADRANT_QUICK_WIN) {
                MoveOptionChip("Q1") {
                    onMove(task.id, Task.QUADRANT_QUICK_WIN)
                    showMoveMenu = false
                }
            }
            if (task.quadrant != Task.QUADRANT_MAJOR_PROJECT) {
                MoveOptionChip("Q2") {
                    onMove(task.id, Task.QUADRANT_MAJOR_PROJECT)
                    showMoveMenu = false
                }
            }
            if (task.quadrant != Task.QUADRANT_FILL_IN) {
                MoveOptionChip("Q3") {
                    onMove(task.id, Task.QUADRANT_FILL_IN)
                    showMoveMenu = false
                }
            }
            if (task.quadrant != Task.QUADRANT_THANKLESS) {
                MoveOptionChip("Q4") {
                    onMove(task.id, Task.QUADRANT_THANKLESS)
                    showMoveMenu = false
                }
            }
        }
    }
}

@Composable
private fun MoveOptionChip(label: String, onClick: () -> Unit) {
    val colors = LocalMahaamColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(colors.accentFaint)
            .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontSize = 10.sp,
            color = colors.accent
        )
    }
}
