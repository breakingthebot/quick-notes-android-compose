# Changelog

## 1.7.0 - 2026-07-05
- Added Note Archiving / Unarchiving Quick Swipe gesture actions using Compose Material 3 `SwipeToDismissBox` APIs.
- Built a visual `SwipeDismissBackground` component with context-aware color transitions (Red for delete actions, Primary for archive/restore actions).
- Configured swipe behaviors dynamically based on the current collection context:
  - Swipe Left (EndToStart): Archives active notes, and restores archived/trashed notes.
  - Swipe Right (StartToEnd): Soft-deletes active/archived notes, and permanently deletes trashed notes.
- Added UI integration test `swipe_actions_archiveAndTrashNotes` to verify swipe-to-archive, swipe-to-restore, and swipe-to-delete flows.

## 1.6.0 - 2026-07-05
- Added Tag Manager (Rename / Delete) providing global tag management features across all saved notes.
- Implemented `renameTag` globally inside `NotesViewModel` to safely batch update tag names across all notes and filter duplicate tags.
- Implemented `deleteTag` globally inside `NotesViewModel` to batch remove tags from all notes.
- Designed a custom material-themed `TagManagerDialog` with sub-dialogs for confirmation prompts and text entry fields.
- Added visual entry points for tag management in the Browse controls tag filtering header.
- Added UI integration test `tagManager_renamesAndDeleteTagsGlobally` to verify global tag rename, duplication removal, and global deletion.

## 1.5.0 - 2026-07-05
- Added Note Reminders & Notifications allowing date and time scheduling for notes, triggering local system notifications at target times.
- Created `ReminderReceiver` broadcast receiver to build system notification channels and publish notifications.
- Created `ReminderScheduler` to register precise system alarms using Android's `AlarmManager`.
- Extended Note schema with a nullable `reminderTime` timestamp (Version 8 Room Database migration).
- Integrated date/time selection using Android's native material date and time pickers.
- Added visual reminder badge indicator on note cards and active indicator inside note details.
- Added UI integration test `reminder_savesNoteReminder` to verify reminder scheduling and display state.

## 1.4.0 - 2026-07-05
- Added Markdown Rich Text Previews allowing bold (`**text**`), italic (`*text*`), and headers (`#`, `##`) to render as styled rich text on note preview cards.
- Created `NoteMarkdownParser` to parse plain text into styled Compose `AnnotatedString` structures.
- Wrapped standard note details text previews and checklist task text previews inside the markdown styling parser.
- Added comprehensive unit tests inside `NoteMarkdownParserTest` and UI integration test `markdown_rendersStylizedNotes` to verify styling parsing and end-to-end rendering correctness.

## 1.3.0 - 2026-07-05
- Added Note Colors / Visual Categories allowing notes to be styled with custom background colors (Mint, Peach, Lavender, Blue).
- Implemented `NoteColorMapper` to map color category selections to theme-compliant, accessible foreground/background color tokens for both light and dark systems.
- Embedded a color selection `FilterChip` row inside the note editor panel.
- Customized note list item containers, text, divider lines, checkboxes, and buttons to use custom palette colors dynamically.
- Registered Room Database migration version 6 to 7, introducing the `color` string column.
- Added Compose UI integration test verifying color choice mutation and persistence.

## 1.2.0 - 2026-07-04
- Added Checklist Mode allowing notes to be formatted as task lists with interactive checkboxes.
- Implemented `NoteChecklistParser` to parse plain text bodies to/from checked (`[x]`) and unchecked (`[ ]`) task states.
- Integrated a "Format as checklist" toggle switch in the note creation and editing layouts.
- Replaced plain body text rendering with interactive checkbox rows on note cards in lists for checklist notes.
- Added Room Database migration version 5 to 6, adding the `isChecklist` column.
- Added JUnit and Robolectric UI integration tests for checklist parsing and interactive checkbox toggles.

## 1.1.0 - 2026-07-04
- Added a Pinned Notes feature allowing important notes to be pinned to the top of Active and Archived lists.
- Integrated `📌` visual indicator prefix to pinned note card titles in the list view.
- Added a Pin/Unpin action button on note cards inside Active and Archived lists.
- Added Room Database migration version 4 to 5, adding the `isPinned` column.
- Updated the list sorting algorithm to sort pinned notes first before applying alphabetical, newest, or oldest ordering.
- Added unit and Robolectric UI tests covering the toggle-pin sorting and toggling states.

## 1.0.0 - 2026-07-04
- Added a Trash Bin (soft-delete) feature allowing notes to be moved to a "Trash" collection.
- Added support for permanently deleting individual notes or emptying the trash collection in one click.
- Added support for restoring deleted notes back to active or archived collections.
- Excluded trashed notes from the active and archived collections and from home-screen launcher widgets.
- Added Room Database migration version 3 to 4, adding the `isDeleted` column.
- Added unit and Robolectric UI integration tests for the soft-delete, restore, and empty-trash flows.

## 0.9.1 - 2026-07-04
- Fixed the home-screen widget refresh path so note mutations now trigger both Glance updates and an explicit `APPWIDGET_UPDATE` broadcast to installed widget instances.
- This addresses stale launcher widget content after archiving, restoring, deleting, or saving notes.

## 0.9.0 - 2026-07-04
- Added a Jetpack Glance home-screen widget that shows recent active notes and opens the app on tap.
- Added a Room snapshot query and widget snapshot loader so the widget reads the same persisted note data as the main app.
- Added a note-mutation notifier so widget instances refresh after save, delete, archive, and restore actions.
- Added widget-specific formatter tests plus launcher metadata and preview/loading resources.

## 0.8.3 - 2026-07-04
- Updated the Robolectric interaction helpers to scroll the shared notes list to off-screen search, tag-filter, collection, and archive/restore controls before interacting with them.
- Kept the screen-level coverage focused on real user paths inside the single scrollable Compose layout instead of bypassing the list behavior.

## 0.8.2 - 2026-07-04
- Replaced the newer Compose assertion helpers in the Robolectric tests with `assertCountEquals` so the screen tests compile against the current UI test dependency set.
- Kept the card-level test targeting and lazy-list scrolling added in the previous pass, so the coverage still checks the intended off-screen note rows reliably.

## 0.8.1 - 2026-07-04
- Added a `notes-list` test tag to the main `LazyColumn` so Robolectric Compose tests can scroll directly to off-screen note cards before asserting on them.
- Reworked the screen-level Robolectric assertions to target note-card semantics instead of ambiguous raw text matches shared by list items and filter chips.
- Kept the coverage focused on the same user flows: create, search, tag filtering, and archive/restore.

## 0.6.0 - 2026-07-03
- Added Compose UI instrumentation tests for create, search, tag filtering, and archive/restore flows.
- Added stable test tags to key inputs, chips, note cards, and action buttons.
- Expanded GitHub Actions with an emulator-backed UI test job in addition to unit tests.
- Hardened the emulator workflow to wait for the Android package manager before installing the test APKs.
- Moved the emulator-backed UI test job to macOS runners to avoid Linux emulator instability in CI.
- Increased the emulator boot timeout to accommodate slower hosted runner startup during UI-test runs.
- Switched the CI emulator to a lighter Android 29 `x86` profile with more CPU allocated for more reliable boot behavior.
- Matched the CI emulator architecture to the GitHub arm64 macOS runner by switching to an Android 30 `arm64-v8a` image.

## 0.7.0 - 2026-07-04
- Replaced flaky emulator-backed Compose instrumentation coverage with Robolectric-backed local JVM Compose UI tests.
- Added reusable in-memory DAO and main-dispatcher test helpers so screen tests can exercise real view-model flows without Room or an emulator.
- Simplified GitHub Actions back to a single reliable JVM test job that runs unit tests and Compose UI tests together with `testDebugUnitTest`.

## 0.8.0 - 2026-07-04
- Extracted a pure `QuickNotesScreen` composable so the notes UI can be rendered from plain state and callbacks without a `ViewModel`.
- Kept `QuickNotesApp` as a thin wrapper that binds `NotesViewModel` state and snackbar messages into the reusable screen.
- Reworked the Robolectric UI tests to drive `QuickNotesScreen` through a local state harness, removing lifecycle and flow timing from the screen-level test path.

## 0.5.0 - 2026-07-03
- Added persisted note tags with comma-separated editor input and in-list tag display.
- Added collection-scoped tag filter chips alongside existing search and sort controls.
- Added a Room schema migration and type converter for stored tag lists.
- Added unit tests for tag parsing, storage formatting, and tag-based list filtering.

## 0.4.1 - 2026-07-03
- Reworked the main screen into a single scrollable list so saved notes remain reachable on smaller screens.
- Added IME-aware padding so the layout behaves better while the keyboard is open.

## 0.4.0 - 2026-07-03
- Polished the main screen with stronger typography, clearer section hierarchy, and richer summary/empty states.
- Increased touch target sizes for key actions and form fields to improve mobile usability.
- Added accessibility-oriented semantics for headings and note cards.
- Refined note card styling to improve scanability and action clarity.

## 0.3.0 - 2026-07-03
- Added archive and restore actions so notes can be hidden from the main list without permanent deletion.
- Added active and archived collection filters to the note list controls.
- Added a Room schema migration to persist archive state for existing installs.
- Expanded note list tests to cover archived-note filtering behavior.

## 0.2.0 - 2026-07-03
- Added note list search so users can match notes by title or body text.
- Added sort controls for newest, oldest, and alphabetical title ordering.
- Added unit tests for note list filtering and sorting behavior.
- Updated documentation to reflect search and sort support.

## 0.1.0 - 2026-07-03
- Scaffolded a Kotlin Android app using Jetpack Compose and Gradle Kotlin DSL.
- Added a Room-backed note model, DAO, repository, and singleton database.
- Built a single-screen note editor with create, edit, delete, snackbar feedback, and empty state handling.
- Added unit tests for the repository behavior and note input sanitizer.
- Added MIT license, repo hygiene files, project documentation, and Android CI workflow fixes for Linux runners.
- Fixed Android theme dependency wiring so CI can resolve the Material 3 app theme during resource linking.
