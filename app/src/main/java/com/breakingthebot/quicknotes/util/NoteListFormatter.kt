/*
 * Filters and sorts notes for list rendering without mutating stored data.
 * Connects to: NotesViewModel, NoteSortOption, and note list tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.ui.NoteCollection
import com.breakingthebot.quicknotes.ui.NoteSortOption
import com.breakingthebot.quicknotes.ui.DateFilterOption
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
        dateFilterOption: DateFilterOption = DateFilterOption.ALL,
        customStartDate: Long? = null,
        customEndDate: Long? = null,
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

        val dateFilteredNotes = when (dateFilterOption) {
            DateFilterOption.ALL -> tagFilteredNotes
            DateFilterOption.TODAY -> {
                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val startOfToday = calendar.timeInMillis
                tagFilteredNotes.filter { it.updatedAt >= startOfToday }
            }
            DateFilterOption.THIS_WEEK -> {
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                tagFilteredNotes.filter { it.updatedAt >= sevenDaysAgo }
            }
            DateFilterOption.CUSTOM -> {
                val start = customStartDate
                val end = customEndDate
                if (start != null && end != null) {
                    val calStart = java.util.Calendar.getInstance().apply {
                        timeInMillis = start
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    val calEnd = java.util.Calendar.getInstance().apply {
                        timeInMillis = end
                        set(java.util.Calendar.HOUR_OF_DAY, 23)
                        set(java.util.Calendar.MINUTE, 59)
                        set(java.util.Calendar.SECOND, 59)
                        set(java.util.Calendar.MILLISECOND, 999)
                    }
                    tagFilteredNotes.filter { it.updatedAt in calStart.timeInMillis..calEnd.timeInMillis }
                } else {
                    tagFilteredNotes
                }
            }
        }

        val filteredNotes = if (normalizedQuery.isBlank()) {
            dateFilteredNotes
        } else {
            dateFilteredNotes.filter { note ->
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
