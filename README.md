# Montej Flashcard App

A beautiful, modern flashcard study app for Android with offline support, file library management, and interactive study features.

## 🎯 Features

- 📚 **Library Management**: Create folders and subfolders
- 📁 **File Organization**: Sort by name, date, modified, type
- 🎴 **Flashcard Study**: Interactive card flipping and navigation
- 👆 **Gestures**: Tap to flip, swipe to navigate
- 📊 **Study Statistics**: Track your progress
- 🎨 **Multiple Themes**: Light, Dark, Extreme Dark modes
- 💾 **Offline Support**: All data stored locally with Room Database
- ✨ **Modern UI**: Beautiful Material Design 3 interface

## 🚀 Getting Started

### Requirements

- Android 8.0+ (API 26)
- Android Studio Flamingo or later
- Kotlin 1.9.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/taurennijore-ux/montej-flashcard-app.git
   cd montej-flashcard-app
   ```

2. **Open in Android Studio**
   - File → Open → Select the project folder
   - Wait for Gradle sync

3. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" or press `Shift + F10`

### Building APK for Installation

```bash
./gradlew clean build
```

The APK will be located in `app/build/outputs/apk/debug/app-debug.apk`

### Installing on Device via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/montej/flashcard/
│   │   ├── data/
│   │   │   ├── local/        # Room database entities and DAOs
│   │   │   └── repository/   # Data repository
│   │   └── ui/
│   │       ├── navigation/   # Navigation setup
│   │       ├── screens/      # UI screens (Library, Flashcard, Settings)
│   │       └── theme/        # Theme and styling
│   ├── res/                  # Resources
│   └── AndroidManifest.xml   # App manifest
```

## 📖 Usage

1. **Create a Library**: Tap "New Folder" to organize your content
2. **Add Flashcards**: Create sample CSV files with Q&A pairs
3. **Study**: Tap on a file to start studying
4. **Interact**:
   - Tap to flip cards
   - Swipe left/right to navigate
   - View progress and statistics
5. **Customize**: Go to Settings to change your theme

## 📋 CSV File Format

CSV files should have exactly 2 columns (Question, Answer):

```csv
What is 2+2?,4
Capital of France?,Paris
Who painted the Mona Lisa?,Leonardo da Vinci
```

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **CSV Parsing**: OpenCSV
- **Navigation**: Jetpack Navigation Compose
- **Design**: Material Design 3
- **Architecture**: MVVM with Repository pattern

## 🎨 Themes

- **Light**: Bright, clean interface
- **Dark**: Easy on the eyes
- **Extreme Dark**: Pure black background
- **System**: Follows device settings

## 📱 App Features Breakdown

### Library Screen
- Folder creation and management
- File organization
- Sorting options (Name, Date, Modified, Type)
- Rename/delete operations
- Breadcrumb navigation

### Flashcard Screen
- Interactive card flipping with tap
- Swipe gestures (left/right)
- Progress indicator
- Study statistics
- Previous/Next buttons

### Settings Screen
- Theme selection
- App information
- Easy settings management

## 🔒 Permissions

- `READ_EXTERNAL_STORAGE`: Read files from device
- `WRITE_EXTERNAL_STORAGE`: Write files to device
- `MANAGE_EXTERNAL_STORAGE`: Manage all files

## 📦 Dependencies

- AndroidX Core & Lifecycle
- Jetpack Compose UI & Material3
- Room Database
- OpenCSV
- Coroutines
- Navigation Compose

## 📝 License

MIT License - Free to use and modify

## 👨‍💻 Author

Developed by Montej Team

## 🤝 Contributing

Contributions are welcome! Feel free to fork and submit pull requests.

## 📧 Support

For issues or feature requests, please open an issue on GitHub.

---

**Made with ❤️ for students and learners**