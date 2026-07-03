/*
 * Normalizes note form input before persistence.
 * Connects to: NotesViewModel and note-related tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

private const val MAX_TITLE_LENGTH = 80

/**
 * Utilities for sanitizing note editor input.
 */
object NoteInputSanitizer {
    /**
     * Trims and constrains a note title for better list readability.
     *
     * @param rawTitle User-entered title text.
     * @return Normalized title value.
     */
    fun sanitizeTitle(rawTitle: String): String {
        return rawTitle.trim().take(MAX_TITLE_LENGTH)
    }

    /**
     * Trims body text while preserving intentional internal line breaks.
     *
     * @param rawBody User-entered body text.
     * @return Normalized body value.
     */
    fun sanitizeBody(rawBody: String): String {
        return rawBody.trim()
    }
}
