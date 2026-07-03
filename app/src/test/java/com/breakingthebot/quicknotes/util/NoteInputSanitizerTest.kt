/*
 * Verifies title and body normalization rules for note input.
 * Connects to: NoteInputSanitizer.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for NoteInputSanitizer.
 */
class NoteInputSanitizerTest {
    /**
     * Confirms whitespace is trimmed from titles.
     */
    @Test
    fun sanitizeTitle_trimsOuterWhitespace() {
        assertEquals("Weekly plan", NoteInputSanitizer.sanitizeTitle("  Weekly plan  "))
    }

    /**
     * Confirms overlong titles are capped for UI readability.
     */
    @Test
    fun sanitizeTitle_limitsTitleLength() {
        val rawTitle = "a".repeat(100)

        assertEquals(80, NoteInputSanitizer.sanitizeTitle(rawTitle).length)
    }

    /**
     * Confirms body text is trimmed without collapsing internal formatting.
     */
    @Test
    fun sanitizeBody_trimsOuterWhitespace() {
        assertEquals("line one\nline two", NoteInputSanitizer.sanitizeBody("  line one\nline two  "))
    }
}
