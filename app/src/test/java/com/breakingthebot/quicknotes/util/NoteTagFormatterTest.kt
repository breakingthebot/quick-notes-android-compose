/*
 * Verifies tag parsing and storage formatting behavior for notes.
 * Connects to: NoteTagFormatter and tag-related UI/state flows.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for NoteTagFormatter.
 */
class NoteTagFormatterTest {
    /**
     * Confirms user input is trimmed, lowercased, and deduplicated.
     */
    @Test
    fun parseInput_normalizesAndDeduplicatesTags() {
        val parsedTags = NoteTagFormatter.parseInput(" Work, ideas,work,  Personal ")

        assertEquals(listOf("work", "ideas", "personal"), parsedTags)
    }

    /**
     * Confirms tag lists serialize and deserialize cleanly.
     */
    @Test
    fun serializeTags_roundTripsTagList() {
        val tags = listOf("work", "ideas")
        val serializedTags = NoteTagFormatter.serializeTags(tags)

        assertEquals(tags, NoteTagFormatter.deserializeTags(serializedTags))
    }
}
