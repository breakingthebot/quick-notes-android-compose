/*
 * Encapsulates note persistence operations behind a small data service.
 * Connects to: NoteDao, NotesViewModel, and note-related tests.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.model.Notebook
import kotlinx.coroutines.flow.Flow

/**
 * Repository that mediates access to note storage.
 *
 * @property noteDao DAO used for actual database work.
 * @property notebookDao DAO used for notebook folder work.
 */
class NoteRepository(
    private val noteDao: NoteDao,
    private val notebookDao: NotebookDao,
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

    /**
     * Streams the list of notebooks sorted alphabetically.
     *
     * @return Observable notebook list.
     */
    fun observeNotebooks(): Flow<List<Notebook>> = notebookDao.observeNotebooks()

    /**
     * Persists a new notebook folder.
     *
     * @param notebook Notebook to insert.
     * @return Row ID of the inserted notebook.
     */
    suspend fun addNotebook(notebook: Notebook): Long = notebookDao.insert(notebook)

    /**
     * Persists updates to an existing notebook (e.g. name).
     *
     * @param notebook Updated notebook content.
     */
    suspend fun updateNotebook(notebook: Notebook) {
        notebookDao.update(notebook)
    }

    /**
     * Deletes a notebook and dissociates all of its notes (sets their notebookId = null).
     *
     * @param notebook Notebook to delete.
     */
    suspend fun deleteNotebook(notebook: Notebook) {
        noteDao.clearNotebookReferences(notebook.id)
        notebookDao.delete(notebook)
    }
}
