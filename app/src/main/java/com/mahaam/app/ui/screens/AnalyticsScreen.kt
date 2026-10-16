package com.mahaam.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaam.app.data.model.DailyLog
import com.mahaam.app.data.model.PomoStats
import com.mahaam.app.data.model.Streak
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.components.ChapterHeader
import com.mahaam.app.ui.theme.LocalMahaamColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    tasks: List<Task>,
    dailyLogs: List<DailyLog>,
    pomoStats: PomoStats?,
    streak: Streak?,
    modifier: Modifier = Modifier
) {
    val colors = LocalMahaamColors.current
    val streakCount = streak?.count ?: 0
    val todayPomoSessions = pomoStats?.todaySessions ?: 0

    // 1. Productivity score calculation
    val weights = mapOf(
        Task.QUADRANT_QUICK_WIN to 3.0,
        Task.QUADRANT_MAJOR_PROJECT to 4.0,
        Task.QUADRANT_FILL_IN to 1.5,
        Task.QUADRANT_THANKLESS to 1.0
    )

    var totalWeighted = 0.0
    var completedWeighted = 0.0
    for (t in tasks) {
        val w = weights[t.quadrant] ?: 2.0
        totalWeighted += w
        if (t.done) completedWeighted += w
    }

    val score = remember(totalWeighted, completedWeighted, streakCount, todayPomoSessions) {
        if (totalWeighted > 0) {
            val base = (completedWeighted / totalWeighted) * 80.0
            val streakBonus = (streakCount * 2.0).coerceAtMost(10.0)
            val focusBonus = (todayPomoSessions * 2.5).coerceAtMost(10.0)
            (base + streakBonus + focusBonus).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    val scoreExplanation = when {
        score >= 80 -> "Outstanding! You are focusing consistently on high-impact priorities."
        score >= 50 -> "Good momentum! Tackle more Major Projects and Quick Wins to increase your score."
        score > 0 -> "Complete more high-impact tasks to increase your score."
        else -> "Add tasks and complete high-impact work to build your productivity score."
    }

    // 2. Quadrant counts
    val q1Count = tasks.count { it.quadrant == Task.QUADRANT_QUICK_WIN }
    val q2Count = tasks.count { it.quadrant == Task.QUADRANT_MAJOR_PROJECT }
    val q3Count = tasks.count { it.quadrant == Task.QUADRANT_FILL_IN }
    val q4Count = tasks.count { it.quadrant == Task.QUADRANT_THANKLESS }
    val totalCount = tasks.size

    // 3. Last 7 days data for bar chart
    val last7Days = remember(dailyLogs) {
        val list = mutableListOf<Triple<String, String, Int>>() // dateStr, label, count
        val cal = Calendar.getInstance()
        val fmtDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val fmtLabel = SimpleDateFormat("EEE d", Locale.US)
        val logMap = dailyLogs.associate { it.date to it.completedCount }

        for (i in 6 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = fmtDate.format(c.time)
            val label = if (i == 0) "Today" else fmtLabel.format(c.time)
            val count = logMap[dateStr] ?: 0
            list.add(Triple(dateStr, label, count))
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .testTag("analytics_screen")
            .fillMaxSize()
            .background(colors.bg),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            ChapterHeader(
                chapterNum = "No. 06",
                chapterArabic = "تحليل",
                title = "Analytics",
                subtitle = "reflection through numbers"
            )
        }

        // Productivity Score Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Productivity Score",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Text(
                            text = "Weighted by impact & focus",
                            fontFamily = FontFamily.Serif,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }

                    Text(
                        text = "$score",
                        fontFamily = FontFamily.Serif,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = scoreExplanation,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Weekly Completion Bar Chart Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Weekly Activity",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = "Tasks completed over the last 7 days",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                WeeklyBarChart(
                    data = last7Days,
                    accentColor = colors.accent,
                    textColor = colors.text,
                    mutedColor = colors.textSecondary,
                    gridColor = colors.border.copy(alpha = 0.5f),
                    trackColor = colors.bgSecondary
                )
            }
        }

        // Quadrant Distribution Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Matrix Distribution",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = "Where your energy is categorized",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                QuadrantProgressRow(
                    label = "Quick Wins",
                    arabic = "إنجاز سريع",
                    count = q1Count,
                    total = totalCount,
                    accentColor = colors.accent
                )
                Spacer(modifier = Modifier.height(10.dp))
                QuadrantProgressRow(
                    label = "Major Projects",
                    arabic = "مشاريع كبيرة",
                    count = q2Count,
                    total = totalCount,
                    accentColor = colors.accent
                )
                Spacer(modifier = Modifier.height(10.dp))
                QuadrantProgressRow(
                    label = "Fill-in",
                    arabic = "مهام جانبية",
                    count = q3Count,
                    total = totalCount,
                    accentColor = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                QuadrantProgressRow(
                    label = "Thankless Task",
                    arabic = "مهام شاقة",
                    count = q4Count,
                    total = totalCount,
                    accentColor = colors.textSecondary
                )
            }
        }

        // Focus Overview Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Focus Dedication",
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = "Time invested in deliberate work",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${pomoStats?.todayMinutes ?: 0} min",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.accent
                        )
                        Text(
                            text = "Today's Focus",
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    Column {
                        Text(
                            text = "${pomoStats?.totalMinutes ?: 0} min",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.accent
                        )
                        Text(
                            text = "All-Time Focus",
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    Column {
                        Text(
                            text = "${pomoStats?.totalSessions ?: 0}",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.accent
                        )
                        Text(
                            text = "Total Sessions",
                            fontFamily = FontFamily.Serif,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuadrantProgressRow(
    label: String,
    arabic: String,
    count: Int,
    total: Int,
    accentColor: Color
) {
    val colors = LocalMahaamColors.current
    val fraction = if (total > 0) count.toFloat() / total else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(label, fontFamily = FontFamily.Serif, fontSize = 13.sp, color = colors.text)
                Text("·", fontSize = 13.sp, color = colors.textSecondary)
                Text(arabic, fontFamily = FontFamily.Serif, fontSize = 12.sp, color = colors.textSecondary)
            }
            Text("$count", fontFamily = FontFamily.Serif, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.text)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.bgSecondary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor)
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Triple<String, String, Int>>,
    accentColor: Color,
    textColor: Color,
    mutedColor: Color,
    gridColor: Color,
    trackColor: Color
) {
    val textMeasurer = rememberTextMeasurer()
    val maxCount = (data.maxOfOrNull { it.third } ?: 1).coerceAtLeast(3)
    val chartMax = (maxCount * 1.2f).toInt().coerceAtLeast(4)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 26f
        val paddingTop = 20f
        val chartHeight = height - paddingBottom - paddingTop

        // Baseline
        val baselineY = height - paddingBottom
        drawLine(
            color = gridColor,
            start = Offset(0f, baselineY),
            end = Offset(width, baselineY),
            strokeWidth = 1.5f
        )

        val colWidth = width / 7f
        val barWidth = (colWidth * 0.45f).coerceIn(16f, 36f)

        data.forEachIndexed { idx, item ->
            val count = item.third
            val centerX = colWidth * idx + colWidth / 2f
            val barX = centerX - barWidth / 2f
            val barH = if (count > 0) (count.toFloat() / chartMax) * chartHeight else 0f
            val barY = baselineY - barH

            // Track column
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(barX, paddingTop),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Bar fill
            if (count > 0) {
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Draw count above bar
                val countLayout = textMeasurer.measure(
                    text = count.toString(),
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                )
                drawText(
                    textLayoutResult = countLayout,
                    topLeft = Offset(centerX - countLayout.size.width / 2f, barY - countLayout.size.height - 2f)
                )
            }

            // Draw label under baseline
            val isToday = idx == 6
            val labelLayout = textMeasurer.measure(
                text = item.second,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 10.sp,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isToday) accentColor else mutedColor
                )
            )
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(centerX - labelLayout.size.width / 2f, baselineY + 4f)
            )
        }
    }
}
