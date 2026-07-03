/*
 * Displays a single note row with edit and delete actions.
 * Connects to: QuickNotesApp, Note model, and TimeFormatter.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.util.TimeFormatter

/**
 * Card for an individual note preview.
 *
 * @param note Note data to render.
 * @param isArchivedCollection Whether the card is being rendered in the archived list.
 * @param onClick Callback for selecting the note for editing.
 * @param onArchiveClick Callback for archiving the note.
 * @param onRestoreClick Callback for restoring the note.
 * @param onDeleteClick Callback for deleting the note.
 */
@Composable
fun NoteListItem(
    note: Note,
    isArchivedCollection: Boolean,
    onClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note-card-${note.title}")
            .semantics {
                contentDescription = "Note titled ${note.title}"
            }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = TimeFormatter.formatUpdatedAt(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
            )
            if (note.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    note.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = if (isArchivedCollection) onRestoreClick else onArchiveClick,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag(
                            if (isArchivedCollection) {
                                "restore-button-${note.title}"
                            } else {
                                "archive-button-${note.title}"
                            },
                        ),
                ) {
                    Text(text = if (isArchivedCollection) "Restore" else "Archive")
                }
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("delete-button-${note.title}"),
                ) {
                    Text(text = "Delete")
                }
            }
        }
    }
}
