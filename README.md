# Quick Notes

Quick Notes is a Kotlin Android app for creating, editing, deleting, searching, and sorting personal notes with local Room storage and a Jetpack Compose UI.

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
I built the app as a straightforward single-screen notes experience. The editor stays at the top, while the saved note list underneath now supports quick text search and client-side sorting, so the app remains usable once the note count grows without introducing extra navigation or a heavier multi-screen flow. I used Room instead of a lighter key-value store because the app already needs structured CRUD behavior, and that choice leaves room for later features like tags, filters, or archived notes without rewriting storage.

The code is split into small files by responsibility: Room entities and DAO in `data` and `model`, formatting and input cleanup in `util`, screen rendering in `ui`, and state management in `viewmodel`. Search and sorting are handled as pure list-formatting logic in the state layer instead of being mixed into the database contract, which keeps the UI responsive and makes the behavior easy to test in isolation.

## Notes
- The current iteration focuses on local CRUD plus lightweight search and sort controls.
- The app currently uses a single-screen flow instead of multi-screen navigation.
- Room version `2.6.1` is used for stable local persistence with Kotlin coroutines.
