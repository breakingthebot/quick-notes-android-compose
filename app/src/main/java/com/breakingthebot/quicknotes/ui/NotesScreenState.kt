/*
 * Defines the immutable UI state for the notes screen.
 * Connects to: NotesViewModel and QuickNotesApp.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import com.breakingthebot.quicknotes.model.Note

/**
 * Snapshot of the screen state needed to render the notes interface.
 *
 * @property notes Stored notes displayed in the list.
 * @property currentTitle Editable title field value.
 * @property currentBody Editable body field value.
 * @property selectedNoteId Existing note being edited, or null for new notes.
 */
data class NotesScreenState(
    val notes: List<Note> = emptyList(),
    val currentTitle: String = "",
    val currentBody: String = "",
    val selectedNoteId: Int? = null,
)
