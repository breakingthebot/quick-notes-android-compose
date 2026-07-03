/*
 * Verifies note list filtering and sorting behavior.
 * Connects to: NoteListFormatter, NoteSortOption, and Note model.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.ui.NoteCollection
import com.breakingthebot.quicknotes.ui.NoteSortOption
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for NoteListFormatter.
 */
class NoteListFormatterTest {
    private val notes = listOf(
        Note(id = 1, title = "Shopping", body = "Buy apples", updatedAt = 300L),
        Note(id = 2, title = "Ideas", body = "Android search flow", updatedAt = 100L, isArchived = true),
        Note(id = 3, title = "Workout", body = "Morning run plan", updatedAt = 200L),
    )

    /**
     * Confirms list order defaults to newest notes first.
     */
    @Test
    fun formatNotes_sortsByNewestFirst() {
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(1, 3), formattedNotes.map { note -> note.id })
    }

    /**
     * Confirms search matches note title and body without case sensitivity.
     */
    @Test
    fun formatNotes_filtersByTitleAndBody() {
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notes,
            noteCollection = NoteCollection.ARCHIVED,
            searchQuery = "ANDROID",
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(2), formattedNotes.map { note -> note.id })
    }

    /**
     * Confirms title sort is alphabetical after filtering.
     */
    @Test
    fun formatNotes_sortsAlphabeticallyByTitle() {
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            sortOption = NoteSortOption.TITLE,
        )

        assertEquals(listOf(1, 3), formattedNotes.map { note -> note.id })
    }

    /**
     * Confirms archived collection only includes archived notes.
     */
    @Test
    fun formatNotes_scopesResultsToArchivedCollection() {
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notes,
            noteCollection = NoteCollection.ARCHIVED,
            searchQuery = "",
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(2), formattedNotes.map { note -> note.id })
    }
}
