# Changelog

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
