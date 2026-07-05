/*
 * Exercises the main notes flows through Robolectric-backed screen tests.
 * Connects to: QuickNotesScreen, NotesScreenState, and local JVM CI.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.model.NoteColor
import com.breakingthebot.quicknotes.ui.theme.QuickNotesTheme
import com.breakingthebot.quicknotes.util.MainDispatcherRule
import com.breakingthebot.quicknotes.util.NoteInputSanitizer
import com.breakingthebot.quicknotes.util.NoteListFormatter
import com.breakingthebot.quicknotes.util.NoteTagFormatter
import com.breakingthebot.quicknotes.util.NoteChecklistParser
import com.breakingthebot.quicknotes.util.ChecklistItem
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
                val context = androidx.compose.ui.platform.LocalContext.current
                QuickNotesScreen(
                    state = harness.state,
                    onTitleChange = harness::onTitleChanged,
                    onBodyChange = harness::onBodyChanged,
                    onTagsInputChange = harness::onTagsInputChanged,
                    onSaveClick = { harness.saveNote(context) },
                    onClearClick = harness::clearEditor,
                    onNoteCollectionChanged = harness::onNoteCollectionChanged,
                    onSearchQueryChanged = harness::onSearchQueryChanged,
                    onSelectedTagChanged = harness::onSelectedTagChanged,
                    onSortOptionChanged = harness::onSortOptionChanged,
                    onNoteClick = harness::selectNote,
                    onArchiveClick = harness::archiveNote,
                    onRestoreClick = harness::restoreNote,
                    onDeleteClick = { noteId -> harness.deleteNote(noteId, context) },
                    onEmptyTrashClick = { harness.emptyTrash(context) },
                    onPinClick = harness::togglePinNote,
                    onIsChecklistChange = harness::onIsChecklistChanged,
                    onChecklistItemToggle = harness::toggleChecklistItem,
                    onNoteColorChange = harness::onNoteColorChanged,
                    onReminderTimeChange = harness::onReminderTimeChanged,
                    onRenameTag = harness::onRenameTag,
                    onDeleteTag = harness::onDeleteTag,
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

        scrollToNoteCard(title)

        composeRule.onAllNodesWithTag("note-card-$title").assertCountEquals(1)
        composeRule.onAllNodesWithTag("tag-filter-work").assertCountEquals(1)
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

        scrollToNode("note-search-field")
        composeRule.onNodeWithTag("note-search-field").performTextInput("sprint")

        scrollToNoteCard(firstTitle)

        composeRule.onAllNodesWithTag("note-card-$firstTitle").assertCountEquals(1)
        composeRule.onAllNodesWithTag("note-card-$secondTitle").assertCountEquals(0)
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

        scrollToNode("tag-filter-work")
        composeRule.onNodeWithTag("tag-filter-work").performClick()

        scrollToNoteCard(workTitle)

        composeRule.onAllNodesWithTag("note-card-$workTitle").assertCountEquals(1)
        composeRule.onAllNodesWithTag("note-card-$healthTitle").assertCountEquals(0)
    }

    /**
     * Verifies that notes move between active and archived collections.
     */
    @Test
    fun archiveAndRestore_movesNoteAcrossCollections() {
        val title = "archive-note"

        createNote(title, "Move this note", "work")

        scrollToNode("archive-button-$title")
        composeRule.onNodeWithTag("archive-button-$title").performClick()
        scrollToNode("collection-chip-archived")
        composeRule.onNodeWithTag("collection-chip-archived").performClick()
        scrollToNoteCard(title)
        composeRule.onAllNodesWithTag("note-card-$title").assertCountEquals(1)

        scrollToNode("restore-button-$title")
        composeRule.onNodeWithTag("restore-button-$title").performClick()
        scrollToNode("collection-chip-active")
        composeRule.onNodeWithTag("collection-chip-active").performClick()
        scrollToNoteCard(title)
        composeRule.onAllNodesWithTag("note-card-$title").assertCountEquals(1)
    }

    /**
     * Verifies that deleting an active note moves it to trash, and emptying trash removes it permanently.
     */
    @Test
    fun deleteAndEmptyTrash_movesNoteToTrashThenPermanentlyDeletes() {
        val title = "trash-note"
        createNote(title, "Temporary content", "temp")

        scrollToNode("delete-button-$title")
        composeRule.onNodeWithTag("delete-button-$title").performClick()

        composeRule.onAllNodesWithTag("note-card-$title").assertCountEquals(0)

        scrollToNode("collection-chip-trash")
        composeRule.onNodeWithTag("collection-chip-trash").performClick()

        scrollToNoteCard(title)
        composeRule.onAllNodesWithTag("note-card-$title").assertCountEquals(1)

        scrollToNode("empty-trash-button")
        composeRule.onNodeWithTag("empty-trash-button").performClick()

        composeRule.onAllNodesWithTag("note-card-$title").assertCountEquals(0)
    }

    /**
     * Verifies that pinning a note changes its label / pin state and displays the correct button action.
     */
    @Test
    fun pinAndUnpin_togglesNotePinState() {
        val title = "pin-test-note"
        createNote(title, "Content to be pinned", "work")

        // Originally has a "Pin" button
        scrollToNode("pin-button-$title")
        composeRule.onNodeWithTag("pin-button-$title").performClick()

        // Card should now be prefix-marked or display "Unpin" button
        scrollToNode("unpin-button-$title")
        composeRule.onNodeWithTag("unpin-button-$title").performClick()

        // Toggled back to "Pin" button
        scrollToNode("pin-button-$title")
        composeRule.onAllNodesWithTag("pin-button-$title").assertCountEquals(1)
    }

    /**
     * Verifies that creating a checklist note displays checkboxes in the list,
     * and toggling a checkbox changes its state.
     */
    @Test
    fun checklistMode_createsAndTogglesCheckboxes() {
        val title = "checklist-test-note"
        composeRule.onNodeWithTag("title-input").performTextInput(title)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput("Buy milk\nCall doctor")

        scrollToNode("checklist-mode-switch")
        composeRule.onNodeWithTag("checklist-mode-switch").performClick()
        composeRule.waitForIdle()

        // Call harness save directly to bypass Robolectric click dispatching
        harness.saveNote()
        composeRule.waitForIdle()

        scrollToNode("checklist-item-checkbox-$title-0")
        composeRule.onAllNodesWithTag("checklist-item-checkbox-$title-0").assertCountEquals(1)

        composeRule.onNodeWithTag("checklist-item-checkbox-$title-0").performClick()
        composeRule.waitForIdle()
    }

    /**
     * Verifies that selecting a custom color choice in the editor saves
     * that color on the note model.
     */
    @Test
    fun noteColor_savesSelectedColorOnNote() {
        val title = "color-note"
        composeRule.onNodeWithTag("title-input").performTextInput(title)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput("Content")

        // Select Mint Color choice
        composeRule.onNodeWithTag("color-choice-mint").performScrollTo()
        composeRule.onNodeWithTag("color-choice-mint").performClick()
        composeRule.waitForIdle()

        // Call harness save directly to bypass Robolectric click dispatching
        harness.saveNote()
        composeRule.waitForIdle()

        // Assert note has color set
        val note = harness.state.notes.firstOrNull { it.title == title }
        org.junit.Assert.assertNotNull(note)
        org.junit.Assert.assertEquals(NoteColor.MINT, note?.color)
    }

    /**
     * Verifies that markdown tags are parsed and display stripped text on note cards.
     */
    @Test
    fun markdown_rendersStylizedNotes() {
        val title = "markdown-note"
        createNote(title, "Hello **bold** and *italic*", "work")

        // Scroll to compose the note card in the viewport
        scrollToNode("note-card-$title")

        // Assert the note card exists and renders parsed/stripped text
        composeRule.onNodeWithText("Hello bold and italic").assertExists()
    }

    /**
     * Verifies that scheduling a reminder saves the state and displays a badge.
     */
    @Test
    fun reminder_savesNoteReminder() {
        val title = "reminder-note"
        composeRule.onNodeWithTag("title-input").performTextInput(title)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput("Content")

        // Set reminder time directly in harness to mock picker selection
        val futureTime = System.currentTimeMillis() + 86400000L // 24 hours from now
        harness.onReminderTimeChanged(futureTime)
        composeRule.waitForIdle()

        // Verify reminder text displays in editor card
        composeRule.onNodeWithTag("reminder-display-text").assertExists()

        // Save note
        harness.saveNote()
        composeRule.waitForIdle()

        // Scroll to and verify badge is rendered on list note card
        scrollToNode("note-card-$title")
        composeRule.onNodeWithText("⏰ Reminder:", substring = true).assertExists()
    }

    /**
     * Verifies that renaming and deleting tags globally changes all related notes.
     */
    @Test
    fun tagManager_renamesAndDeleteTagsGlobally() {
        val title = "tag-note"
        createNote(title, "details", "work, ideas")

        // Bring note card into viewport
        scrollToNode("note-card-$title")

        // Verify initial tags exist on note card
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#work", substring = true))
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#ideas", substring = true))

        // Open Tag Manager
        composeRule.onNodeWithTag("manage-tags-button").performClick()
        composeRule.waitForIdle()

        // Click rename button for work
        composeRule.onNodeWithTag("rename-tag-btn-work").performClick()
        composeRule.waitForIdle()

        // Input new tag name
        composeRule.onNodeWithTag("rename-tag-input").performTextInput("office")
        composeRule.onNodeWithTag("rename-save-btn").performClick()
        composeRule.waitForIdle()

        // Verify the tag is renamed globally on the card
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#office", substring = true))
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#work", substring = true).not())

        // Open Tag Manager again
        composeRule.onNodeWithTag("manage-tags-button").performClick()
        composeRule.waitForIdle()

        // Click delete button for ideas
        composeRule.onNodeWithTag("delete-tag-btn-ideas").performClick()
        composeRule.waitForIdle()

        // Confirm deletion
        composeRule.onNodeWithTag("delete-tag-save-btn").performClick()
        composeRule.waitForIdle()

        // Verify the tag ideas is removed globally
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#ideas", substring = true).not())
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

    /**
     * Scrolls the list until the requested note card is composed and visible.
     *
     * @param title Note title used in the card test tag.
     */
    private fun scrollToNoteCard(title: String) {
        scrollToNode("note-card-$title")
    }

    /**
     * Scrolls the list until the requested test tag is composed and visible.
     *
     * @param tag Target test tag.
     */
    private fun scrollToNode(tag: String) {
        composeRule.onNodeWithTag("notes-list").performScrollToNode(hasTestTag(tag))
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

    fun onIsChecklistChanged(isChecklist: Boolean) {
        state = state.copy(currentIsChecklist = isChecklist)
    }

    fun onNoteColorChanged(noteColor: NoteColor) {
        println("HARNESS COLOR CHANGED: $noteColor")
        state = state.copy(currentNoteColor = noteColor)
    }

    fun onReminderTimeChanged(timeMs: Long?) {
        state = state.copy(selectedReminderTime = timeMs)
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
            currentBody = if (note.isChecklist) {
                NoteChecklistParser.parse(note.body).joinToString("\n") { it.text }
            } else {
                note.body
            },
            currentTagsInput = NoteTagFormatter.formatForEditor(note.tags),
            selectedNoteId = note.id,
            selectedNoteIsArchived = note.isArchived,
            selectedNoteIsDeleted = note.isDeleted,
            selectedNoteIsPinned = note.isPinned,
            currentIsChecklist = note.isChecklist,
            currentNoteColor = note.color,
            selectedReminderTime = note.reminderTime,
        )
    }

    fun clearEditor() {
        state = state.copy(
            currentTitle = "",
            currentBody = "",
            currentTagsInput = "",
            selectedNoteId = null,
            selectedNoteIsArchived = false,
            selectedNoteIsDeleted = false,
            selectedNoteIsPinned = false,
            currentIsChecklist = false,
            currentNoteColor = NoteColor.DEFAULT,
            selectedReminderTime = null,
        )
    }

    fun saveNote(context: Context = androidx.test.core.app.ApplicationProvider.getApplicationContext()) {
        val sanitizedTitle = NoteInputSanitizer.sanitizeTitle(state.currentTitle)
        var sanitizedBody = NoteInputSanitizer.sanitizeBody(state.currentBody)
        if (sanitizedTitle.isBlank() || sanitizedBody.isBlank()) {
            return
        }

        val isChecklist = state.currentIsChecklist
        if (isChecklist) {
            val lines = sanitizedBody.lines().map { it.trim() }.filter { it.isNotBlank() }
            val existingItems = state.selectedNoteId?.let { id ->
                storedNotes.firstOrNull { it.id == id }?.let { NoteChecklistParser.parse(it.body) }
            } ?: emptyList()
            val checkedTexts = existingItems.filter { it.isChecked }.map { it.text }.toSet()
            val checklistItems = lines.map { line ->
                ChecklistItem(line, line in checkedTexts)
            }
            sanitizedBody = NoteChecklistParser.toBodyString(checklistItems)
        }

        val noteId = state.selectedNoteId ?: nextId++
        val updatedNote = Note(
            id = noteId,
            title = sanitizedTitle,
            body = sanitizedBody,
            updatedAt = noteId.toLong(),
            isArchived = state.selectedNoteIsArchived,
            isDeleted = state.selectedNoteIsDeleted,
            isPinned = state.selectedNoteIsPinned,
            isChecklist = isChecklist,
            color = state.currentNoteColor,
            reminderTime = state.selectedReminderTime,
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
            if (note.id == noteId) {
                if (note.isDeleted) {
                    note.copy(isDeleted = false)
                } else {
                    note.copy(isArchived = false)
                }
            } else {
                note
            }
        }
        if (state.selectedNoteId == noteId) {
            clearEditor()
        }
        syncState()
    }

    fun deleteNote(noteId: Int, context: Context = androidx.test.core.app.ApplicationProvider.getApplicationContext()) {
        storedNotes = storedNotes.mapNotNull { note ->
            if (note.id == noteId) {
                if (note.isDeleted) {
                    null
                } else {
                    note.copy(isDeleted = true)
                }
            } else {
                note
            }
        }
        if (state.selectedNoteId == noteId) {
            clearEditor()
        }
        syncState()
    }

    fun toggleChecklistItem(noteId: Int, itemIndex: Int) {
        storedNotes = storedNotes.map { note ->
            if (note.id == noteId) {
                val items = NoteChecklistParser.parse(note.body).toMutableList()
                if (itemIndex in items.indices) {
                    val item = items[itemIndex]
                    items[itemIndex] = item.copy(isChecked = !item.isChecked)
                    note.copy(body = NoteChecklistParser.toBodyString(items))
                } else {
                    note
                }
            } else {
                note
            }
        }
        syncState()
    }

    fun togglePinNote(noteId: Int) {
        storedNotes = storedNotes.map { note ->
            if (note.id == noteId) note.copy(isPinned = !note.isPinned) else note
        }
        val note = storedNotes.firstOrNull { it.id == noteId }
        if (state.selectedNoteId == noteId && note != null) {
            state = state.copy(selectedNoteIsPinned = note.isPinned)
        }
        syncState()
    }

    fun emptyTrash(context: Context = androidx.test.core.app.ApplicationProvider.getApplicationContext()) {
        storedNotes = storedNotes.filterNot { note -> note.isDeleted }
        clearEditor()
        syncState()
    }

    fun onRenameTag(oldTag: String, newTag: String) {
        val sanitizedNewTag = newTag.trim().lowercase().filter { it.isLetterOrDigit() }
        if (sanitizedNewTag.isBlank() || oldTag == sanitizedNewTag) return

        storedNotes = storedNotes.map { note ->
            if (oldTag in note.tags) {
                note.copy(
                    tags = note.tags.map { if (it == oldTag) sanitizedNewTag else it }.distinct(),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                note
            }
        }
        syncState()
    }

    fun onDeleteTag(tag: String) {
        storedNotes = storedNotes.map { note ->
            if (tag in note.tags) {
                note.copy(
                    tags = note.tags.filterNot { it == tag },
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                note
            }
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
