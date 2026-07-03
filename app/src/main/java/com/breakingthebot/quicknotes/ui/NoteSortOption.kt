/*
 * Defines supported note list sort modes for the screen.
 * Connects to: NotesScreenState, NotesViewModel, and QuickNotesApp.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

/**
 * Sort options available for the note list.
 *
 * @property label User-facing text shown in the controls.
 */
enum class NoteSortOption(
    val label: String,
) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    TITLE("Title"),
}
