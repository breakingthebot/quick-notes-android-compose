/*
 * Formats note timestamps for human-readable display.
 * Connects to: NoteListItem and time-formatting tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val noteTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, h:mm a")

/**
 * Converts timestamps into compact note metadata strings.
 */
object TimeFormatter {
    /**
     * Formats an epoch timestamp in the current system time zone.
     *
     * @param updatedAt Epoch millis timestamp.
     * @return Display string for UI metadata.
     */
    fun formatUpdatedAt(updatedAt: Long): String {
        return noteTimeFormatter.format(
            Instant.ofEpochMilli(updatedAt).atZone(ZoneId.systemDefault()),
        )
    }
}
