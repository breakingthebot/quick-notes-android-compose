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
        Note(id = 1, title = "Shopping", body = "Buy apples", updatedAt = 300L, tags = listOf("home")),
        Note(id = 2, title = "Ideas", body = "Android search flow", updatedAt = 100L, isArchived = true, tags = listOf("ideas")),
        Note(id = 3, title = "Workout", body = "Morning run plan", updatedAt = 200L, tags = listOf("health")),
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
            selectedTag = null,
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
            selectedTag = null,
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
            selectedTag = null,
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
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(2), formattedNotes.map { note -> note.id })
    }

    /**
     * Confirms tag filters limit results to notes carrying the selected tag.
     */
    @Test
    fun formatNotes_filtersBySelectedTag() {
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = "health",
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(3), formattedNotes.map { note -> note.id })
    }

    /**
     * Confirms available tags are collected from the selected collection only.
     */
    @Test
    fun availableTags_returnsCollectionScopedTags() {
        val availableTags = NoteListFormatter.availableTags(
            notes = notes,
            noteCollection = NoteCollection.ACTIVE,
        )

        assertEquals(listOf("health", "home"), availableTags)
    }

    /**
     * Confirms trash collection only includes deleted notes.
     */
    @Test
    fun formatNotes_scopesResultsToTrashCollection() {
        val notesWithDeleted = notes + Note(id = 4, title = "Trash Note", body = "Old info", updatedAt = 50L, isDeleted = true, tags = listOf("old"))
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notesWithDeleted,
            noteCollection = NoteCollection.TRASH,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(4), formattedNotes.map { note -> note.id })
    }

    /**
     * Confirms active and archived collections exclude deleted notes.
     */
    @Test
    fun formatNotes_excludesDeletedNotesFromActiveAndArchive() {
        val notesWithDeleted = notes + Note(id = 4, title = "Trash Note", body = "Old info", updatedAt = 500L, isDeleted = true)
        val activeNotes = NoteListFormatter.formatNotes(
            notes = notesWithDeleted,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
        )
        val archivedNotes = NoteListFormatter.formatNotes(
            notes = notesWithDeleted,
            noteCollection = NoteCollection.ARCHIVED,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
        )

        assertEquals(listOf(1, 3), activeNotes.map { note -> note.id })
        assertEquals(listOf(2), archivedNotes.map { note -> note.id })
    }

    /**
     * Confirms available tags are collected from the trash collection only.
     */
    @Test
    fun availableTags_returnsTrashCollectionScopedTags() {
        val notesWithDeleted = notes + Note(id = 4, title = "Trash Note", body = "Old info", updatedAt = 50L, isDeleted = true, tags = listOf("old"))
        val availableTags = NoteListFormatter.availableTags(
            notes = notesWithDeleted,
            noteCollection = NoteCollection.TRASH,
        )

        assertEquals(listOf("old"), availableTags)
    }

    /**
     * Confirms that pinned notes are always sorted to the top of the list first.
     */
    @Test
    fun formatNotes_sortsPinnedNotesToTopFirst() {
        val notesWithPinned = listOf(
            Note(id = 1, title = "Shopping", body = "Buy apples", updatedAt = 300L, tags = listOf("home")),
            Note(id = 2, title = "Ideas", body = "Android search", updatedAt = 100L, isPinned = true, tags = listOf("ideas")),
            Note(id = 3, title = "Workout", body = "Morning run", updatedAt = 200L, tags = listOf("health")),
        )
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = notesWithPinned,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
        )

        // Note 2 (pinned) should come first, even though Note 1 is newer (300L > 100L)
        assertEquals(listOf(2, 1, 3), formattedNotes.map { note -> note.id })
    }
}
