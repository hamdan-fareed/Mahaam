/**
 * مهام — Mahaam
 * A calm, editorial desktop productivity application.
 *
 * Fully offline-first: Eisenhower Matrix, Pomodoro Timer, Habit Tracker, & Analytics.
 */

'use strict';

// ─── UTILITY HELPERS ─────────────────────────────────────────────────────────

/**
 * Generate a unique ID (UUID v4) with fallback.
 * @returns {string}
 */
function generateId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * Returns today's local date in 'YYYY-MM-DD' format.
 * @returns {string}
 */
function getTodayString() {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Calculates calendar day difference between two 'YYYY-MM-DD' strings (d2 - d1).
 * @param {string} dateStr1
 * @param {string} dateStr2
 * @returns {number}
 */
function getDaysDifference(dateStr1, dateStr2) {
  if (!dateStr1 || !dateStr2) return 0;
  const d1 = new Date(dateStr1 + 'T00:00:00');
  const d2 = new Date(dateStr2 + 'T00:00:00');
  const diffMs = d2.getTime() - d1.getTime();
  return Math.round(diffMs / (1000 * 60 * 60 * 24));
}

/**
 * Escape unsafe HTML characters to prevent XSS.
 * @param {string} str
 * @returns {string}
 */
function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**
 * Formats an ISO date string (YYYY-MM-DD) into a human-readable display string (e.g. "Oct 15").
 * @param {string} dateStr
 * @returns {string}
 */
function formatDisplayDate(dateStr) {
  if (!dateStr) return '';
  try {
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const d = new Date(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10));
      return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    }
    const d = new Date(dateStr);
    return isNaN(d.getTime()) ? dateStr : d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  } catch (e) {
    return dateStr;
  }
}

/**
 * Returns an array of the last 7 calendar days up to today.
 * @returns {Array<{dateStr: string, weekday: string, dayNum: number, isToday: boolean}>}
 */
function getLast7Days() {
  const days = [];
  const today = new Date();
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;
    const weekday = d.toLocaleDateString(undefined, { weekday: 'short' });
    const isToday = i === 0;
    days.push({ dateStr, weekday, dayNum: d.getDate(), isToday });
  }
  return days;
}

/**
 * Returns an array of the last 30 calendar days up to today (oldest first on index 0 to newest on index 29).
 * @returns {Array<{dateStr: string, dayNum: number, monthName: string, isToday: boolean}>}
 */
function getLast30Days() {
  const days = [];
  const today = new Date();
  for (let i = 29; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;
    const dayNum = d.getDate();
    const monthName = d.toLocaleDateString(undefined, { month: 'short' });
    const isToday = i === 0;
    days.push({ dateStr, dayNum, monthName, isToday });
  }
  return days;
}

const HABIT_BASE_RGBS = {
  indigo: [74, 88, 153],
  crimson: [166, 61, 64],
  matcha: [91, 117, 83]
};

/**
 * Generates an audio chime using Web Audio API when a timer finishes.
 */
function playTimerChime() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    const now = ctx.currentTime;

    const notes = [
      { freq: 523.25, time: 0, dur: 0.2 },      // C5
      { freq: 659.25, time: 0.15, dur: 0.2 },   // E5
      { freq: 783.99, time: 0.3, dur: 0.45 }    // G5
    ];

    notes.forEach(note => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(note.freq, now + note.time);
      gain.gain.setValueAtTime(0.2, now + note.time);
      gain.gain.exponentialRampToValueAtTime(0.001, now + note.time + note.dur);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now + note.time);
      osc.stop(now + note.time + note.dur);
    });
  } catch (err) {
    console.warn('Audio chime playback failed:', err);
  }
}

// ─── CONSTANTS & DICTIONARIES ────────────────────────────────────────────────

const QUADRANT_NAMES = {
  'high-impact-low-effort': 'Quick Win',
  'high-impact-high-effort': 'Major Project',
  'low-impact-low-effort': 'Fill-in',
  'low-impact-high-effort': 'Thankless Task'
};

const PRIORITY_NAMES = {
  'high': 'High',
  'medium': 'Medium',
  'low': 'Low'
};

const INK_HEX_FALLBACKS = {
  indigo: '#4338CA',
  crimson: '#991B1B',
  matcha: '#2D6A4F'
};

const MOTIVATIONAL_QUOTES = [
  { text: "The secret of getting ahead is getting started.", author: "سلام" },
  { text: "Simplicity is prerequisite for reliability and peace of mind.", author: "مهام" },
  { text: "Focus is a muscle. The more you protect your attention, the stronger it grows.", author: "رواق" },
  { text: "Do not confuse movement with purposeful progress.", author: "حكمة" },
  { text: "Small deeds done consistently are better than great deeds perpetually planned.", author: "إنجاز" },
  { text: "Order and calm are the first steps toward mastery.", author: "نظام" },
  { text: "What we do every day matters infinitely more than what we do once in a while.", author: "عزيمة" },
  { text: "Clarity comes from engagement, not from thought alone.", author: "عمل" },
  { text: "Perfection is achieved not when there is nothing left to add, but when nothing can be taken away.", author: "إتقان" },
  { text: "Quiet discipline produces enduring outcomes.", author: "صبر" },
  { text: "Action is the foundational key to all meaningful achievement.", author: "مسار" },
  { text: "Calmness is a superpower in a noisy, distracted world.", author: "سكينة" }
];

/**
 * Returns a quote selected deterministically for today.
 * @returns {{text: string, author: string}}
 */
function getDailyQuote() {
  const today = getTodayString();
  let hash = 0;
  for (let i = 0; i < today.length; i++) {
    hash = (hash * 31 + today.charCodeAt(i)) >>> 0;
  }
  return MOTIVATIONAL_QUOTES[hash % MOTIVATIONAL_QUOTES.length];
}

// ─── MAIN APPLICATION OBJECT ─────────────────────────────────────────────────

const app = {
  // Core state
  tasks: [],
  habits: [],
  pomo: {
    totalSessions: 0,
    todaySessions: 0,
    todayDate: '',
    totalMinutes: 0,
    todayMinutes: 0
  },
  dailyLog: {},
  streak: {
    count: 0,
    lastDate: ''
  },

  // Pomodoro timer state
  timerInterval: null,
  timerRafId: null,
  timerSeconds: 25 * 60,
  timerEndTime: 0,
  timerRunning: false,
  timerMode: 'work', // 'work', 'short-break', 'long-break'
  timerSessionCount: 0,

  // Navigation
  currentView: 'dashboard',

  // ─── INITIALIZATION ────────────────────────────────────────────────────────

  init() {
    this.loadAllData();
    this.checkPomoDayReset();
    this.checkStreakOnStartup();
    this.applyThemeAndInk();
    this.initTimerDuration();
    this.bindEvents();
    this.render();
  },

  // ─── PERSISTENCE ───────────────────────────────────────────────────────────

  loadAllData() {
    // Tasks
    try {
      const rawTasks = localStorage.getItem('mahaam-tasks');
      this.tasks = rawTasks ? JSON.parse(rawTasks) : [];
      if (!Array.isArray(this.tasks)) this.tasks = [];
    } catch (e) {
      this.tasks = [];
    }

    // Habits
    try {
      const rawHabits = localStorage.getItem('mahaam-habits');
      const loaded = rawHabits ? JSON.parse(rawHabits) : [];
      this.habits = Array.isArray(loaded) ? loaded : [];
      this.habits.forEach(h => {
        if (!h.id) h.id = generateId();
        h.label = (h.label || h.name || 'Untitled Habit').trim();
        h.name = h.label;
        if (!['indigo', 'crimson', 'matcha'].includes(h.base_color)) {
          h.base_color = 'indigo';
        }
        if (!['duration', 'count'].includes(h.shading_mode)) {
          h.shading_mode = 'count';
        }
        if (!Array.isArray(h.entries)) {
          h.entries = [];
          if (h.completions && typeof h.completions === 'object') {
            Object.keys(h.completions).forEach(d => {
              if (h.completions[d]) {
                h.entries.push({
                  id: generateId(),
                  timestamp: `${d}T12:00:00`,
                  date: d,
                  time: '12:00',
                  duration: 25,
                  count: 1,
                  note: 'Migrated entry'
                });
              }
            });
          }
        }
        h.expanded = !!h.expanded;
      });
    } catch (e) {
      this.habits = [];
    }

    // Pomodoro stats
    try {
      const rawPomo = localStorage.getItem('mahaam-pomo');
      const parsedPomo = rawPomo ? JSON.parse(rawPomo) : null;
      this.pomo = Object.assign({
        totalSessions: 0,
        todaySessions: 0,
        todayDate: getTodayString(),
        totalMinutes: 0,
        todayMinutes: 0
      }, parsedPomo);
    } catch (e) {
      this.pomo = {
        totalSessions: 0,
        todaySessions: 0,
        todayDate: getTodayString(),
        totalMinutes: 0,
        todayMinutes: 0
      };
    }

    // Daily completion log
    try {
      const rawDailyLog = localStorage.getItem('mahaam-daily-log');
      this.dailyLog = rawDailyLog ? JSON.parse(rawDailyLog) : {};
      if (typeof this.dailyLog !== 'object' || this.dailyLog === null) this.dailyLog = {};
    } catch (e) {
      this.dailyLog = {};
    }

    // Streak
    try {
      const rawStreak = localStorage.getItem('mahaam-streak');
      const parsedStreak = rawStreak ? JSON.parse(rawStreak) : null;
      this.streak = Object.assign({ count: 0, lastDate: '' }, parsedStreak);
    } catch (e) {
      this.streak = { count: 0, lastDate: '' };
    }
  },

  saveTasks() {
    localStorage.setItem('mahaam-tasks', JSON.stringify(this.tasks));
  },

  saveHabits() {
    localStorage.setItem('mahaam-habits', JSON.stringify(this.habits));
  },

  savePomo() {
    localStorage.setItem('mahaam-pomo', JSON.stringify(this.pomo));
  },

  saveDailyLog() {
    localStorage.setItem('mahaam-daily-log', JSON.stringify(this.dailyLog));
  },

  saveStreak() {
    localStorage.setItem('mahaam-streak', JSON.stringify(this.streak));
  },

  // ─── STREAK & POMO RESET LOGIC ─────────────────────────────────────────────

  checkPomoDayReset() {
    const today = getTodayString();
    if (this.pomo.todayDate !== today) {
      this.pomo.todayDate = today;
      this.pomo.todaySessions = 0;
      this.pomo.todayMinutes = 0;
      this.savePomo();
    }
  },

  checkStreakOnStartup() {
    if (!this.streak.lastDate) return;
    const today = getTodayString();
    const diff = getDaysDifference(this.streak.lastDate, today);
    // If more than 1 day has passed without completing a task, reset streak count to 0
    if (diff > 1) {
      this.streak.count = 0;
      this.saveStreak();
    }
  },

  recordStreakOnCompletion() {
    const today = getTodayString();
    if (this.streak.lastDate === today) {
      // Already credited for today
      return;
    }
    if (!this.streak.lastDate) {
      this.streak.count = 1;
      this.streak.lastDate = today;
    } else {
      const diff = getDaysDifference(this.streak.lastDate, today);
      if (diff === 1) {
        // Completed yesterday, streak increments
        this.streak.count = (this.streak.count || 0) + 1;
        this.streak.lastDate = today;
      } else if (diff === 0) {
        // Today
      } else {
        // Gap > 1 day
        this.streak.count = 1;
        this.streak.lastDate = today;
      }
    }
    this.saveStreak();
  },

  // ─── THEME & INK MANAGEMENT ────────────────────────────────────────────────

  applyThemeAndInk() {
    const savedTheme = localStorage.getItem('mahaam-theme') || 'day';
    const savedInk = localStorage.getItem('mahaam-ink') || 'indigo';

    this.setTheme(savedTheme, false);
    this.setInk(savedInk, false);
  },

  setTheme(theme, save = true) {
    const validTheme = theme === 'night' ? 'night' : 'day';
    document.documentElement.setAttribute('data-theme', validTheme);
    if (save) {
      localStorage.setItem('mahaam-theme', validTheme);
    }
    document.querySelectorAll('.theme-btn').forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-theme') === validTheme);
    });
    const mobileThemeBtn = document.getElementById('mobile-theme-btn');
    if (mobileThemeBtn) {
      mobileThemeBtn.textContent = validTheme === 'day' ? 'Night' : 'Day';
    }
    const metaTheme = document.querySelector('meta[name="theme-color"]');
    if (metaTheme) {
      metaTheme.setAttribute('content', validTheme === 'night' ? '#1A1A1A' : '#FAFAF8');
    }
    if (this.currentView === 'stats') {
      this.renderStats();
    }
  },

  setInk(ink, save = true) {
    const validInks = ['indigo', 'crimson', 'matcha'];
    const chosenInk = validInks.includes(ink) ? ink : 'indigo';
    document.documentElement.setAttribute('data-ink', chosenInk);
    if (save) {
      localStorage.setItem('mahaam-ink', chosenInk);
    }
    document.querySelectorAll('.ink-btn').forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-ink') === chosenInk);
    });
    if (this.currentView === 'stats') {
      this.renderStats();
    }
  },

  // ─── NAVIGATION ────────────────────────────────────────────────────────────

  switchView(viewName) {
    const validViews = ['dashboard', 'matrix', 'tasks', 'focus', 'habits', 'stats'];
    if (!validViews.includes(viewName)) return;

    this.currentView = viewName;

    // Update desktop nav items
    document.querySelectorAll('.nav-item').forEach(el => {
      el.classList.toggle('active', el.getAttribute('data-view') === viewName);
    });

    // Update mobile bottom nav items
    document.querySelectorAll('.mobile-nav-item').forEach(el => {
      el.classList.toggle('active', el.getAttribute('data-view') === viewName);
    });

    // Update view sections
    document.querySelectorAll('.view').forEach(el => {
      el.classList.toggle('active', el.id === `view-${viewName}`);
    });

    // Re-render specific view requirements
    if (viewName === 'stats') {
      // Small timeout allows layout transition to compute proper canvas dimensions
      setTimeout(() => this.renderStats(), 30);
    } else if (viewName === 'matrix') {
      this.renderMatrix();
    } else if (viewName === 'tasks') {
      this.renderTasks();
    } else if (viewName === 'focus') {
      this.renderFocus();
    } else if (viewName === 'habits') {
      this.renderHabits();
    } else if (viewName === 'dashboard') {
      this.renderDashboard();
    }
  },

  // ─── TASK MANAGEMENT ───────────────────────────────────────────────────────

  addTask(title, quadrant, priority, notes, dueDate) {
    const trimmedTitle = (title || '').trim();
    if (!trimmedTitle) return null;

    const validQuadrants = [
      'high-impact-low-effort',
      'high-impact-high-effort',
      'low-impact-low-effort',
      'low-impact-high-effort'
    ];
    const validPriorities = ['high', 'medium', 'low'];

    const task = {
      id: generateId(),
      title: trimmedTitle,
      notes: (notes || '').trim(),
      quadrant: validQuadrants.includes(quadrant) ? quadrant : 'high-impact-low-effort',
      priority: validPriorities.includes(priority) ? priority : 'medium',
      done: false,
      dueDate: dueDate || null,
      createdAt: new Date().toISOString(),
      completedAt: null
    };

    this.tasks.push(task);
    this.saveTasks();
    this.render();
    return task;
  },

  toggleTask(id) {
    const task = this.tasks.find(t => t.id === id);
    if (!task) return;

    task.done = !task.done;
    const today = getTodayString();

    if (task.done) {
      task.completedAt = new Date().toISOString();
      this.dailyLog[today] = (this.dailyLog[today] || 0) + 1;
      this.saveDailyLog();
      this.recordStreakOnCompletion();
    } else {
      task.completedAt = null;
      if (this.dailyLog[today] && this.dailyLog[today] > 0) {
        this.dailyLog[today]--;
        if (this.dailyLog[today] === 0) {
          delete this.dailyLog[today];
        }
        this.saveDailyLog();
      }
    }

    this.saveTasks();
    this.render();
  },

  deleteTask(id) {
    const index = this.tasks.findIndex(t => t.id === id);
    if (index !== -1) {
      this.tasks.splice(index, 1);
      this.saveTasks();
      this.render();
    }
  },

  editTask(id, updates) {
    const task = this.tasks.find(t => t.id === id);
    if (!task) return;

    if (typeof updates.title === 'string') {
      const trimmed = updates.title.trim();
      if (trimmed) task.title = trimmed;
    }
    if (typeof updates.notes === 'string') {
      task.notes = updates.notes.trim();
    }
    if (updates.quadrant && QUADRANT_NAMES[updates.quadrant]) {
      task.quadrant = updates.quadrant;
    }
    if (updates.priority && PRIORITY_NAMES[updates.priority]) {
      task.priority = updates.priority;
    }
    if (updates.dueDate !== undefined) {
      task.dueDate = updates.dueDate || null;
    }

    this.saveTasks();
    this.render();
  },

  moveTask(id, newQuadrant) {
    if (!QUADRANT_NAMES[newQuadrant]) return;
    const task = this.tasks.find(t => t.id === id);
    if (task && task.quadrant !== newQuadrant) {
      task.quadrant = newQuadrant;
      this.saveTasks();
      this.render();
    }
  },

  // ─── DRAG AND DROP (MATRIX) ────────────────────────────────────────────────

  handleDragStart(event) {
    const chip = event.target.closest('.matrix-task-chip');
    if (!chip) return;
    const id = chip.dataset.id;
    if (!id) return;

    event.dataTransfer.setData('text/plain', id);
    event.dataTransfer.effectAllowed = 'move';
    chip.classList.add('dragging');

    const onDragEnd = () => {
      chip.classList.remove('dragging');
      chip.removeEventListener('dragend', onDragEnd);
    };
    chip.addEventListener('dragend', onDragEnd);
  },

  handleDrop(event, quadrant) {
    event.preventDefault();
    const id = event.dataTransfer.getData('text/plain');
    if (id) {
      this.moveTask(id, quadrant);
    }
  },

  // ─── EDIT MODAL ────────────────────────────────────────────────────────────

  openEditModal(id) {
    const task = this.tasks.find(t => t.id === id);
    if (!task) return;

    const modalOverlay = document.getElementById('modal-overlay');
    const idInput = document.getElementById('edit-task-id');
    const titleInput = document.getElementById('edit-task-title');
    const notesInput = document.getElementById('edit-task-notes');
    const quadrantSelect = document.getElementById('edit-task-quadrant');
    const prioritySelect = document.getElementById('edit-task-priority');
    const dueInput = document.getElementById('edit-task-due');

    if (idInput) idInput.value = task.id;
    if (titleInput) titleInput.value = task.title || '';
    if (notesInput) notesInput.value = task.notes || '';
    if (quadrantSelect) quadrantSelect.value = task.quadrant || 'high-impact-low-effort';
    if (prioritySelect) prioritySelect.value = task.priority || 'medium';
    if (dueInput) dueInput.value = task.dueDate || '';

    if (modalOverlay) {
      modalOverlay.style.display = 'flex';
      setTimeout(() => {
        if (titleInput) titleInput.focus();
      }, 50);
    }
  },

  closeEditModal() {
    const modalOverlay = document.getElementById('modal-overlay');
    if (modalOverlay) {
      modalOverlay.style.display = 'none';
    }
  },

  openShortcutsModal() {
    const shortcutsOverlay = document.getElementById('shortcuts-overlay');
    if (shortcutsOverlay) {
      shortcutsOverlay.style.display = 'flex';
    }
  },

  closeShortcutsModal() {
    const shortcutsOverlay = document.getElementById('shortcuts-overlay');
    if (shortcutsOverlay) {
      shortcutsOverlay.style.display = 'none';
    }
  },

  // ─── HABITS MANAGEMENT ─────────────────────────────────────────────────────

  addHabit(label, base_color = 'indigo', shading_mode = 'count') {
    const trimmed = (label || '').trim();
    if (!trimmed) return;

    const validColors = ['indigo', 'crimson', 'matcha'];
    const validModes = ['count', 'duration'];

    const habit = {
      id: generateId(),
      label: trimmed,
      name: trimmed,
      base_color: validColors.includes(base_color) ? base_color : 'indigo',
      shading_mode: validModes.includes(shading_mode) ? shading_mode : 'count',
      createdAt: getTodayString(),
      entries: [],
      expanded: false
    };

    this.habits.push(habit);
    this.saveHabits();
    this.renderHabits();
  },

  editHabit(habitId, updates) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit) return;

    if (updates.label !== undefined) {
      const trimmed = updates.label.trim();
      if (trimmed) {
        habit.label = trimmed;
        habit.name = trimmed;
      }
    }
    if (['indigo', 'crimson', 'matcha'].includes(updates.base_color)) {
      habit.base_color = updates.base_color;
    }
    if (['count', 'duration'].includes(updates.shading_mode)) {
      habit.shading_mode = updates.shading_mode;
    }

    this.saveHabits();
    this.renderHabits();
  },

  deleteHabit(habitId) {
    const habit = this.habits.find(h => h.id === habitId);
    const habitName = habit ? ` "${habit.label || habit.name}"` : '';
    if (window.confirm(`Are you sure you want to remove the habit${habitName}?`)) {
      this.habits = this.habits.filter(h => h.id !== habitId);
      this.saveHabits();
      this.renderHabits();
    }
  },

  toggleHabitExpanded(habitId) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit) return;
    habit.expanded = !habit.expanded;
    this.saveHabits();
    this.renderHabits();
  },

  quickLogHabit(habitId) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit) return;

    const now = new Date();
    const today = getTodayString();
    const hours = String(now.getHours()).padStart(2, '0');
    const mins = String(now.getMinutes()).padStart(2, '0');
    const timeStr = `${hours}:${mins}`;

    const defaultDuration = 25;
    const defaultCount = 1;

    const entry = {
      id: generateId(),
      timestamp: now.toISOString(),
      date: today,
      time: timeStr,
      duration: defaultDuration,
      count: defaultCount,
      note: ''
    };

    if (!Array.isArray(habit.entries)) habit.entries = [];
    habit.entries.push(entry);

    this.saveHabits();
    this.renderHabits();
  },

  logHabitEntry(habitId, dateStr, timeStr, value, note) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit) return;

    const targetDate = dateStr || getTodayString();
    const targetTime = timeStr || '12:00';
    const numVal = Math.max(1, parseInt(value, 10) || 1);

    const entry = {
      id: generateId(),
      timestamp: `${targetDate}T${targetTime}:00`,
      date: targetDate,
      time: targetTime,
      duration: habit.shading_mode === 'duration' ? numVal : 25,
      count: habit.shading_mode === 'count' ? numVal : 1,
      note: (note || '').trim()
    };

    if (!Array.isArray(habit.entries)) habit.entries = [];
    habit.entries.push(entry);

    this.saveHabits();
    this.renderHabits();
  },

  deleteHabitEntry(habitId, entryId) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit || !Array.isArray(habit.entries)) return;

    habit.entries = habit.entries.filter(e => e.id !== entryId);
    this.saveHabits();
    this.renderHabits();
  },

  openLogEntryModal(habitId) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit) return;

    const overlay = document.getElementById('habit-entry-modal-overlay');
    const idInput = document.getElementById('entry-habit-id');
    const nameDisplay = document.getElementById('entry-habit-name-display');
    const dateInput = document.getElementById('entry-date');
    const timeInput = document.getElementById('entry-time');
    const valLabel = document.getElementById('entry-val-label');
    const valInput = document.getElementById('entry-val');
    const noteInput = document.getElementById('entry-note');

    if (idInput) idInput.value = habit.id;
    if (nameDisplay) nameDisplay.textContent = `Habit: ${habit.label || habit.name}`;
    if (dateInput) dateInput.value = getTodayString();

    const now = new Date();
    const curTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    if (timeInput) timeInput.value = curTime;

    if (valLabel) {
      valLabel.textContent = habit.shading_mode === 'duration' ? 'Duration (minutes)' : 'Completions / Count (×)';
    }
    if (valInput) {
      valInput.value = habit.shading_mode === 'duration' ? '30' : '1';
    }
    if (noteInput) noteInput.value = '';

    if (overlay) overlay.style.display = 'flex';
  },

  closeLogEntryModal() {
    const overlay = document.getElementById('habit-entry-modal-overlay');
    if (overlay) overlay.style.display = 'none';
  },

  openEditHabitModal(habitId) {
    const habit = this.habits.find(h => h.id === habitId);
    if (!habit) return;

    const overlay = document.getElementById('habit-edit-modal-overlay');
    const idInput = document.getElementById('edit-habit-id');
    const labelInput = document.getElementById('edit-habit-label');
    const modeSelect = document.getElementById('edit-habit-shading-mode');
    const colorValInput = document.getElementById('edit-habit-color-val');
    const colorPicker = document.getElementById('edit-habit-color-picker');

    if (idInput) idInput.value = habit.id;
    if (labelInput) labelInput.value = habit.label || habit.name || '';
    if (modeSelect) modeSelect.value = habit.shading_mode || 'count';
    if (colorValInput) colorValInput.value = habit.base_color || 'indigo';

    if (colorPicker) {
      colorPicker.querySelectorAll('.habit-color-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.color === (habit.base_color || 'indigo'));
      });
    }

    if (overlay) overlay.style.display = 'flex';
  },

  closeEditHabitModal() {
    const overlay = document.getElementById('habit-edit-modal-overlay');
    if (overlay) overlay.style.display = 'none';
  },

  // ─── POMODORO TIMER ────────────────────────────────────────────────────────

  initTimerDuration() {
    const workInput = document.getElementById('setting-work');
    const workMins = workInput ? parseInt(workInput.value, 10) || 25 : 25;
    this.timerSeconds = workMins * 60;
    this.timerMode = 'work';
    this.timerRunning = false;
    this.updateTimerDisplay();
  },

  getDurationForMode(mode) {
    if (mode === 'short-break') {
      const input = document.getElementById('setting-short-break');
      return (input ? parseInt(input.value, 10) || 5 : 5) * 60;
    }
    if (mode === 'long-break') {
      const input = document.getElementById('setting-long-break');
      return (input ? parseInt(input.value, 10) || 15 : 15) * 60;
    }
    const input = document.getElementById('setting-work');
    return (input ? parseInt(input.value, 10) || 25 : 25) * 60;
  },

  startTimer() {
    if (this.timerRunning) return;

    if (this.timerSeconds <= 0) {
      this.timerSeconds = this.getDurationForMode(this.timerMode);
    }

    this.timerRunning = true;
    this.timerEndTime = Date.now() + this.timerSeconds * 1000;

    const startBtn = document.getElementById('btn-timer-start');
    const pauseBtn = document.getElementById('btn-timer-pause');
    if (startBtn) startBtn.disabled = true;
    if (pauseBtn) {
      pauseBtn.disabled = false;
      pauseBtn.textContent = 'Pause';
    }

    this.startTimerLoop();
  },

  pauseTimer() {
    if (!this.timerRunning) return;

    this.timerRunning = false;
    if (this.timerRafId) {
      cancelAnimationFrame(this.timerRafId);
      this.timerRafId = null;
    }

    // Accurate calculation of remaining seconds
    this.timerSeconds = Math.max(0, Math.ceil((this.timerEndTime - Date.now()) / 1000));

    const startBtn = document.getElementById('btn-timer-start');
    const pauseBtn = document.getElementById('btn-timer-pause');
    if (startBtn) startBtn.disabled = true;
    if (pauseBtn) {
      pauseBtn.disabled = false;
      pauseBtn.textContent = 'Resume';
    }

    this.updateTimerDisplay();
  },

  resumeTimer() {
    if (this.timerRunning) return;

    this.timerRunning = true;
    this.timerEndTime = Date.now() + this.timerSeconds * 1000;

    const pauseBtn = document.getElementById('btn-timer-pause');
    if (pauseBtn) {
      pauseBtn.textContent = 'Pause';
    }

    this.startTimerLoop();
  },

  toggleTimer() {
    const pauseBtn = document.getElementById('btn-timer-pause');
    if (this.timerRunning) {
      this.pauseTimer();
    } else if (pauseBtn && pauseBtn.textContent === 'Resume') {
      this.resumeTimer();
    } else {
      this.startTimer();
    }
  },

  resetTimer() {
    this.timerRunning = false;
    if (this.timerRafId) {
      cancelAnimationFrame(this.timerRafId);
      this.timerRafId = null;
    }

    this.timerMode = 'work';
    const workInput = document.getElementById('setting-work');
    const workMins = workInput ? parseInt(workInput.value, 10) || 25 : 25;
    this.timerSeconds = workMins * 60;

    const startBtn = document.getElementById('btn-timer-start');
    const pauseBtn = document.getElementById('btn-timer-pause');
    const timerLabel = document.getElementById('timer-label');

    if (startBtn) startBtn.disabled = false;
    if (pauseBtn) {
      pauseBtn.disabled = true;
      pauseBtn.textContent = 'Pause';
    }
    if (timerLabel) {
      timerLabel.textContent = 'Work Session';
    }

    this.updateTimerDisplay();
  },

  startTimerLoop() {
    const step = () => {
      if (!this.timerRunning) return;

      const now = Date.now();
      const remaining = Math.max(0, Math.ceil((this.timerEndTime - now) / 1000));

      if (remaining !== this.timerSeconds) {
        this.timerSeconds = remaining;
        this.updateTimerDisplay();
      }

      if (remaining <= 0) {
        this.handleTimerComplete();
      } else {
        this.timerRafId = requestAnimationFrame(step);
      }
    };

    this.timerRafId = requestAnimationFrame(step);
  },

  handleTimerComplete() {
    this.timerRunning = false;
    if (this.timerRafId) {
      cancelAnimationFrame(this.timerRafId);
      this.timerRafId = null;
    }

    // Play completion chime
    playTimerChime();

    const workInput = document.getElementById('setting-work');
    const workMins = workInput ? parseInt(workInput.value, 10) || 25 : 25;
    const sessionsSettingInput = document.getElementById('setting-sessions');
    const targetSessions = sessionsSettingInput ? parseInt(sessionsSettingInput.value, 10) || 4 : 4;

    this.checkPomoDayReset();

    const timerLabel = document.getElementById('timer-label');

    if (this.timerMode === 'work') {
      this.pomo.totalSessions++;
      this.pomo.todaySessions++;
      this.pomo.totalMinutes += workMins;
      this.pomo.todayMinutes += workMins;
      this.timerSessionCount++;
      this.savePomo();

      if (this.timerSessionCount >= targetSessions) {
        this.timerMode = 'long-break';
        this.timerSessionCount = 0;
        this.timerSeconds = this.getDurationForMode('long-break');
        if (timerLabel) timerLabel.textContent = 'Long Break';
      } else {
        this.timerMode = 'short-break';
        this.timerSeconds = this.getDurationForMode('short-break');
        if (timerLabel) timerLabel.textContent = 'Short Break';
      }
    } else {
      // Completed break, back to work
      this.timerMode = 'work';
      this.timerSeconds = this.getDurationForMode('work');
      if (timerLabel) timerLabel.textContent = 'Work Session';
    }

    this.updateTimerDisplay();
    this.renderFocus();
    this.renderStats();

    // Auto-start next session
    this.timerRunning = true;
    this.timerEndTime = Date.now() + this.timerSeconds * 1000;
    const startBtn = document.getElementById('btn-timer-start');
    const pauseBtn = document.getElementById('btn-timer-pause');
    if (startBtn) startBtn.disabled = true;
    if (pauseBtn) {
      pauseBtn.disabled = false;
      pauseBtn.textContent = 'Pause';
    }
    this.startTimerLoop();
  },

  updateTimerDisplay() {
    const mins = Math.floor(this.timerSeconds / 60);
    const secs = this.timerSeconds % 60;
    const minEl = document.getElementById('timer-minutes');
    const secEl = document.getElementById('timer-seconds');
    if (minEl) minEl.textContent = String(mins).padStart(2, '0');
    if (secEl) secEl.textContent = String(secs).padStart(2, '0');
  },

  // ─── BULK ACTIONS ──────────────────────────────────────────────────────────

  clearCompletedTasks() {
    const doneCount = this.tasks.filter(t => t.done).length;
    if (doneCount === 0) return;

    if (window.confirm(`Clear all ${doneCount} completed task${doneCount > 1 ? 's' : ''}?`)) {
      this.tasks = this.tasks.filter(t => !t.done);
      this.saveTasks();
      this.render();
    }
  },

  exportTasks() {
    try {
      const dataStr = JSON.stringify(this.tasks, null, 2);
      const blob = new Blob([dataStr], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `mahaam-tasks-${getTodayString()}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error('Failed to export tasks:', e);
    }
  },

  // ─── RENDERING MASTER DISPATCHER ───────────────────────────────────────────

  render() {
    this.renderDashboard();
    this.renderMatrix();
    this.renderTasks();
    this.renderFocus();
    this.renderHabits();
    this.renderStats();
  },

  // ─── RENDER: DASHBOARD ─────────────────────────────────────────────────────

  renderDashboard() {
    const totalCount = this.tasks.length;
    const doneCount = this.tasks.filter(t => t.done).length;
    const pendingCount = totalCount - doneCount;

    const statTotal = document.getElementById('stat-total');
    const statDone = document.getElementById('stat-done');
    const statPending = document.getElementById('stat-pending');
    const statStreak = document.getElementById('stat-streak');

    if (statTotal) statTotal.textContent = totalCount;
    if (statDone) statDone.textContent = doneCount;
    if (statPending) statPending.textContent = pendingCount;
    if (statStreak) statStreak.textContent = this.streak.count || 0;

    // Render today's pending tasks
    const todayTasksContainer = document.getElementById('today-tasks');
    if (todayTasksContainer) {
      const today = getTodayString();
      // Tasks due today or overdue or created today
      let todayList = this.tasks.filter(t => {
        if (t.done) return false;
        if (t.dueDate) {
          return t.dueDate <= today;
        }
        return t.createdAt.slice(0, 10) === today;
      });

      // If no tasks specifically matched today's date, show top pending tasks
      if (todayList.length === 0) {
        todayList = this.tasks.filter(t => !t.done).slice(0, 5);
      }

      if (todayList.length === 0) {
        todayTasksContainer.innerHTML = '<p class="empty-state"><em>No tasks yet — add one above</em></p>';
      } else {
        let html = '';
        todayList.forEach(task => {
          const quadName = QUADRANT_NAMES[task.quadrant] || task.quadrant;
          const prioName = PRIORITY_NAMES[task.priority] || task.priority;
          let meta = `${escapeHtml(quadName)} · ${escapeHtml(prioName)}`;
          if (task.dueDate) {
            meta += ` · Due ${formatDisplayDate(task.dueDate)}`;
          }

          html += `
            <div class='task-item' data-id='${task.id}'>
              <button class='task-checkbox' onclick='app.toggleTask("${task.id}")' aria-label="Complete task">
                <span class='check-mark'>✓</span>
              </button>
              <div class='task-content'>
                <span class='task-title'>${escapeHtml(task.title)}</span>
                <span class='task-meta'>${meta}</span>
                ${task.notes ? `<span class='task-notes-preview'>${escapeHtml(task.notes)}</span>` : ''}
              </div>
              <button class='task-edit-btn' onclick='app.openEditModal("${task.id}")'>Edit</button>
            </div>
          `;
        });
        todayTasksContainer.innerHTML = html;
      }
    }
  },

  // ─── RENDER: MATRIX ────────────────────────────────────────────────────────

  renderMatrix() {
    const quadrantIds = {
      'high-impact-low-effort': 'q-high-impact-low-effort',
      'high-impact-high-effort': 'q-high-impact-high-effort',
      'low-impact-low-effort': 'q-low-impact-low-effort',
      'low-impact-high-effort': 'q-low-impact-high-effort'
    };

    // Clear quadrant containers
    Object.values(quadrantIds).forEach(elemId => {
      const el = document.getElementById(elemId);
      if (el) el.innerHTML = '';
    });

    // Populate each quadrant
    this.tasks.forEach(task => {
      const containerId = quadrantIds[task.quadrant];
      if (!containerId) return;
      const container = document.getElementById(containerId);
      if (!container) return;

      if (!task.done) {
        // Pending draggable chip
        const chip = document.createElement('div');
        chip.className = 'matrix-task-chip';
        chip.setAttribute('draggable', 'true');
        chip.setAttribute('data-id', task.id);
        chip.setAttribute('ondragstart', 'app.handleDragStart(event)');
        chip.setAttribute('title', task.title);

        chip.innerHTML = `
          <span class='chip-checkbox' onclick='event.stopPropagation(); app.toggleTask("${task.id}")'></span>
          <span class='chip-title' onclick='app.openEditModal("${task.id}")'>${escapeHtml(task.title)}</span>
        `;
        container.appendChild(chip);
      } else {
        // Completed task chip (muted & strike-through)
        const chip = document.createElement('div');
        chip.className = 'matrix-task-chip done';
        chip.setAttribute('draggable', 'false');
        chip.setAttribute('data-id', task.id);
        chip.setAttribute('title', task.title);

        chip.innerHTML = `
          <span class='chip-checkbox checked' onclick='event.stopPropagation(); app.toggleTask("${task.id}")'>✓</span>
          <span class='chip-title' onclick='app.openEditModal("${task.id}")'>${escapeHtml(task.title)}</span>
        `;
        container.appendChild(chip);
      }
    });
  },

  // ─── RENDER: ALL TASKS ─────────────────────────────────────────────────────

  renderTasks() {
    const searchInput = document.getElementById('task-search');
    const filterQuadrant = document.getElementById('filter-quadrant');
    const filterStatus = document.getElementById('filter-status');
    const filterPriority = document.getElementById('filter-priority');
    const taskList = document.getElementById('all-tasks-list');
    const bulkActions = document.getElementById('bulk-actions');

    if (!taskList) return;

    const query = searchInput ? searchInput.value.trim().toLowerCase() : '';
    const qFilter = filterQuadrant ? filterQuadrant.value : 'all';
    const sFilter = filterStatus ? filterStatus.value : 'all';
    const pFilter = filterPriority ? filterPriority.value : 'all';

    // Show or hide bulk actions based on total task count
    if (bulkActions) {
      bulkActions.style.display = this.tasks.length > 0 ? 'flex' : 'none';
    }

    // Filter tasks
    let filtered = this.tasks.filter(task => {
      if (qFilter !== 'all' && task.quadrant !== qFilter) return false;
      if (sFilter === 'pending' && task.done) return false;
      if (sFilter === 'done' && !task.done) return false;
      if (pFilter !== 'all' && task.priority !== pFilter) return false;

      if (query) {
        const titleMatch = task.title && task.title.toLowerCase().includes(query);
        const notesMatch = task.notes && task.notes.toLowerCase().includes(query);
        if (!titleMatch && !notesMatch) return false;
      }
      return true;
    });

    // Priority ordering helper
    const prioOrder = { high: 1, medium: 2, low: 3 };

    // Sort: pending tasks first (by priority, then newer), completed tasks at the bottom
    filtered.sort((a, b) => {
      if (a.done !== b.done) {
        return a.done ? 1 : -1;
      }
      if (!a.done) {
        const pA = prioOrder[a.priority] || 2;
        const pB = prioOrder[b.priority] || 2;
        if (pA !== pB) return pA - pB;
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      }
      return new Date(b.completedAt || b.createdAt).getTime() - new Date(a.completedAt || a.createdAt).getTime();
    });

    if (filtered.length === 0) {
      const emptyMsg = this.tasks.length === 0 ? 'No tasks yet' : 'No matching tasks found';
      taskList.innerHTML = `<p class="empty-state"><em>${emptyMsg}</em></p>`;
      return;
    }

    let html = '';
    filtered.forEach(task => {
      const quadName = QUADRANT_NAMES[task.quadrant] || task.quadrant;
      const prioName = PRIORITY_NAMES[task.priority] || task.priority;
      let meta = `${escapeHtml(quadName)} · ${escapeHtml(prioName)}`;
      if (task.dueDate) {
        meta += ` · Due ${formatDisplayDate(task.dueDate)}`;
      }

      const checkboxClass = task.done ? 'task-checkbox checked' : 'task-checkbox';
      const titleClass = task.done ? 'task-title done' : 'task-title';
      const notesHtml = task.notes ? `<span class='task-notes-preview'>${escapeHtml(task.notes)}</span>` : '';

      html += `
        <div class='task-item' data-id='${task.id}'>
          <button class='${checkboxClass}' onclick='app.toggleTask("${task.id}")' aria-label="Toggle task status">
            <span class='check-mark'>✓</span>
          </button>
          <div class='task-content'>
            <span class='${titleClass}'>${escapeHtml(task.title)}</span>
            <span class='task-meta'>${meta}</span>
            ${notesHtml}
          </div>
          <button class='task-edit-btn' onclick='app.openEditModal("${task.id}")'>Edit</button>
        </div>
      `;
    });

    taskList.innerHTML = html;
  },

  // ─── RENDER: FOCUS ─────────────────────────────────────────────────────────

  renderFocus() {
    const pomoToday = document.getElementById('pomo-today');
    const pomoTotal = document.getElementById('pomo-total');
    const pomoSession = document.getElementById('pomo-session');
    const select = document.getElementById('focus-task-select');

    if (pomoToday) pomoToday.textContent = this.pomo.todaySessions;
    if (pomoTotal) pomoTotal.textContent = this.pomo.totalSessions;
    if (pomoSession) pomoSession.textContent = this.timerSessionCount;

    if (select) {
      const previousValue = select.value;
      const pendingTasks = this.tasks.filter(t => !t.done);

      let options = '<option value="">— select a task —</option>';
      pendingTasks.forEach(task => {
        const isSelected = task.id === previousValue ? 'selected' : '';
        options += `<option value="${task.id}" ${isSelected}>${escapeHtml(task.title)}</option>`;
      });

      select.innerHTML = options;
    }
  },

  // ─── RENDER: HABITS ────────────────────────────────────────────────────────

  renderHabits() {
    const habitsList = document.getElementById('habits-list');
    if (!habitsList) return;

    if (this.habits.length === 0) {
      habitsList.innerHTML = '<p class="empty-state"><em>No habits tracked yet — create your first habit above</em></p>';
      return;
    }

    const last30Days = getLast30Days();
    let html = '';

    this.habits.forEach(habit => {
      const color = habit.base_color || 'indigo';
      const [r, g, b] = HABIT_BASE_RGBS[color] || [74, 88, 153];
      const isDuration = habit.shading_mode === 'duration';
      const unitLabel = isDuration ? 'min' : '×';
      const modeText = isDuration ? 'Duration (min)' : 'Count (×)';

      // 1. Compute 30-day aggregate values
      const dailyAggregates = last30Days.map(day => {
        const dayEntries = (habit.entries || []).filter(e => e.date === day.dateStr);
        let val = 0;
        if (isDuration) {
          val = dayEntries.reduce((sum, e) => sum + (parseInt(e.duration, 10) || 0), 0);
        } else {
          val = dayEntries.reduce((sum, e) => sum + (parseInt(e.count, 10) || 1), 0);
        }
        return {
          ...day,
          val,
          entryCount: dayEntries.length
        };
      });

      const maxValue = Math.max(0, ...dailyAggregates.map(d => d.val));
      const total30Val = dailyAggregates.reduce((sum, d) => sum + d.val, 0);
      const totalEntriesAllTime = (habit.entries || []).length;

      // 2. Build 30-day strip blocks
      let stripBlocksHtml = '';
      dailyAggregates.forEach(d => {
        const intensity = maxValue > 0 ? (d.val / maxValue) : 0;
        let bgStyle = 'background-color: var(--bg);';
        let borderStyle = 'border-color: var(--border);';

        if (d.val > 0) {
          const alpha = (0.22 + intensity * 0.78).toFixed(2);
          bgStyle = `background-color: rgba(${r}, ${g}, ${b}, ${alpha});`;
          borderStyle = `border-color: rgba(${r}, ${g}, ${b}, ${(0.4 + intensity * 0.5).toFixed(2)});`;
        }

        const isTodayClass = d.isToday ? 'is-today' : '';
        const exactValText = d.val > 0 ? `${d.val} ${unitLabel}` : 'No activity';
        const tooltipText = `${formatDisplayDate(d.dateStr)} · ${exactValText}`;

        stripBlocksHtml += `
          <div class="habit-day-block ${isTodayClass}" style="${bgStyle} ${borderStyle}">
            <div class="day-tooltip">${escapeHtml(tooltipText)}</div>
          </div>
        `;
      });

      // 3. Build full history log (chronological from day 1 to today)
      let historyHtml = '';
      if (habit.expanded) {
        const allEntries = [...(habit.entries || [])].sort((a, b) => {
          const tA = new Date(a.timestamp || `${a.date}T${a.time || '12:00'}:00`).getTime();
          const tB = new Date(b.timestamp || `${b.date}T${b.time || '12:00'}:00`).getTime();
          return tA - tB; // Chronological from first recorded entry
        });

        if (allEntries.length === 0) {
          historyHtml = `
            <div class="habit-history-panel">
              <div class="habit-history-header">
                <span class="habit-history-title">Full History Log (Day 1 → Today)</span>
              </div>
              <div class="history-empty-log">
                No entries recorded yet. Click <strong>+ Quick</strong> or <strong>+ Log</strong> to add your first session.
              </div>
            </div>
          `;
        } else {
          let rowsHtml = '';
          allEntries.forEach(entry => {
            const entryVal = isDuration ? `${entry.duration || 25} min` : `${entry.count || 1}×`;
            const badgeStyle = `background-color: rgba(${r}, ${g}, ${b}, 0.15); color: rgb(${r}, ${g}, ${b}); border: 1px solid rgba(${r}, ${g}, ${b}, 0.35);`;
            const noteText = entry.note ? `<span class="history-entry-note" title="${escapeHtml(entry.note)}">${escapeHtml(entry.note)}</span>` : '';
            const displayTime = entry.time || '12:00';

            rowsHtml += `
              <div class="history-entry-row">
                <div class="history-entry-left">
                  <span class="history-entry-date">${formatDisplayDate(entry.date)}</span>
                  <span class="history-entry-time">${escapeHtml(displayTime)}</span>
                  <span class="history-entry-badge" style="${badgeStyle}">${entryVal}</span>
                  ${noteText}
                </div>
                <button type="button" class="history-entry-delete" onclick="event.stopPropagation(); app.deleteHabitEntry('${habit.id}', '${entry.id}')" title="Delete entry">&times;</button>
              </div>
            `;
          });

          historyHtml = `
            <div class="habit-history-panel">
              <div class="habit-history-header">
                <span class="habit-history-title">Full History Log · ${allEntries.length} ${allEntries.length === 1 ? 'entry' : 'entries'} (Day 1 → Today)</span>
              </div>
              <div class="habit-history-list">
                ${rowsHtml}
              </div>
            </div>
          `;
        }
      }

      // Summary label
      const summaryStats = isDuration
        ? `Last 30d: ${total30Val} min · Peak: ${maxValue} min`
        : `Last 30d: ${total30Val}× · Peak: ${maxValue}×`;

      html += `
        <div class="habit-card" data-id="${habit.id}">
          <div class="habit-card-header" onclick="app.toggleHabitExpanded('${habit.id}')" title="Click to ${habit.expanded ? 'collapse' : 'view full history'}">
            <div class="habit-title-group">
              <span class="habit-color-swatch ${color}"></span>
              <h3 class="habit-label">${escapeHtml(habit.label)}</h3>
              <span class="habit-mode-badge">${modeText}</span>
            </div>
            <div class="habit-card-actions" onclick="event.stopPropagation()">
              <button type="button" class="habit-action-btn primary" onclick="app.quickLogHabit('${habit.id}')" title="Quick record for today">+ Quick</button>
              <button type="button" class="habit-action-btn" onclick="app.openLogEntryModal('${habit.id}')" title="Log session with custom time & note">+ Log</button>
              <button type="button" class="habit-action-btn" onclick="app.openEditHabitModal('${habit.id}')" title="Edit habit configuration">Edit</button>
              <button type="button" class="habit-action-btn" onclick="app.toggleHabitExpanded('${habit.id}')" title="Toggle full history list">
                ${habit.expanded ? '▲ Hide' : `▼ History (${totalEntriesAllTime})`}
              </button>
            </div>
          </div>

          <div class="habit-strip-section" onclick="app.toggleHabitExpanded('${habit.id}')" title="Click to ${habit.expanded ? 'collapse' : 'view full history'}">
            <div class="habit-strip-meta">
              <span class="habit-strip-stats">${summaryStats}</span>
              <span class="habit-strip-max">Peak intensity: ${maxValue > 0 ? (maxValue + ' ' + unitLabel) : '0'}</span>
            </div>
            <div class="habit-strip-scroll">
              <div class="habit-strip">
                ${stripBlocksHtml}
              </div>
            </div>
            <div class="habit-strip-labels">
              <span>30 days ago</span>
              <span>Today</span>
            </div>
          </div>

          ${historyHtml}
        </div>
      `;
    });

    habitsList.innerHTML = html;
  },

  // ─── RENDER: ANALYTICS & STATS ─────────────────────────────────────────────

  renderStats() {
    // 1. Quadrant distribution counts & bar tracks
    const countQ1 = this.tasks.filter(t => t.quadrant === 'high-impact-low-effort').length;
    const countQ2 = this.tasks.filter(t => t.quadrant === 'high-impact-high-effort').length;
    const countQ3 = this.tasks.filter(t => t.quadrant === 'low-impact-low-effort').length;
    const countQ4 = this.tasks.filter(t => t.quadrant === 'low-impact-high-effort').length;
    const total = this.tasks.length;

    const elCountQ1 = document.getElementById('count-q1');
    const elCountQ2 = document.getElementById('count-q2');
    const elCountQ3 = document.getElementById('count-q3');
    const elCountQ4 = document.getElementById('count-q4');

    if (elCountQ1) elCountQ1.textContent = countQ1;
    if (elCountQ2) elCountQ2.textContent = countQ2;
    if (elCountQ3) elCountQ3.textContent = countQ3;
    if (elCountQ4) elCountQ4.textContent = countQ4;

    const barQ1 = document.getElementById('bar-q1');
    const barQ2 = document.getElementById('bar-q2');
    const barQ3 = document.getElementById('bar-q3');
    const barQ4 = document.getElementById('bar-q4');

    if (barQ1) barQ1.style.width = total > 0 ? `${(countQ1 / total) * 100}%` : '0%';
    if (barQ2) barQ2.style.width = total > 0 ? `${(countQ2 / total) * 100}%` : '0%';
    if (barQ3) barQ3.style.width = total > 0 ? `${(countQ3 / total) * 100}%` : '0%';
    if (barQ4) barQ4.style.width = total > 0 ? `${(countQ4 / total) * 100}%` : '0%';

    // 2. Productivity score: weighted by quadrant
    let totalWeighted = 0;
    let completedWeighted = 0;
    const weights = {
      'high-impact-low-effort': 3,
      'high-impact-high-effort': 4,
      'low-impact-low-effort': 1.5,
      'low-impact-high-effort': 1
    };

    this.tasks.forEach(t => {
      const w = weights[t.quadrant] || 2;
      totalWeighted += w;
      if (t.done) completedWeighted += w;
    });

    let score = 0;
    const prodScoreEl = document.getElementById('prod-score');
    const scoreExplainEl = document.getElementById('score-explain');

    if (totalWeighted > 0) {
      const baseScore = (completedWeighted / totalWeighted) * 80;
      const streakBonus = Math.min(10, (this.streak.count || 0) * 2);
      const focusBonus = Math.min(10, (this.pomo.todaySessions || 0) * 2.5);
      score = Math.min(100, Math.round(baseScore + streakBonus + focusBonus));
    } else {
      score = 0;
    }

    if (prodScoreEl) prodScoreEl.textContent = score;

    if (scoreExplainEl) {
      if (score >= 80) {
        scoreExplainEl.innerHTML = '<em>Outstanding! You are focusing consistently on high-impact priorities.</em>';
      } else if (score >= 50) {
        scoreExplainEl.innerHTML = '<em>Good momentum! Tackle more Major Projects and Quick Wins to increase your score.</em>';
      } else if (score > 0) {
        scoreExplainEl.innerHTML = '<em>Complete more high-impact tasks to increase your score.</em>';
      } else {
        scoreExplainEl.innerHTML = '<em>Add tasks and complete high-impact work to build your productivity score.</em>';
      }
    }

    // 3. Focus time stats
    const focusTodayEl = document.getElementById('focus-today-min');
    const focusTotalEl = document.getElementById('focus-total-min');
    if (focusTodayEl) focusTodayEl.textContent = this.pomo.todayMinutes;
    if (focusTotalEl) focusTotalEl.textContent = this.pomo.totalMinutes;

    // 4. Canvas weekly bar chart
    this.drawWeeklyChart();
  },

  drawWeeklyChart() {
    const canvas = document.getElementById('canvas-weekly');
    if (!canvas) return;

    const parent = canvas.parentElement;
    const width = parent ? parent.clientWidth || 700 : 700;
    const height = 240;
    const dpr = window.devicePixelRatio || 1;

    canvas.width = width * dpr;
    canvas.height = height * dpr;
    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.save();
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, width, height);

    const isNight = document.documentElement.getAttribute('data-theme') === 'night';
    const ink = document.documentElement.getAttribute('data-ink') || 'indigo';

    // Theme & ink colors
    const computedAccent = getComputedStyle(document.documentElement).getPropertyValue('--accent').trim();
    const accentColor = computedAccent || INK_HEX_FALLBACKS[ink] || '#4338CA';
    const textColor = isNight ? '#E5E7EB' : '#374151';
    const mutedColor = isNight ? '#9CA3AF' : '#6B7280';
    const gridColor = isNight ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)';
    const trackColor = isNight ? 'rgba(255, 255, 255, 0.03)' : 'rgba(0, 0, 0, 0.02)';

    const last7Days = getLast7Days();
    const counts = last7Days.map(d => this.dailyLog[d.dateStr] || 0);
    const maxVal = Math.max(5, ...counts);
    const chartMax = Math.ceil(maxVal * 1.15);

    const padding = { left: 45, right: 25, top: 30, bottom: 42 };
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    // Gridlines & Y-axis labels
    const gridSteps = 3;
    ctx.textAlign = 'right';
    ctx.textBaseline = 'middle';
    ctx.font = '10px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
    ctx.fillStyle = mutedColor;

    for (let i = 0; i <= gridSteps; i++) {
      const stepVal = Math.round((chartMax / gridSteps) * (gridSteps - i));
      const y = padding.top + (chartH / gridSteps) * i;

      ctx.beginPath();
      ctx.strokeStyle = gridColor;
      ctx.lineWidth = 1;
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();

      ctx.fillText(String(stepVal), padding.left - 10, y);
    }

    // Baseline
    const baselineY = padding.top + chartH;
    ctx.beginPath();
    ctx.strokeStyle = gridColor;
    ctx.lineWidth = 1.5;
    ctx.moveTo(padding.left, baselineY);
    ctx.lineTo(width - padding.right, baselineY);
    ctx.stroke();

    // Bars
    const colWidth = chartW / 7;
    const barWidth = Math.min(40, Math.max(16, colWidth * 0.45));

    last7Days.forEach((day, idx) => {
      const count = counts[idx];
      const centerX = padding.left + colWidth * idx + colWidth / 2;
      const barX = centerX - barWidth / 2;
      const barHeight = count > 0 ? (count / chartMax) * chartH : 0;
      const barY = baselineY - barHeight;

      // Draw light track column
      ctx.fillStyle = trackColor;
      ctx.fillRect(barX, padding.top, barWidth, chartH);

      // Draw bar
      if (count > 0) {
        ctx.fillStyle = accentColor;
        this.fillRoundedRect(ctx, barX, barY, barWidth, barHeight, Math.min(4, barWidth / 2));

        // Draw count above bar
        ctx.fillStyle = textColor;
        ctx.font = '600 11px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'bottom';
        ctx.fillText(String(count), centerX, barY - 4);
      }

      // Day label
      ctx.fillStyle = day.isToday ? accentColor : mutedColor;
      ctx.font = day.isToday
        ? '600 11px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif'
        : '400 11px -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'top';

      const labelText = day.isToday ? 'Today' : `${day.weekday} ${day.dayNum}`;
      ctx.fillText(labelText, centerX, baselineY + 8);
    });

    ctx.restore();
  },

  fillRoundedRect(ctx, x, y, width, height, radius) {
    if (width <= 0 || height <= 0) return;
    const r = Math.min(radius, width / 2, height / 2);
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + width - r, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + r);
    ctx.lineTo(x + width, y + height);
    ctx.lineTo(x, y + height);
    ctx.lineTo(x, y + r);
    ctx.quadraticCurveTo(x, y, x + r, y);
    ctx.closePath();
    ctx.fill();
  },

  // ─── EVENT LISTENERS SETUP ─────────────────────────────────────────────────

  bindEvents() {
    // 1. Navigation switching (Desktop)
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', () => {
        const view = item.getAttribute('data-view');
        if (view) this.switchView(view);
      });
    });

    // 1b. Mobile Navigation switching (Phones)
    document.querySelectorAll('.mobile-nav-item').forEach(item => {
      item.addEventListener('click', () => {
        const view = item.getAttribute('data-view');
        if (view) this.switchView(view);
      });
    });

    // 2. Theme switcher buttons (Desktop)
    document.querySelectorAll('.theme-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const theme = btn.getAttribute('data-theme');
        if (theme) this.setTheme(theme);
      });
    });

    // 2b. Mobile Theme toggle button (Phones)
    const mobileThemeBtn = document.getElementById('mobile-theme-btn');
    if (mobileThemeBtn) {
      mobileThemeBtn.addEventListener('click', () => {
        const curTheme = document.documentElement.getAttribute('data-theme') || 'day';
        this.setTheme(curTheme === 'day' ? 'night' : 'day');
      });
    }

    // 2c. Register ServiceWorker if on web server (PWA offline for Android phones)
    if ('serviceWorker' in navigator && window.location.protocol.startsWith('http')) {
      navigator.serviceWorker.register('sw.js').catch(err => {
        console.warn('Service worker registration note:', err);
      });
    }

    // 3. Ink color selector buttons
    document.querySelectorAll('.ink-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const ink = btn.getAttribute('data-ink');
        if (ink) this.setInk(ink);
      });
    });

    // 4. Quick Add form (Dashboard)
    const quickAddForm = document.getElementById('quick-add-form');
    if (quickAddForm) {
      quickAddForm.addEventListener('submit', e => {
        e.preventDefault();
        const input = document.getElementById('quick-add-input');
        const quadrantSelect = document.getElementById('quick-add-quadrant');
        if (!input) return;

        const title = input.value.trim();
        const quadrant = quadrantSelect ? quadrantSelect.value : 'high-impact-low-effort';

        if (title) {
          this.addTask(title, quadrant, 'medium', '', null);
          input.value = '';
        }
      });
    }

    // 5. Full Add Task form (Tasks View)
    const addTaskForm = document.getElementById('add-task-form');
    if (addTaskForm) {
      addTaskForm.addEventListener('submit', e => {
        e.preventDefault();
        const titleEl = document.getElementById('task-title');
        const notesEl = document.getElementById('task-notes');
        const quadEl = document.getElementById('task-quadrant');
        const prioEl = document.getElementById('task-priority');
        const dueEl = document.getElementById('task-due');

        if (!titleEl) return;
        const title = titleEl.value.trim();
        const notes = notesEl ? notesEl.value.trim() : '';
        const quad = quadEl ? quadEl.value : 'high-impact-low-effort';
        const prio = prioEl ? prioEl.value : 'medium';
        const due = dueEl ? dueEl.value || null : null;

        if (title) {
          this.addTask(title, quad, prio, notes, due);
          addTaskForm.reset();
        }
      });
    }

    // 6. Search & Filters (Tasks View)
    const searchInput = document.getElementById('task-search');
    if (searchInput) {
      searchInput.addEventListener('input', () => this.renderTasks());
    }

    const filterQuadrant = document.getElementById('filter-quadrant');
    if (filterQuadrant) {
      filterQuadrant.addEventListener('change', () => this.renderTasks());
    }

    const filterStatus = document.getElementById('filter-status');
    if (filterStatus) {
      filterStatus.addEventListener('change', () => this.renderTasks());
    }

    const filterPriority = document.getElementById('filter-priority');
    if (filterPriority) {
      filterPriority.addEventListener('change', () => this.renderTasks());
    }

    // 7. Bulk Actions
    const clearDoneBtn = document.getElementById('btn-clear-done');
    if (clearDoneBtn) {
      clearDoneBtn.addEventListener('click', () => this.clearCompletedTasks());
    }

    const exportBtn = document.getElementById('btn-export');
    if (exportBtn) {
      exportBtn.addEventListener('click', () => this.exportTasks());
    }

    // 8. Habit Add form & color selection
    const addHabitForm = document.getElementById('add-habit-form');
    if (addHabitForm) {
      // Color buttons selector inside add habit form
      const colorBtns = addHabitForm.querySelectorAll('.habit-color-btn');
      colorBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          colorBtns.forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
        });
      });

      addHabitForm.addEventListener('submit', e => {
        e.preventDefault();
        const input = document.getElementById('habit-name-input');
        const modeSelect = document.getElementById('habit-shading-mode');
        const activeColorBtn = addHabitForm.querySelector('.habit-color-btn.active');
        const color = activeColorBtn ? activeColorBtn.dataset.color : 'indigo';
        const mode = modeSelect ? modeSelect.value : 'count';

        if (input) {
          const label = input.value.trim();
          if (label) {
            this.addHabit(label, color, mode);
            input.value = '';
          }
        }
      });
    }

    // 8b. Habit Entry Logging Form & Modal
    const entryForm = document.getElementById('habit-entry-form');
    if (entryForm) {
      entryForm.addEventListener('submit', e => {
        e.preventDefault();
        const habitId = document.getElementById('entry-habit-id')?.value;
        const date = document.getElementById('entry-date')?.value;
        const time = document.getElementById('entry-time')?.value;
        const val = document.getElementById('entry-val')?.value;
        const note = document.getElementById('entry-note')?.value;

        if (habitId) {
          this.logHabitEntry(habitId, date, time, val, note);
          this.closeLogEntryModal();
        }
      });
    }

    const closeEntryModalBtn = document.getElementById('habit-entry-modal-close');
    if (closeEntryModalBtn) {
      closeEntryModalBtn.addEventListener('click', () => this.closeLogEntryModal());
    }

    const cancelEntryBtn = document.getElementById('btn-cancel-entry');
    if (cancelEntryBtn) {
      cancelEntryBtn.addEventListener('click', () => this.closeLogEntryModal());
    }

    const entryOverlay = document.getElementById('habit-entry-modal-overlay');
    if (entryOverlay) {
      entryOverlay.addEventListener('click', e => {
        if (e.target === entryOverlay) this.closeLogEntryModal();
      });
    }

    // 8c. Habit Edit Form & Modal
    const editHabitColorPicker = document.getElementById('edit-habit-color-picker');
    if (editHabitColorPicker) {
      const editColorBtns = editHabitColorPicker.querySelectorAll('.habit-color-btn');
      editColorBtns.forEach(btn => {
        btn.addEventListener('click', () => {
          editColorBtns.forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
          const colorInput = document.getElementById('edit-habit-color-val');
          if (colorInput) colorInput.value = btn.dataset.color;
        });
      });
    }

    const editHabitForm = document.getElementById('habit-edit-form');
    if (editHabitForm) {
      editHabitForm.addEventListener('submit', e => {
        e.preventDefault();
        const habitId = document.getElementById('edit-habit-id')?.value;
        const label = document.getElementById('edit-habit-label')?.value;
        const color = document.getElementById('edit-habit-color-val')?.value || 'indigo';
        const mode = document.getElementById('edit-habit-shading-mode')?.value || 'count';

        if (habitId) {
          this.editHabit(habitId, { label, base_color: color, shading_mode: mode });
          this.closeEditHabitModal();
        }
      });
    }

    const closeEditHabitBtn = document.getElementById('habit-edit-modal-close');
    if (closeEditHabitBtn) {
      closeEditHabitBtn.addEventListener('click', () => this.closeEditHabitModal());
    }

    const cancelEditHabitBtn = document.getElementById('btn-cancel-edit-habit');
    if (cancelEditHabitBtn) {
      cancelEditHabitBtn.addEventListener('click', () => this.closeEditHabitModal());
    }

    const deleteHabitModalBtn = document.getElementById('btn-delete-habit-modal');
    if (deleteHabitModalBtn) {
      deleteHabitModalBtn.addEventListener('click', () => {
        const habitId = document.getElementById('edit-habit-id')?.value;
        if (habitId) {
          this.deleteHabit(habitId);
          this.closeEditHabitModal();
        }
      });
    }

    const editHabitOverlay = document.getElementById('habit-edit-modal-overlay');
    if (editHabitOverlay) {
      editHabitOverlay.addEventListener('click', e => {
        if (e.target === editHabitOverlay) this.closeEditHabitModal();
      });
    }

    // 9. Pomodoro Timer Controls
    const startTimerBtn = document.getElementById('btn-timer-start');
    if (startTimerBtn) {
      startTimerBtn.addEventListener('click', () => this.startTimer());
    }

    const pauseTimerBtn = document.getElementById('btn-timer-pause');
    if (pauseTimerBtn) {
      pauseTimerBtn.addEventListener('click', () => {
        if (this.timerRunning) {
          this.pauseTimer();
        } else {
          this.resumeTimer();
        }
      });
    }

    const resetTimerBtn = document.getElementById('btn-timer-reset');
    if (resetTimerBtn) {
      resetTimerBtn.addEventListener('click', () => this.resetTimer());
    }

    // Timer Setting Changes (update remaining time when idle)
    const settingWork = document.getElementById('setting-work');
    if (settingWork) {
      settingWork.addEventListener('change', () => {
        if (!this.timerRunning && this.timerMode === 'work') {
          this.timerSeconds = this.getDurationForMode('work');
          this.updateTimerDisplay();
        }
      });
    }

    const settingShortBreak = document.getElementById('setting-short-break');
    if (settingShortBreak) {
      settingShortBreak.addEventListener('change', () => {
        if (!this.timerRunning && this.timerMode === 'short-break') {
          this.timerSeconds = this.getDurationForMode('short-break');
          this.updateTimerDisplay();
        }
      });
    }

    const settingLongBreak = document.getElementById('setting-long-break');
    if (settingLongBreak) {
      settingLongBreak.addEventListener('change', () => {
        if (!this.timerRunning && this.timerMode === 'long-break') {
          this.timerSeconds = this.getDurationForMode('long-break');
          this.updateTimerDisplay();
        }
      });
    }

    // 10. Edit Modal Form & Controls
    const editForm = document.getElementById('edit-task-form');
    if (editForm) {
      editForm.addEventListener('submit', e => {
        e.preventDefault();
        const id = document.getElementById('edit-task-id')?.value;
        if (!id) return;

        const title = document.getElementById('edit-task-title')?.value || '';
        const notes = document.getElementById('edit-task-notes')?.value || '';
        const quadrant = document.getElementById('edit-task-quadrant')?.value || 'high-impact-low-effort';
        const priority = document.getElementById('edit-task-priority')?.value || 'medium';
        const dueDate = document.getElementById('edit-task-due')?.value || null;

        this.editTask(id, { title, notes, quadrant, priority, dueDate });
        this.closeEditModal();
      });
    }

    const modalCloseBtn = document.getElementById('modal-close');
    if (modalCloseBtn) {
      modalCloseBtn.addEventListener('click', () => this.closeEditModal());
    }

    const modalOverlay = document.getElementById('modal-overlay');
    if (modalOverlay) {
      modalOverlay.addEventListener('click', e => {
        if (e.target === modalOverlay) this.closeEditModal();
      });
    }

    const deleteTaskBtn = document.getElementById('btn-delete-task');
    if (deleteTaskBtn) {
      deleteTaskBtn.addEventListener('click', () => {
        const id = document.getElementById('edit-task-id')?.value;
        if (id) {
          this.deleteTask(id);
          this.closeEditModal();
        }
      });
    }

    // 11. Shortcuts Modal Controls
    const shortcutsCloseBtn = document.getElementById('shortcuts-close');
    if (shortcutsCloseBtn) {
      shortcutsCloseBtn.addEventListener('click', () => this.closeShortcutsModal());
    }

    const shortcutsOverlay = document.getElementById('shortcuts-overlay');
    if (shortcutsOverlay) {
      shortcutsOverlay.addEventListener('click', e => {
        if (e.target === shortcutsOverlay) this.closeShortcutsModal();
      });
    }

    // 12. Keyboard Shortcuts Handler
    document.addEventListener('keydown', e => {
      // Escape closes any active modal
      if (e.key === 'Escape') {
        this.closeEditModal();
        this.closeShortcutsModal();
        this.closeLogEntryModal();
        this.closeEditHabitModal();
        return;
      }

      // Check if user is typing in an input element
      const activeEl = document.activeElement;
      const isInput = activeEl && (
        activeEl.tagName === 'INPUT' ||
        activeEl.tagName === 'TEXTAREA' ||
        activeEl.tagName === 'SELECT' ||
        activeEl.isContentEditable
      );

      if (isInput) return;

      // ? : Shortcuts modal
      if (e.key === '?' || (e.shiftKey && e.key === '/')) {
        e.preventDefault();
        this.openShortcutsModal();
        return;
      }

      // N : Quick add task
      if (e.key === 'n' || e.key === 'N') {
        e.preventDefault();
        if (this.currentView !== 'dashboard') {
          this.switchView('dashboard');
        }
        const quickInput = document.getElementById('quick-add-input');
        if (quickInput) {
          quickInput.focus();
          quickInput.select();
        }
        return;
      }

      // / : Search tasks
      if (e.key === '/') {
        e.preventDefault();
        if (this.currentView !== 'tasks') {
          this.switchView('tasks');
        }
        const searchBox = document.getElementById('task-search');
        if (searchBox) {
          searchBox.focus();
          searchBox.select();
        }
        return;
      }

      // D : Toggle Day / Night
      if (e.key === 'd' || e.key === 'D') {
        e.preventDefault();
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'day';
        const nextTheme = currentTheme === 'day' ? 'night' : 'day';
        this.setTheme(nextTheme);
        return;
      }

      // Space : Start / Pause timer (only when in focus mode)
      if (e.code === 'Space' || e.key === ' ') {
        if (this.currentView === 'focus') {
          e.preventDefault();
          this.toggleTimer();
          return;
        }
      }

      // 1-6 : Switch Views
      const viewKeyMap = {
        '1': 'dashboard',
        '2': 'matrix',
        '3': 'tasks',
        '4': 'focus',
        '5': 'habits',
        '6': 'stats'
      };
      if (viewKeyMap[e.key]) {
        e.preventDefault();
        this.switchView(viewKeyMap[e.key]);
        return;
      }
    });

    // 13. Responsive chart resize
    window.addEventListener('resize', () => {
      if (this.currentView === 'stats') {
        this.renderStats();
      }
    });

    // 14. Visibility change handler (keeps timer in sync when switching apps)
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden && this.timerRunning) {
        const remaining = Math.max(0, Math.ceil((this.timerEndTime - Date.now()) / 1000));
        this.timerSeconds = remaining;
        this.updateTimerDisplay();
      }
    });
  }
};

// ─── ATTACH TO WINDOW & INITIALIZE ───────────────────────────────────────────

window.app = app;

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => app.init());
} else {
  app.init();
}
