package com.mahaam.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.DailyLog
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.HabitEntry
import com.mahaam.app.data.model.PomoStats
import com.mahaam.app.data.model.Streak
import com.mahaam.app.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface MahaamDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY done ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks WHERE done = 1")
    suspend fun deleteCompletedTasks()

    // Habits
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String)

    // Habit Entries
    @Query("SELECT * FROM habit_entries ORDER BY date ASC, time ASC")
    fun getAllHabitEntries(): Flow<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date ASC, time ASC")
    fun getEntriesForHabit(habitId: String): Flow<List<HabitEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitEntry(entry: HabitEntry)

    @Query("DELETE FROM habit_entries WHERE id = :id")
    suspend fun deleteHabitEntryById(id: String)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId")
    suspend fun deleteEntriesForHabit(habitId: String)

    // Pomo Stats
    @Query("SELECT * FROM pomo_stats WHERE id = 1")
    fun getPomoStats(): Flow<PomoStats?>

    @Query("SELECT * FROM pomo_stats WHERE id = 1")
    suspend fun getPomoStatsDirect(): PomoStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePomoStats(stats: PomoStats)

    // Daily Log
    @Query("SELECT * FROM daily_log")
    fun getDailyLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE date = :date")
    suspend fun getDailyLogForDate(date: String): DailyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyLog(log: DailyLog)

    @Query("DELETE FROM daily_log WHERE date = :date")
    suspend fun deleteDailyLog(date: String)

    // Streak
    @Query("SELECT * FROM streak WHERE id = 1")
    fun getStreak(): Flow<Streak?>

    @Query("SELECT * FROM streak WHERE id = 1")
    suspend fun getStreakDirect(): Streak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: Streak)

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettings)
}
