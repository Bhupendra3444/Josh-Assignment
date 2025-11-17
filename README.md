# Sample Tasks KMM Prototype

A Kotlin Multiplatform (Android + Desktop) prototype that guides enumerators through practice recording tasks (text reading, image description, photo capture) and stores submissions locally via SQLDelight. Compose Multiplatform drives every UI surface with shared presentation logic and lightweight platform adapters.

## Features

- **Onboarding flow** covering Start → Noise Test → Task Selection → Task detail screens.
- **Noise qualification** with a simulated 0–60 dB meter plus guidance messaging.
- **Task screens**
  - Text Reading task with dummyjson product snippets, press-and-hold recorder, 10–20 s validation, QA checkboxes, full playback controls, and persistence.
  - Image Description task with real image loading (Kamel) and recording validation.
  - Photo Capture task with stubbed camera capture, description field, optional audio, playback, and metadata storage.
- **Task history** list summarizing totals, duration, and providing playback controls for each submission.
- **Infrastructure**
  - SQLDelight schema + repository exposing `Flow<List<TaskRecord>>`.
  - Recording/Noise/Camera abstractions and platform implementations with TODO markers where native work remains.
  - Ktor client (dummyjson) with kotlinx serialization.
  - Recording validation unit tests.

## Project Layout

```
shared/        # Multiplatform domain, Compose UI, SQLDelight schema, repositories, expect APIs
androidApp/    # Android launcher with Compose + platform services
desktopApp/    # Compose Desktop entry point
```

Key packages:

- `com.example.sampletasks.ui` – shared Compose navigation + screens.
- `com.example.sampletasks.audio` – recording, noise, camera abstractions + impls.
- `com.example.sampletasks.data` – SQLDelight driver factories & repository.
- `com.example.sampletasks.network` – Dummyjson client.
- `com.example.sampletasks.model` – models, drafts, validators, DTOs.

## Prerequisites

- JDK 17+
- Android Studio Giraffe+/IntelliJ IDEA 2023.3+
- Android SDK 34 (API level 34, build-tools 34.0.0)

## Build & Run

All commands assume PowerShell from the repo root.

### 1. Sync dependencies

```powershell
.\gradlew.bat tasks
```

### 2. Run shared unit tests

```powershell
.\gradlew.bat shared:test
```

### 3. Android app (debug)

```powershell
.\gradlew.bat androidApp:installDebug
```

Launch on any API 24+ device/emulator. Audio/camera implementations are stubbed until TODOs are fulfilled, so outputs are simulated files.

### 4. Android release APK

```powershell
.\gradlew.bat androidApp:assembleRelease
```

The unsigned artifact lives at `androidApp/build/outputs/apk/release/androidApp-release.apk`. Releases currently reuse the debug signing config for convenience—replace with production signing before distribution.

### 5. Desktop app

```powershell
.\gradlew.bat desktopApp:run
```

Runs the Compose Desktop window with stubbed platform services.

## Extending / TODOs

- `AndroidRecordingManager` includes a `TODO(audio-codec)` placeholder for wiring real AAC/PCM capture.
- Recording playback currently uses MediaPlayer/Clip-backed transport controls without waveform visualization. Replace with production audio stack when codecs land.
- Replace simulated noise/camera implementations with microphone + CameraX/AVFoundation.
- Add UI polish, accessibility review, and instrumentation tests after hardware integrations land.

## Release Checklist

1. Update signing configs in `androidApp/build.gradle.kts` when moving beyond QA.
2. Run the full build: `.\gradlew.bat clean shared:test androidApp:assembleRelease desktopApp:package`.
3. Upload `androidApp/build/outputs/apk/release/androidApp-release.apk` (or signed variant).
4. Tag + push the repository once QA passes.

## License

Internal prototype for the provided assignment; no open-source license specified.
