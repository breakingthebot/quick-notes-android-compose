/*
 * Formats note data into a compact widget-friendly snapshot.
 * Connects to: Note model, QuickNotesWidgetSnapshot, and widget tests.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.widget

import com.breakingthebot.quicknotes.model.Note

private const val WIDGET_PREVIEW_LENGTH = 72

/**
 * Shared formatter for Quick Notes widget content.
 */
object QuickNotesWidgetFormatter {
    /**
     * Builds widget display state from recent active notes.
     *
     * @param notes Recent active notes ordered by newest first.
     * @return Snapshot ready for Glance rendering.
     */
    fun format(notes: List<Note>): QuickNotesWidgetSnapshot {
        return QuickNotesWidgetSnapshot(
            noteCountLabel = noteCountLabel(notes.size),
            notes = notes.map { note ->
                QuickNotesWidgetNote(
                    title = note.title,
                    preview = previewText(note.body),
                )
            },
            emptyMessage = "Create a note to see it on your home screen.",
        )
    }

    /**
     * Builds the note-count label shown under the widget title.
     *
     * @param noteCount Number of active notes included in the snapshot.
     * @return Human-readable summary text.
     */
    fun noteCountLabel(noteCount: Int): String {
        return when (noteCount) {
            0 -> "No active notes"
            1 -> "1 active note"
            else -> "$noteCount active notes"
        }
    }

    private fun previewText(body: String): String {
        val compactBody = body
            .trim()
            .replace(Regex("\\s+"), " ")

        return if (compactBody.length <= WIDGET_PREVIEW_LENGTH) {
            compactBody
        } else {
            compactBody.take(WIDGET_PREVIEW_LENGTH - 1).trimEnd() + "…"
        }
    }
}
