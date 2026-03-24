# How to Run Book Bridge (Kotlin/Android)

This project is a Jetpack Compose-based Android application. Follow the instructions below to run it on your machine.

## Prerequisites
- **Java JDK**: 11 or higher (currently using Java 24).
- **Android SDK**: Required for CLI builds (`platform-tools`, `build-tools`).
- **Android Studio** (Recommended): For the best development experience.

---

## 1. Running via Android Studio (easiest)
1. Open **Android Studio**.
2. Select **Open** and choose the `d:\book-bridge` directory.
3. Wait for Gradle sync to complete.
4. Select a **Virtual Device (Emulator)** or connect a **Physical Device**.
5. Click the **Run** (green play) button.

## 2. Running via Command Line (Windows)
You can use the provided `gradlew.bat` wrapper to build and install the app.

### Build the project:
```powershell
.\gradlew.bat assembleDebug
```

### Install and Run on a connected device/emulator:
```powershell
.\gradlew.bat installDebug
```

### Run Unit Tests:
```powershell
.\gradlew.bat test
```

---

## 3. Common Gradle Tasks
| Task | Description |
| :--- | :--- |
| `tasks` | List all available Gradle tasks |
| `clean` | Deletes the build directory |
| `lint` | Runs lint checks for potential bugs |
| `bundleRelease` | Generates an Android App Bundle (.aab) |

---

## Troubleshooting
- **Java Version**: If you encounter issues with Java 24, ensure `JAVA_HOME` is set to JDK 17 or higher (the project targets JVM 11).
- **Gradle Sync**: If the build fails in Android Studio, try `File > Invalidate Caches / Restart`.
