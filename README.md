# مهام — Mahaam (Android)

> A calm, editorial productivity application built natively with Android, Kotlin, and Jetpack Compose. Features an Eisenhower Matrix, 30-day habit intensity tracker, Pomodoro focus timer with ambient chime, and weighted productivity analytics. 100% offline-first with Room database persistence.

---

## ✨ Features

- **01. Dashboard (`بداية`)**:
  - Daily overview at a glance with quick stats (Total, Completed, Pending, Day Streak).
  - Deterministic daily motivational quote with authentic Arabic calligraphy metadata.
  - Quick-add form with instant quadrant assignment.
  - Today's prioritized tasks list.

- **02. The Matrix (`نظام`)**:
  - Full 2x2 Eisenhower priority matrix with high/low impact and effort axes.
  - Q1: Quick Wins (`إنجاز سريع`) — High Impact · Low Effort
  - Q2: Major Projects (`مشاريع كبيرة`) — High Impact · High Effort
  - Q3: Fill-in (`مهام جانبية`) — Low Impact · Low Effort
  - Q4: Thankless Task (`مهام شاقة`) — Low Impact · High Effort
  - Interactive chips with completion toggles, detail editing, and instant quadrant reassignment.

- **03. Tasks (`قائمة`)**:
  - Master catalog with search (title/notes) and filtering by status, quadrant, and priority.
  - Full task creation dialog with due date picker and rich notes.
  - Bulk actions: clear completed tasks and export tasks as JSON text.

- **04. Focus (`تركيز`)**:
  - Pomodoro focus timer with Work Session, Short Break, and Long Break cycle intervals.
  - Associate timer sessions with specific pending tasks.
  - Custom procedural C5–E5–G5 chime synthesis and subtle haptic feedback on completion.
  - Customizable session durations and target session counts.

- **05. Habits (`عادات`)**:
  - 30-day horizontal activity strip and intensity heatmap (normalized by daily aggregate vs peak).
  - Base ink colors: Indigo, Crimson, and Matcha.
  - Tracking modes: Completion count (`×`) or duration (`min`).
  - "+ Quick" one-tap logging and detailed timestamped session logging with personal notes.
  - Expandable chronological history log from Day 1 to Today.

- **06. Analytics (`تحليل`)**:
  - Dynamic Productivity Score (0–100) weighted by quadrant impact, streak consistency, and focus minutes.
  - Custom canvas 7-day weekly activity bar chart.
  - Matrix energy distribution tracks.
  - Focus time dedication counters.

---

## 🎨 Themes & Inks

- **Themes**: Day (`#FAFAF8`) and Night (`#1A1A1A`) modes.
- **Inks**: Indigo (`#4A5899`), Crimson (`#A63D40`), and Matcha (`#5B7553`).
- **Typography**: Editorial, book-like serif typography.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with AndroidViewModel and unidirectional StateFlow
- **Local Persistence**: Room SQLite Database (100% offline, private)
- **Minimum SDK**: 24 (Android 7.0+)
- **Target SDK**: 36 (Android 16+)
- **Build System**: Gradle Kotlin DSL (`.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 📄 License

MIT
