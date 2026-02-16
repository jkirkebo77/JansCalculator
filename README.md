# Jan's Calculator

A minimalist Android calculator app built with Kotlin + Jetpack Compose.

It started as a small test project and grew into a clean, practical calculator with a responsive UI.

## Features

- Basic operations: +, −, ×, ÷
- Square root (√)
- Clear and Backspace
- Decimal input
- Error handling (e.g., divide-by-zero, √ of negative numbers)
- Single-line display with adaptive formatting to avoid wrapping on different screen sizes
- Operator press confirmation in the display (e.g., 123+)

## Screenshot

![Jan's Calculator screenshot](docs/screenshots/app.png)

## Requirements

- Android Studio (recommended)
- JDK 21 (or Android Studio bundled JBR)
- Android device or emulator

## Build (APK)

From the project root, run:

```bash
./gradlew :app:assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

## Install on a device (ADB)

Enable Developer options + USB debugging on your phone, then run:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If more than one device/emulator is connected:

```bash
adb -s <DEVICE_ID> install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure (high level)

- `app/` – Android app module
- `app/src/main/java/.../MainActivity.kt` – main Compose UI and calculator logic
- `app/src/main/res/` – resources (launcher icon, strings, themes, etc.)

## Notes

- The app label is set via `@string/app_name` in `app/src/main/res/values/strings.xml`.
- Launcher icons are generated under `app/src/main/res/mipmap-*` (often as `.webp`).

## License

No license. All rights reserved.
