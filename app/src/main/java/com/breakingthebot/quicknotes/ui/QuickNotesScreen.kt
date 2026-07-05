/*
 * Renders the reusable notes screen from plain state and callbacks.
 * Connects to: NotesScreenState, NoteListItem, and QuickNotesApp.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import com.breakingthebot.quicknotes.model.NoteColor
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reusable screen composable driven entirely by state and callbacks.
 *
 * @param state Current note screen state.
 * @param snackbarHost Optional snackbar host slot.
 * @param onTitleChange Callback for title edits.
 * @param onBodyChange Callback for body edits.
 * @param onTagsInputChange Callback for tag field edits.
 * @param onSaveClick Callback for save action.
 * @param onClearClick Callback for clear action.
 * @param onNoteCollectionChanged Callback for active/archive filter changes.
 * @param onSearchQueryChanged Callback for search updates.
 * @param onSelectedTagChanged Callback for tag filter changes.
 * @param onSortOptionChanged Callback for sort changes.
 * @param onNoteClick Callback for selecting a note.
 * @param onArchiveClick Callback for archiving a note.
 * @param onRestoreClick Callback for restoring a note.
 * @param onDeleteClick Callback for deleting a note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(
    state: NotesScreenState,
    snackbarHost: @Composable (() -> Unit)? = null,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onTagsInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
    onNoteCollectionChanged: (NoteCollection) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSelectedTagChanged: (String?) -> Unit,
    onSortOptionChanged: (NoteSortOption) -> Unit,
    onNoteClick: (Int) -> Unit,
    onArchiveClick: (Int) -> Unit,
    onRestoreClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onEmptyTrashClick: () -> Unit,
    onPinClick: (Int) -> Unit,
    onIsChecklistChange: (Boolean) -> Unit,
    onChecklistItemToggle: (Int, Int) -> Unit,
    onNoteColorChange: (NoteColor) -> Unit,
    onReminderTimeChange: (Long?) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onShareClick: (Int) -> Unit,
    onDateFilterOptionChanged: (DateFilterOption) -> Unit,
    onCustomDateRangeChanged: (Long?, Long?) -> Unit,
    onCreateNotebook: (String) -> Unit,
    onRenameNotebook: (Int, String) -> Unit,
    onDeleteNotebook: (Int) -> Unit,
    onNotebookSelected: (Int?) -> Unit,
    onCurrentNoteNotebookChanged: (Int?) -> Unit,
    onCurrentNoteImageChanged: (String?) -> Unit,
) {
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
            snackbarHost?.invoke()
        },
    ) { innerPadding ->
        LazyColumn(
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
                .imePadding()
                .testTag("notes-list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ScreenSummary(state = state)
            }
            item {
                NoteEditorCard(
                    state = state,
                    onTitleChange = onTitleChange,
                    onBodyChange = onBodyChange,
                    onTagsInputChange = onTagsInputChange,
                    onSaveClick = onSaveClick,
                    onClearClick = onClearClick,
                    onIsChecklistChange = onIsChecklistChange,
                    onNoteColorChange = onNoteColorChange,
                    onReminderTimeChange = onReminderTimeChange,
                    onCurrentNoteNotebookChanged = onCurrentNoteNotebookChanged,
                    onCurrentNoteImageChanged = onCurrentNoteImageChanged,
                )
            }
            item {
                BrowseNotesCard(
                    state = state,
                    onNoteCollectionChanged = onNoteCollectionChanged,
                    onSearchQueryChanged = onSearchQueryChanged,
                )
            }
            item {
                FoldersFilterCard(
                    state = state,
                    onNotebookSelected = onNotebookSelected,
                    onCreateNotebook = onCreateNotebook,
                    onRenameNotebook = onRenameNotebook,
                    onDeleteNotebook = onDeleteNotebook,
                )
            }
            item {
                TagsFilterCard(
                    state = state,
                    onSelectedTagChanged = onSelectedTagChanged,
                    onRenameTag = onRenameTag,
                    onDeleteTag = onDeleteTag,
                )
            }
            item {
                FiltersAndSortCard(
                    state = state,
                    onSortOptionChanged = onSortOptionChanged,
                    onEmptyTrashClick = onEmptyTrashClick,
                    onDateFilterOptionChanged = onDateFilterOptionChanged,
                    onCustomDateRangeChanged = onCustomDateRangeChanged,
                )
            }
            NotesListContent(
                state = state,
                onNoteClick = onNoteClick,
                onArchiveClick = onArchiveClick,
                onRestoreClick = onRestoreClick,
                onDeleteClick = onDeleteClick,
                onPinClick = onPinClick,
                onChecklistItemToggle = onChecklistItemToggle,
                onShareClick = onShareClick,
            )
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseNotesCard(
    state: NotesScreenState,
    onNoteCollectionChanged: (NoteCollection) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("browse-notes-card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        modifier = Modifier.testTag("collection-chip-${noteCollection.name.lowercase()}"),
                        label = { Text(text = noteCollection.label) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text(text = "Search title, details, or tags") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .testTag("note-search-field"),
                singleLine = true,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoldersFilterCard(
    state: NotesScreenState,
    onNotebookSelected: (Int?) -> Unit,
    onCreateNotebook: (String) -> Unit,
    onRenameNotebook: (Int, String) -> Unit,
    onDeleteNotebook: (Int) -> Unit,
) {
    var showFolderManager by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("folders-filter-card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Folders",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                androidx.compose.material3.TextButton(
                    onClick = { showFolderManager = true },
                    modifier = Modifier.testTag("manage-folders-button")
                ) {
                    Text(text = "Manage folders", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("folders-scroll-row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.selectedNotebookId == null,
                    onClick = { onNotebookSelected(null) },
                    modifier = Modifier.testTag("folder-filter-all"),
                    label = { Text(text = "📁 All Folders") },
                )
                state.notebooks.forEach { notebook ->
                    FilterChip(
                        selected = state.selectedNotebookId == notebook.id,
                        onClick = { onNotebookSelected(notebook.id) },
                        modifier = Modifier.testTag("folder-filter-${notebook.name}"),
                        label = { Text(text = "📁 ${notebook.name}") },
                    )
                }
            }

            if (showFolderManager) {
                FolderManagerDialog(
                    notebooks = state.notebooks,
                    onDismiss = { showFolderManager = false },
                    onCreate = onCreateNotebook,
                    onRename = onRenameNotebook,
                    onDelete = onDeleteNotebook
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsFilterCard(
    state: NotesScreenState,
    onSelectedTagChanged: (String?) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
) {
    var showTagManager by remember { mutableStateOf(false) }

    if (state.availableTags.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().testTag("tags-filter-card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter by tag",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { showTagManager = true },
                        modifier = Modifier.testTag("manage-tags-button")
                    ) {
                        Text(text = "Manage tags", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.selectedTag == null,
                        onClick = { onSelectedTagChanged(null) },
                        modifier = Modifier.testTag("tag-filter-all"),
                        label = { Text(text = "All tags") },
                    )
                    state.availableTags.forEach { tag ->
                        FilterChip(
                            selected = state.selectedTag == tag,
                            onClick = { onSelectedTagChanged(tag) },
                            modifier = Modifier.testTag("tag-filter-$tag"),
                            label = { Text(text = "#$tag") },
                        )
                    }
                }

                if (showTagManager) {
                    TagManagerDialog(
                        availableTags = state.availableTags,
                        onDismiss = { showTagManager = false },
                        onRename = onRenameTag,
                        onDelete = onDeleteTag
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersAndSortCard(
    state: NotesScreenState,
    onSortOptionChanged: (NoteSortOption) -> Unit,
    onEmptyTrashClick: () -> Unit,
    onDateFilterOptionChanged: (DateFilterOption) -> Unit,
    onCustomDateRangeChanged: (Long?, Long?) -> Unit,
) {
    var showDatePickerRange by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("filters-and-sort-card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dateRangeLabel = remember(state.dateFilterOption, state.customStartDate, state.customEndDate) {
                if (state.dateFilterOption == DateFilterOption.CUSTOM && state.customStartDate != null && state.customEndDate != null) {
                    val sdf = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.getDefault())
                    " (${sdf.format(java.util.Date(state.customStartDate))} - ${sdf.format(java.util.Date(state.customEndDate))})"
                } else {
                    ""
                }
            }

            Text(
                text = "Filter by date$dateRangeLabel",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateFilterOption.entries.forEach { option ->
                    FilterChip(
                        selected = state.dateFilterOption == option,
                        onClick = {
                            if (option == DateFilterOption.CUSTOM) {
                                showDatePickerRange = true
                            } else {
                                onDateFilterOptionChanged(option)
                            }
                        },
                        modifier = Modifier.testTag("date-filter-${option.name.lowercase()}"),
                        label = { Text(text = option.label) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (showDatePickerRange) {
                val dateRangePickerState = rememberDateRangePickerState()
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showDatePickerRange = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("date-range-dialog"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Select Date Range",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.DateRangePicker(
                                state = dateRangePickerState,
                                modifier = Modifier.weight(1f, fill = false).testTag("date-range-picker")
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.TextButton(
                                    onClick = { showDatePickerRange = false },
                                    modifier = Modifier.testTag("date-range-cancel")
                                ) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val start = dateRangePickerState.selectedStartDateMillis
                                        val end = dateRangePickerState.selectedEndDateMillis
                                        if (start != null && end != null) {
                                            onCustomDateRangeChanged(start, end)
                                            onDateFilterOptionChanged(DateFilterOption.CUSTOM)
                                        }
                                        showDatePickerRange = false
                                    },
                                    modifier = Modifier.testTag("date-range-save")
                                ) {
                                    Text("OK")
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NoteSortOption.entries.forEach { sortOption ->
                    FilterChip(
                        selected = state.sortOption == sortOption,
                        onClick = { onSortOptionChanged(sortOption) },
                        modifier = Modifier.testTag("sort-chip-${sortOption.name.lowercase()}"),
                        label = { Text(text = sortOption.label) },
                    )
                }
            }
            if (state.noteCollection == NoteCollection.TRASH && state.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEmptyTrashClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("empty-trash-button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(text = "Empty trash")
                }
            }
        }
    }
}

@Composable
private fun NoteEditorCard(
    state: NotesScreenState,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onTagsInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
    onIsChecklistChange: (Boolean) -> Unit,
    onNoteColorChange: (NoteColor) -> Unit,
    onReminderTimeChange: (Long?) -> Unit,
    onCurrentNoteNotebookChanged: (Int?) -> Unit,
    onCurrentNoteImageChanged: (String?) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let { uri ->
                    onCurrentNoteImageChanged(uri.toString())
                }
            }
        }
    )

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                val privateUri = com.breakingthebot.quicknotes.util.ImageHelper.copyUriToPrivateStorage(context, uri)
                if (privateUri != null) {
                    onCurrentNoteImageChanged(privateUri.toString())
                }
            }
        }
    )

    var attachedBitmap by remember(state.currentNoteImageUri) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(state.currentNoteImageUri) {
        val uriStr = state.currentNoteImageUri
        if (uriStr != null) {
            val loaded = com.breakingthebot.quicknotes.util.ImageHelper.decodeBitmapFromUri(context, uriStr)
            if (loaded != null) {
                attachedBitmap = loaded.asImageBitmap()
            }
        } else {
            attachedBitmap = null
        }
    }

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

            attachedBitmap?.let { bitmap ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("editor-image-preview-container")
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = "Attached image notes preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    androidx.compose.material3.IconButton(
                        onClick = { onCurrentNoteImageChanged(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.CircleShape)
                            .size(36.dp)
                            .testTag("remove-image-button")
                    ) {
                        Text(text = "❌", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = state.currentTitle,
                onValueChange = onTitleChange,
                label = { Text(text = "Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .testTag("title-input"),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Format as checklist",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = state.currentIsChecklist,
                    onCheckedChange = onIsChecklistChange,
                    modifier = Modifier.testTag("checklist-mode-switch"),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Attach Image Options
            Text(
                text = "Attach Image",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val tempUri = com.breakingthebot.quicknotes.util.ImageHelper.createTempImageUri(context)
                        tempCameraUri = tempUri
                        cameraLauncher.launch(tempUri)
                    },
                    modifier = Modifier.weight(1f).testTag("take-photo-button"),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = "📷 Take Photo")
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f).testTag("choose-gallery-button"),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = "🖼️ From Gallery")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.currentBody,
                onValueChange = onBodyChange,
                label = { Text(text = "Details") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .testTag("body-input"),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Note color",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoteColor.entries.forEach { noteColor ->
                    val colorValue = when (noteColor) {
                        NoteColor.DEFAULT -> MaterialTheme.colorScheme.surface
                        NoteColor.MINT -> Color(0xFFE8F5E9)
                        NoteColor.PEACH -> Color(0xFFFFEBD5)
                        NoteColor.LAVENDER -> Color(0xFFF3E8FF)
                        NoteColor.BLUE -> Color(0xFFE3F2FD)
                    }
                    val isSelected = state.currentNoteColor == noteColor
                    FilterChip(
                        selected = isSelected,
                        onClick = { onNoteColorChange(noteColor) },
                        modifier = Modifier.testTag("color-choice-${noteColor.name.lowercase()}"),
                        label = { Text(text = noteColor.label) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = colorValue,
                            selectedContainerColor = colorValue,
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val context = LocalContext.current
            val currentReminder = state.selectedReminderTime
            val calendar = remember(currentReminder) {
                java.util.Calendar.getInstance().apply {
                    if (currentReminder != null) {
                        timeInMillis = currentReminder
                    }
                }
            }
            val onPickDateTime = {
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(java.util.Calendar.YEAR, year)
                        calendar.set(java.util.Calendar.MONTH, month)
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)

                        android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                                calendar.set(java.util.Calendar.MINUTE, minute)
                                calendar.set(java.util.Calendar.SECOND, 0)
                                calendar.set(java.util.Calendar.MILLISECOND, 0)
                                onReminderTimeChange(calendar.timeInMillis)
                            },
                            calendar.get(java.util.Calendar.HOUR_OF_DAY),
                            calendar.get(java.util.Calendar.MINUTE),
                            false
                        ).show()
                    },
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                ).show()
            }
            val reminderText = remember(currentReminder) {
                if (currentReminder != null) {
                    val sdf = java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(currentReminder))
                } else {
                    null
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (reminderText != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "⏰ Reminder: $reminderText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("reminder-display-text")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.IconButton(
                            onClick = { onReminderTimeChange(null) },
                            modifier = Modifier.size(24.dp).testTag("clear-reminder-button")
                        ) {
                            Text(
                                text = "✕",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onPickDateTime,
                        modifier = Modifier.testTag("set-reminder-button")
                    ) {
                        Text(text = "⏰ Set Reminder")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Assign to folder / notebook
            Text(
                text = "Assign to folder",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("editor-folder-row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.currentNoteNotebookId == null,
                    onClick = { onCurrentNoteNotebookChanged(null) },
                    modifier = Modifier.testTag("folder-option-none"),
                    label = { Text(text = "None") },
                )
                state.notebooks.forEach { notebook ->
                    FilterChip(
                        selected = state.currentNoteNotebookId == notebook.id,
                        onClick = { onCurrentNoteNotebookChanged(notebook.id) },
                        modifier = Modifier.testTag("folder-option-${notebook.name}"),
                        label = { Text(text = "📁 ${notebook.name}") },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.currentTagsInput,
                onValueChange = onTagsInputChange,
                label = { Text(text = "Tags") },
                supportingText = { Text(text = "Use commas to separate tags, for example: work, ideas") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .testTag("tags-input"),
                singleLine = true,
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
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("save-note-button"),
                ) {
                    Text(text = if (state.selectedNoteId == null) "Save note" else "Update note")
                }
                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("clear-note-button"),
                ) {
                    Text(text = "Clear")
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.NotesListContent(
    state: NotesScreenState,
    onNoteClick: (Int) -> Unit,
    onArchiveClick: (Int) -> Unit,
    onRestoreClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onPinClick: (Int) -> Unit,
    onChecklistItemToggle: (Int, Int) -> Unit,
    onShareClick: (Int) -> Unit,
) {
    if (state.notes.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyNotesState(state = state)
            }
        }
        return
    }

    items(state.notes, key = { it.id }) { note ->
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onDeleteClick(note.id)
                        true
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        if (note.isDeleted || note.isArchived) {
                            onRestoreClick(note.id)
                        } else {
                            onArchiveClick(note.id)
                        }
                        true
                    }
                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = { SwipeDismissBackground(dismissState = dismissState) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("swipe-dismiss-box-${note.id}")
        ) {
            NoteListItem(
                note = note,
                noteCollection = state.noteCollection,
                onClick = { onNoteClick(note.id) },
                onArchiveClick = { onArchiveClick(note.id) },
                onRestoreClick = { onRestoreClick(note.id) },
                onDeleteClick = { onDeleteClick(note.id) },
                onPinClick = { onPinClick(note.id) },
                onChecklistItemToggle = { itemIndex -> onChecklistItemToggle(note.id, itemIndex) },
                onShareClick = { onShareClick(note.id) },
                searchQuery = state.searchQuery,
                notebookName = state.notebooks.firstOrNull { it.id == note.notebookId }?.name,
            )
        }
    }
}

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

private fun emptyStateTitle(state: NotesScreenState): String {
    return if (state.searchQuery.isNotBlank()) {
        "No matching notes"
    } else if (state.noteCollection == NoteCollection.ARCHIVED) {
        "Nothing archived yet"
    } else {
        "Your note list is empty"
    }
}

private fun emptyStateMessage(state: NotesScreenState): String {
    return if (state.searchQuery.isNotBlank()) {
        "Try a different keyword or switch collections to broaden the results."
    } else if (state.noteCollection == NoteCollection.ARCHIVED) {
        "Archived notes will appear here after you move them out of the active list."
    } else {
        "Create a note above to start building a quick, searchable notebook."
    }
}

private fun summaryMessage(state: NotesScreenState): String {
    return when {
        state.noteCollection == NoteCollection.ARCHIVED && state.searchQuery.isBlank() ->
            "Review saved notes that are out of the main workflow but still available."
        state.selectedTag != null ->
            "Filtering the ${state.noteCollection.label.lowercase()} list by #${state.selectedTag}."
        state.noteCollection == NoteCollection.ARCHIVED ->
            "Search your archived notes without mixing them into the active list."
        state.searchQuery.isNotBlank() ->
            "Search is filtering the active list while your editor stays ready for the next update."
        else ->
            "Keep important notes active, archive older ones, and find everything quickly."
    }
}

@Composable
private fun TagManagerDialog(
    availableTags: List<String>,
    onDismiss: () -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var tagToRename by remember { mutableStateOf<String?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var tagToDelete by remember { mutableStateOf<String?>(null) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("tag-manager-dialog"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Tags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Text(text = "✕", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(availableTags.size) { index ->
                        val tag = availableTags[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("tag-manager-row-$tag"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        tagToRename = tag
                                        renameInput = tag
                                    },
                                    modifier = Modifier.size(36.dp).testTag("rename-tag-btn-$tag")
                                ) {
                                    Text(text = "✏️", style = MaterialTheme.typography.bodyMedium)
                                }
                                androidx.compose.material3.IconButton(
                                    onClick = { tagToDelete = tag },
                                    modifier = Modifier.size(36.dp).testTag("delete-tag-btn-$tag")
                                ) {
                                    Text(text = "🗑️", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).testTag("tag-manager-close")
                ) {
                    Text(text = "Close")
                }
            }
        }
    }

    // Subdialog: Rename input
    if (tagToRename != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { tagToRename = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("rename-dialog"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rename Tag",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("New Tag Name") },
                        modifier = Modifier.fillMaxWidth().testTag("rename-tag-input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { tagToRename = null },
                            modifier = Modifier.testTag("rename-cancel-btn")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val old = tagToRename
                                if (old != null && renameInput.isNotBlank()) {
                                    onRename(old, renameInput)
                                }
                                tagToRename = null
                            },
                            modifier = Modifier.testTag("rename-save-btn")
                        ) {
                            Text("Rename")
                        }
                    }
                }
            }
        }
    }

    // Subdialog: Delete confirmation
    if (tagToDelete != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { tagToDelete = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("delete-tag-confirm-dialog"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Delete Tag",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Are you sure you want to delete tag #$tagToDelete globally? It will be removed from all notes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { tagToDelete = null },
                            modifier = Modifier.testTag("delete-tag-cancel-btn")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val tag = tagToDelete
                                if (tag != null) {
                                    onDelete(tag)
                                }
                                tagToDelete = null
                            },
                            modifier = Modifier.testTag("delete-tag-save-btn")
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeDismissBackground(dismissState: androidx.compose.material3.SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val color = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            else -> Alignment.CenterEnd
        }
    ) {
        if (direction == SwipeToDismissBoxValue.StartToEnd) {
            Text(
                text = "🗑️ Trash",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (direction == SwipeToDismissBoxValue.EndToStart) {
            Text(
                text = "📁 Move",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FolderManagerDialog(
    notebooks: List<com.breakingthebot.quicknotes.model.Notebook>,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (Int, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var folderToRename by remember { mutableStateOf<com.breakingthebot.quicknotes.model.Notebook?>(null) }
    var folderToDelete by remember { mutableStateOf<com.breakingthebot.quicknotes.model.Notebook?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var createInput by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("folder-manager-dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Manage Folders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Create Folder Form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = createInput,
                        onValueChange = { createInput = it },
                        label = { Text("New Folder Name") },
                        modifier = Modifier.weight(1f).testTag("create-folder-input"),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (createInput.isNotBlank()) {
                                onCreate(createInput)
                                createInput = ""
                            }
                        },
                        modifier = Modifier.testTag("create-folder-btn")
                    ) {
                        Text("Create")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable list of existing folders
                Text(
                    text = "Existing Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .testTag("folder-manager-list")
                ) {
                    items(notebooks.size) { index ->
                        val notebook = notebooks[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📁 ${notebook.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        folderToRename = notebook
                                        renameInput = notebook.name
                                    },
                                    modifier = Modifier.size(36.dp).testTag("rename-folder-btn-${notebook.name}")
                                ) {
                                    Text(text = "✏️", style = MaterialTheme.typography.bodyMedium)
                                }
                                androidx.compose.material3.IconButton(
                                    onClick = { folderToDelete = notebook },
                                    modifier = Modifier.size(36.dp).testTag("delete-folder-btn-${notebook.name}")
                                ) {
                                    Text(text = "🗑️", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).testTag("folder-manager-close")
                ) {
                    Text(text = "Close")
                }
            }
        }
    }

    // Subdialog: Rename input
    if (folderToRename != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { folderToRename = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("rename-folder-dialog"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rename Folder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("New Folder Name") },
                        modifier = Modifier.fillMaxWidth().testTag("rename-folder-input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { folderToRename = null },
                            modifier = Modifier.testTag("rename-folder-cancel-btn")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val old = folderToRename
                                if (old != null && renameInput.isNotBlank()) {
                                    onRename(old.id, renameInput)
                                }
                                folderToRename = null
                            },
                            modifier = Modifier.testTag("rename-folder-save-btn")
                        ) {
                            Text("Rename")
                        }
                    }
                }
            }
        }
    }

    // Subdialog: Delete confirmation
    if (folderToDelete != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { folderToDelete = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("delete-folder-dialog"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Delete Folder?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to delete folder \"${folderToDelete?.name}\"? The notes in it will remain safe and become uncategorized.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { folderToDelete = null },
                            modifier = Modifier.testTag("delete-folder-cancel-btn")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val target = folderToDelete
                                if (target != null) {
                                    onDelete(target.id)
                                }
                                folderToDelete = null
                            },
                            modifier = Modifier.testTag("delete-folder-save-btn")
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
