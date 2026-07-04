/*
 * Exercises the main notes flows through Robolectric-backed Compose UI tests.
 * Connects to: QuickNotesApp, NotesViewModel, InMemoryNoteDao, and local JVM CI.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.breakingthebot.quicknotes.data.InMemoryNoteDao
import com.breakingthebot.quicknotes.data.NoteRepository
import com.breakingthebot.quicknotes.ui.theme.QuickNotesTheme
import com.breakingthebot.quicknotes.util.MainDispatcherRule
import com.breakingthebot.quicknotes.viewmodel.NotesViewModel
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

    private lateinit var viewModel: NotesViewModel

    /**
     * Sets up a fresh screen instance backed by in-memory note storage.
     */
    @Before
    fun setUp() {
        viewModel = NotesViewModel(
            repository = NoteRepository(InMemoryNoteDao()),
        )

        composeRule.setContent {
            QuickNotesTheme {
                QuickNotesApp(viewModel = viewModel)
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
