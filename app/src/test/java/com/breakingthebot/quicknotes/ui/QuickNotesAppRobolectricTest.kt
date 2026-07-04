/*
 * Exercises the main notes flows through Robolectric-backed screen tests.
 * Connects to: QuickNotesScreen, NotesScreenState, and local JVM CI.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.ui.theme.QuickNotesTheme
import com.breakingthebot.quicknotes.util.MainDispatcherRule
import com.breakingthebot.quicknotes.util.NoteInputSanitizer
import com.breakingthebot.quicknotes.util.NoteListFormatter
import com.breakingthebot.quicknotes.util.NoteTagFormatter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Local JVM Compose tests for the highest-value user flows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class QuickNotesAppRobolectricTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var harness: QuickNotesScreenHarness

    /**
     * Sets up a fresh screen instance backed by local mutable state.
     */
    @Before
    fun setUp() {
        harness = QuickNotesScreenHarness()

        composeRule.setContent {
            QuickNotesTheme {
                QuickNotesScreen(
                    state = harness.state,
                    onTitleChange = harness::onTitleChanged,
                    onBodyChange = harness::onBodyChanged,
                    onTagsInputChange = harness::onTagsInputChanged,
                    onSaveClick = harness::saveNote,
                    onClearClick = harness::clearEditor,
                    onNoteCollectionChanged = harness::onNoteCollectionChanged,
                    onSearchQueryChanged = harness::onSearchQueryChanged,
                    onSelectedTagChanged = harness::onSelectedTagChanged,
                    onSortOptionChanged = harness::onSortOptionChanged,
                    onNoteClick = harness::selectNote,
                    onArchiveClick = harness::archiveNote,
                    onRestoreClick = harness::restoreNote,
                    onDeleteClick = harness::deleteNote,
                )
            }
        }
    }

    /**
     * Verifies that saving a note renders it in the active list.
     */
    @Test
    fun createNote_showsSavedNoteInList() {
        val title = "sprint-plan"

        createNote(
            title = title,
            body = "Write JVM Compose tests",
            tags = "work, test",
        )

        composeRule.onAllNodesWithText(title).assertCountEquals(1)
        composeRule.onAllNodesWithText("#work").assertCountEquals(1)
    }

    /**
     * Verifies that search narrows the visible note list.
     */
    @Test
    fun search_filtersVisibleNotes() {
        val firstTitle = "sprint-plan"
        val secondTitle = "groceries-list"

        createNote(firstTitle, "Write JVM Compose tests", "work")
        createNote(secondTitle, "Buy apples", "home")

        composeRule.onNodeWithTag("note-search-field").performScrollTo()
        composeRule.onNodeWithTag("note-search-field").performTextInput("sprint")

        composeRule.onAllNodesWithText(firstTitle).assertCountEquals(1)
        composeRule.onAllNodesWithText(secondTitle).assertCountEquals(0)
    }

    /**
     * Verifies that tag chips filter the current collection.
     */
    @Test
    fun tagFilter_limitsNotesToSelectedTag() {
        val workTitle = "work-note"
        val healthTitle = "health-note"

        createNote(workTitle, "Write JVM Compose tests", "work")
        createNote(healthTitle, "Morning session", "health")

        composeRule.onNodeWithTag("tag-filter-work").performScrollTo()
        composeRule.onNodeWithTag("tag-filter-work").performClick()

        composeRule.onAllNodesWithText(workTitle).assertCountEquals(1)
        composeRule.onAllNodesWithText(healthTitle).assertCountEquals(0)
    }

    /**
     * Verifies that notes move between active and archived collections.
     */
    @Test
    fun archiveAndRestore_movesNoteAcrossCollections() {
        val title = "archive-note"

        createNote(title, "Move this note", "work")

        composeRule.onNodeWithTag("archive-button-$title").performScrollTo()
        composeRule.onNodeWithTag("archive-button-$title").performClick()
        composeRule.onNodeWithTag("collection-chip-archived").performScrollTo()
        composeRule.onNodeWithTag("collection-chip-archived").performClick()
        composeRule.onAllNodesWithText(title).assertCountEquals(1)

        composeRule.onNodeWithTag("restore-button-$title").performScrollTo()
        composeRule.onNodeWithTag("restore-button-$title").performClick()
        composeRule.onNodeWithTag("collection-chip-active").performScrollTo()
        composeRule.onNodeWithTag("collection-chip-active").performClick()
        composeRule.onAllNodesWithText(title).assertCountEquals(1)
    }

    /**
     * Creates a note through the public screen UI.
     *
     * @param title Note title.
     * @param body Note body.
     * @param tags Comma-separated tag string.
     */
    private fun createNote(
        title: String,
        body: String,
        tags: String,
    ) {
        composeRule.onNodeWithTag("title-input").performTextInput(title)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput(body)
        composeRule.onNodeWithTag("tags-input").performScrollTo()
        composeRule.onNodeWithTag("tags-input").performTextInput(tags)
        composeRule.onNodeWithTag("save-note-button").performScrollTo()
        composeRule.onNodeWithTag("save-note-button").performClick()
        composeRule.waitForIdle()
    }
}

/**
 * Test-only state container that mimics the screen callbacks without lifecycle or Room.
 */
private class QuickNotesScreenHarness {
    private var storedNotes = emptyList<Note>()
    private var nextId = 1

    var state by mutableStateOf(NotesScreenState())
        private set

    fun onTitleChanged(title: String) {
        state = state.copy(currentTitle = title)
    }

    fun onBodyChanged(body: String) {
        state = state.copy(currentBody = body)
    }

    fun onTagsInputChanged(tagsInput: String) {
        state = state.copy(currentTagsInput = tagsInput)
    }

    fun onSearchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)
        syncState()
    }

    fun onSelectedTagChanged(selectedTag: String?) {
        state = state.copy(selectedTag = selectedTag)
        syncState()
    }

    fun onSortOptionChanged(sortOption: NoteSortOption) {
        state = state.copy(sortOption = sortOption)
        syncState()
    }

    fun onNoteCollectionChanged(noteCollection: NoteCollection) {
        state = state.copy(
            noteCollection = noteCollection,
            selectedTag = null,
        )
        syncState()
    }

    fun selectNote(noteId: Int) {
        val note = storedNotes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        state = state.copy(
            currentTitle = note.title,
            currentBody = note.body,
            currentTagsInput = NoteTagFormatter.formatForEditor(note.tags),
            selectedNoteId = note.id,
            selectedNoteIsArchived = note.isArchived,
        )
    }

    fun clearEditor() {
        state = state.copy(
            currentTitle = "",
            currentBody = "",
            currentTagsInput = "",
            selectedNoteId = null,
            selectedNoteIsArchived = false,
        )
    }

    fun saveNote() {
        val sanitizedTitle = NoteInputSanitizer.sanitizeTitle(state.currentTitle)
        val sanitizedBody = NoteInputSanitizer.sanitizeBody(state.currentBody)
        if (sanitizedTitle.isBlank() || sanitizedBody.isBlank()) {
            return
        }

        val noteId = state.selectedNoteId ?: nextId++
        val updatedNote = Note(
            id = noteId,
            title = sanitizedTitle,
            body = sanitizedBody,
            updatedAt = noteId.toLong(),
            isArchived = state.selectedNoteIsArchived,
            tags = NoteTagFormatter.parseInput(state.currentTagsInput),
        )

        storedNotes = (storedNotes.filterNot { note -> note.id == noteId } + updatedNote)
        clearEditor()
        syncState()
    }

    fun archiveNote(noteId: Int) {
        storedNotes = storedNotes.map { note ->
            if (note.id == noteId) note.copy(isArchived = true) else note
        }
        if (state.selectedNoteId == noteId) {
            clearEditor()
        }
        syncState()
    }

    fun restoreNote(noteId: Int) {
        storedNotes = storedNotes.map { note ->
            if (note.id == noteId) note.copy(isArchived = false) else note
        }
        if (state.selectedNoteId == noteId) {
            clearEditor()
        }
        syncState()
    }

    fun deleteNote(noteId: Int) {
        storedNotes = storedNotes.filterNot { note -> note.id == noteId }
        if (state.selectedNoteId == noteId) {
            clearEditor()
        }
        syncState()
    }

    private fun syncState() {
        state = state.copy(
            notes = NoteListFormatter.formatNotes(
                notes = storedNotes,
                noteCollection = state.noteCollection,
                searchQuery = state.searchQuery,
                selectedTag = state.selectedTag,
                sortOption = state.sortOption,
            ),
            availableTags = NoteListFormatter.availableTags(
                notes = storedNotes,
                noteCollection = state.noteCollection,
            ),
        )
    }
}
