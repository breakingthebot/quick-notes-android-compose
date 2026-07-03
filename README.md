# Quick Notes

Quick Notes is a Kotlin Android app for creating, editing, and deleting personal notes with local Room storage and a Jetpack Compose UI.

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
I built the first iteration as a straightforward single-screen notes app. The app keeps the interface simple: a note editor at the top and a saved-notes list underneath, so the create/edit/delete loop is obvious without extra navigation. I used Room instead of a lighter key-value store because the app already needs structured CRUD behavior, and that choice leaves room for later features like search, tags, filters, or archived notes without rewriting storage.

The code is split into small files by responsibility: Room entities and DAO in `data` and `model`, formatting and input cleanup in `util`, screen rendering in `ui`, and state management in `viewmodel`. That structure keeps the first release small enough to understand quickly while still matching a production-style Android architecture a team could extend.

## Notes
- This first iteration focuses on core local CRUD only.
- The app currently uses a single-screen flow instead of multi-screen navigation.
- Room version `2.6.1` is used for stable local persistence with Kotlin coroutines.
