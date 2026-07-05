/*
 * Enum representing date range filtering options.
 * Connects to: NoteListFormatter, NotesScreenState, NotesViewModel, QuickNotesScreen.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.ui

/**
 * Options for filtering notes by their last updated timestamp.
 */
enum class DateFilterOption(val label: String) {
    ALL("All Dates"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    CUSTOM("Custom Range")
}
