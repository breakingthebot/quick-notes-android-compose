/*
 * Filters and sorts notes for list rendering without mutating stored data.
 * Connects to: NotesViewModel, NoteSortOption, and note list tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.ui.NoteSortOption
import java.util.Locale

/**
 * Applies search and sort rules to note collections.
 */
object NoteListFormatter {
    /**
     * Returns notes matching the current query and sort mode.
     *
     * @param notes Stored notes from the repository.
     * @param searchQuery User-entered search query.
     * @param sortOption Selected list ordering mode.
     * @return Notes ready for on-screen display.
     */
    fun formatNotes(
        notes: List<Note>,
        searchQuery: String,
        sortOption: NoteSortOption,
    ): List<Note> {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.getDefault())

        val filteredNotes = if (normalizedQuery.isBlank()) {
            notes
        } else {
            notes.filter { note ->
                note.title.lowercase(Locale.getDefault()).contains(normalizedQuery) ||
                    note.body.lowercase(Locale.getDefault()).contains(normalizedQuery)
            }
        }

        return when (sortOption) {
            NoteSortOption.NEWEST -> filteredNotes.sortedByDescending { note -> note.updatedAt }
            NoteSortOption.OLDEST -> filteredNotes.sortedBy { note -> note.updatedAt }
            NoteSortOption.TITLE -> filteredNotes.sortedBy { note -> note.title.lowercase(Locale.getDefault()) }
        }
    }
}
