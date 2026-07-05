/*
 * Binds the NotesViewModel to the reusable QuickNotesScreen composable.
 * Connects to: NotesViewModel, QuickNotesScreen, and snackbar messages.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breakingthebot.quicknotes.viewmodel.NotesViewModel

/**
 * Root screen composable for creating, editing, and deleting notes.
 *
 * @param viewModel View model that owns screen state and note actions.
 */
@Composable
fun QuickNotesApp(viewModel: NotesViewModel) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    QuickNotesScreen(
        state = screenState,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        onTitleChange = viewModel::onTitleChanged,
        onBodyChange = viewModel::onBodyChanged,
        onTagsInputChange = viewModel::onTagsInputChanged,
        onSaveClick = viewModel::saveNote,
        onClearClick = viewModel::clearEditor,
        onNoteCollectionChanged = viewModel::onNoteCollectionChanged,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSelectedTagChanged = viewModel::onSelectedTagChanged,
        onSortOptionChanged = viewModel::onSortOptionChanged,
        onNoteClick = viewModel::selectNote,
        onArchiveClick = viewModel::archiveNote,
        onRestoreClick = viewModel::restoreNote,
        onDeleteClick = viewModel::deleteNote,
        onEmptyTrashClick = viewModel::emptyTrash,
        onPinClick = viewModel::togglePinNote,
        onIsChecklistChange = viewModel::onIsChecklistChanged,
        onChecklistItemToggle = viewModel::toggleChecklistItem,
        onNoteColorChange = viewModel::onNoteColorChanged,
    )
}
