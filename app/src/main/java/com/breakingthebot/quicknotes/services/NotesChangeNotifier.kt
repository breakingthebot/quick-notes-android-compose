/*
 * Defines side effects that run after notes change in local storage.
 * Connects to: NotesViewModel and widget refresh implementations.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.services

/**
 * Contract for post-mutation note side effects.
 */
interface NotesChangeNotifier {
    /**
     * Handles a completed note data change.
     */
    suspend fun onNotesChanged()
}
