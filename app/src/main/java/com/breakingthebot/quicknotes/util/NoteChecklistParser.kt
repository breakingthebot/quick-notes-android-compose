/*
 * Parses note body plain text into structured checklist items and back.
 * Connects to: Note model, NotesViewModel, and NoteListItem.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.util

/**
 * Represents a single item in a note's checklist.
 *
 * @property text The description of the checklist item.
 * @property isChecked Whether the item is checked.
 */
data class ChecklistItem(
    val text: String,
    val isChecked: Boolean
)

/**
 * Utility to parse and format checklist note body content.
 */
object NoteChecklistParser {
    /**
     * Parses a note body string into a list of checklist items.
     *
     * @param body Raw note body string.
     * @return List of parsed checklist items.
     */
    fun parse(body: String): List<ChecklistItem> {
        if (body.isBlank()) return emptyList()
        return body.lines().map { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[x]") || trimmed.startsWith("[X]") -> {
                    ChecklistItem(trimmed.substring(3).trim(), true)
                }
                trimmed.startsWith("[ ]") -> {
                    ChecklistItem(trimmed.substring(3).trim(), false)
                }
                else -> {
                    ChecklistItem(trimmed, false)
                }
            }
        }
    }

    /**
     * Formats a list of checklist items into a note body string.
     *
     * @param items List of checklist items.
     * @return Formatted note body string.
     */
    fun toBodyString(items: List<ChecklistItem>): String {
        return items.joinToString("\n") { item ->
            val prefix = if (item.isChecked) "[x]" else "[ ]"
            "$prefix ${item.text}"
        }
    }
}
