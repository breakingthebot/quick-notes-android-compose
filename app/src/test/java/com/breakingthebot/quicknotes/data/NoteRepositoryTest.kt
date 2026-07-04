/*
 * Verifies repository behavior against a fake DAO implementation.
 * Connects to: NoteRepository, NoteDao, and Note model.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import com.breakingthebot.quicknotes.model.Note
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
        val fakeDao = InMemoryNoteDao()
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
        val fakeDao = InMemoryNoteDao()
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
        val fakeDao = InMemoryNoteDao()
        val repository = NoteRepository(fakeDao)
        val note = Note(id = 9, title = "Keep", body = "Track", updatedAt = 3L)

        repository.addNote(note)
        repository.deleteNote(note)

        assertEquals(emptyList<Note>(), fakeDao.observeNotes().first())
    }
}
