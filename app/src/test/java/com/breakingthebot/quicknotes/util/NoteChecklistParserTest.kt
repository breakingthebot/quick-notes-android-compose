/*
 * Verifies checklist parsing and formatting behavior.
 * Connects to: NoteChecklistParser and ChecklistItem.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for NoteChecklistParser.
 */
class NoteChecklistParserTest {

    /**
     * Confirms that empty body string returns empty checklist item list.
     */
    @Test
    fun parse_emptyBody_returnsEmptyList() {
        val items = NoteChecklistParser.parse("")
        assertTrue(items.isEmpty())
    }

    /**
     * Confirms that plain text lines default to unchecked checklist items.
     */
    @Test
    fun parse_plainText_returnsUncheckedItems() {
        val body = "Buy milk\nCall doctor"
        val items = NoteChecklistParser.parse(body)

        assertEquals(2, items.size)
        assertEquals("Buy milk", items[0].text)
        assertFalse(items[0].isChecked)
        assertEquals("Call doctor", items[1].text)
        assertFalse(items[1].isChecked)
    }

    /**
     * Confirms that lines with checked or unchecked prefixes are parsed correctly.
     */
    @Test
    fun parse_checkedAndUncheckedPrefixes_parsesCorrectly() {
        val body = "[ ] Buy milk\n[x] Call doctor\n[X] Gym session"
        val items = NoteChecklistParser.parse(body)

        assertEquals(3, items.size)
        assertEquals("Buy milk", items[0].text)
        assertFalse(items[0].isChecked)
        assertEquals("Call doctor", items[1].text)
        assertTrue(items[1].isChecked)
        assertEquals("Gym session", items[2].text)
        assertTrue(items[2].isChecked)
    }

    /**
     * Confirms checklist items format back to note body text correctly.
     */
    @Test
    fun toBodyString_formatsCorrectly() {
        val items = listOf(
            ChecklistItem("Buy milk", false),
            ChecklistItem("Call doctor", true)
        )
        val body = NoteChecklistParser.toBodyString(items)
        assertEquals("[ ] Buy milk\n[x] Call doctor", body)
    }
}
