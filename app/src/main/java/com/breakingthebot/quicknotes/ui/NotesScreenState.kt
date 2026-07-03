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
 * @property currentTagsInput Editable comma-separated tag field.
 * @property selectedNoteId Existing note being edited, or null for new notes.
 * @property selectedNoteIsArchived Archive state for the selected note being edited.
 * @property selectedTag Active tag filter, if any.
 * @property availableTags Distinct tags available in the current collection.
 */
data class NotesScreenState(
    val notes: List<Note> = emptyList(),
    val currentTitle: String = "",
    val currentBody: String = "",
    val currentTagsInput: String = "",
    val selectedNoteId: Int? = null,
    val selectedNoteIsArchived: Boolean = false,
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val sortOption: NoteSortOption = NoteSortOption.NEWEST,
    val noteCollection: NoteCollection = NoteCollection.ACTIVE,
)
