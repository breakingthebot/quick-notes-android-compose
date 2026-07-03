/*
 * Verifies repository behavior against a fake DAO implementation.
 * Connects to: NoteRepository, NoteDao, and Note model.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import com.breakingthebot.quicknotes.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for NoteRepository.
 */
class NoteRepositoryTest {
    /**
     * Confirms repository insert delegates to the DAO.
     */
    @Test
    fun addNote_insertsNoteIntoDao() = runTest {
        val fakeDao = FakeNoteDao()
        val repository = NoteRepository(fakeDao)
        val note = Note(id = 1, title = "Title", body = "Body", updatedAt = 1L)

        repository.addNote(note)

        assertEquals(listOf(note), fakeDao.observeNotes().first())
    }

    /**
     * Confirms repository update replaces existing note content.
     */
    @Test
    fun updateNote_updatesStoredNote() = runTest {
        val fakeDao = FakeNoteDao()
        val repository = NoteRepository(fakeDao)
        val original = Note(id = 7, title = "Old", body = "Body", updatedAt = 2L)
        val updated = original.copy(title = "New")

        repository.addNote(original)
        repository.updateNote(updated)

        assertEquals(listOf(updated), fakeDao.observeNotes().first())
    }

    /**
     * Confirms repository delete removes stored notes.
     */
    @Test
    fun deleteNote_removesStoredNote() = runTest {
        val fakeDao = FakeNoteDao()
        val repository = NoteRepository(fakeDao)
        val note = Note(id = 9, title = "Keep", body = "Track", updatedAt = 3L)

        repository.addNote(note)
        repository.deleteNote(note)

        assertEquals(emptyList<Note>(), fakeDao.observeNotes().first())
    }
}

/**
 * In-memory DAO used for repository unit tests.
 */
private class FakeNoteDao : NoteDao {
    private val notes = MutableStateFlow(emptyList<Note>())

    /**
     * Streams the in-memory note list.
     *
     * @return Observable list of notes.
     */
    override fun observeNotes(): Flow<List<Note>> = notes

    /**
     * Adds or replaces a note in the in-memory list.
     *
     * @param note Note to store.
     */
    override suspend fun insert(note: Note) {
        notes.value = (notes.value.filterNot { existingNote -> existingNote.id == note.id } + note)
            .sortedByDescending { storedNote -> storedNote.updatedAt }
    }

    /**
     * Replaces a note in the in-memory list.
     *
     * @param note Note to update.
     */
    override suspend fun update(note: Note) {
        insert(note)
    }

    /**
     * Removes a note from the in-memory list.
     *
     * @param note Note to remove.
     */
    override suspend fun delete(note: Note) {
        notes.value = notes.value.filterNot { existingNote -> existingNote.id == note.id }
    }
}
