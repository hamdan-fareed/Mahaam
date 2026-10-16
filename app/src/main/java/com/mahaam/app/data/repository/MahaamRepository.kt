package com.mahaam.app.data.repository

import com.mahaam.app.data.local.MahaamDao
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.DailyLog
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.HabitEntry
import com.mahaam.app.data.model.PomoStats
import com.mahaam.app.data.model.Streak
import com.mahaam.app.data.model.Task
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MahaamRepository(private val dao: MahaamDao) {

    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    val allHabits: Flow<List<Habit>> = dao.getAllHabits()
    val allHabitEntries: Flow<List<HabitEntry>> = dao.getAllHabitEntries()
    val pomoStats: Flow<PomoStats?> = dao.getPomoStats()
    val dailyLogs: Flow<List<DailyLog>> = dao.getDailyLogs()
    val streak: Flow<Streak?> = dao.getStreak()
    val settings: Flow<AppSettings?> = dao.getSettings()

    fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("HH:mm", Locale.US).format(Date())
    }

    private fun getCurrentIsoTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
    }

    private fun getDaysDifference(dateStr1: String, dateStr2: String): Int {
        if (dateStr1.isBlank() || dateStr2.isBlank()) return 0
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d1 = format.parse(dateStr1) ?: return 0
            val d2 = format.parse(dateStr2) ?: return 0
            val diffMs = d2.time - d1.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    // --- Startup checks ---
    suspend fun checkStartupState() {
        val today = getTodayString()

        // 1. Settings initial
        var s = dao.getSettingsDirect()
        if (s == null) {
            s = AppSettings()
            dao.insertOrUpdateSettings(s)
        }

        // 2. Streak check
        var curStreak = dao.getStreakDirect()
        if (curStreak == null) {
            curStreak = Streak(id = 1, count = 0, lastDate = "")
            dao.insertOrUpdateStreak(curStreak)
        } else if (curStreak.lastDate.isNotBlank()) {
            val diff = getDaysDifference(curStreak.lastDate, today)
            if (diff > 1) {
                dao.insertOrUpdateStreak(curStreak.copy(count = 0))
            }
        }

        // 3. Pomo check
        var curPomo = dao.getPomoStatsDirect()
        if (curPomo == null) {
            curPomo = PomoStats(id = 1, todayDate = today)
            dao.insertOrUpdatePomoStats(curPomo)
        } else if (curPomo.todayDate != today) {
            dao.insertOrUpdatePomoStats(
                curPomo.copy(
                    todayDate = today,
                    todaySessions = 0,
                    todayMinutes = 0
                )
            )
        }
    }

    // --- Task operations ---
    suspend fun addTask(
        title: String,
        quadrant: String,
        priority: String,
        notes: String = "",
        dueDate: String? = null
    ): Task? {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return null

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = trimmed,
            notes = notes.trim(),
            quadrant = quadrant,
            priority = priority,
            done = false,
            dueDate = dueDate?.takeIf { it.isNotBlank() },
            createdAt = getCurrentIsoTimestamp()
        )
        dao.insertTask(task)
        return task
    }

    suspend fun toggleTask(id: String) {
        val task = dao.getTaskById(id) ?: return
        val newDone = !task.done
        val today = getTodayString()

        val updatedTask = if (newDone) {
            task.copy(done = true, completedAt = getCurrentIsoTimestamp())
        } else {
            task.copy(done = false, completedAt = null)
        }
        dao.updateTask(updatedTask)

        // Update DailyLog
        val currentLog = dao.getDailyLogForDate(today)
        if (newDone) {
            val newCount = (currentLog?.completedCount ?: 0) + 1
            dao.insertOrUpdateDailyLog(DailyLog(date = today, completedCount = newCount))
            recordStreakOnCompletion()
        } else {
            if (currentLog != null && currentLog.completedCount > 0) {
                val newCount = currentLog.completedCount - 1
                if (newCount <= 0) {
                    dao.deleteDailyLog(today)
                } else {
                    dao.insertOrUpdateDailyLog(currentLog.copy(completedCount = newCount))
                }
            }
        }
    }

    private suspend fun recordStreakOnCompletion() {
        val today = getTodayString()
        val curStreak = dao.getStreakDirect() ?: Streak()
        if (curStreak.lastDate == today) {
            // Already credited today
            return
        }
        val newCount = if (curStreak.lastDate.isBlank()) {
            1
        } else {
            val diff = getDaysDifference(curStreak.lastDate, today)
            if (diff == 1) {
                curStreak.count + 1
            } else if (diff == 0) {
                curStreak.count
            } else {
                1
            }
        }
        dao.insertOrUpdateStreak(Streak(id = 1, count = newCount, lastDate = today))
    }

    suspend fun editTask(
        id: String,
        title: String,
        quadrant: String,
        priority: String,
        notes: String,
        dueDate: String?
    ) {
        val task = dao.getTaskById(id) ?: return
        val updated = task.copy(
            title = title.trim().ifEmpty { task.title },
            quadrant = quadrant,
            priority = priority,
            notes = notes.trim(),
            dueDate = dueDate?.takeIf { it.isNotBlank() }
        )
        dao.updateTask(updated)
    }

    suspend fun moveTaskQuadrant(id: String, newQuadrant: String) {
        val task = dao.getTaskById(id) ?: return
        if (task.quadrant != newQuadrant) {
            dao.updateTask(task.copy(quadrant = newQuadrant))
        }
    }

    suspend fun deleteTask(id: String) {
        dao.deleteTaskById(id)
    }

    suspend fun clearCompletedTasks() {
        dao.deleteCompletedTasks()
    }

    suspend fun exportTasksJson(tasks: List<Task>): String {
        val array = JSONArray()
        for (task in tasks) {
            val obj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("notes", task.notes)
                put("quadrant", task.quadrant)
                put("priority", task.priority)
                put("done", task.done)
                put("dueDate", task.dueDate ?: JSONObject.NULL)
                put("createdAt", task.createdAt)
                put("completedAt", task.completedAt ?: JSONObject.NULL)
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    // --- Habit operations ---
    suspend fun addHabit(label: String, color: String = Habit.COLOR_INDIGO, mode: String = Habit.MODE_COUNT): Habit? {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return null

        val habit = Habit(
            id = UUID.randomUUID().toString(),
            label = trimmed,
            baseColor = color,
            shadingMode = mode,
            createdAt = getTodayString(),
            expanded = false
        )
        dao.insertHabit(habit)
        return habit
    }

    suspend fun editHabit(id: String, label: String, color: String, mode: String) {
        val habit = dao.getHabitById(id) ?: return
        dao.updateHabit(
            habit.copy(
                label = label.trim().ifEmpty { habit.label },
                baseColor = color,
                shadingMode = mode
            )
        )
    }

    suspend fun deleteHabit(id: String) {
        dao.deleteHabitById(id)
        dao.deleteEntriesForHabit(id)
    }

    suspend fun toggleHabitExpanded(id: String) {
        val habit = dao.getHabitById(id) ?: return
        dao.updateHabit(habit.copy(expanded = !habit.expanded))
    }

    suspend fun quickLogHabit(habitId: String) {
        val habit = dao.getHabitById(habitId) ?: return
        val entry = HabitEntry(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            timestamp = getCurrentIsoTimestamp(),
            date = getTodayString(),
            time = getCurrentTimeString(),
            duration = if (habit.shadingMode == Habit.MODE_DURATION) 25 else 25,
            count = if (habit.shadingMode == Habit.MODE_COUNT) 1 else 1,
            note = ""
        )
        dao.insertHabitEntry(entry)
    }

    suspend fun logHabitEntry(
        habitId: String,
        date: String,
        time: String,
        value: Int,
        note: String
    ) {
        val habit = dao.getHabitById(habitId) ?: return
        val entry = HabitEntry(
            id = UUID.randomUUID().toString(),
            habitId = habitId,
            timestamp = "${date}T${time}:00Z",
            date = date.ifBlank { getTodayString() },
            time = time.ifBlank { "12:00" },
            duration = if (habit.shadingMode == Habit.MODE_DURATION) value.coerceAtLeast(1) else 25,
            count = if (habit.shadingMode == Habit.MODE_COUNT) value.coerceAtLeast(1) else 1,
            note = note.trim()
        )
        dao.insertHabitEntry(entry)
    }

    suspend fun deleteHabitEntry(id: String) {
        dao.deleteHabitEntryById(id)
    }

    // --- Pomodoro operations ---
    suspend fun recordPomoSession(workMinutes: Int) {
        val today = getTodayString()
        val curPomo = dao.getPomoStatsDirect() ?: PomoStats(id = 1, todayDate = today)
        val isSameDay = curPomo.todayDate == today

        val updated = curPomo.copy(
            totalSessions = curPomo.totalSessions + 1,
            todaySessions = if (isSameDay) curPomo.todaySessions + 1 else 1,
            todayDate = today,
            totalMinutes = curPomo.totalMinutes + workMinutes,
            todayMinutes = if (isSameDay) curPomo.todayMinutes + workMinutes else workMinutes
        )
        dao.insertOrUpdatePomoStats(updated)
    }

    // --- Settings operations ---
    suspend fun updateTheme(theme: String) {
        val s = dao.getSettingsDirect() ?: AppSettings()
        dao.insertOrUpdateSettings(s.copy(theme = theme))
    }

    suspend fun updateInk(ink: String) {
        val s = dao.getSettingsDirect() ?: AppSettings()
        dao.insertOrUpdateSettings(s.copy(ink = ink))
    }

    suspend fun updateTimerSettings(work: Int, shortBreak: Int, longBreak: Int, sessions: Int) {
        val s = dao.getSettingsDirect() ?: AppSettings()
        dao.insertOrUpdateSettings(
            s.copy(
                workMinutes = work.coerceAtLeast(1),
                shortBreakMinutes = shortBreak.coerceAtLeast(1),
                longBreakMinutes = longBreak.coerceAtLeast(1),
                targetSessions = sessions.coerceAtLeast(1)
            )
        )
    }
}
