/*
 * Converts note tag lists to and from Room-compatible string storage.
 * Connects to: Note entity, QuickNotesDatabase, and tag parsing utilities.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import androidx.room.TypeConverter
import com.breakingthebot.quicknotes.util.NoteTagFormatter

/**
 * Bridges tag collections between Kotlin lists and Room text columns.
 */
class TagListConverter {
    /**
     * Serializes a tag list for database storage.
     *
     * @param tags Ordered note tag list.
     * @return Stable string representation for Room.
     */
    @TypeConverter
    fun fromTags(tags: List<String>): String {
        return NoteTagFormatter.serializeTags(tags)
    }

    /**
     * Deserializes database text back into a tag list.
     *
     * @param serializedTags Stored tag string.
     * @return Parsed note tag list.
     */
    @TypeConverter
    fun toTags(serializedTags: String): List<String> {
        return NoteTagFormatter.deserializeTags(serializedTags)
    }
}
