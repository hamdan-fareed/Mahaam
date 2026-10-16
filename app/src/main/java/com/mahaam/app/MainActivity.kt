package com.mahaam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.padding
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.Task
import com.mahaam.app.ui.components.AddHabitDialog
import com.mahaam.app.ui.components.AddTaskDialog
import com.mahaam.app.ui.components.EditHabitDialog
import com.mahaam.app.ui.components.EditTaskDialog
import com.mahaam.app.ui.components.LogHabitEntryDialog
import com.mahaam.app.ui.components.MahaamTopAppBar
import com.mahaam.app.ui.components.TimerSettingsDialog
import com.mahaam.app.ui.screens.AnalyticsScreen
import com.mahaam.app.ui.screens.DashboardScreen
import com.mahaam.app.ui.screens.FocusScreen
import com.mahaam.app.ui.screens.HabitsScreen
import com.mahaam.app.ui.screens.MatrixScreen
import com.mahaam.app.ui.screens.TasksScreen
import com.mahaam.app.ui.theme.LocalMahaamColors
import com.mahaam.app.ui.theme.MahaamTheme
import com.mahaam.app.ui.viewmodel.MahaamView
import com.mahaam.app.ui.viewmodel.MahaamViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MahaamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val currentSettings = settings ?: AppSettings()
            val isNight = currentSettings.theme == "night"
            val currentInk = currentSettings.ink

            MahaamTheme(darkTheme = isNight, ink = currentInk) {
                MahaamApp(viewModel = viewModel, isNight = isNight, currentInk = currentInk)
            }
        }
    }
}

@Composable
fun MahaamApp(
    viewModel: MahaamViewModel,
    isNight: Boolean,
    currentInk: String
) {
    val colors = LocalMahaamColors.current
    val currentView by viewModel.currentView.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val habitEntries by viewModel.habitEntries.collectAsStateWithLifecycle()
    val pomoStats by viewModel.pomoStats.collectAsStateWithLifecycle()
    val dailyLogs by viewModel.dailyLogs.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentSettings = settings ?: AppSettings()

    // Timer states
    val timerRunning by viewModel.timerRunning.collectAsStateWithLifecycle()
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val timerSessionCount by viewModel.timerSessionCount.collectAsStateWithLifecycle()
    val selectedFocusTaskId by viewModel.selectedFocusTaskId.collectAsStateWithLifecycle()

    // Task filters
    val searchQuery by viewModel.taskSearchQuery.collectAsStateWithLifecycle()
    val filterQuadrant by viewModel.filterQuadrant.collectAsStateWithLifecycle()
    val filterStatus by viewModel.filterStatus.collectAsStateWithLifecycle()
    val filterPriority by viewModel.filterPriority.collectAsStateWithLifecycle()

    // Active Dialogs
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val isAddTaskDialogOpen by viewModel.isAddTaskDialogOpen.collectAsStateWithLifecycle()
    val loggingHabit by viewModel.loggingHabit.collectAsStateWithLifecycle()
    val editingHabit by viewModel.editingHabit.collectAsStateWithLifecycle()
    val isAddHabitDialogOpen by viewModel.isAddHabitDialogOpen.collectAsStateWithLifecycle()
    val isTimerSettingsDialogOpen by viewModel.isTimerSettingsDialogOpen.collectAsStateWithLifecycle()

    var prefilledQuadrantForAdd by remember { mutableStateOf(Task.QUADRANT_QUICK_WIN) }

    // BackHandler: Return to Dashboard if not already on Dashboard
    BackHandler(enabled = currentView != MahaamView.DASHBOARD) {
        viewModel.switchView(MahaamView.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
        topBar = {
            MahaamTopAppBar(
                isNight = isNight,
                currentInk = currentInk,
                onToggleTheme = { viewModel.toggleTheme() },
                onSelectInk = { ink -> viewModel.setInk(ink) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bg)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.border)
                )

                NavigationBar(
                    containerColor = colors.bg,
                    contentColor = colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        MahaamView.DASHBOARD to Icons.Outlined.Dashboard,
                        MahaamView.MATRIX to Icons.Outlined.GridView,
                        MahaamView.TASKS to Icons.Outlined.Checklist,
                        MahaamView.FOCUS to Icons.Outlined.Timer,
                        MahaamView.HABITS to Icons.Outlined.CalendarToday,
                        MahaamView.ANALYTICS to Icons.Outlined.BarChart
                    )

                    navItems.forEach { (view, icon) ->
                        val isSelected = currentView == view
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.switchView(view) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = view.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = view.title,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.accent,
                                selectedTextColor = colors.accent,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = colors.accentFaint
                            ),
                            modifier = Modifier.testTag("nav_item_${view.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
        ) {
            when (currentView) {
                MahaamView.DASHBOARD -> DashboardScreen(
                    tasks = tasks,
                    streak = streak,
                    quote = viewModel.dailyQuote,
                    onToggleTask = { viewModel.toggleTask(it) },
                    onEditTask = { viewModel.openEditTaskDialog(it) },
                    onQuickAddTask = { title, quad -> viewModel.addTask(title, quad, Task.PRIORITY_MEDIUM) },
                    modifier = Modifier.padding(innerPadding)
                )

                MahaamView.MATRIX -> MatrixScreen(
                    tasks = tasks,
                    onToggleTask = { viewModel.toggleTask(it) },
                    onEditTask = { viewModel.openEditTaskDialog(it) },
                    onAddTaskToQuadrant = { quadrant ->
                        prefilledQuadrantForAdd = quadrant
                        viewModel.openAddTaskDialog()
                    },
                    onMoveTaskQuadrant = { id, quadrant ->
                        viewModel.moveTaskQuadrant(id, quadrant)
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                MahaamView.TASKS -> TasksScreen(
                    tasks = tasks,
                    searchQuery = searchQuery,
                    filterQuadrant = filterQuadrant,
                    filterStatus = filterStatus,
                    filterPriority = filterPriority,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onQuadrantChange = { viewModel.setFilterQuadrant(it) },
                    onStatusChange = { viewModel.setFilterStatus(it) },
                    onPriorityChange = { viewModel.setFilterPriority(it) },
                    onToggleTask = { viewModel.toggleTask(it) },
                    onEditTask = { viewModel.openEditTaskDialog(it) },
                    onOpenAddTaskDialog = {
                        prefilledQuadrantForAdd = Task.QUADRANT_QUICK_WIN
                        viewModel.openAddTaskDialog()
                    },
                    onClearCompleted = { viewModel.clearCompletedTasks() },
                    onExportTasks = { onReady -> viewModel.exportTasksJson(onReady) },
                    modifier = Modifier.padding(innerPadding)
                )

                MahaamView.FOCUS -> FocusScreen(
                    timerRunning = timerRunning,
                    timerMode = timerMode,
                    timerSeconds = timerSeconds,
                    timerSessionCount = timerSessionCount,
                    tasks = tasks,
                    selectedTaskId = selectedFocusTaskId,
                    pomoStats = pomoStats,
                    settings = currentSettings,
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onResetTimer = { viewModel.resetTimer() },
                    onSelectTask = { viewModel.setSelectedFocusTaskId(it) },
                    onOpenSettingsDialog = { viewModel.openTimerSettingsDialog() },
                    modifier = Modifier.padding(innerPadding)
                )

                MahaamView.HABITS -> HabitsScreen(
                    habits = habits,
                    entries = habitEntries,
                    onQuickLog = { viewModel.quickLogHabit(it) },
                    onOpenLogDialog = { viewModel.openLogHabitDialog(it) },
                    onOpenEditDialog = { viewModel.openEditHabitDialog(it) },
                    onToggleExpanded = { viewModel.toggleHabitExpanded(it) },
                    onDeleteEntry = { viewModel.deleteHabitEntry(it) },
                    onOpenAddHabitDialog = { viewModel.openAddHabitDialog() },
                    modifier = Modifier.padding(innerPadding)
                )

                MahaamView.ANALYTICS -> AnalyticsScreen(
                    tasks = tasks,
                    dailyLogs = dailyLogs,
                    pomoStats = pomoStats,
                    streak = streak,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            // Dialogs
            editingTask?.let { task ->
                EditTaskDialog(
                    task = task,
                    onDismiss = { viewModel.closeEditTaskDialog() },
                    onSave = { id, title, quadrant, priority, notes, due ->
                        viewModel.editTask(id, title, quadrant, priority, notes, due)
                    },
                    onDelete = { id -> viewModel.deleteTask(id) }
                )
            }

            if (isAddTaskDialogOpen) {
                AddTaskDialog(
                    initialQuadrant = prefilledQuadrantForAdd,
                    onDismiss = { viewModel.closeAddTaskDialog() },
                    onAdd = { title, quadrant, priority, notes, due ->
                        viewModel.addTask(title, quadrant, priority, notes, due)
                        viewModel.closeAddTaskDialog()
                    }
                )
            }

            loggingHabit?.let { habit ->
                LogHabitEntryDialog(
                    habit = habit,
                    onDismiss = { viewModel.closeLogHabitDialog() },
                    onSave = { date, time, value, note ->
                        viewModel.logHabitEntry(habit.id, date, time, value, note)
                    }
                )
            }

            editingHabit?.let { habit ->
                EditHabitDialog(
                    habit = habit,
                    onDismiss = { viewModel.closeEditHabitDialog() },
                    onSave = { label, color, mode ->
                        viewModel.editHabit(habit.id, label, color, mode)
                    },
                    onDelete = { viewModel.deleteHabit(habit.id) }
                )
            }

            if (isAddHabitDialogOpen) {
                AddHabitDialog(
                    onDismiss = { viewModel.closeAddHabitDialog() },
                    onAdd = { label, color, mode ->
                        viewModel.addHabit(label, color, mode)
                    }
                )
            }

            if (isTimerSettingsDialogOpen) {
                TimerSettingsDialog(
                    settings = currentSettings,
                    onDismiss = { viewModel.closeTimerSettingsDialog() },
                    onSave = { work, shortBreak, longBreak, sessions ->
                        viewModel.updateTimerSettings(work, shortBreak, longBreak, sessions)
                    }
                )
            }
        }
    }
}
