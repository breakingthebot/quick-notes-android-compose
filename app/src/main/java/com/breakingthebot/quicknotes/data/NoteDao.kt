/*
 * Exposes Room database operations for notes.
 * Connects to: Note entity, NoteRepository, and QuickNotesDatabase.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.breakingthebot.quicknotes.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes note rows in the local Room database.
 */
@Dao
interface NoteDao {
    /**
     * Streams all notes sorted by most recently updated first.
     *
     * @return Observable note list.
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<Note>>

    /**
     * Inserts a new note into storage.
     *
     * @param note Note content to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    /**
     * Updates an existing note.
     *
     * @param note Existing note with changed fields.
     */
    @Update
    suspend fun update(note: Note)

    /**
     * Removes a note from storage.
     *
     * @param note Note to delete.
     */
    @Delete
    suspend fun delete(note: Note)
}
