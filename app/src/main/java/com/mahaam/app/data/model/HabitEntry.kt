package com.mahaam.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "habit_entries",
    indices = [Index(value = ["habitId"]), Index(value = ["date"])]
)
data class HabitEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val timestamp: String,
    val date: String, // YYYY-MM-DD
    val time: String = "12:00", // HH:mm
    val duration: Int = 25,
    val count: Int = 1,
    val note: String = ""
)
