/*
 * Encapsulates note persistence operations behind a small data service.
 * Connects to: NoteDao, NotesViewModel, and note-related tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import com.breakingthebot.quicknotes.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository that mediates access to note storage.
 *
 * @property noteDao DAO used for actual database work.
 */
class NoteRepository(
    private val noteDao: NoteDao,
) {
    /**
     * Streams the current note list.
     *
     * @return Observable notes ordered by update time.
     */
    fun observeNotes(): Flow<List<Note>> = noteDao.observeNotes()

    /**
     * Persists a new note.
     *
     * @param note Note to insert.
     */
    suspend fun addNote(note: Note): Long {
        return noteDao.insert(note)
    }

    /**
     * Persists changes to an existing note.
     *
     * @param note Updated note values.
     */
    suspend fun updateNote(note: Note) {
        noteDao.update(note)
    }

    /**
     * Removes a stored note.
     *
     * @param note Note to delete.
     */
    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
    }

    /**
     * Permanently empties all deleted notes from database.
     */
    suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }
}
