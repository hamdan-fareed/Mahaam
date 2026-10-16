package com.mahaam.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.components.ChapterHeader
import com.mahaam.app.ui.components.TaskItemCard
import com.mahaam.app.ui.theme.InkCrimson
import com.mahaam.app.ui.theme.LocalMahaamColors

@Composable
fun TasksScreen(
    tasks: List<Task>,
    searchQuery: String,
    filterQuadrant: String,
    filterStatus: String,
    filterPriority: String,
    onSearchChange: (String) -> Unit,
    onQuadrantChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onEditTask: (Task) -> Unit,
    onOpenAddTaskDialog: () -> Unit,
    onClearCompleted: () -> Unit,
    onExportTasks: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current
    val context = LocalContext.current

    val filteredTasks = remember(tasks, searchQuery, filterQuadrant, filterStatus, filterPriority) {
        val q = searchQuery.trim().lowercase()
        val list = tasks.filter { task ->
            if (filterQuadrant != "all" && task.quadrant != filterQuadrant) return@filter false
            if (filterStatus == "pending" && task.done) return@filter false
            if (filterStatus == "done" && !task.done) return@filter false
            if (filterPriority != "all" && task.priority != filterPriority) return@filter false
            if (q.isNotEmpty()) {
                val matchesTitle = task.title.lowercase().contains(q)
                val matchesNotes = task.notes.lowercase().contains(q)
                if (!matchesTitle && !matchesNotes) return@filter false
            }
            true
        }

        val priorityRank = mapOf(Task.PRIORITY_HIGH to 1, Task.PRIORITY_MEDIUM to 2, Task.PRIORITY_LOW to 3)

        list.sortedWith(
            compareBy<Task> { it.done }
                .thenBy { if (!it.done) priorityRank[it.priority] ?: 2 else 0 }
                .thenByDescending { if (!it.done) it.createdAt else (it.completedAt ?: it.createdAt) }
        )
    }

    val doneCount = tasks.count { it.done }

    LazyColumn(
        modifier = modifier
            .testTag("tasks_screen")
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ChapterHeader(
                chapterNum = "No. 03",
                chapterArabic = "قائمة",
                title = "Tasks",
                subtitle = "the master catalogue"
            )
        }

        // Search bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search tasks...", fontFamily = FontFamily.Serif, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.textSecondary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .testTag("task_search_input")
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    )
                )
            }
        }

        // Filters Horizontal Scroll Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status filters
                FilterChip("All", filterStatus == "all") { onStatusChange("all") }
                FilterChip("Pending", filterStatus == "pending") { onStatusChange("pending") }
                FilterChip("Completed", filterStatus == "done") { onStatusChange("done") }

                Spacer(modifier = Modifier.width(6.dp))

                // Priority filters
                FilterChip("High Prio", filterPriority == Task.PRIORITY_HIGH) {
                    onPriorityChange(if (filterPriority == Task.PRIORITY_HIGH) "all" else Task.PRIORITY_HIGH)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Quadrant filters
                FilterChip("Quick Wins", filterQuadrant == Task.QUADRANT_QUICK_WIN) {
                    onQuadrantChange(if (filterQuadrant == Task.QUADRANT_QUICK_WIN) "all" else Task.QUADRANT_QUICK_WIN)
                }
                FilterChip("Major", filterQuadrant == Task.QUADRANT_MAJOR_PROJECT) {
                    onQuadrantChange(if (filterQuadrant == Task.QUADRANT_MAJOR_PROJECT) "all" else Task.QUADRANT_MAJOR_PROJECT)
                }
            }
        }

        // Actions Bar: Add Task + Bulk Actions (Clear Done, Export)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Add Task Button
                Box(
                    modifier = Modifier
                        .testTag("open_add_task_dialog_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent)
                        .clickable { onOpenAddTaskDialog() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = colors.bg,
                            modifier = Modifier.padding(1.dp)
                        )
                        Text(
                            text = "New Task",
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.bg
                        )
                    }
                }

                // Bulk actions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (doneCount > 0) {
                        Box(
                            modifier = Modifier
                                .testTag("clear_completed_btn")
                                .clip(RoundedCornerShape(4.dp))
                                .background(InkCrimson.copy(alpha = 0.12f))
                                .border(1.dp, InkCrimson.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .clickable { onClearCompleted() }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Clear Done ($doneCount)",
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp,
                                color = InkCrimson
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .testTag("export_tasks_btn")
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.bgSecondary)
                            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                            .clickable {
                                onExportTasks { jsonText ->
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, jsonText)
                                        type = "application/json"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Export Tasks JSON")
                                    context.startActivity(shareIntent)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Tasks",
                                tint = colors.text,
                                modifier = Modifier.padding(1.dp)
                            )
                            Text(
                                text = "Export",
                                fontFamily = FontFamily.Serif,
                                fontSize = 12.sp,
                                color = colors.text
                            )
                        }
                    }
                }
            }
        }

        // Tasks Count Label
        item {
            Text(
                text = "${filteredTasks.size} ${if (filteredTasks.size == 1) "task" else "tasks"}",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // Tasks List
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tasks.isEmpty()) "No tasks yet — create one above" else "No matching tasks found",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
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
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = LocalMahaamColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) colors.accentFaint else colors.surface)
            .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            color = if (isSelected) colors.accent else colors.textSecondary
        )
    }
}
