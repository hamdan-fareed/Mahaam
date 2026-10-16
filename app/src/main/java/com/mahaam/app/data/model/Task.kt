package com.mahaam.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val quadrant: String = QUADRANT_QUICK_WIN,
    val priority: String = PRIORITY_MEDIUM,
    val done: Boolean = false,
    val dueDate: String? = null,
    val createdAt: String = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
    val completedAt: String? = null
) {
    companion object {
        const val QUADRANT_QUICK_WIN = "high-impact-low-effort"
        const val QUADRANT_MAJOR_PROJECT = "high-impact-high-effort"
        const val QUADRANT_FILL_IN = "low-impact-low-effort"
        const val QUADRANT_THANKLESS = "low-impact-high-effort"

        const val PRIORITY_HIGH = "high"
        const val PRIORITY_MEDIUM = "medium"
        const val PRIORITY_LOW = "low"

        fun getQuadrantName(quadrant: String): String = when (quadrant) {
            QUADRANT_QUICK_WIN -> "Quick Win"
            QUADRANT_MAJOR_PROJECT -> "Major Project"
            QUADRANT_FILL_IN -> "Fill-in"
            QUADRANT_THANKLESS -> "Thankless Task"
            else -> "Quick Win"
        }

        fun getQuadrantArabic(quadrant: String): String = when (quadrant) {
            QUADRANT_QUICK_WIN -> "إنجاز سريع"
            QUADRANT_MAJOR_PROJECT -> "مشاريع كبيرة"
            QUADRANT_FILL_IN -> "مهام جانبية"
            QUADRANT_THANKLESS -> "مهام شاقة"
            else -> "إنجاز سريع"
        }

        fun getQuadrantDesc(quadrant: String): String = when (quadrant) {
            QUADRANT_QUICK_WIN -> "High Impact · Low Effort"
            QUADRANT_MAJOR_PROJECT -> "High Impact · High Effort"
            QUADRANT_FILL_IN -> "Low Impact · Low Effort"
            QUADRANT_THANKLESS -> "Low Impact · High Effort"
            else -> ""
        }

        fun getPriorityName(priority: String): String = when (priority) {
            PRIORITY_HIGH -> "High"
            PRIORITY_MEDIUM -> "Medium"
            PRIORITY_LOW -> "Low"
            else -> "Medium"
        }
    }
}
