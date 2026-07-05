/*
 * Unit tests for NoteMarkdownParser.
 * Connects to: NoteMarkdownParser.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.util

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies parsing logic inside NoteMarkdownParser.
 */
class NoteMarkdownParserTest {

    @Test
    fun parse_plainText_returnsUnstyledString() {
        val result = NoteMarkdownParser.parse("Hello World")
        assertEquals("Hello World", result.text)
        assertEquals(0, result.spanStyles.size)
    }

    @Test
    fun parse_boldText_appliesBoldStyle() {
        val result = NoteMarkdownParser.parse("Hello **World**")
        assertEquals("Hello World", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(FontWeight.Bold, result.spanStyles[0].item.fontWeight)
    }

    @Test
    fun parse_italicText_appliesItalicStyle() {
        val result = NoteMarkdownParser.parse("Hello *World*")
        assertEquals("Hello World", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(FontStyle.Italic, result.spanStyles[0].item.fontStyle)
    }

    @Test
    fun parse_headers_appliesBoldToLines() {
        val result = NoteMarkdownParser.parse("# Header\n## Header 2")
        assertEquals("Header\nHeader 2", result.text)
        assertEquals(2, result.spanStyles.size)
        assertEquals(FontWeight.Bold, result.spanStyles[0].item.fontWeight)
        assertEquals(FontWeight.Bold, result.spanStyles[1].item.fontWeight)
    }

    @Test
    fun parse_unpairedMarkers_rendersRawCharacters() {
        val result = NoteMarkdownParser.parse("Hello **World")
        assertEquals("Hello **World", result.text)
        assertEquals(0, result.spanStyles.size)
    }
}
