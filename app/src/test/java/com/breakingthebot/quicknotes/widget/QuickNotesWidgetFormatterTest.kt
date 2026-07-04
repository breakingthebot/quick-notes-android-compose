/*
 * Verifies the snapshot formatting used by the Quick Notes home-screen widget.
 * Connects to: QuickNotesWidgetFormatter, QuickNotesWidgetSnapshot, and Note model.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.widget

import com.breakingthebot.quicknotes.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for QuickNotesWidgetFormatter.
 */
class QuickNotesWidgetFormatterTest {
    /**
     * Confirms the empty-state snapshot is returned when there are no notes.
     */
    @Test
    fun format_returnsEmptySnapshotWhenNoNotesExist() {
        val snapshot = QuickNotesWidgetFormatter.format(emptyList())

        assertEquals("No active notes", snapshot.noteCountLabel)
        assertTrue(snapshot.notes.isEmpty())
        assertEquals("Create a note to see it on your home screen.", snapshot.emptyMessage)
    }

    /**
     * Confirms note-count summaries use singular and plural copy correctly.
     */
    @Test
    fun noteCountLabel_formatsCountCopy() {
        assertEquals("1 active note", QuickNotesWidgetFormatter.noteCountLabel(1))
        assertEquals("3 active notes", QuickNotesWidgetFormatter.noteCountLabel(3))
    }

    /**
     * Confirms body previews collapse whitespace and trim long copy.
     */
    @Test
    fun format_compactsAndTruncatesBodyPreview() {
        val snapshot = QuickNotesWidgetFormatter.format(
            listOf(
                Note(
                    id = 1,
                    title = "Sprint plan",
                    body = "Line one\n\nLine two with extra spacing that should collapse into one readable widget preview sentence.",
                    updatedAt = 1L,
                ),
            ),
        )

        assertEquals("1 active note", snapshot.noteCountLabel)
        assertEquals("Sprint plan", snapshot.notes.first().title)
        assertTrue(snapshot.notes.first().preview.startsWith("Line one Line two"))
        assertTrue(snapshot.notes.first().preview.endsWith("…"))
    }
}
