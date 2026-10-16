package com.mahaam.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomo_stats")
data class PomoStats(
    @PrimaryKey
    val id: Int = 1,
    val totalSessions: Int = 0,
    val todaySessions: Int = 0,
    val todayDate: String = "",
    val totalMinutes: Int = 0,
    val todayMinutes: Int = 0
)

@Entity(tableName = "daily_log")
data class DailyLog(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val completedCount: Int = 0
)

@Entity(tableName = "streak")
data class Streak(
    @PrimaryKey
    val id: Int = 1,
    val count: Int = 0,
    val lastDate: String = ""
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val theme: String = "day", // 'day', 'night'
    val ink: String = "indigo", // 'indigo', 'crimson', 'matcha'
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val targetSessions: Int = 4
)
