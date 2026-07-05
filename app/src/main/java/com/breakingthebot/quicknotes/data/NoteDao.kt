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
     * Loads the most recent active notes for widget snapshots.
     *
     * @param limit Maximum number of active notes to return.
     * @return Recent active notes ordered by latest update.
     */
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentActiveNotes(limit: Int): List<Note>

    /**
     * Permanently removes all notes marked as deleted.
     */
    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()

    /**
     * Inserts a new note into storage.
     *
     * @param note Note content to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

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
