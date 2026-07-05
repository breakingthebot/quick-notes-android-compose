# Quick Notes

Quick Notes is a Kotlin Android app for creating, editing, tagging, archiving, restoring, deleting, searching, and sorting personal notes with local Room storage, a Jetpack Compose UI, a polished accessible single-screen workflow, automated JVM-based UI regression coverage, and a home-screen widget for recent active notes.

## Stack
- Kotlin
- Android SDK 35
- Jetpack Compose Material 3
- Jetpack Glance AppWidget
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
I built the app as a straightforward single-screen notes experience. The editor stays at the top, while the saved note list underneath supports quick text search, client-side sorting, tags, and a split between active and archived notes, so the app remains usable once the note count grows without introducing extra navigation or a heavier multi-screen flow. In this iteration, I extended that same local-first model onto the Android home screen with a widget that shows the latest active notes and opens the app when tapped, and then tightened the refresh behavior so launcher widgets repaint immediately after note mutations instead of waiting for the widget host to recreate them.

In the latest builds:
- I added a soft-delete Trash collection so deleted notes don't immediately vanish. Instead of deleting notes permanently when hitting delete on active/archived collections, they are marked as deleted and moved into a new "Trash" collection. Inside the Trash view, note cards are not editable on tap, but instead offer options to either restore the note (returning it to active or archived state) or permanently delete it. I also added a bulk "Empty trash" button to permanently clear all trashed notes at once.
- I added a Pinned Notes feature so users can pin important active or archived notes to the top of the list. Pinned notes display a visual `📌` prefix on their titles and stay at the very top of their respective lists regardless of the selected sort option (newest, oldest, or alphabetical). Tapping the "Pin" or "Unpin" toggle buttons instantly updates their position.
- I added Checklist Mode allowing notes to be toggled as interactive todo lists. Instead of adding new tables and relations, it persists as Markdown task lists using `[ ]` and `[x]` line prefixes in the existing note body, which keeps search functional and schemas clean. A dedicated parser maps back and forth, stripping task markup inside the editor for a clean text representation and matching checked item values by line description during saves.
- I added Note Colors / Visual Categories so notes can be styled with custom background colors. A visual mapper converts the selections into high-contrast, accessibility-compliant background and content colors for both Light and Dark theme configurations.
- I added Markdown Rich Text Previews allowing bold (`**text**`), italic (`*text*`), and headers (`#`, `##`) to render as stylized text blocks inside note cards. The text is parsed on the fly into Compose `AnnotatedString` objects to avoid introducing bulky third-party libraries.
- I added Note Reminders & Notifications which allows scheduling alarms using Android's `AlarmManager` and native `DatePickerDialog`/`TimePickerDialog` pickers. When an alarm triggers, `ReminderReceiver` generates high-priority notification channel signals to notify the user. Reminders are cleared automatically if notes are edited/saved without them, permanently deleted, or if the Trash collection is emptied.
- I added a Tag Manager (Rename / Delete) enabling global tag operations across all saved notes. Users can tap "Manage tags" in the tag browse row to display a scrollable dialog. Tapping Rename modifies the tag across all notes in a single batch update (auto-deduplicating tag lists), while tapping Delete permanently strips the tag from all notes.
- I added Note Archiving / Unarchiving Quick Swipe gestures to speed up notebook organization. Users can swipe left on an active note card to instantly archive it, or swipe right to move it to the Trash. Swipe actions dynamically adapt inside Archived or Trash views to restore or permanently delete items.
- I added Export / Share Notes allowing users to export their notes using Android's native system share sheet chooser via `ACTION_SEND` intents. To prevent UI clipping or truncation on narrow devices, the note card actions row is styled to scroll horizontally.
- I added Search Match Text Highlighting to make finding matching notes easier. Typing in the search input field highlights matching substrings case-insensitively within note preview titles, body texts, and checklist items. It parses existing Compose `AnnotatedString` structures to preserve Markdown styling (like bold or italic headers) underneath search overlays.
- I added Search Filters & Date Range Filtering allowing users to scope visible notes by update timestamps (All Dates, Today, This Week, or a Custom Date Range). Custom Date Range selection uses Compose Material 3's native `DateRangePicker` dialogue, normalizing range boundaries to cover full start and end days (00:00:00.000 to 23:59:59.999).
- I added Folder Organization / Notebooks Grouping to categorize and organize notes. Users can create, rename, and delete custom folders/notebooks through a dedicated `FolderManagerDialog` overlay. Notes can be assigned to folders in the note editor using a horizontal scrollable chips row. Notes in folders display a folder badge on their preview cards, and the note list can be filtered dynamically using a scrollable folders chip row in the browse panel. Room database schema version has been bumped to 9 with migration code that safely preserves user data by nullifying notebook references if a notebook/folder is deleted.
- I added Camera Picture Taking and Gallery Image selection support for Image Notes. Attached images render as full-width cropped preview banners on note cards and interactive thumbnails inside the editor card. File integrations use Android's `FileProvider` and private app file paths to capture and cache images securely. Room database schema has been bumped to 10 with version migration code to support `imageUri` properties on the Note model.


The code is split into small files by responsibility: Room entities and DAO in `data` and `model`, formatting and input cleanup in `util`, screen rendering in `ui`, widget rendering and snapshot loading in `widget`, mutation side effects in `services`, and state management in `viewmodel`. Search, collection scoping, tag filtering, and sorting are handled as pure list-formatting logic in the state layer instead of being mixed into the database contract, while the widget reads a small recent-note snapshot from the same Room database and refreshes through a notifier after note mutations. That notifier now does both a Glance refresh and an explicit widget-update broadcast so launcher hosts repaint reliably. Database changes are managed via version 10 migrations that add the `Notebook` entity, the `notebookId` column, and the `imageUri` column to notes. The presentation layer still keeps a clean split between the pure `QuickNotesScreen` composable and the `QuickNotesApp` view-model wrapper, and the CI workflow runs `testDebugUnitTest` for logic and Robolectric-backed UI coverage.

## Notes
- The current iteration focuses on local CRUD plus tags, search, sort, archive/restore, a UI/accessibility polish pass, a fully scrollable single-screen layout, a reusable screen/view-model split, Robolectric-backed Compose UI regression tests, and a Glance-based home-screen widget with a more reliable refresh path.
- The app currently uses a single-screen flow instead of multi-screen navigation.
- Room version `2.6.1` is used for stable local persistence with Kotlin coroutines.
