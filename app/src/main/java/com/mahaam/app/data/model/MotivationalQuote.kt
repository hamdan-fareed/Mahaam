package com.mahaam.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MotivationalQuote(
    val text: String,
    val author: String
) {
    companion object {
        val QUOTES = listOf(
            MotivationalQuote("The secret of getting ahead is getting started.", "سلام"),
            MotivationalQuote("Simplicity is prerequisite for reliability and peace of mind.", "مهام"),
            MotivationalQuote("Focus is a muscle. The more you protect your attention, the stronger it grows.", "رواق"),
            MotivationalQuote("Do not confuse movement with purposeful progress.", "حكمة"),
            MotivationalQuote("Small deeds done consistently are better than great deeds perpetually planned.", "إنجاز"),
            MotivationalQuote("Order and calm are the first steps toward mastery.", "نظام"),
            MotivationalQuote("What we do every day matters infinitely more than what we do once in a while.", "عزيمة"),
            MotivationalQuote("Clarity comes from engagement, not from thought alone.", "عمل"),
            MotivationalQuote("Perfection is achieved not when there is nothing left to add, but when nothing can be taken away.", "إتقان"),
            MotivationalQuote("Quiet discipline produces enduring outcomes.", "صبر"),
            MotivationalQuote("Action is the foundational key to all meaningful achievement.", "مسار"),
            MotivationalQuote("Calmness is a superpower in a noisy, distracted world.", "سكينة")
        )

        fun getDailyQuote(): MotivationalQuote {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            var hash = 0
            for (ch in today) {
                hash = (hash * 31 + ch.code)
            }
            val index = Math.abs(hash) % QUOTES.size
            return QUOTES[index]
        }
    }
}
