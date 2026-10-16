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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.theme.LocalMahaamColors
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TaskItemCard(
    task: Task,
    onToggle: (String) -> Unit,
    onEdit: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current

    Row(
        modifier = modifier
            .testTag("task_item_${task.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .clickable { onEdit(task) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkbox button
        Box(
            modifier = Modifier
                .testTag("task_checkbox_${task.id}")
                .size(24.dp)
                .clip(CircleShape)
                .background(if (task.done) colors.accent else colors.bgSecondary)
                .border(1.dp, if (task.done) colors.accent else colors.border, CircleShape)
                .clickable { onToggle(task.id) },
            contentAlignment = Alignment.Center
        ) {
            if (task.done) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = colors.bg,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Task content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = task.title,
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = if (task.done) colors.textSecondary else colors.text,
                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Meta tags (Quadrant + Priority + Due Date)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = Task.getQuadrantName(task.quadrant),
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = colors.accent
                )
                Text(
                    text = "·",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = Task.getPriorityName(task.priority),
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                task.dueDate?.let { due ->
                    Text(
                        text = "·",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "Due ${formatDisplayDate(due)}",
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (task.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = task.notes,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 2
                )
            }
        }

        // Edit button
        Box(
            modifier = Modifier
                .testTag("task_edit_btn_${task.id}")
                .clip(RoundedCornerShape(4.dp))
                .background(colors.bgSecondary)
                .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                .clickable { onEdit(task) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Edit",
                fontFamily = FontFamily.Serif,
                fontSize = 12.sp,
                color = colors.text
            )
        }
    }
}

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = parser.parse(dateStr) ?: return dateStr
        val formatter = SimpleDateFormat("MMM d", Locale.US)
        formatter.format(d)
    } catch (_: Exception) {
        dateStr
    }
}
