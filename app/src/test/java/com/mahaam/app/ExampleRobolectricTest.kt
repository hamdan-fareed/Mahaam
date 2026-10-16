package com.mahaam.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mahaam.app.data.local.MahaamDatabase
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.Task
import com.mahaam.app.data.repository.MahaamRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mahaam", appName)
    }

    @Test
    fun `repository adds and toggles task properly`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MahaamDatabase.getDatabase(context)
        val repository = MahaamRepository(db.dao())

        val task = repository.addTask(
            title = "Test Deep Work",
            quadrant = Task.QUADRANT_QUICK_WIN,
            priority = Task.PRIORITY_HIGH,
            notes = "Robolectric test note"
        )
        assertNotNull(task)

        val tasksAfterAdd = repository.allTasks.first()
        assertTrue(tasksAfterAdd.any { it.title == "Test Deep Work" && !it.done })

        repository.toggleTask(task!!.id)
        val tasksAfterToggle = repository.allTasks.first()
        val toggled = tasksAfterToggle.find { it.id == task.id }
        assertNotNull(toggled)
        assertTrue(toggled!!.done)

        // Streak should be updated on completion
        val streak = repository.streak.first()
        assertNotNull(streak)
        assertTrue((streak?.count ?: 0) >= 1)
    }

    @Test
    fun `repository adds and logs habit entry`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MahaamDatabase.getDatabase(context)
        val repository = MahaamRepository(db.dao())

        val habit = repository.addHabit(
            label = "Morning Contemplation",
            color = Habit.COLOR_MATCHA,
            mode = Habit.MODE_DURATION
        )
        assertNotNull(habit)

        repository.quickLogHabit(habit!!.id)
        val entries = repository.allHabitEntries.first()
        assertTrue(entries.any { it.habitId == habit.id })
    }
}
