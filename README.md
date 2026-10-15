# Notes App 📝

A sleek, modern, and light-weight Android Notes application built using **Kotlin**, **Android Material 3 Design**, and **SharedPreferences**.

---

## ✨ Features

- ➕ **Add Notes**: Create new notes with a title and multi-line content.
- ✏️ **Edit Notes**: Tap on any note or click the edit icon to update existing titles and content.
- 🗑️ **Delete Notes**: Delete notes with a confirmation dialog to prevent accidental deletion.
- 🔍 **Real-Time Search**: Search through note titles and content instantly.
- 📊 **Dynamic Note Counter**: Displays total note count or search results count in real-time.
- 💾 **Local Data Persistence**: Notes are automatically saved locally using `SharedPreferences` & `Gson`.
- 🌓 **Dark Mode Support**: Adapts nicely to Light and Dark themes.
- 📱 **Edge-to-Edge Layout**: Fully integrated with modern Android edge-to-edge system insets.

---

## 🛠️ Tech Stack & Dependencies

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [XML Layouts](https://developer.android.com/guide/topics/ui/declaring-layout) with [Material 3 Components](https://m3.material.io/)
- **Architecture**: Single Activity (`MainActivity`) with `RecyclerView` and Custom Adapter
- **Libraries**:
  - `com.google.android.material:material` - Material Components
  - `androidx.recyclerview:recyclerview` - RecyclerView
  - `androidx.cardview:cardview` - CardView
  - `com.google.code.gson:gson` - JSON Serialization/Deserialization

---

## 🚀 Getting Started

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/NOTESAPP.git
   ```

2. **Open in Android Studio**
   - Open Android Studio.
   - Select **Open an Existing Project** and browse to the project directory.

3. **Build & Run**
   - Connect an Android device or start an emulator.
   - Click **Run (Shift + F10)** in Android Studio.
