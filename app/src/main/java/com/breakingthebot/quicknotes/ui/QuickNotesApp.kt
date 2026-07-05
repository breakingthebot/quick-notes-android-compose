/*
 * Binds the NotesViewModel to the reusable QuickNotesScreen composable.
 * Connects to: NotesViewModel, QuickNotesScreen, and snackbar messages.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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

    val context = LocalContext.current

    QuickNotesScreen(
        state = screenState,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        onTitleChange = viewModel::onTitleChanged,
        onBodyChange = viewModel::onBodyChanged,
        onTagsInputChange = viewModel::onTagsInputChanged,
        onSaveClick = { viewModel.saveNote(context) },
        onClearClick = viewModel::clearEditor,
        onNoteCollectionChanged = viewModel::onNoteCollectionChanged,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSelectedTagChanged = viewModel::onSelectedTagChanged,
        onSortOptionChanged = viewModel::onSortOptionChanged,
        onNoteClick = viewModel::selectNote,
        onArchiveClick = viewModel::archiveNote,
        onRestoreClick = viewModel::restoreNote,
        onDeleteClick = { noteId -> viewModel.deleteNote(noteId, context) },
        onEmptyTrashClick = { viewModel.emptyTrash(context) },
        onPinClick = viewModel::togglePinNote,
        onIsChecklistChange = viewModel::onIsChecklistChanged,
        onChecklistItemToggle = viewModel::toggleChecklistItem,
        onNoteColorChange = viewModel::onNoteColorChanged,
        onReminderTimeChange = viewModel::onReminderTimeChanged,
        onRenameTag = viewModel::renameTag,
        onDeleteTag = viewModel::deleteTag,
        onShareClick = { noteId ->
            val note = screenState.notes.firstOrNull { it.id == noteId }
            if (note != null) {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, note.title)
                    putExtra(android.content.Intent.EXTRA_TEXT, "${note.title}\n\n${note.body}")
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Note"))
            }
        },
        onDateFilterOptionChanged = viewModel::onDateFilterOptionChanged,
        onCustomDateRangeChanged = viewModel::onCustomDateRangeChanged,
    )
}
