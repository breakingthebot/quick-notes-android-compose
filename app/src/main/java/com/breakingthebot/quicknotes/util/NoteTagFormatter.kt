/*
 * Normalizes, formats, and serializes note tags for storage and UI display.
 * Connects to: NotesViewModel, TagListConverter, and tag tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

/**
 * Shared helpers for note tag parsing and formatting.
 */
object NoteTagFormatter {
    private const val STORAGE_SEPARATOR = "|"
    private const val INPUT_SEPARATOR = ","

    /**
     * Parses comma-separated user input into a normalized unique tag list.
     *
     * @param rawTagInput User-entered tag text.
     * @return Clean note tag list.
     */
    fun parseInput(rawTagInput: String): List<String> {
        return rawTagInput.split(INPUT_SEPARATOR)
            .map { tag -> tag.trim().lowercase() }
            .filter { tag -> tag.isNotBlank() }
            .distinct()
    }

    /**
     * Formats a tag list for the editor field.
     *
     * @param tags Stored tag list.
     * @return Comma-separated editor string.
     */
    fun formatForEditor(tags: List<String>): String {
        return tags.joinToString(separator = ", ")
    }

    /**
     * Serializes tags for Room storage.
     *
     * @param tags Tag list to persist.
     * @return Stable string representation.
     */
    fun serializeTags(tags: List<String>): String {
        return tags.joinToString(separator = STORAGE_SEPARATOR)
    }

    /**
     * Deserializes stored tags back into a list.
     *
     * @param serializedTags Stored tag text.
     * @return Parsed tag list.
     */
    fun deserializeTags(serializedTags: String): List<String> {
        if (serializedTags.isBlank()) {
            return emptyList()
        }

        return serializedTags.split(STORAGE_SEPARATOR)
            .map { tag -> tag.trim() }
            .filter { tag -> tag.isNotBlank() }
    }
}
