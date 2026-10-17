# مهام — Mahaam

> A calm, editorial task manager inspired by book typography, with an Eisenhower Matrix, 30-day habit intensity tracker, and Pomodoro focus timer. 100% offline.

---

## ✨ Features

- **Eisenhower Matrix**: Organize tasks into 4 quadrants (Quick Wins, Major Projects, Fill-ins, Thankless Tasks) with intuitive drag-and-drop.
- **Habit Tracker**:
  - Horizontal 30-day quick-glance intensity strip (normalized by duration or completion count).
  - 3 custom base ink colors (Indigo, Crimson, Matcha).
  - Expandable full chronological history from Day 1.
- **Pomodoro Timer**: Clean, book-like focus timer with customizable work/break intervals and session tracking.
- **Calm Editorial Aesthetic**: Single-column book layout, Zen typography, Day/Night modes, and 3 subtle accent inks.
- **100% Offline & Private**: Zero cloud dependencies or external trackers; all data persists locally in `localStorage`.

---

## 📱 Multi-Platform Support

| Platform | Format | Details |
|---|---|---|
| **macOS** | `.dmg` installer | Native Apple Silicon (ARM64) build |
| **Windows** | `.zip` portable | Standalone 64-bit desktop build |
| **Android** | PWA & Android Studio project | Mobile-responsive with thumb navigation & PWA install |

---

## 🚀 Getting Started

### Prerequisites

- Node.js (v18+ recommended)
- npm

### Installation

```bash
# Clone the repository
git clone https://github.com/hamdan-fareed/mahaam.git
cd mahaam

# Install dependencies
npm install

# Run desktop app locally
npm start
```

### Packaging Builds

```bash
# Build macOS .dmg
npm run build

# Build Windows portable .zip
npm run build:win
```

### Mobile / Android

- **PWA**: Run `python3 -m http.server 8080` in the `src/` directory and open on your mobile browser to install to your home screen.
- **Android Studio**: Open the `android/` directory directly in Android Studio to build a native `.apk`.

---

## 📄 License

MIT
