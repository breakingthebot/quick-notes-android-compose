# Changelog

## 0.6.0 - 2026-07-03
- Added Compose UI instrumentation tests for create, search, tag filtering, and archive/restore flows.
- Added stable test tags to key inputs, chips, note cards, and action buttons.
- Expanded GitHub Actions with an emulator-backed UI test job in addition to unit tests.
- Hardened the emulator workflow to wait for the Android package manager before installing the test APKs.
- Moved the emulator-backed UI test job to macOS runners to avoid Linux emulator instability in CI.
- Increased the emulator boot timeout to accommodate slower hosted runner startup during UI-test runs.

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
