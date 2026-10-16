package com.mahaam.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.DailyLog
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.HabitEntry
import com.mahaam.app.data.model.PomoStats
import com.mahaam.app.data.model.Streak
import com.mahaam.app.data.model.Task

@Database(
    entities = [
        Task::class,
        Habit::class,
        HabitEntry::class,
        PomoStats::class,
        DailyLog::class,
        Streak::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MahaamDatabase : RoomDatabase() {
    abstract fun dao(): MahaamDao

    companion object {
        @Volatile
        private var INSTANCE: MahaamDatabase? = null

        fun getDatabase(context: Context): MahaamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MahaamDatabase::class.java,
                    "mahaam_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
