package com.mahaam.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahaam.app.data.local.MahaamDatabase
import com.mahaam.app.data.model.AppSettings
import com.mahaam.app.data.model.Habit
import com.mahaam.app.data.model.HabitEntry
import com.mahaam.app.data.model.MotivationalQuote
import com.mahaam.app.data.model.PomoStats
import com.mahaam.app.data.model.Streak
import com.mahaam.app.data.model.Task
import com.mahaam.app.data.repository.MahaamRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class MahaamView(val number: String, val title: String, val arabic: String, val subtitle: String) {
    DASHBOARD("No. 01", "Dashboard", "بداية", "your day, at a glance"),
    MATRIX("No. 02", "The Matrix", "نظام", "prioritise with clarity"),
    TASKS("No. 03", "Tasks", "قائمة", "the master catalogue"),
    FOCUS("No. 04", "Focus", "تركيز", "deep work, deliberate rest"),
    HABITS("No. 05", "Habits", "عادات", "small deeds, quiet discipline"),
    ANALYTICS("No. 06", "Analytics", "تحليل", "reflection through numbers")
}

class MahaamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MahaamRepository
    val dailyQuote = MotivationalQuote.getDailyQuote()

    init {
        val db = MahaamDatabase.getDatabase(application)
        repository = MahaamRepository(db.dao())
        viewModelScope.launch {
            repository.checkStartupState()
        }
    }

    // Flows from repository
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitEntries: StateFlow<List<HabitEntry>> = repository.allHabitEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pomoStats: StateFlow<PomoStats?> = repository.pomoStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyLogs = repository.dailyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak: StateFlow<Streak?> = repository.streak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<AppSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current navigation view
    private val _currentView = MutableStateFlow(MahaamView.DASHBOARD)
    val currentView: StateFlow<MahaamView> = _currentView.asStateFlow()

    fun switchView(view: MahaamView) {
        _currentView.value = view
    }

    // --- Task Filters ---
    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

    private val _filterQuadrant = MutableStateFlow("all")
    val filterQuadrant: StateFlow<String> = _filterQuadrant.asStateFlow()

    private val _filterStatus = MutableStateFlow("all")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _filterPriority = MutableStateFlow("all")
    val filterPriority: StateFlow<String> = _filterPriority.asStateFlow()

    fun setSearchQuery(query: String) { _taskSearchQuery.value = query }
    fun setFilterQuadrant(quadrant: String) { _filterQuadrant.value = quadrant }
    fun setFilterStatus(status: String) { _filterStatus.value = status }
    fun setFilterPriority(priority: String) { _filterPriority.value = priority }

    // --- Dialogs & Active Sheet state ---
    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _isAddTaskDialogOpen = MutableStateFlow(false)
    val isAddTaskDialogOpen: StateFlow<Boolean> = _isAddTaskDialogOpen.asStateFlow()

    private val _loggingHabit = MutableStateFlow<Habit?>(null)
    val loggingHabit: StateFlow<Habit?> = _loggingHabit.asStateFlow()

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    private val _isAddHabitDialogOpen = MutableStateFlow(false)
    val isAddHabitDialogOpen: StateFlow<Boolean> = _isAddHabitDialogOpen.asStateFlow()

    private val _isTimerSettingsDialogOpen = MutableStateFlow(false)
    val isTimerSettingsDialogOpen: StateFlow<Boolean> = _isTimerSettingsDialogOpen.asStateFlow()

    fun openEditTaskDialog(task: Task) { _editingTask.value = task }
    fun closeEditTaskDialog() { _editingTask.value = null }

    fun openAddTaskDialog() { _isAddTaskDialogOpen.value = true }
    fun closeAddTaskDialog() { _isAddTaskDialogOpen.value = false }

    fun openLogHabitDialog(habit: Habit) { _loggingHabit.value = habit }
    fun closeLogHabitDialog() { _loggingHabit.value = null }

    fun openEditHabitDialog(habit: Habit) { _editingHabit.value = habit }
    fun closeEditHabitDialog() { _editingHabit.value = null }

    fun openAddHabitDialog() { _isAddHabitDialogOpen.value = true }
    fun closeAddHabitDialog() { _isAddHabitDialogOpen.value = false }

    fun openTimerSettingsDialog() { _isTimerSettingsDialogOpen.value = true }
    fun closeTimerSettingsDialog() { _isTimerSettingsDialogOpen.value = false }

    // --- Task Actions ---
    fun addTask(title: String, quadrant: String, priority: String, notes: String = "", dueDate: String? = null) {
        viewModelScope.launch {
            repository.addTask(title, quadrant, priority, notes, dueDate)
        }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch {
            repository.toggleTask(id)
        }
    }

    fun editTask(id: String, title: String, quadrant: String, priority: String, notes: String, dueDate: String?) {
        viewModelScope.launch {
            repository.editTask(id, title, quadrant, priority, notes, dueDate)
            closeEditTaskDialog()
        }
    }

    fun moveTaskQuadrant(id: String, newQuadrant: String) {
        viewModelScope.launch {
            repository.moveTaskQuadrant(id, newQuadrant)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
            closeEditTaskDialog()
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
        }
    }

    fun exportTasksJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportTasksJson(tasks.value)
            onReady(json)
        }
    }

    // --- Habit Actions ---
    fun addHabit(label: String, color: String, mode: String) {
        viewModelScope.launch {
            repository.addHabit(label, color, mode)
            closeAddHabitDialog()
        }
    }

    fun editHabit(id: String, label: String, color: String, mode: String) {
        viewModelScope.launch {
            repository.editHabit(id, label, color, mode)
            closeEditHabitDialog()
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            repository.deleteHabit(id)
            closeEditHabitDialog()
        }
    }

    fun toggleHabitExpanded(id: String) {
        viewModelScope.launch {
            repository.toggleHabitExpanded(id)
        }
    }

    fun quickLogHabit(habitId: String) {
        viewModelScope.launch {
            repository.quickLogHabit(habitId)
        }
    }

    fun logHabitEntry(habitId: String, date: String, time: String, value: Int, note: String) {
        viewModelScope.launch {
            repository.logHabitEntry(habitId, date, time, value, note)
            closeLogHabitDialog()
        }
    }

    fun deleteHabitEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteHabitEntry(entryId)
        }
    }

    // --- Pomodoro Timer State & Logic ---
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow("work") // "work", "short-break", "long-break"
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    private val _timerSeconds = MutableStateFlow(25 * 60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerSessionCount = MutableStateFlow(0)
    val timerSessionCount: StateFlow<Int> = _timerSessionCount.asStateFlow()

    private val _selectedFocusTaskId = MutableStateFlow<String?>(null)
    val selectedFocusTaskId: StateFlow<String?> = _selectedFocusTaskId.asStateFlow()

    fun setSelectedFocusTaskId(id: String?) {
        _selectedFocusTaskId.value = id
    }

    private var timerJob: Job? = null

    fun getDurationForMode(mode: String): Int {
        val s = settings.value ?: AppSettings()
        return when (mode) {
            "short-break" -> s.shortBreakMinutes * 60
            "long-break" -> s.longBreakMinutes * 60
            else -> s.workMinutes * 60
        }
    }

    fun startTimer() {
        if (_timerRunning.value) return
        if (_timerSeconds.value <= 0) {
            _timerSeconds.value = getDurationForMode(_timerMode.value)
        }
        _timerRunning.value = true
        startTimerLoop()
    }

    fun pauseTimer() {
        if (!_timerRunning.value) return
        _timerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        _timerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        _timerMode.value = "work"
        _timerSeconds.value = getDurationForMode("work")
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerRunning.value && _timerSeconds.value > 0) {
                delay(1000)
                _timerSeconds.value -= 1
            }
            if (_timerSeconds.value <= 0) {
                handleTimerComplete()
            }
        }
    }

    private fun handleTimerComplete() {
        playCompletionChimeAndVibrate()

        val s = settings.value ?: AppSettings()
        val targetSessions = s.targetSessions
        val workMins = s.workMinutes

        if (_timerMode.value == "work") {
            viewModelScope.launch {
                repository.recordPomoSession(workMins)
            }
            val newSessionCount = _timerSessionCount.value + 1
            if (newSessionCount >= targetSessions) {
                _timerSessionCount.value = 0
                _timerMode.value = "long-break"
                _timerSeconds.value = getDurationForMode("long-break")
            } else {
                _timerSessionCount.value = newSessionCount
                _timerMode.value = "short-break"
                _timerSeconds.value = getDurationForMode("short-break")
            }
        } else {
            _timerMode.value = "work"
            _timerSeconds.value = getDurationForMode("work")
        }

        // Auto continue timer
        _timerRunning.value = true
        startTimerLoop()
    }

    private fun playCompletionChimeAndVibrate() {
        val context = getApplication<Application>()

        // 1. Gentle Vibration
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 150, 100, 250)
                    val amplitudes = intArrayOf(0, 180, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(300)
                }
            }
        } catch (_: Exception) {}

        // 2. Chime synthesis using AudioTrack (matching Web Audio API C5-E5-G5 chime)
        viewModelScope.launch {
            try {
                val sampleRate = 44100
                val totalDuration = 0.8
                val numSamples = (sampleRate * totalDuration).toInt()
                val samples = ShortArray(numSamples)

                val notes = listOf(
                    Triple(523.25, 0.0, 0.2),      // C5
                    Triple(659.25, 0.15, 0.2),     // E5
                    Triple(783.99, 0.3, 0.45)      // G5
                )

                for (note in notes) {
                    val freq = note.first
                    val startSample = (note.second * sampleRate).toInt()
                    val durSamples = (note.third * sampleRate).toInt()

                    for (i in 0 until durSamples) {
                        val sampleIdx = startSample + i
                        if (sampleIdx < numSamples) {
                            val t = i.toDouble() / sampleRate
                            val envelope = (1.0 - (i.toDouble() / durSamples)).coerceIn(0.0, 1.0)
                            val wave = sin(2.0 * Math.PI * freq * t) * envelope * 0.25
                            val sampleVal = (wave * Short.MAX_VALUE).toInt().toShort()
                            samples[sampleIdx] = (samples[sampleIdx] + sampleVal).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        }
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                delay(900)
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    // --- Settings Actions ---
    fun toggleTheme() {
        val curTheme = settings.value?.theme ?: "day"
        val nextTheme = if (curTheme == "day") "night" else "day"
        viewModelScope.launch {
            repository.updateTheme(nextTheme)
        }
    }

    fun setInk(ink: String) {
        viewModelScope.launch {
            repository.updateInk(ink)
        }
    }

    fun updateTimerSettings(work: Int, shortBreak: Int, longBreak: Int, sessions: Int) {
        viewModelScope.launch {
            repository.updateTimerSettings(work, shortBreak, longBreak, sessions)
            if (!_timerRunning.value) {
                _timerSeconds.value = getDurationForMode(_timerMode.value)
            }
            closeTimerSettingsDialog()
        }
    }
}
