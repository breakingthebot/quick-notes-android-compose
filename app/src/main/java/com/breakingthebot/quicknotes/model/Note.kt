/*
 * Defines the note entity stored in Room and displayed in the UI.
 * Connects to: NoteDao, QuickNotesDatabase, and NotesViewModel.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single note in local storage.
 *
 * @property id Stable Room primary key.
 * @property title User-provided note title.
 * @property body User-provided note body content.
 * @property updatedAt Epoch millis for the most recent edit.
 * @property isArchived Whether the note is hidden from the active list.
 * @property tags Lightweight labels used for organization and filtering.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val tags: List<String> = emptyList(),
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val isChecklist: Boolean = false,
)
