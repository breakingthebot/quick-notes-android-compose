/*
 * Exercises the main user flows of the notes app through Compose UI tests.
 * Connects to: MainActivity, Compose screen test tags, and GitHub Actions instrumentation runs.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for the primary note management flows.
 */
@RunWith(AndroidJUnit4::class)
class NotesAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * Verifies that a note can be created and rendered in the list.
     */
    @Test
    fun createNote_showsSavedNoteInList() {
        val title = uniqueTitle("sprint-plan")
        createNote(
            title = title,
            body = "Write UI tests",
            tags = "work, test",
        )

        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.onNodeWithText("#work").assertIsDisplayed()
    }

    /**
     * Verifies that search narrows the visible note list.
     */
    @Test
    fun search_filtersVisibleNotes() {
        val firstTitle = uniqueTitle("sprint-plan")
        val secondTitle = uniqueTitle("groceries")

        createNote(firstTitle, "Write UI tests", "work")
        createNote(secondTitle, "Buy apples", "home")

        composeRule.onNodeWithTag("note-search-field")
            .performTextInput("Sprint")

        composeRule.onNodeWithText(firstTitle).assertIsDisplayed()
        composeRule.onNodeWithText(secondTitle).assertDoesNotExist()
    }

    /**
     * Verifies that tag chips filter the active collection.
     */
    @Test
    fun tagFilter_limitsNotesToSelectedTag() {
        val workTitle = uniqueTitle("work-note")
        val healthTitle = uniqueTitle("health-note")

        createNote(workTitle, "Write UI tests", "work")
        createNote(healthTitle, "Morning session", "health")

        composeRule.onNodeWithTag("tag-filter-work").performClick()

        composeRule.onNodeWithText(workTitle).assertIsDisplayed()
        composeRule.onNodeWithText(healthTitle).assertDoesNotExist()
    }

    /**
     * Verifies that notes can move between active and archived collections.
     */
    @Test
    fun archiveAndRestore_movesNoteAcrossCollections() {
        val title = uniqueTitle("archive-note")
        createNote(title, "Write UI tests", "work")

        composeRule.onNodeWithTag("archive-button-$title").performClick()

        composeRule.onNodeWithTag("collection-chip-archived").performClick()
        composeRule.onNodeWithText(title).assertIsDisplayed()

        composeRule.onNodeWithTag("restore-button-$title").performClick()
        composeRule.onNodeWithTag("collection-chip-active").performClick()
        composeRule.onNodeWithText(title).assertIsDisplayed()
    }

    /**
     * Creates a note through the public UI.
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
        composeRule.onNodeWithTag("body-input").performTextInput(body)
        composeRule.onNodeWithTag("tags-input").performTextInput(tags)
        composeRule.onNodeWithTag("save-note-button").performClick()
    }

    /**
     * Returns a unique title so tests do not depend on global app storage state.
     *
     * @param prefix Stable prefix for the scenario.
     * @return Unique note title.
     */
    private fun uniqueTitle(prefix: String): String {
        return "$prefix-${System.nanoTime()}"
    }
}
