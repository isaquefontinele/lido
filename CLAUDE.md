# CLAUDE.md

Guidance for Claude Code (and other contributors) working in this repository.

## Project

Lido is an open-source, local-first read-it-later app for Android and iOS (a Pocket alternative).
No accounts, no project-owned server, no analytics (see NFR-01/02 in `docs/REQUIREMENTS.md`).
Product scope lives in `README.md`, milestones in `ROADMAP.md`, requirements in `docs/REQUIREMENTS.md`.
GitHub issues map 1:1 to roadmap items; keep each change scoped to its issue.

## Stack

- Kotlin Multiplatform + Compose Multiplatform (Android + iOS), versions in `gradle/libs.versions.toml`
- Android Gradle Plugin 9 with the `com.android.kotlin.multiplatform.library` plugin for KMP modules
- SQLDelight (SQLite, FTS later) for the local library
- Koin for dependency injection
- Ktor (fetching), Klead (extraction), kotlinx.serialization (sync/import/export) — added by later milestones
- Swift/SwiftUI only where the platform requires it (iOS host app, Share Extension)

## Module layout

```
androidApp/   Android application (Activity, Application, manifest). Thin: starts Koin, hosts App().
composeApp/   KMP library with the shared Compose UI. Exports the `ComposeApp` framework for iOS.
shared/       KMP library with non-UI code: SQLDelight database, repositories, Koin modules.
              Feature areas from the README (capture, reader, library, highlights, sync,
              importexport) start as packages here and are split into modules only when needed.
iosApp/       Xcode host app (generated from iosApp/project.yml with XcodeGen).
```

Dependency direction: `androidApp` → `composeApp` → `shared`. `shared` must not depend on Compose.

Base package: `io.github.isaquefontinele.lido`.

## Build and test

Requirements: JDK 17+, Android SDK (compileSdk 36), Xcode 16+ for iOS.

```bash
./gradlew :androidApp:assembleDebug        # build the Android app
./gradlew :androidApp:installDebug         # install on a device/emulator
./gradlew :shared:allTests                 # shared tests (Android host + iOS simulator)
./gradlew :shared:testAndroidHostTest      # shared tests, JVM only (fast)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # iOS framework
open iosApp/iosApp.xcodeproj               # run on iOS (the build phase calls Gradle)
```

After editing `iosApp/project.yml`, regenerate the project with `cd iosApp && xcodegen generate`.
SQLDelight code is generated from `shared/src/commonMain/sqldelight/**.sq` on build.

## Conventions

- Code, comments, docs and commit messages in English. UI strings will support en + pt-BR (FR-13).
- Small, focused commits in imperative mood (`Add ...`, `Fix ...`). The commit that completes an
  issue ends with `Closes #N`.
- Add dependencies through the version catalog (`gradle/libs.versions.toml`), never inline.
- Put logic in `commonMain`; use `expect`/`actual` or Koin platform modules only for platform APIs.
- Shared tests go in `shared/src/commonTest`; platform test helpers in `androidHostTest`/`iosTest`.
- Never commit secrets or signing files (see `.gitignore`).
