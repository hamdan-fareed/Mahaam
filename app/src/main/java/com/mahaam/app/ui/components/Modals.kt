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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.theme.InkCrimson
import com.mahaam.app.ui.theme.InkIndigo
import com.mahaam.app.ui.theme.InkMatcha
import com.mahaam.app.ui.theme.LocalMahaamColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (id: String, title: String, quadrant: String, priority: String, notes: String, dueDate: String?) -> Unit,
    onDelete: (id: String) -> Unit
) {
    val colors = LocalMahaamColors.current
    var title by remember { mutableStateOf(task.title) }
    var notes by remember { mutableStateOf(task.notes) }
    var quadrant by remember { mutableStateOf(task.quadrant) }
    var priority by remember { mutableStateOf(task.priority) }
    var dueDate by remember { mutableStateOf(task.dueDate ?: "") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .testTag("edit_task_dialog")
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Edit Task",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            EditorialTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                testTag = "edit_task_title_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quadrant picker
            Text(
                text = "Quadrant",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            QuadrantSelector(
                selected = quadrant,
                onSelect = { quadrant = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Priority picker
            Text(
                text = "Priority",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            PrioritySelector(
                selected = priority,
                onSelect = { priority = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Due Date
            EditorialTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = "Due Date (YYYY-MM-DD)",
                testTag = "edit_task_due_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Notes
            EditorialTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                testTag = "edit_task_notes_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button
                Box(
                    modifier = Modifier
                        .testTag("delete_task_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(InkCrimson.copy(alpha = 0.12f))
                        .border(1.dp, InkCrimson.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .clickable { onDelete(task.id) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        color = InkCrimson
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.bgSecondary)
                            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .testTag("save_task_btn")
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accent)
                            .clickable {
                                onSave(task.id, title, quadrant, priority, notes, dueDate.ifBlank { null })
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            color = colors.bg
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    initialQuadrant: String = Task.QUADRANT_QUICK_WIN,
    onDismiss: () -> Unit,
    onAdd: (title: String, quadrant: String, priority: String, notes: String, dueDate: String?) -> Unit
) {
    val colors = LocalMahaamColors.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var quadrant by remember { mutableStateOf(initialQuadrant) }
    var priority by remember { mutableStateOf(Task.PRIORITY_MEDIUM) }
    var dueDate by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .testTag("add_task_dialog")
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "New Task",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditorialTextField(
                value = title,
                onValueChange = { title = it },
                label = "What needs doing?",
                testTag = "add_task_title_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Quadrant",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            QuadrantSelector(
                selected = quadrant,
                onSelect = { quadrant = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Priority",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            PrioritySelector(
                selected = priority,
                onSelect = { priority = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = "Due Date (YYYY-MM-DD)",
                testTag = "add_task_due_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                singleLine = false,
                testTag = "add_task_notes_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .testTag("submit_add_task_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent)
                        .clickable {
                            if (title.isNotBlank()) {
                                onAdd(title, quadrant, priority, notes, dueDate.ifBlank { null })
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Add Task",
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp,
                        color = colors.bg
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogHabitEntryDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (date: String, time: String, value: Int, note: String) -> Unit
) {
    val colors = LocalMahaamColors.current
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val nowTime = remember { SimpleDateFormat("HH:mm", Locale.US).format(Date()) }

    var date by remember { mutableStateOf(today) }
    var time by remember { mutableStateOf(nowTime) }
    var valueStr by remember {
        mutableStateOf(if (habit.shadingMode == Habit.MODE_DURATION) "30" else "1")
    }
    var note by remember { mutableStateOf("") }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .testTag("log_habit_entry_dialog")
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Log Habit Activity",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Text(
                text = habit.label,
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditorialTextField(
                value = date,
                onValueChange = { date = it },
                label = "Date (YYYY-MM-DD)",
                testTag = "log_habit_date_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = time,
                onValueChange = { time = it },
                label = "Time (HH:mm)",
                testTag = "log_habit_time_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = valueStr,
                onValueChange = { valueStr = it },
                label = if (habit.shadingMode == Habit.MODE_DURATION) "Duration (minutes)" else "Count (×)",
                testTag = "log_habit_value_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = note,
                onValueChange = { note = it },
                label = "Note (optional)",
                singleLine = false,
                testTag = "log_habit_note_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.textSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .testTag("submit_log_habit_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent)
                        .clickable {
                            val v = valueStr.toIntOrNull() ?: 1
                            onSave(date, time, v, note)
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Record Entry", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.bg)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (label: String, color: String, mode: String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalMahaamColors.current
    var label by remember { mutableStateOf(habit.label) }
    var color by remember { mutableStateOf(habit.baseColor) }
    var mode by remember { mutableStateOf(habit.shadingMode) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .testTag("edit_habit_dialog")
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(22.dp)
        ) {
            Text(
                text = "Edit Habit",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditorialTextField(
                value = label,
                onValueChange = { label = it },
                label = "Habit Name",
                testTag = "edit_habit_name_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Color Swatch", fontFamily = FontFamily.Serif, fontSize = 12.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitColorSwatch(colorName = Habit.COLOR_INDIGO, sampleColor = InkIndigo, isSelected = color == Habit.COLOR_INDIGO) { color = Habit.COLOR_INDIGO }
                HabitColorSwatch(colorName = Habit.COLOR_CRIMSON, sampleColor = InkCrimson, isSelected = color == Habit.COLOR_CRIMSON) { color = Habit.COLOR_CRIMSON }
                HabitColorSwatch(colorName = Habit.COLOR_MATCHA, sampleColor = InkMatcha, isSelected = color == Habit.COLOR_MATCHA) { color = Habit.COLOR_MATCHA }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Tracking Mode", fontFamily = FontFamily.Serif, fontSize = 12.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeChip("Count (×)", isSelected = mode == Habit.MODE_COUNT) { mode = Habit.MODE_COUNT }
                ModeChip("Duration (min)", isSelected = mode == Habit.MODE_DURATION) { mode = Habit.MODE_DURATION }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .testTag("delete_habit_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(InkCrimson.copy(alpha = 0.12f))
                        .border(1.dp, InkCrimson.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .clickable { onDelete() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Delete", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = InkCrimson)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.bgSecondary)
                            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Cancel", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.textSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .testTag("save_habit_btn")
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accent)
                            .clickable {
                                if (label.isNotBlank()) onSave(label, color, mode)
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text("Save", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.bg)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (label: String, color: String, mode: String) -> Unit
) {
    val colors = LocalMahaamColors.current
    var label by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(Habit.COLOR_INDIGO) }
    var mode by remember { mutableStateOf(Habit.MODE_COUNT) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .testTag("add_habit_dialog")
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(22.dp)
        ) {
            Text(
                text = "New Habit",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditorialTextField(
                value = label,
                onValueChange = { label = it },
                label = "Habit Name (e.g. Deep Reading)",
                testTag = "add_habit_name_input"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Color Swatch", fontFamily = FontFamily.Serif, fontSize = 12.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitColorSwatch(colorName = Habit.COLOR_INDIGO, sampleColor = InkIndigo, isSelected = color == Habit.COLOR_INDIGO) { color = Habit.COLOR_INDIGO }
                HabitColorSwatch(colorName = Habit.COLOR_CRIMSON, sampleColor = InkCrimson, isSelected = color == Habit.COLOR_CRIMSON) { color = Habit.COLOR_CRIMSON }
                HabitColorSwatch(colorName = Habit.COLOR_MATCHA, sampleColor = InkMatcha, isSelected = color == Habit.COLOR_MATCHA) { color = Habit.COLOR_MATCHA }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Tracking Mode", fontFamily = FontFamily.Serif, fontSize = 12.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeChip("Count (×)", isSelected = mode == Habit.MODE_COUNT) { mode = Habit.MODE_COUNT }
                ModeChip("Duration (min)", isSelected = mode == Habit.MODE_DURATION) { mode = Habit.MODE_DURATION }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.textSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .testTag("submit_add_habit_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent)
                        .clickable {
                            if (label.isNotBlank()) onAdd(label, color, mode)
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Create Habit", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.bg)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (work: Int, shortBreak: Int, longBreak: Int, sessions: Int) -> Unit
) {
    val colors = LocalMahaamColors.current
    var work by remember { mutableIntStateOf(settings.workMinutes) }
    var shortBreak by remember { mutableIntStateOf(settings.shortBreakMinutes) }
    var longBreak by remember { mutableIntStateOf(settings.longBreakMinutes) }
    var sessions by remember { mutableIntStateOf(settings.targetSessions) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .testTag("timer_settings_dialog")
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(22.dp)
        ) {
            Text(
                text = "Timer Durations",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(14.dp))

            EditorialTextField(
                value = work.toString(),
                onValueChange = { work = it.toIntOrNull() ?: 25 },
                label = "Work Session (minutes)",
                testTag = "setting_work_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = shortBreak.toString(),
                onValueChange = { shortBreak = it.toIntOrNull() ?: 5 },
                label = "Short Break (minutes)",
                testTag = "setting_short_break_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = longBreak.toString(),
                onValueChange = { longBreak = it.toIntOrNull() ?: 15 },
                label = "Long Break (minutes)",
                testTag = "setting_long_break_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditorialTextField(
                value = sessions.toString(),
                onValueChange = { sessions = it.toIntOrNull() ?: 4 },
                label = "Sessions before Long Break",
                testTag = "setting_sessions_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.bgSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Cancel", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.textSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .testTag("save_timer_settings_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent)
                        .clickable {
                            onSave(work, shortBreak, longBreak, sessions)
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("Save Settings", fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.bg)
                }
            }
        }
    }
}

@Composable
private fun EditorialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    testTag: String = ""
) {
    val colors = LocalMahaamColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = FontFamily.Serif, fontSize = 13.sp) },
        singleLine = singleLine,
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.textSecondary,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text
        )
    )
}

@Composable
private fun QuadrantSelector(selected: String, onSelect: (String) -> Unit) {
    val quadrants = listOf(
        Task.QUADRANT_QUICK_WIN to "Quick Win",
        Task.QUADRANT_MAJOR_PROJECT to "Major Project",
        Task.QUADRANT_FILL_IN to "Fill-in",
        Task.QUADRANT_THANKLESS to "Thankless Task"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeChip(quadrants[0].second, selected == quadrants[0].first) { onSelect(quadrants[0].first) }
            ModeChip(quadrants[1].second, selected == quadrants[1].first) { onSelect(quadrants[1].first) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeChip(quadrants[2].second, selected == quadrants[2].first) { onSelect(quadrants[2].first) }
            ModeChip(quadrants[3].second, selected == quadrants[3].first) { onSelect(quadrants[3].first) }
        }
    }
}

@Composable
private fun PrioritySelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModeChip("High", selected == Task.PRIORITY_HIGH) { onSelect(Task.PRIORITY_HIGH) }
        ModeChip("Medium", selected == Task.PRIORITY_MEDIUM) { onSelect(Task.PRIORITY_MEDIUM) }
        ModeChip("Low", selected == Task.PRIORITY_LOW) { onSelect(Task.PRIORITY_LOW) }
    }
}

@Composable
private fun ModeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = LocalMahaamColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) colors.accentFaint else colors.bgSecondary)
            .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontSize = 12.sp,
            color = if (isSelected) colors.accent else colors.text
        )
    }
}

@Composable
private fun HabitColorSwatch(
    colorName: String,
    sampleColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isSelected) sampleColor.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 20.dp else 16.dp)
                .clip(CircleShape)
                .background(sampleColor)
        )
    }
}
