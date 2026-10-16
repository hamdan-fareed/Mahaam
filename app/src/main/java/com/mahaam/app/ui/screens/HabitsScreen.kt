package com.mahaam.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.HabitEntry
import com.mahaam.app.ui.components.ChapterHeader
import com.mahaam.app.ui.theme.InkCrimson
import com.mahaam.app.ui.theme.InkIndigo
import com.mahaam.app.ui.theme.InkMatcha
import com.mahaam.app.ui.theme.LocalMahaamColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    entries: List<HabitEntry>,
    onQuickLog: (String) -> Unit,
    onOpenLogDialog: (Habit) -> Unit,
    onOpenEditDialog: (Habit) -> Unit,
    onToggleExpanded: (String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onOpenAddHabitDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    LazyColumn(
        modifier = modifier
            .testTag("habits_screen")
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ChapterHeader(
                chapterNum = "No. 05",
                chapterArabic = "عادات",
                title = "Habits",
                subtitle = "small deeds, quiet discipline"
            )
        }

        // New Habit Action Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .testTag("open_add_habit_dialog_btn")
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.accent)
                        .clickable { onOpenAddHabitDialog() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Habit",
                            tint = colors.bg,
                            modifier = Modifier.padding(1.dp)
                        )
                        Text(
                            text = "New Habit",
                            fontFamily = FontFamily.Serif,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.bg
                        )
                    }
                }
            }
        }

        // Habits List
        if (habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No habits tracked yet — create your first habit above",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                val habitEntries = remember(entries, habit.id) {
                    entries.filter { it.habitId == habit.id }
                }
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    HabitCard(
                        habit = habit,
                        entries = habitEntries,
                        onQuickLog = { onQuickLog(habit.id) },
                        onOpenLog = { onOpenLogDialog(habit) },
                        onOpenEdit = { onOpenEditDialog(habit) },
                        onToggleExpanded = { onToggleExpanded(habit.id) },
                        onDeleteEntry = onDeleteEntry
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    entries: List<HabitEntry>,
    onQuickLog: () -> Unit,
    onOpenLog: () -> Unit,
    onOpenEdit: () -> Unit,
    onToggleExpanded: () -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    val colors = LocalMahaamColors.current

    val baseHabitColor = when (habit.baseColor) {
        Habit.COLOR_CRIMSON -> InkCrimson
        Habit.COLOR_MATCHA -> InkMatcha
        else -> InkIndigo
    }

    val isDuration = habit.shadingMode == Habit.MODE_DURATION
    val unitLabel = if (isDuration) "min" else "×"
    val modeText = if (isDuration) "Duration (min)" else "Count (×)"

    // Compute last 30 days daily aggregates
    val last30Days = remember { getLast30DayStrings() }
    val aggregates = remember(entries, last30Days, isDuration) {
        last30Days.map { dateStr ->
            val dayEntries = entries.filter { it.date == dateStr }
            val sum = if (isDuration) {
                dayEntries.sumOf { it.duration }
            } else {
                dayEntries.sumOf { it.count }
            }
            dateStr to sum
        }
    }

    val peakValue = remember(aggregates) {
        aggregates.maxOfOrNull { it.second } ?: 0
    }
    val total30Val = remember(aggregates) {
        aggregates.sumOf { it.second }
    }

    Column(
        modifier = Modifier
            .testTag("habit_card_${habit.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(baseHabitColor)
                )
                Text(
                    text = habit.label,
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.bgSecondary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = modeText,
                        fontFamily = FontFamily.Serif,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HabitActionButton("+ Quick", isPrimary = true) { onQuickLog() }
                HabitActionButton("+ Log") { onOpenLog() }
                HabitActionButton("Edit") { onOpenEdit() }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 30-Day Activity Strip Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isDuration) "Last 30d: $total30Val min · Peak: $peakValue min" else "Last 30d: $total30Val× · Peak: $peakValue×",
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (habit.expanded) "▲ Hide" else "▼ History (${entries.size})",
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        color = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 30-Day Grid Blocks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                aggregates.forEach { (dateStr, value) ->
                    val intensity = if (peakValue > 0) (value.toFloat() / peakValue) else 0f
                    val blockColor = if (value > 0) {
                        baseHabitColor.copy(alpha = (0.25f + intensity * 0.75f).coerceIn(0.25f, 1f))
                    } else {
                        colors.bgSecondary
                    }

                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(blockColor)
                            .border(
                                1.dp,
                                if (value > 0) baseHabitColor.copy(alpha = 0.5f) else colors.border,
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30 days ago", fontFamily = FontFamily.Serif, fontSize = 10.sp, color = colors.textSecondary)
                Text("Today", fontFamily = FontFamily.Serif, fontSize = 10.sp, color = colors.textSecondary)
            }
        }

        // Expanded History Panel
        if (habit.expanded) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.border)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Full History Log · ${entries.size} entries",
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (entries.isEmpty()) {
                Text(
                    text = "No entries recorded yet. Tap + Quick or + Log above to add one.",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    entries.sortedByDescending { it.timestamp }.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.bgSecondary)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = formatShortDate(entry.date),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.text
                                )
                                Text(
                                    text = entry.time,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(baseHabitColor.copy(alpha = 0.15f))
                                        .border(1.dp, baseHabitColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isDuration) "${entry.duration} min" else "${entry.count}×",
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 11.sp,
                                        color = baseHabitColor
                                    )
                                }
                                if (entry.note.isNotBlank()) {
                                    Text(
                                        text = "“${entry.note}”",
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteEntry(entry.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete entry",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitActionButton(label: String, isPrimary: Boolean = false, onClick: () -> Unit) {
    val colors = LocalMahaamColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPrimary) colors.accentFaint else colors.bgSecondary)
            .border(1.dp, if (isPrimary) colors.accent else colors.border, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontSize = 11.sp,
            color = if (isPrimary) colors.accent else colors.text
        )
    }
}

private fun getLast30DayStrings(): List<String> {
    val list = mutableListOf<String>()
    val cal = Calendar.getInstance()
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    for (i in 29 downTo 0) {
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, -i)
        list.add(fmt.format(c.time))
    }
    return list
}

private fun formatShortDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = parser.parse(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("MMM d", Locale.US)
        formatter.format(d)
    } catch (_: Exception) {
        dateStr
    }
}
