/*
 * Owns note screen state and coordinates UI actions with Room storage.
 * Connects to: NoteRepository, NotesScreenState, and Compose UI.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakingthebot.quicknotes.data.NoteRepository
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.ui.NoteCollection
import com.breakingthebot.quicknotes.ui.NoteSortOption
import com.breakingthebot.quicknotes.ui.NotesScreenState
import com.breakingthebot.quicknotes.util.NoteInputSanitizer
import com.breakingthebot.quicknotes.util.NoteListFormatter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View model for note CRUD interactions.
 *
 * @property repository Repository used to persist note changes.
 */
class NotesViewModel(
    private val repository: NoteRepository,
) : ViewModel() {
    private val editorState = MutableStateFlow(NotesScreenState())
    private val messageEvents = MutableSharedFlow<String>()

    /**
     * Public screen state derived from editor inputs and stored notes.
     */
    val screenState: StateFlow<NotesScreenState> = combine(
        repository.observeNotes(),
        editorState,
    ) { notes, editor ->
        editor.copy(
            notes = NoteListFormatter.formatNotes(
                notes = notes,
                noteCollection = editor.noteCollection,
                searchQuery = editor.searchQuery,
                sortOption = editor.sortOption,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = NotesScreenState(),
    )

    /**
     * Snackbar-style one-off user messages.
     */
    val messages = messageEvents.asSharedFlow()

    /**
     * Updates the title field in the editor state.
     *
     * @param title New title field value.
     */
    fun onTitleChanged(title: String) {
        editorState.value = editorState.value.copy(currentTitle = title)
    }

    /**
     * Updates the body field in the editor state.
     *
     * @param body New body field value.
     */
    fun onBodyChanged(body: String) {
        editorState.value = editorState.value.copy(currentBody = body)
    }

    /**
     * Updates the search query used to filter the visible note list.
     *
     * @param query New search field value.
     */
    fun onSearchQueryChanged(query: String) {
        editorState.value = editorState.value.copy(searchQuery = query)
    }

    /**
     * Updates the selected note list sort mode.
     *
     * @param sortOption New list ordering selection.
     */
    fun onSortOptionChanged(sortOption: NoteSortOption) {
        editorState.value = editorState.value.copy(sortOption = sortOption)
    }

    /**
     * Updates the visible note collection between active and archived notes.
     *
     * @param noteCollection New collection tab selection.
     */
    fun onNoteCollectionChanged(noteCollection: NoteCollection) {
        editorState.value = editorState.value.copy(noteCollection = noteCollection)
    }

    /**
     * Loads an existing note into the editor.
     *
     * @param noteId Identifier of the note to edit.
     */
    fun selectNote(noteId: Int) {
        val selectedNote = screenState.value.notes.firstOrNull { note -> note.id == noteId } ?: return
        editorState.value = editorState.value.copy(
            currentTitle = selectedNote.title,
            currentBody = selectedNote.body,
            selectedNoteId = selectedNote.id,
            selectedNoteIsArchived = selectedNote.isArchived,
        )
    }

    /**
     * Clears the editor back to create mode.
     */
    fun clearEditor() {
        editorState.value = editorState.value.copy(
            currentTitle = "",
            currentBody = "",
            selectedNoteId = null,
            selectedNoteIsArchived = false,
        )
    }

    /**
     * Inserts or updates a note after validating editor content.
     */
    fun saveNote() {
        val sanitizedTitle = NoteInputSanitizer.sanitizeTitle(editorState.value.currentTitle)
        val sanitizedBody = NoteInputSanitizer.sanitizeBody(editorState.value.currentBody)

        if (sanitizedTitle.isBlank() || sanitizedBody.isBlank()) {
            emitMessage("Add both a title and note details before saving.")
            return
        }

        val noteId = editorState.value.selectedNoteId
        val note = Note(
            id = noteId ?: 0,
            title = sanitizedTitle,
            body = sanitizedBody,
            updatedAt = System.currentTimeMillis(),
            isArchived = editorState.value.selectedNoteIsArchived,
        )

        viewModelScope.launch {
            if (noteId == null) {
                repository.addNote(note)
                emitMessage("Note saved.")
            } else {
                repository.updateNote(note)
                emitMessage("Note updated.")
            }
            clearEditor()
        }
    }

    /**
     * Deletes a selected note if it exists.
     *
     * @param noteId Identifier of the note to remove.
     */
    fun deleteNote(noteId: Int) {
        val note = screenState.value.notes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        viewModelScope.launch {
            repository.deleteNote(note)
            if (editorState.value.selectedNoteId == noteId) {
                clearEditor()
            }
            emitMessage("Note deleted.")
        }
    }

    /**
     * Moves a note into the archive collection.
     *
     * @param noteId Identifier of the note to archive.
     */
    fun archiveNote(noteId: Int) {
        val note = screenState.value.notes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isArchived = true,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            if (editorState.value.selectedNoteId == noteId) {
                clearEditor()
            }
            emitMessage("Note archived.")
        }
    }

    /**
     * Restores a note back to the active collection.
     *
     * @param noteId Identifier of the note to restore.
     */
    fun restoreNote(noteId: Int) {
        val note = screenState.value.notes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        viewModelScope.launch {
            repository.updateNote(
                note.copy(
                    isArchived = false,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            if (editorState.value.selectedNoteId == noteId) {
                clearEditor()
            }
            emitMessage("Note restored.")
        }
    }

    /**
     * Emits a message event without exposing the mutable flow.
     *
     * @param message User-facing message string.
     */
    private fun emitMessage(message: String) {
        viewModelScope.launch {
            messageEvents.emit(message)
        }
    }
}
