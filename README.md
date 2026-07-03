# Quick Notes

Quick Notes is a Kotlin Android app for creating, editing, archiving, restoring, deleting, searching, and sorting personal notes with local Room storage, a Jetpack Compose UI, and a polished accessible single-screen workflow.

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
I built the app as a straightforward single-screen notes experience. The editor stays at the top, while the saved note list underneath supports quick text search, client-side sorting, and a split between active and archived notes, so the app remains usable once the note count grows without introducing extra navigation or a heavier multi-screen flow. In the latest pass I tightened the UI hierarchy, empty states, and interaction sizing, then moved the whole screen into one scrollable flow so the notes list stays reachable on smaller devices and while the keyboard is open.

The code is split into small files by responsibility: Room entities and DAO in `data` and `model`, formatting and input cleanup in `util`, screen rendering in `ui`, and state management in `viewmodel`. Search, collection scoping, and sorting are handled as pure list-formatting logic in the state layer instead of being mixed into the database contract, while the archive flag itself is stored in Room and migrated explicitly so the feature behaves like real product data rather than a cosmetic filter. The presentation layer now also carries clearer typography, stronger card hierarchy, and accessibility-focused semantics without complicating the underlying app logic.

## Notes
- The current iteration focuses on local CRUD plus search, sort, archive/restore, a UI/accessibility polish pass, and a fully scrollable single-screen layout.
- The app currently uses a single-screen flow instead of multi-screen navigation.
- Room version `2.6.1` is used for stable local persistence with Kotlin coroutines.
