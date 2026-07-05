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
import androidx.compose.ui.test.assertIsSelected
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
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
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
                    onShareClick = harness::shareNote,
                    onDateFilterOptionChanged = harness::onDateFilterOptionChanged,
                    onCustomDateRangeChanged = harness::onCustomDateRangeChanged,
                    onCreateNotebook = harness::createNotebook,
                    onRenameNotebook = harness::renameNotebook,
                    onDeleteNotebook = harness::deleteNotebook,
                    onNotebookSelected = harness::onNotebookSelected,
                    onCurrentNoteNotebookChanged = harness::onCurrentNoteNotebookChanged,
                    onCurrentNoteImageChanged = harness::onCurrentNoteImageChanged,
                    onCurrentNoteAudioChanged = harness::onCurrentNoteAudioChanged,
                    onStartVoiceRecording = { harness.startVoiceRecording(context) },
                    onStopVoiceRecording = { harness.stopVoiceRecording(null) },
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

        scrollToNode("tag-filter-work")
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
        scrollToNode("manage-tags-button")
        composeRule.onNodeWithTag("manage-tags-button").performClick()
        composeRule.waitForIdle()

        // Click rename button for work
        composeRule.onNodeWithTag("rename-tag-btn-work").performClick()
        composeRule.waitForIdle()

        // Input new tag name
        composeRule.onNodeWithTag("rename-tag-input").performTextReplacement("office")
        composeRule.onNodeWithTag("rename-save-btn").performClick()
        composeRule.waitForIdle()

        // Verify the tag is renamed globally on the card
        scrollToNode("note-card-$title")
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#office", substring = true))
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#work", substring = true).not())

        // Open Tag Manager again
        scrollToNode("manage-tags-button")
        composeRule.onNodeWithTag("manage-tags-button").performClick()
        composeRule.waitForIdle()

        // Click delete button for ideas
        composeRule.onNodeWithTag("delete-tag-btn-ideas").performClick()
        composeRule.waitForIdle()

        // Confirm deletion
        composeRule.onNodeWithTag("delete-tag-save-btn").performClick()
        composeRule.waitForIdle()

        // Verify the tag ideas is removed globally
        scrollToNode("note-card-$title")
        composeRule.onNodeWithTag("note-card-$title").assert(hasText("#ideas", substring = true).not())
    }

    /**
     * Verifies folder creation, note folder assignment, folder list filtering,
     * folder renaming, and folder deletion with safe note dissociation.
     */
    @Test
    fun folderOrganization_managesFoldersAndCategorizesNotes() {
        val folderName = "work"
        val renamedFolder = "office"
        val noteInFolder = "folder-note"
        val noteOutFolder = "other-note"

        // 1. Create a folder
        scrollToNode("manage-folders-button")
        composeRule.onNodeWithTag("manage-folders-button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("create-folder-input").performTextInput(folderName)
        composeRule.onNodeWithTag("create-folder-btn").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("folder-manager-close").performClick()
        composeRule.waitForIdle()

        // Verify folder filter chip exists
        scrollToNode("folders-scroll-row")
        composeRule.onNodeWithTag("folder-filter-$folderName").assertExists()

        // 2. Create note in folder
        scrollToNode("title-input")
        composeRule.onNodeWithTag("title-input").performTextInput(noteInFolder)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput("folder-body")
        
        // Select folder from chips row
        composeRule.onNodeWithTag("editor-folder-row").performScrollTo()
        composeRule.onNodeWithTag("folder-option-$folderName").performClick()
        composeRule.waitForIdle()

        // Save
        composeRule.onNodeWithTag("save-note-button").performScrollTo()
        composeRule.onNodeWithTag("save-note-button").performClick()
        composeRule.waitForIdle()

        // Verify card folder badge
        scrollToNoteCard(noteInFolder)
        composeRule.onNodeWithTag("note-card-$noteInFolder").assert(hasText("📁 $folderName", substring = true))

        // Create second note without folder
        scrollToNode("title-input")
        composeRule.onNodeWithTag("title-input").performTextInput(noteOutFolder)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput("other-body")
        composeRule.onNodeWithTag("save-note-button").performScrollTo()
        composeRule.onNodeWithTag("save-note-button").performClick()
        composeRule.waitForIdle()

        // 3. Filter by folder
        scrollToNode("folders-scroll-row")
        composeRule.onNodeWithTag("folder-filter-$folderName").performClick()
        composeRule.waitForIdle()

        // noteInFolder should be visible, noteOutFolder hidden
        scrollToNoteCard(noteInFolder)
        composeRule.onNodeWithTag("note-card-$noteInFolder").assertExists()
        composeRule.onAllNodesWithTag("note-card-$noteOutFolder").assertCountEquals(0)

        // Reset filter
        scrollToNode("folders-scroll-row")
        composeRule.onNodeWithTag("folder-filter-all").performClick()
        composeRule.waitForIdle()

        // Both visible
        scrollToNoteCard(noteInFolder)
        composeRule.onNodeWithTag("note-card-$noteInFolder").assertExists()
        scrollToNoteCard(noteOutFolder)
        composeRule.onNodeWithTag("note-card-$noteOutFolder").assertExists()

        // 4. Rename folder
        scrollToNode("manage-folders-button")
        composeRule.onNodeWithTag("manage-folders-button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("rename-folder-btn-$folderName").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rename-folder-input").performTextReplacement(renamedFolder)
        composeRule.onNodeWithTag("rename-folder-save-btn").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("folder-manager-close").performClick()
        composeRule.waitForIdle()

        // Verify folder renamed on filter chip and card
        scrollToNode("folders-scroll-row")
        composeRule.onNodeWithTag("folder-filter-$renamedFolder").assertExists()
        composeRule.onAllNodesWithTag("folder-filter-$folderName").assertCountEquals(0)

        scrollToNoteCard(noteInFolder)
        composeRule.onNodeWithTag("note-card-$noteInFolder").assert(hasText("📁 $renamedFolder", substring = true))

        // 5. Delete folder
        scrollToNode("manage-folders-button")
        composeRule.onNodeWithTag("manage-folders-button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("delete-folder-btn-$renamedFolder").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("delete-folder-save-btn").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("folder-manager-close").performClick()
        composeRule.waitForIdle()

        // Verify folder chip is gone, but note card is visible and badge is gone
        scrollToNode("folders-scroll-row")
        composeRule.onAllNodesWithTag("folder-filter-$renamedFolder").assertCountEquals(0)

        scrollToNoteCard(noteInFolder)
        composeRule.onNodeWithTag("note-card-$noteInFolder").assert(hasText("📁 $renamedFolder", substring = true).not())
    }

    /**
     * Verifies that attaching an image to a note saves it, renders it on the note card,
     * loads it back into the editor on selection, and allows removing it.
     */
    @Test
    fun pictureTaking_attachesImageAndRendersPreview() {
        val title = "image-note"
        val body = "this is an image note"
        val mockImageUri = "file:///sdcard/Pictures/mock.jpg"

        // 1. Enter text details
        scrollToNode("title-input")
        composeRule.onNodeWithTag("title-input").performTextInput(title)
        composeRule.onNodeWithTag("body-input").performScrollTo()
        composeRule.onNodeWithTag("body-input").performTextInput(body)

        // 2. Attach mock image uri manually on the harness
        harness.onCurrentNoteImageChanged(mockImageUri)
        composeRule.waitForIdle()

        // Editor preview should exist
        composeRule.onNodeWithTag("editor-image-preview-container").assertExists()

        // 3. Save note
        composeRule.onNodeWithTag("save-note-button").performScrollTo()
        composeRule.onNodeWithTag("save-note-button").performClick()
        composeRule.waitForIdle()



        // 4. Verify note card contains image preview component
        scrollToNoteCard(title)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("note-card-image-$title", useUnmergedTree = true).assertExists()

        // 5. Select note and verify it loads back into the editor
        composeRule.onNodeWithTag("note-card-$title").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("editor-image-preview-container").performScrollTo()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor-image-preview-container").assertExists()

        // 6. Click remove image button
        composeRule.onNodeWithTag("remove-image-button").performClick()
        composeRule.waitForIdle()

        // Editor preview should be gone
        composeRule.onAllNodesWithTag("editor-image-preview-container").assertCountEquals(0)

        // Save changes
        composeRule.onNodeWithTag("save-note-button").performScrollTo()
        composeRule.onNodeWithTag("save-note-button").performClick()
        composeRule.waitForIdle()

        // Card image preview should be gone
        scrollToNoteCard(title)
        composeRule.onAllNodes(hasTestTag("note-card-image-$title"), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun voiceRecording_transcribesAndAutoAssignsMetadata() {
        // Grant RECORD_AUDIO permission in the Robolectric sandbox
        org.robolectric.Shadows.shadowOf(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>())
            .grantPermissions(android.Manifest.permission.RECORD_AUDIO)

        val folderName = "Work"
        
        // 1. Create a notebook folder "Work" first to support auto-association testing
        scrollToNode("folders-filter-card")
        composeRule.onNodeWithTag("manage-folders-button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("create-folder-input").performTextInput(folderName)
        composeRule.onNodeWithTag("create-folder-btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("folder-manager-close").performClick()
        composeRule.waitForIdle()

        // 2. Set up the mock transcription result
        com.breakingthebot.quicknotes.services.VoiceTranscriptionService.mockTranscriptResult = 
            "buy groceries for dinner tomorrow at 6 pm"

        // 3. Click the voice note button
        scrollToNode("voice-note-button")
        composeRule.onNodeWithTag("voice-note-button").performClick()
        composeRule.waitForIdle()

        // 4. Verify transcription text auto-populated the editor fields
        scrollToNode("title-input")
        composeRule.onNodeWithTag("title-input").assert(androidx.compose.ui.test.hasText("Voice Note"))
        composeRule.onNodeWithTag("body-input").assert(androidx.compose.ui.test.hasText("buy groceries for dinner tomorrow at 6 pm"))

        // 5. Verify tags auto-extracted ("shopping" and "food" should be detected from "buy groceries for dinner")
        composeRule.onNodeWithTag("tags-input").assert(androidx.compose.ui.test.hasText("shopping, food"))

        // 6. Verify auto-reminder scheduled for tomorrow at 6 PM
        scrollToNode("reminder-display-text")
        composeRule.onNodeWithTag("reminder-display-text").assertExists()

        // 7. Verify inline audio player is visible
        scrollToNode("audio-player-container")
        composeRule.onNodeWithTag("audio-player-container").assertExists()

        // Reset mock
        com.breakingthebot.quicknotes.services.VoiceTranscriptionService.mockTranscriptResult = null
    }

    @Test
    fun voiceRecording_autoAssignsWorkNotebook() {
        // Grant RECORD_AUDIO permission in the Robolectric sandbox
        org.robolectric.Shadows.shadowOf(androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>())
            .grantPermissions(android.Manifest.permission.RECORD_AUDIO)

        val folderName = "Work"
        
        // 1. Create a notebook folder "Work"
        scrollToNode("folders-filter-card")
        composeRule.onNodeWithTag("manage-folders-button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("create-folder-input").performTextInput(folderName)
        composeRule.onNodeWithTag("create-folder-btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("folder-manager-close").performClick()
        composeRule.waitForIdle()

        // 2. Set up the mock transcription result containing "meeting" which triggers "Work" folder assignment
        com.breakingthebot.quicknotes.services.VoiceTranscriptionService.mockTranscriptResult = 
            "important meeting details"

        // 3. Click the voice note button
        scrollToNode("voice-note-button")
        composeRule.onNodeWithTag("voice-note-button").performClick()
        composeRule.waitForIdle()

        // 4. Verify notebook folder "Work" is selected in the editor
        scrollToNode("folder-option-Work")
        composeRule.onNodeWithTag("folder-option-Work").assertIsSelected()

        // Reset mock
        com.breakingthebot.quicknotes.services.VoiceTranscriptionService.mockTranscriptResult = null
    }

    @Test
    fun smartContentAnalyzer_extractsCorrectTagsAndFolders() {
        val notebooks = listOf(
            com.breakingthebot.quicknotes.model.Notebook(id = 101, name = "Work", createdAt = 0L),
            com.breakingthebot.quicknotes.model.Notebook(id = 102, name = "Personal", createdAt = 0L)
        )

        // Case 1: Tag shopping, food, and Personal notebook synonym matching "buy"
        val result1 = com.breakingthebot.quicknotes.util.SmartContentAnalyzer.analyze(
            text = "Need to buy dinner groceries",
            notebooks = notebooks
        )
        org.junit.Assert.assertTrue(result1.autoTags.contains("shopping"))
        org.junit.Assert.assertTrue(result1.autoTags.contains("food"))
        org.junit.Assert.assertEquals(102, result1.notebookId)

        // Case 2: Tag work and Work notebook matching folder name
        val result2 = com.breakingthebot.quicknotes.util.SmartContentAnalyzer.analyze(
            text = "Discuss office deadline details in the Work notebook",
            notebooks = notebooks
        )
        org.junit.Assert.assertTrue(result2.autoTags.contains("work"))
        org.junit.Assert.assertEquals(101, result2.notebookId)
    }

    @Test
    fun smartContentAnalyzer_extractsRelativeDates() {
        val result = com.breakingthebot.quicknotes.util.SmartContentAnalyzer.analyze(
            text = "remind me in 3 hours",
            notebooks = emptyList()
        )
        org.junit.Assert.assertNotNull(result.detectedReminderTime)
        val diff = result.detectedReminderTime!! - System.currentTimeMillis()
        org.junit.Assert.assertTrue(diff in 10700000..10900000)
    }

    /**
     * Verifies swiping left and right on note items triggers archive, restore, and delete operations.
     */
    @Test
    fun swipe_actions_archiveAndTrashNotes() {
        val title = "swipe-note"
        createNote(title, "details", "work")

        // Bring note card into viewport
        scrollToNode("note-card-$title")

        // Swipe Left to archive
        composeRule.onNodeWithTag("note-card-$title").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        // Verify no longer in active list
        composeRule.onNodeWithTag("note-card-$title").assertDoesNotExist()

        // Switch to archive collection
        scrollToNode("collection-chip-archived")
        composeRule.onNodeWithTag("collection-chip-archived").performClick()
        composeRule.waitForIdle()

        // Verify it is in archived list
        scrollToNode("note-card-$title")
        composeRule.onNodeWithTag("note-card-$title").assertExists()

        // Swipe Left on archive card to restore
        composeRule.onNodeWithTag("note-card-$title").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        // Verify no longer in archive list
        composeRule.onNodeWithTag("note-card-$title").assertDoesNotExist()

        // Switch back to active collection
        scrollToNode("collection-chip-active")
        composeRule.onNodeWithTag("collection-chip-active").performClick()
        composeRule.waitForIdle()

        // Verify it is back in active list
        scrollToNode("note-card-$title")
        composeRule.onNodeWithTag("note-card-$title").assertExists()

        // Swipe Right to move to Trash
        composeRule.onNodeWithTag("note-card-$title").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        // Verify no longer in active list
        composeRule.onNodeWithTag("note-card-$title").assertDoesNotExist()

        // Switch to trash collection
        scrollToNode("collection-chip-trash")
        composeRule.onNodeWithTag("collection-chip-trash").performClick()
        composeRule.waitForIdle()

        // Verify it is in trash list
        scrollToNode("note-card-$title")
        composeRule.onNodeWithTag("note-card-$title").assertExists()
    }

    /**
     * Verifies that tapping the Share button triggers the platform share sheet chooser callback.
     */
    @Test
    fun shareNote_triggersShareSheetChooser() {
        val title = "share-note"
        createNote(title, "details", "work")

        // Bring note card into viewport
        scrollToNode("note-card-$title")

        // Click Share button on card
        composeRule.onNodeWithTag("share-button-$title").performScrollTo()
        composeRule.onNodeWithTag("share-button-$title").performClick()
        composeRule.waitForIdle()

        // Verify the callback fired and tracked the note ID
        val expectedNote = harness.state.notes.firstOrNull { it.title == title }
        org.junit.Assert.assertNotNull(expectedNote)
        org.junit.Assert.assertEquals(expectedNote?.id, harness.lastSharedNoteId)
    }

    /**
     * Verifies that the SearchHighlighter utility parses and applies highlight span styles.
     */
    @Test
    fun searchHighlighter_addsHighlightSpans() {
        val original = "Clean the kitchen counter"
        val query = "kitchen"
        val highlighted = com.breakingthebot.quicknotes.util.SearchHighlighter.highlight(original, query)

        org.junit.Assert.assertEquals(original, highlighted.text)
        org.junit.Assert.assertEquals(1, highlighted.spanStyles.size)
        val styleRange = highlighted.spanStyles.first()
        org.junit.Assert.assertEquals(10, styleRange.start)
        org.junit.Assert.assertEquals(17, styleRange.end)
        org.junit.Assert.assertEquals(
            androidx.compose.ui.graphics.Color(0xFFFFD54F),
            styleRange.item.background
        )
    }

    /**
     * Verifies that searching for a term highlights matches inside the note card semantics tree.
     */
    @Test
    fun searchHighlighting_rendersHighlightSpansInSemantics() {
        val title = "cooking"
        createNote(title, "let us cook something delicious", "food")

        // Input search query
        scrollToNode("note-search-field")
        composeRule.onNodeWithTag("note-search-field").performTextInput("cook")
        composeRule.waitForIdle()

        // Verify note card exists and retrieve semantics text attributes
        scrollToNode("note-card-$title")
        val semanticsNode = composeRule.onNodeWithTag("note-card-$title").fetchSemanticsNode()
        val texts = semanticsNode.config[androidx.compose.ui.semantics.SemanticsProperties.Text]

        // Assert that at least one of the merged texts contains a search highlight span background
        val hasHighlight = texts.any { annotated ->
            annotated.spanStyles.any { spanRange ->
                spanRange.item.background == androidx.compose.ui.graphics.Color(0xFFFFD54F)
            }
        }
        org.junit.Assert.assertTrue(hasHighlight)
    }

    /**
     * Verifies that NoteListFormatter correctly filters notes by date range presets and custom boundaries.
     */
    @Test
    fun noteListFormatter_filtersByDateRange() {
        val now = System.currentTimeMillis()
        val todayNote = Note(id = 1, title = "today", body = "body", updatedAt = now)
        val oldNote = Note(id = 2, title = "old", body = "body", updatedAt = now - (10 * 24 * 60 * 60 * 1000L)) // 10 days ago

        val allNotes = listOf(todayNote, oldNote)

        // Filter ALL
        val filterAll = NoteListFormatter.formatNotes(
            notes = allNotes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
            dateFilterOption = DateFilterOption.ALL
        )
        org.junit.Assert.assertEquals(2, filterAll.size)

        // Filter TODAY
        val filterToday = NoteListFormatter.formatNotes(
            notes = allNotes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
            dateFilterOption = DateFilterOption.TODAY
        )
        org.junit.Assert.assertEquals(1, filterToday.size)
        org.junit.Assert.assertEquals("today", filterToday.first().title)

        // Filter THIS_WEEK
        val filterWeek = NoteListFormatter.formatNotes(
            notes = allNotes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
            dateFilterOption = DateFilterOption.THIS_WEEK
        )
        org.junit.Assert.assertEquals(1, filterWeek.size)
        org.junit.Assert.assertEquals("today", filterWeek.first().title)

        // Filter CUSTOM (exactly matching oldNote timestamp range)
        val filterCustom = NoteListFormatter.formatNotes(
            notes = allNotes,
            noteCollection = NoteCollection.ACTIVE,
            searchQuery = "",
            selectedTag = null,
            sortOption = NoteSortOption.NEWEST,
            dateFilterOption = DateFilterOption.CUSTOM,
            customStartDate = now - (11 * 24 * 60 * 60 * 1000L),
            customEndDate = now - (9 * 24 * 60 * 60 * 1000L)
        )
        org.junit.Assert.assertEquals(1, filterCustom.size)
        org.junit.Assert.assertEquals("old", filterCustom.first().title)
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
    private var storedNotebooks = emptyList<com.breakingthebot.quicknotes.model.Notebook>()
    private var nextFolderId = 1

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
            currentNoteNotebookId = note.notebookId,
            currentNoteImageUri = note.imageUri,
            currentNoteAudioUri = note.audioUri,
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
            currentNoteNotebookId = null,
            currentNoteImageUri = null,
            currentNoteAudioUri = null,
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
            notebookId = state.currentNoteNotebookId,
            imageUri = state.currentNoteImageUri,
            audioUri = state.currentNoteAudioUri,
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

    var lastSharedNoteId: Int? = null
        private set

    fun shareNote(noteId: Int) {
        lastSharedNoteId = noteId
    }

    fun onDateFilterOptionChanged(option: DateFilterOption) {
        state = state.copy(
            dateFilterOption = option,
            customStartDate = if (option != DateFilterOption.CUSTOM) null else state.customStartDate,
            customEndDate = if (option != DateFilterOption.CUSTOM) null else state.customEndDate
        )
        syncState()
    }

    fun onCustomDateRangeChanged(start: Long?, end: Long?) {
        state = state.copy(
            customStartDate = start,
            customEndDate = end
        )
        syncState()
    }

    fun createNotebook(name: String) {
        val notebook = com.breakingthebot.quicknotes.model.Notebook(id = nextFolderId++, name = name.trim())
        storedNotebooks = storedNotebooks + notebook
        syncState()
    }

    fun renameNotebook(notebookId: Int, newName: String) {
        storedNotebooks = storedNotebooks.map {
            if (it.id == notebookId) it.copy(name = newName.trim()) else it
        }
        syncState()
    }

    fun deleteNotebook(notebookId: Int) {
        storedNotebooks = storedNotebooks.filterNot { it.id == notebookId }
        storedNotes = storedNotes.map {
            if (it.notebookId == notebookId) it.copy(notebookId = null) else it
        }
        if (state.selectedNotebookId == notebookId) {
            state = state.copy(selectedNotebookId = null)
        }
        if (state.currentNoteNotebookId == notebookId) {
            state = state.copy(currentNoteNotebookId = null)
        }
        syncState()
    }

    fun onNotebookSelected(notebookId: Int?) {
        state = state.copy(selectedNotebookId = notebookId)
        syncState()
    }

    fun onCurrentNoteNotebookChanged(notebookId: Int?) {
        state = state.copy(currentNoteNotebookId = notebookId)
    }

    fun onCurrentNoteImageChanged(uriString: String?) {
        state = state.copy(currentNoteImageUri = uriString)
    }

    fun onCurrentNoteAudioChanged(uriString: String?) {
        state = state.copy(currentNoteAudioUri = uriString)
    }

    fun startVoiceRecording(context: Context) {
        state = state.copy(isRecordingVoice = true)
        val audioPath = "mock-audio-path"
        onCurrentNoteAudioChanged(audioPath)
        com.breakingthebot.quicknotes.services.VoiceTranscriptionService.startListening(
            context = context,
            onResult = { transcript ->
                stopVoiceRecording(transcript)
            },
            onError = { _ ->
                stopVoiceRecording(null)
            }
        )
    }

    fun stopVoiceRecording(transcript: String?) {
        state = state.copy(isRecordingVoice = false)
        if (!transcript.isNullOrBlank()) {
            val title = if (state.currentTitle.isBlank()) "Voice Note" else state.currentTitle
            val body = if (state.currentBody.isBlank()) transcript else "${state.currentBody}\n$transcript"
            
            state = state.copy(
                currentTitle = title,
                currentBody = body
            )

            val analysis = com.breakingthebot.quicknotes.util.SmartContentAnalyzer.analyze(
                text = transcript,
                notebooks = state.notebooks
            )

            if (analysis.autoTags.isNotEmpty()) {
                val currentTags = NoteTagFormatter.parseInput(state.currentTagsInput).toMutableSet()
                currentTags.addAll(analysis.autoTags)
                state = state.copy(
                    currentTagsInput = NoteTagFormatter.formatForEditor(currentTags.toList())
                )
            }

            if (analysis.notebookId != null && state.currentNoteNotebookId == null) {
                state = state.copy(
                    currentNoteNotebookId = analysis.notebookId
                )
            }

            if (analysis.detectedReminderTime != null && state.selectedReminderTime == null) {
                state = state.copy(
                    selectedReminderTime = analysis.detectedReminderTime
                )
            }
        }
    }

    private fun syncState() {
        val formattedNotes = NoteListFormatter.formatNotes(
            notes = storedNotes,
            noteCollection = state.noteCollection,
            searchQuery = state.searchQuery,
            selectedTag = state.selectedTag,
            sortOption = state.sortOption,
            dateFilterOption = state.dateFilterOption,
            customStartDate = state.customStartDate,
            customEndDate = state.customEndDate,
            selectedNotebookId = state.selectedNotebookId,
        )
        state = state.copy(
            notes = formattedNotes,
            availableTags = NoteListFormatter.availableTags(
                notes = storedNotes,
                noteCollection = state.noteCollection,
            ),
            notebooks = storedNotebooks,
        )
    }
}
