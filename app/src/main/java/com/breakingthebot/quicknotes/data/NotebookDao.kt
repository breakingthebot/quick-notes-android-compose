/*
 * Exposes Room database operations for notebooks.
 * Connects to: Notebook entity, NoteRepository, and QuickNotesDatabase.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.breakingthebot.quicknotes.model.Notebook
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes notebook rows in the local Room database.
 */
@Dao
interface NotebookDao {
    /**
     * Streams all notebooks sorted alphabetically by name.
     *
     * @return Observable list of notebooks.
     */
    @Query("SELECT * FROM notebooks ORDER BY name ASC")
    fun observeNotebooks(): Flow<List<Notebook>>

    /**
     * Inserts a new notebook folder.
     *
     * @param notebook Notebook to insert.
     * @return Row ID of the inserted notebook.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notebook: Notebook): Long

    /**
     * Updates notebook metadata (e.g. name).
     *
     * @param notebook Notebook to update.
     */
    @Update
    suspend fun update(notebook: Notebook)

    /**
     * Removes a notebook from storage.
     *
     * @param notebook Notebook to delete.
     */
    @Delete
    suspend fun delete(notebook: Notebook)
}
