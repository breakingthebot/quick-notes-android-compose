/*
 * Renders the main Compose notes experience and binds user events.
 * Connects to: NotesViewModel, NotesScreenState, and NoteListItem.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            TopAppBar(
                title = {
                    Text(
                        text = "Quick Notes",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                windowInsets = WindowInsets.statusBars,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            ScreenSummary(state = screenState)
            Spacer(modifier = Modifier.height(16.dp))
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
 * Renders a compact summary that explains the current list context.
 *
 * @param state Current UI state driving the summary text.
 */
@Composable
private fun ScreenSummary(state: NotesScreenState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (state.noteCollection == NoteCollection.ACTIVE) {
                    "Capture what matters"
                } else {
                    "Archived for later"
                },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summaryMessage(state = state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Browse notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Switch collections, search, or change list order.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
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
                    .defaultMinSize(minHeight = 56.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val editorTitle = if (state.selectedNoteId == null) "Create note" else "Edit note"
            Text(
                text = editorTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = if (state.selectedNoteId == null) {
                    "Draft a quick note with a clear title and a few details."
                } else {
                    "Update the selected note, then save the changes."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.currentTitle,
                onValueChange = onTitleChange,
                label = { Text(text = "Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.currentBody,
                onValueChange = onBodyChange,
                label = { Text(text = "Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 52.dp),
                ) {
                    Text(text = if (state.selectedNoteId == null) "Save note" else "Update note")
                }
                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 52.dp),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyNotesState(state = state)
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
 * Renders a richer empty state for either active or archived collections.
 *
 * @param state Current UI state.
 */
@Composable
private fun EmptyNotesState(state: NotesScreenState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = emptyStateTitle(state = state),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = emptyStateMessage(state = state),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Returns the empty-state title for the current screen context.
 *
 * @param state Current UI state.
 * @return Empty-state title.
 */
private fun emptyStateTitle(state: NotesScreenState): String {
    return if (state.searchQuery.isNotBlank()) {
        "No matching notes"
    } else if (state.noteCollection == NoteCollection.ARCHIVED) {
        "Nothing archived yet"
    } else {
        "Your note list is empty"
    }
}

/**
 * Returns the empty-state body copy for the current screen context.
 *
 * @param state Current UI state.
 * @return Empty-state description.
 */
private fun emptyStateMessage(state: NotesScreenState): String {
    return if (state.searchQuery.isNotBlank()) {
        "Try a different keyword or switch collections to broaden the results."
    } else if (state.noteCollection == NoteCollection.ARCHIVED) {
        "Archived notes will appear here after you move them out of the active list."
    } else {
        "Create a note above to start building a quick, searchable notebook."
    }
}

/**
 * Returns the screen summary copy shown above the editor.
 *
 * @param state Current UI state.
 * @return Summary sentence for the current collection and search state.
 */
private fun summaryMessage(state: NotesScreenState): String {
    return when {
        state.noteCollection == NoteCollection.ARCHIVED && state.searchQuery.isBlank() ->
            "Review saved notes that are out of the main workflow but still available."
        state.noteCollection == NoteCollection.ARCHIVED ->
            "Search your archived notes without mixing them into the active list."
        state.searchQuery.isNotBlank() ->
            "Search is filtering the active list while your editor stays ready for the next update."
        else ->
            "Keep important notes active, archive older ones, and find everything quickly."
    }
}
