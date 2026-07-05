/*
 * Provides a reusable in-memory NoteDao implementation for local JVM tests.
 * Connects to: NoteRepository tests, Compose UI tests, and the NoteDao contract.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.data

import com.breakingthebot.quicknotes.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory DAO used by JVM tests to avoid Room or emulator dependencies.
 */
class InMemoryNoteDao : NoteDao {
    private val notes = MutableStateFlow(emptyList<Note>())
    private var nextId = 1

    /**
     * Streams the current in-memory notes list.
     *
     * @return Observable note list sorted by update time.
     */
    override fun observeNotes(): Flow<List<Note>> = notes

    /**
     * Returns the most recent active notes for widget-related tests.
     *
     * @param limit Maximum number of notes to return.
     * @return Recent active notes only.
     */
    override suspend fun getRecentActiveNotes(limit: Int): List<Note> {
        return notes.value
            .filter { note -> !note.isArchived && !note.isDeleted }
            .sortedByDescending { note -> note.updatedAt }
            .take(limit)
    }

    /**
     * Permanently removes all deleted notes from memory.
     */
    override suspend fun emptyTrash() {
        notes.value = notes.value.filterNot { note -> note.isDeleted }
    }

    /**
     * Adds or replaces a note in memory.
     *
     * @param note Note to store.
     */
    override suspend fun insert(note: Note): Long {
        val storedNote = if (note.id == 0) {
            note.copy(id = nextId++)
        } else {
            nextId = maxOf(nextId, note.id + 1)
            note
        }
        notes.value = (notes.value.filterNot { existingNote -> existingNote.id == storedNote.id } + storedNote)
            .sortedByDescending { existingNote -> existingNote.updatedAt }
        return storedNote.id.toLong()
    }

    /**
     * Replaces an existing note in memory.
     *
     * @param note Note to update.
     */
    override suspend fun update(note: Note) {
        insert(note)
    }

    /**
     * Removes a note from memory.
     *
     * @param note Note to delete.
     */
    override suspend fun delete(note: Note) {
        notes.value = notes.value.filterNot { existingNote -> existingNote.id == note.id }
    }

    /**
     * Dissociates notes from a deleted notebook.
     *
     * @param notebookId The id of the deleted notebook.
     */
    override suspend fun clearNotebookReferences(notebookId: Int) {
        notes.value = notes.value.map { note ->
            if (note.notebookId == notebookId) note.copy(notebookId = null) else note
        }
    }
}
