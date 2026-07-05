/*
 * Filters and sorts notes for list rendering without mutating stored data.
 * Connects to: NotesViewModel, NoteSortOption, and note list tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.ui.NoteCollection
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
     * @param noteCollection Selected visible note collection.
     * @param searchQuery User-entered search query.
     * @param selectedTag Active tag filter, if any.
     * @param sortOption Selected list ordering mode.
     * @return Notes ready for on-screen display.
     */
    fun formatNotes(
        notes: List<Note>,
        noteCollection: NoteCollection,
        searchQuery: String,
        selectedTag: String?,
        sortOption: NoteSortOption,
    ): List<Note> {
        val normalizedQuery = searchQuery.trim().lowercase(Locale.getDefault())
        val scopedNotes = notes.filter { note ->
            when (noteCollection) {
                NoteCollection.ACTIVE -> !note.isDeleted && !note.isArchived
                NoteCollection.ARCHIVED -> !note.isDeleted && note.isArchived
                NoteCollection.TRASH -> note.isDeleted
            }
        }
        val tagFilteredNotes = if (selectedTag.isNullOrBlank()) {
            scopedNotes
        } else {
            scopedNotes.filter { note -> selectedTag in note.tags }
        }

        val filteredNotes = if (normalizedQuery.isBlank()) {
            tagFilteredNotes
        } else {
            tagFilteredNotes.filter { note ->
                note.title.lowercase(Locale.getDefault()).contains(normalizedQuery) ||
                    note.body.lowercase(Locale.getDefault()).contains(normalizedQuery) ||
                    note.tags.any { tag -> tag.lowercase(Locale.getDefault()).contains(normalizedQuery) }
            }
        }

        return when (sortOption) {
            NoteSortOption.NEWEST -> filteredNotes.sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenByDescending { it.updatedAt }
            )
            NoteSortOption.OLDEST -> filteredNotes.sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenBy { it.updatedAt }
            )
            NoteSortOption.TITLE -> filteredNotes.sortedWith(
                compareByDescending<Note> { it.isPinned }
                    .thenBy { it.title.lowercase(Locale.getDefault()) }
            )
        }
    }

    /**
     * Returns distinct tags available inside the selected collection.
     *
     * @param notes Stored notes from the repository.
     * @param noteCollection Selected visible note collection.
     * @return Alphabetical distinct tag list for filter chips.
     */
    fun availableTags(
        notes: List<Note>,
        noteCollection: NoteCollection,
    ): List<String> {
        return notes.asSequence()
            .filter { note ->
                when (noteCollection) {
                    NoteCollection.ACTIVE -> !note.isDeleted && !note.isArchived
                    NoteCollection.ARCHIVED -> !note.isDeleted && note.isArchived
                    NoteCollection.TRASH -> note.isDeleted
                }
            }
            .flatMap { note -> note.tags.asSequence() }
            .distinct()
            .sorted()
            .toList()
    }
}
