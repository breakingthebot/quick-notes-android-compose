/*
 * Defines the notebook entity stored in Room for categorizing notes.
 * Connects to: Note entity, NotebookDao, and QuickNotesDatabase.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a Folder or Notebook containing multiple categorized notes.
 *
 * @property id Stable Room primary key.
 * @property name User-provided folder/notebook name.
 * @property createdAt Epoch millis timestamp when created.
 */
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
