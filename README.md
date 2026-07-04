# Quick Notes

Quick Notes is a Kotlin Android app for creating, editing, tagging, archiving, restoring, deleting, searching, and sorting personal notes with local Room storage, a Jetpack Compose UI, a polished accessible single-screen workflow, and automated UI regression coverage.

## Stack
- Kotlin
- Android SDK 35
- Jetpack Compose Material 3
- Room
- Gradle Kotlin DSL
- JUnit 4

## Setup
1. Install Android Studio with Android SDK 35 and JDK 17+ available.
2. Clone the repository.
3. Open the project in Android Studio or use the Gradle wrapper from a terminal.
4. Let Gradle sync and download any missing dependencies.

## Environment Variables
This project does not require environment variables right now. `.env.example` is included as a placeholder because the repo standard requires it.

## Running Locally
1. Create `local.properties` if Android Studio does not generate it automatically:
   `sdk.dir=C\\:\\Users\\marve\\AppData\\Local\\Android\\Sdk`
2. Run `.\gradlew.bat testDebugUnitTest` for unit tests.
3. Run `.\gradlew.bat installDebug` with an emulator or Android device connected.

## Deployed
Not deployed. This is a local Android application.

## Architecture Notes
I built the app as a straightforward single-screen notes experience. The editor stays at the top, while the saved note list underneath supports quick text search, client-side sorting, tags, and a split between active and archived notes, so the app remains usable once the note count grows without introducing extra navigation or a heavier multi-screen flow. In the latest pass I kept that feature set stable and added end-to-end Compose UI coverage for the highest-value flows: creating notes, searching, filtering by tag, and moving notes between active and archived collections.

The code is split into small files by responsibility: Room entities and DAO in `data` and `model`, formatting and input cleanup in `util`, screen rendering in `ui`, and state management in `viewmodel`. Search, collection scoping, tag filtering, and sorting are handled as pure list-formatting logic in the state layer instead of being mixed into the database contract, while the archive flag and tags are stored in Room and migrated explicitly so those features behave like real product data rather than cosmetic filters. The presentation layer now also carries clearer typography, stronger card hierarchy, and accessibility-focused semantics, and the app has Compose instrumentation coverage to catch regressions in the real user flows rather than only in helper logic. The CI workflow waits for the emulator package manager to come online before installing the app and test APKs, runs that emulator job on macOS to avoid the instability of unaccelerated Linux Android emulators, uses a longer boot timeout to tolerate slower hosted startup, and now matches the emulator image architecture to the hosted arm64 runner for more predictable CI startup.

## Notes
- The current iteration focuses on local CRUD plus tags, search, sort, archive/restore, a UI/accessibility polish pass, a fully scrollable single-screen layout, and Compose UI regression tests.
- The app currently uses a single-screen flow instead of multi-screen navigation.
- Room version `2.6.1` is used for stable local persistence with Kotlin coroutines.
