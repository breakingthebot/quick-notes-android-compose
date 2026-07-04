# Quick Notes

Quick Notes is a Kotlin Android app for creating, editing, tagging, archiving, restoring, deleting, searching, and sorting personal notes with local Room storage, a Jetpack Compose UI, a polished accessible single-screen workflow, and automated JVM-based UI regression coverage.

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
I built the app as a straightforward single-screen notes experience. The editor stays at the top, while the saved note list underneath supports quick text search, client-side sorting, tags, and a split between active and archived notes, so the app remains usable once the note count grows without introducing extra navigation or a heavier multi-screen flow. In the latest pass I kept that feature set stable and tightened the Compose regression coverage so the JVM screen tests explicitly scroll through the same single-list layout to reach off-screen controls and note cards, using the assertion APIs supported by the current Compose test stack.

The code is split into small files by responsibility: Room entities and DAO in `data` and `model`, formatting and input cleanup in `util`, screen rendering in `ui`, and state management in `viewmodel`. Search, collection scoping, tag filtering, and sorting are handled as pure list-formatting logic in the state layer instead of being mixed into the database contract, while the archive flag and tags are stored in Room and migrated explicitly so those features behave like real product data rather than cosmetic filters. The presentation layer now has a clean split between the pure `QuickNotesScreen` composable and the `QuickNotesApp` view-model wrapper, which makes the screen easier to test and easier to evolve later. The CI workflow stays on a single Linux job and runs `testDebugUnitTest`, which covers both the logic tests and the screen-level Compose regression tests in a more predictable free setup.

## Notes
- The current iteration focuses on local CRUD plus tags, search, sort, archive/restore, a UI/accessibility polish pass, a fully scrollable single-screen layout, a reusable screen/view-model split, and Robolectric-backed Compose UI regression tests with list-aware scrolling for both assertions and interactions.
- The app currently uses a single-screen flow instead of multi-screen navigation.
- Room version `2.6.1` is used for stable local persistence with Kotlin coroutines.
