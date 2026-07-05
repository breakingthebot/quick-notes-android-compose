/*
 * Defines which note collection is currently visible on screen.
 * Connects to: NotesScreenState, NotesViewModel, and QuickNotesApp.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

/**
 * Note collections available in the list view.
 *
 * @property label User-facing collection label.
 */
enum class NoteCollection(
    val label: String,
) {
    ACTIVE("Active"),
    ARCHIVED("Archived"),
    TRASH("Trash"),
}
