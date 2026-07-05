/*
 * Owns note screen state and coordinates UI actions with Room storage.
 * Connects to: NoteRepository, NotesScreenState, and Compose UI.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakingthebot.quicknotes.data.NoteRepository
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.model.Notebook
import com.breakingthebot.quicknotes.model.NoteColor
import com.breakingthebot.quicknotes.services.NotesChangeNotifier
import com.breakingthebot.quicknotes.ui.NoteCollection
import com.breakingthebot.quicknotes.ui.NoteSortOption
import com.breakingthebot.quicknotes.ui.DateFilterOption
import com.breakingthebot.quicknotes.ui.NotesScreenState
import com.breakingthebot.quicknotes.util.NoteInputSanitizer
import com.breakingthebot.quicknotes.util.NoteListFormatter
import com.breakingthebot.quicknotes.util.NoteTagFormatter
import com.breakingthebot.quicknotes.util.NoteChecklistParser
import com.breakingthebot.quicknotes.util.ChecklistItem
import com.breakingthebot.quicknotes.util.ReminderScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View model for note CRUD interactions.
 *
 * @property repository Repository used to persist note changes.
 * @property notesChangeNotifier Side effects that run after persisted note updates.
 */
class NotesViewModel(
    private val repository: NoteRepository,
    private val notesChangeNotifier: NotesChangeNotifier,
) : ViewModel() {
    private val editorState = MutableStateFlow(NotesScreenState())
    private val messageEvents = MutableSharedFlow<String>()

    /**
     * Public screen state derived from editor inputs and stored notes.
     */
    val screenState: StateFlow<NotesScreenState> = combine(
        repository.observeNotes(),
        repository.observeNotebooks(),
        editorState,
    ) { notes, notebooks, editor ->
        editor.copy(
            notes = NoteListFormatter.formatNotes(
                notes = notes,
                noteCollection = editor.noteCollection,
                searchQuery = editor.searchQuery,
                selectedTag = editor.selectedTag,
                sortOption = editor.sortOption,
                dateFilterOption = editor.dateFilterOption,
                customStartDate = editor.customStartDate,
                customEndDate = editor.customEndDate,
                selectedNotebookId = editor.selectedNotebookId,
            ),
            availableTags = NoteListFormatter.availableTags(
                notes = notes,
                noteCollection = editor.noteCollection,
            ),
            notebooks = notebooks,
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
     * Updates the editor tag field.
     *
     * @param tagsInput New comma-separated tag input.
     */
    fun onTagsInputChanged(tagsInput: String) {
        editorState.value = editorState.value.copy(currentTagsInput = tagsInput)
    }

    /**
     * Updates whether the note is formatted as a checklist.
     *
     * @param isChecklist True to enable checklist formatting.
     */
    fun onIsChecklistChanged(isChecklist: Boolean) {
        editorState.value = editorState.value.copy(currentIsChecklist = isChecklist)
    }

    /**
     * Updates the custom category color for the note.
     *
     * @param noteColor The selected note background color option.
     */
    fun onNoteColorChanged(noteColor: NoteColor) {
        editorState.value = editorState.value.copy(currentNoteColor = noteColor)
    }

    /**
     * Updates the scheduled reminder timestamp for the note.
     *
     * @param timeMs Reminder epoch timestamp, or null to clear.
     */
    fun onReminderTimeChanged(timeMs: Long?) {
        editorState.value = editorState.value.copy(selectedReminderTime = timeMs)
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
     * Updates the active tag filter.
     *
     * @param selectedTag New tag filter, or null to clear it.
     */
    fun onSelectedTagChanged(selectedTag: String?) {
        editorState.value = editorState.value.copy(selectedTag = selectedTag)
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
        editorState.value = editorState.value.copy(
            noteCollection = noteCollection,
            selectedTag = null,
        )
    }

    /**
     * Updates the date range filter option.
     *
     * @param option Selected DateFilterOption.
     */
    fun onDateFilterOptionChanged(option: DateFilterOption) {
        editorState.value = editorState.value.copy(
            dateFilterOption = option,
            customStartDate = if (option != DateFilterOption.CUSTOM) null else editorState.value.customStartDate,
            customEndDate = if (option != DateFilterOption.CUSTOM) null else editorState.value.customEndDate
        )
    }

    /**
     * Updates the custom date range values.
     *
     * @param start Start epoch millis, or null.
     * @param end End epoch millis, or null.
     */
    fun onCustomDateRangeChanged(start: Long?, end: Long?) {
        editorState.value = editorState.value.copy(
            customStartDate = start,
            customEndDate = end
        )
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
            currentBody = if (selectedNote.isChecklist) {
                NoteChecklistParser.parse(selectedNote.body).joinToString("\n") { it.text }
            } else {
                selectedNote.body
            },
            currentTagsInput = NoteTagFormatter.formatForEditor(selectedNote.tags),
            selectedNoteId = selectedNote.id,
            selectedNoteIsArchived = selectedNote.isArchived,
            selectedNoteIsDeleted = selectedNote.isDeleted,
            selectedNoteIsPinned = selectedNote.isPinned,
            currentIsChecklist = selectedNote.isChecklist,
            currentNoteColor = selectedNote.color,
            selectedReminderTime = selectedNote.reminderTime,
            currentNoteNotebookId = selectedNote.notebookId,
        )
    }

    /**
     * Clears the editor back to create mode.
     */
    fun clearEditor() {
        editorState.value = editorState.value.copy(
            currentTitle = "",
            currentBody = "",
            currentTagsInput = "",
            selectedNoteId = null,
            selectedNoteIsArchived = false,
            selectedNoteIsDeleted = false,
            selectedNoteIsPinned = false,
            currentIsChecklist = false,
            currentNoteColor = NoteColor.DEFAULT,
            selectedReminderTime = null,
            currentNoteNotebookId = null,
        )
    }

    /**
     * Inserts or updates a note after validating editor content.
     */
    fun saveNote(context: Context) {
        val sanitizedTitle = NoteInputSanitizer.sanitizeTitle(editorState.value.currentTitle)
        val sanitizedBody = NoteInputSanitizer.sanitizeBody(editorState.value.currentBody)
        val parsedTags = NoteTagFormatter.parseInput(editorState.value.currentTagsInput)

        if (sanitizedTitle.isBlank() || sanitizedBody.isBlank()) {
            emitMessage("Add both a title and note details before saving.")
            return
        }

        val isChecklist = editorState.value.currentIsChecklist
        var body = sanitizedBody
        if (isChecklist) {
            val lines = body.lines().map { it.trim() }.filter { it.isNotBlank() }
            val existingItems = editorState.value.selectedNoteId?.let { id ->
                screenState.value.notes.firstOrNull { it.id == id }?.let { NoteChecklistParser.parse(it.body) }
            } ?: emptyList()
            val checkedTexts = existingItems.filter { it.isChecked }.map { it.text }.toSet()
            val checklistItems = lines.map { line ->
                ChecklistItem(line, line in checkedTexts)
            }
            body = NoteChecklistParser.toBodyString(checklistItems)
        }

        val noteId = editorState.value.selectedNoteId
        val reminderTime = editorState.value.selectedReminderTime
        val note = Note(
            id = noteId ?: 0,
            title = sanitizedTitle,
            body = body,
            updatedAt = System.currentTimeMillis(),
            isArchived = editorState.value.selectedNoteIsArchived,
            tags = parsedTags,
            isDeleted = editorState.value.selectedNoteIsDeleted,
            isPinned = editorState.value.selectedNoteIsPinned,
            isChecklist = isChecklist,
            color = editorState.value.currentNoteColor,
            reminderTime = reminderTime,
            notebookId = editorState.value.currentNoteNotebookId,
        )

        viewModelScope.launch {
            if (note.id == 0) {
                val insertedId = repository.addNote(note).toInt()
                if (reminderTime != null && reminderTime > System.currentTimeMillis()) {
                    ReminderScheduler.scheduleReminder(context, insertedId, note.title, note.body, reminderTime)
                }
                emitMessage("Note saved.")
            } else {
                repository.updateNote(note)
                if (reminderTime != null) {
                    if (reminderTime > System.currentTimeMillis()) {
                        ReminderScheduler.scheduleReminder(context, note.id, note.title, note.body, reminderTime)
                    } else {
                        ReminderScheduler.cancelReminder(context, note.id)
                    }
                } else {
                    ReminderScheduler.cancelReminder(context, note.id)
                }
                emitMessage("Note updated.")
            }
            clearEditor()
            notesChangeNotifier.onNotesChanged()
        }
    }

    /**
     * Deletes a selected note. If the note is already in the Trash collection,
     * it is permanently deleted. Otherwise, it is soft-deleted (moved to Trash).
     *
     * @param noteId Identifier of the note to remove.
     */
    fun deleteNote(noteId: Int, context: Context) {
        val note = screenState.value.notes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        viewModelScope.launch {
            if (note.isDeleted) {
                repository.deleteNote(note)
                ReminderScheduler.cancelReminder(context, note.id)
                emitMessage("Note permanently deleted.")
            } else {
                repository.updateNote(
                    note.copy(
                        isDeleted = true,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
                ReminderScheduler.cancelReminder(context, note.id)
                emitMessage("Note moved to trash.")
            }
            notesChangeNotifier.onNotesChanged()
            if (editorState.value.selectedNoteId == noteId) {
                clearEditor()
            }
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
            notesChangeNotifier.onNotesChanged()
            if (editorState.value.selectedNoteId == noteId) {
                clearEditor()
            }
            emitMessage("Note archived.")
        }
    }

    /**
     * Restores a note back to active collection. If the note is in Trash,
     * it restores its deleted state. Otherwise, it restores its archive state.
     *
     * @param noteId Identifier of the note to restore.
     */
    fun restoreNote(noteId: Int) {
        val note = screenState.value.notes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        viewModelScope.launch {
            if (note.isDeleted) {
                repository.updateNote(
                    note.copy(
                        isDeleted = false,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            } else {
                repository.updateNote(
                    note.copy(
                        isArchived = false,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
            notesChangeNotifier.onNotesChanged()
            if (editorState.value.selectedNoteId == noteId) {
                clearEditor()
            }
            emitMessage("Note restored.")
        }
    }

    /**
     * Toggles the checked state of a checklist item.
     *
     * @param noteId Identifier of the note.
     * @param itemIndex Index of the checklist item.
     */
    fun toggleChecklistItem(noteId: Int, itemIndex: Int) {
        val note = screenState.value.notes.firstOrNull { it.id == noteId } ?: return
        val items = NoteChecklistParser.parse(note.body).toMutableList()
        if (itemIndex in items.indices) {
            val item = items[itemIndex]
            items[itemIndex] = item.copy(isChecked = !item.isChecked)
            viewModelScope.launch {
                repository.updateNote(
                    note.copy(
                        body = NoteChecklistParser.toBodyString(items),
                        updatedAt = System.currentTimeMillis()
                    )
                )
                notesChangeNotifier.onNotesChanged()
            }
        }
    }

    /**
     * Toggles the pinned state of a note.
     *
     * @param noteId Identifier of the note to pin or unpin.
     */
    fun togglePinNote(noteId: Int) {
        val note = screenState.value.notes.firstOrNull { existingNote -> existingNote.id == noteId } ?: return
        viewModelScope.launch {
            val updatedNote = note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis(),
            )
            repository.updateNote(updatedNote)
            notesChangeNotifier.onNotesChanged()
            if (editorState.value.selectedNoteId == noteId) {
                editorState.value = editorState.value.copy(selectedNoteIsPinned = updatedNote.isPinned)
            }
            emitMessage(if (updatedNote.isPinned) "Note pinned." else "Note unpinned.")
        }
    }

    /**
     * Permanently deletes all notes currently marked as deleted (in Trash).
     */
    fun emptyTrash(context: Context) {
        val deletedNotes = screenState.value.notes.filter { it.isDeleted }
        viewModelScope.launch {
            deletedNotes.forEach { note ->
                ReminderScheduler.cancelReminder(context, note.id)
            }
            repository.emptyTrash()
            notesChangeNotifier.onNotesChanged()
            clearEditor()
            emitMessage("Trash emptied.")
        }
    }

    /**
     * Renames a tag globally across all notes.
     *
     * @param oldTag The existing tag name.
     * @param newTag The replacement tag name.
     */
    fun renameTag(oldTag: String, newTag: String) {
        val sanitizedNewTag = newTag.trim().lowercase().filter { it.isLetterOrDigit() }
        if (sanitizedNewTag.isBlank() || oldTag == sanitizedNewTag) return

        viewModelScope.launch {
            val allNotes = repository.observeNotes().first()
            allNotes.forEach { note ->
                if (oldTag in note.tags) {
                    val updatedTags = note.tags.map { if (it == oldTag) sanitizedNewTag else it }.distinct()
                    repository.updateNote(note.copy(tags = updatedTags, updatedAt = System.currentTimeMillis()))
                }
            }
            notesChangeNotifier.onNotesChanged()
            emitMessage("Tag renamed globally.")
        }
    }

    /**
     * Removes a tag globally from all notes.
     *
     * @param tag The tag name to delete.
     */
    fun deleteTag(tag: String) {
        viewModelScope.launch {
            val allNotes = repository.observeNotes().first()
            allNotes.forEach { note ->
                if (tag in note.tags) {
                    val updatedTags = note.tags.filterNot { it == tag }
                    repository.updateNote(note.copy(tags = updatedTags, updatedAt = System.currentTimeMillis()))
                }
            }
            notesChangeNotifier.onNotesChanged()
            emitMessage("Tag deleted globally.")
        }
    }

    /**
     * Creates a new folder / notebook.
     *
     * @param name The notebook name.
     */
    fun createNotebook(name: String) {
        val sanitized = name.trim()
        if (sanitized.isBlank()) {
            emitMessage("Folder name cannot be blank.")
            return
        }
        viewModelScope.launch {
            val notebook = Notebook(name = sanitized)
            repository.addNotebook(notebook)
            emitMessage("Folder '$sanitized' created.")
        }
    }

    /**
     * Renames a folder / notebook.
     *
     * @param notebookId The notebook identifier.
     * @param newName The new name.
     */
    fun renameNotebook(notebookId: Int, newName: String) {
        val sanitized = newName.trim()
        if (sanitized.isBlank()) {
            emitMessage("Folder name cannot be blank.")
            return
        }
        viewModelScope.launch {
            val currentNotebooks = screenState.value.notebooks
            val existing = currentNotebooks.firstOrNull { it.id == notebookId }
            if (existing != null) {
                repository.updateNotebook(existing.copy(name = sanitized))
                emitMessage("Folder renamed to '$sanitized'.")
            }
        }
    }

    /**
     * Deletes a folder / notebook.
     *
     * @param notebookId The notebook identifier.
     */
    fun deleteNotebook(notebookId: Int) {
        viewModelScope.launch {
            val currentNotebooks = screenState.value.notebooks
            val existing = currentNotebooks.firstOrNull { it.id == notebookId }
            if (existing != null) {
                repository.deleteNotebook(existing)
                // If the currently selected notebook was deleted, reset filter to All Folders
                if (editorState.value.selectedNotebookId == notebookId) {
                    editorState.value = editorState.value.copy(selectedNotebookId = null)
                }
                // If the currently edited note has this notebook, reset it to null
                if (editorState.value.currentNoteNotebookId == notebookId) {
                    editorState.value = editorState.value.copy(currentNoteNotebookId = null)
                }
                emitMessage("Folder '${existing.name}' deleted. Notes were uncategorized.")
            }
        }
    }

    /**
     * Updates the active folder / notebook list filter.
     *
     * @param notebookId The notebook ID to view, or null to view all notes.
     */
    fun onNotebookSelected(notebookId: Int?) {
        editorState.value = editorState.value.copy(selectedNotebookId = notebookId)
    }

    /**
     * Updates the folder assigned to the note currently being edited.
     *
     * @param notebookId The notebook ID to assign, or null for uncategorized notes.
     */
    fun onCurrentNoteNotebookChanged(notebookId: Int?) {
        editorState.value = editorState.value.copy(currentNoteNotebookId = notebookId)
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
