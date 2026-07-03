/*
 * Renders the main Compose notes experience and binds user events.
 * Connects to: NotesViewModel, NotesScreenState, and NoteListItem.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breakingthebot.quicknotes.viewmodel.NotesViewModel

/**
 * Root screen composable for creating, editing, and deleting notes.
 *
 * @param viewModel View model that owns screen state and note actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesApp(viewModel: NotesViewModel) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = "Quick Notes") })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            NoteEditorCard(
                state = screenState,
                onTitleChange = viewModel::onTitleChanged,
                onBodyChange = viewModel::onBodyChanged,
                onSaveClick = viewModel::saveNote,
                onClearClick = viewModel::clearEditor,
            )
            Spacer(modifier = Modifier.height(16.dp))
            NoteListControls(
                state = screenState,
                onNoteCollectionChanged = viewModel::onNoteCollectionChanged,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSortOptionChanged = viewModel::onSortOptionChanged,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                NotesList(
                    state = screenState,
                    onNoteClick = viewModel::selectNote,
                    onArchiveClick = viewModel::archiveNote,
                    onRestoreClick = viewModel::restoreNote,
                    onDeleteClick = viewModel::deleteNote,
                )
            }
        }
    }
}

/**
 * Renders the note list search and sort controls.
 *
 * @param state Current UI state with search and sort values.
 * @param onSearchQueryChanged Callback for search text changes.
 * @param onSortOptionChanged Callback for sort mode changes.
 */
@Composable
private fun NoteListControls(
    state: NotesScreenState,
    onNoteCollectionChanged: (NoteCollection) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSortOptionChanged: (NoteSortOption) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Find notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NoteCollection.entries.forEach { noteCollection ->
                    FilterChip(
                        selected = state.noteCollection == noteCollection,
                        onClick = { onNoteCollectionChanged(noteCollection) },
                        label = { Text(text = noteCollection.label) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text(text = "Search title or details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note-search-field"),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NoteSortOption.entries.forEach { sortOption ->
                    FilterChip(
                        selected = state.sortOption == sortOption,
                        onClick = { onSortOptionChanged(sortOption) },
                        label = { Text(text = sortOption.label) },
                    )
                }
            }
        }
    }
}

/**
 * Renders the editable note form.
 *
 * @param state Current UI state.
 * @param onTitleChange Callback for title changes.
 * @param onBodyChange Callback for body changes.
 * @param onSaveClick Callback for saving the current note.
 * @param onClearClick Callback for clearing the editor.
 */
@Composable
private fun NoteEditorCard(
    state: NotesScreenState,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val editorTitle = if (state.selectedNoteId == null) "Create note" else "Edit note"
            Text(
                text = editorTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.currentTitle,
                onValueChange = onTitleChange,
                label = { Text(text = "Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.currentBody,
                onValueChange = onBodyChange,
                label = { Text(text = "Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = if (state.selectedNoteId == null) "Save note" else "Update note")
                }
                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "Clear")
                }
            }
        }
    }
}

/**
 * Renders either the empty state or the list of notes.
 *
 * @param state Current UI state with notes and editor values.
 * @param onNoteClick Callback for selecting a note.
 * @param onDeleteClick Callback for deleting a note.
 */
@Composable
private fun NotesList(
    state: NotesScreenState,
    onNoteClick: (Int) -> Unit,
    onArchiveClick: (Int) -> Unit,
    onRestoreClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
) {
    if (state.notes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyStateMessage(
                    searchQuery = state.searchQuery,
                    noteCollection = state.noteCollection,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = state.notes, key = { note -> note.id }) { note ->
            NoteListItem(
                note = note,
                isArchivedCollection = state.noteCollection == NoteCollection.ARCHIVED,
                onClick = { onNoteClick(note.id) },
                onArchiveClick = { onArchiveClick(note.id) },
                onRestoreClick = { onRestoreClick(note.id) },
                onDeleteClick = { onDeleteClick(note.id) },
            )
        }
    }
}

/**
 * Returns the empty-state copy for the current search context.
 *
 * @param searchQuery Current search text.
 * @return User-facing empty-state message.
 */
private fun emptyStateMessage(
    searchQuery: String,
    noteCollection: NoteCollection = NoteCollection.ACTIVE,
): String {
    return if (searchQuery.isBlank() && noteCollection == NoteCollection.ACTIVE) {
        "No active notes yet. Add one above to get started."
    } else if (searchQuery.isBlank() && noteCollection == NoteCollection.ARCHIVED) {
        "No archived notes yet."
    } else {
        "No notes match that search yet."
    }
}
