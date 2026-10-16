package com.mahaam.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val baseColor: String = "indigo", // 'indigo', 'crimson', 'matcha'
    val shadingMode: String = "count", // 'count', 'duration'
    val createdAt: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()),
    val expanded: Boolean = false
) {
    companion object {
        const val COLOR_INDIGO = "indigo"
        const val COLOR_CRIMSON = "crimson"
        const val COLOR_MATCHA = "matcha"

        const val MODE_COUNT = "count"
        const val MODE_DURATION = "duration"
    }
}
