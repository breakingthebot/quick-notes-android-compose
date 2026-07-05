/*
 * Displays a single note row with edit and delete actions.
 * Connects to: QuickNotesApp, Note model, and TimeFormatter.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.model.NoteColor
import com.breakingthebot.quicknotes.ui.theme.NoteColorMapper
import com.breakingthebot.quicknotes.util.TimeFormatter
import com.breakingthebot.quicknotes.util.NoteChecklistParser
import com.breakingthebot.quicknotes.util.NoteMarkdownParser

/**
 * Card for an individual note preview.
 *
 * @param note Note data to render.
 * @param noteCollection Which collection this item belongs to.
 * @param onClick Callback for selecting the note for editing.
 * @param onArchiveClick Callback for archiving the note.
 * @param onRestoreClick Callback for restoring the note.
 * @param onDeleteClick Callback for deleting the note.
 * @param onPinClick Callback for pinning/unpinning the note.
 */
@Composable
fun NoteListItem(
    note: Note,
    noteCollection: NoteCollection,
    onClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPinClick: () -> Unit,
    onChecklistItemToggle: (Int) -> Unit,
    onShareClick: () -> Unit,
) {
    val backgroundColor = NoteColorMapper.getBackgroundColor(note.color)
    val contentColor = NoteColorMapper.getOnBackgroundColor(note.color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note-card-${note.title}")
            .semantics {
                contentDescription = "Note titled ${note.title}"
            }
            .let {
                if (noteCollection != NoteCollection.TRASH) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (note.isPinned) "📌 " + note.title else note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = TimeFormatter.formatUpdatedAt(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = contentColor.copy(alpha = 0.2f),
            )
            if (note.isChecklist) {
                val items = remember(note.body) { NoteChecklistParser.parse(note.body) }
                Column(
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { onChecklistItemToggle(index) },
                                modifier = Modifier.testTag("checklist-item-checkbox-${note.title}-$index"),
                                colors = androidx.compose.material3.CheckboxDefaults.colors(
                                    checkedColor = contentColor,
                                    checkmarkColor = backgroundColor,
                                    uncheckedColor = contentColor.copy(alpha = 0.6f)
                                )
                            )
                            Text(
                                text = NoteMarkdownParser.parse(item.text),
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (item.isChecked) contentColor.copy(alpha = 0.6f) else contentColor
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = NoteMarkdownParser.parse(note.body),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                    color = contentColor,
                )
            }
            val reminderText = remember(note.reminderTime) {
                if (note.reminderTime != null) {
                    val sdf = java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(note.reminderTime))
                } else {
                    null
                }
            }
            if (reminderText != null) {
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏰ Reminder: $reminderText",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.9f)
                    )
                }
            }
            if (note.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    note.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor,
                        )
                    }
                }
            }
            val buttonColors = ButtonDefaults.outlinedButtonColors(
                contentColor = contentColor
            )
            val buttonBorder = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (noteCollection == NoteCollection.TRASH) {
                    OutlinedButton(
                        onClick = onRestoreClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("restore-button-${note.title}"),
                        colors = buttonColors,
                        border = buttonBorder,
                    ) {
                        Text(text = "Restore")
                    }
                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("delete-button-${note.title}"),
                        colors = buttonColors,
                        border = buttonBorder,
                    ) {
                        Text(text = "Delete permanently")
                    }
                } else {
                    OutlinedButton(
                        onClick = onPinClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag(if (note.isPinned) "unpin-button-${note.title}" else "pin-button-${note.title}"),
                        colors = buttonColors,
                        border = buttonBorder,
                    ) {
                        Text(text = if (note.isPinned) "Unpin" else "Pin")
                    }
                    OutlinedButton(
                        onClick = if (noteCollection == NoteCollection.ARCHIVED) onRestoreClick else onArchiveClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag(
                                if (noteCollection == NoteCollection.ARCHIVED) {
                                    "restore-button-${note.title}"
                                } else {
                                    "archive-button-${note.title}"
                                },
                            ),
                        colors = buttonColors,
                        border = buttonBorder,
                    ) {
                        Text(text = if (noteCollection == NoteCollection.ARCHIVED) "Restore" else "Archive")
                    }
                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("delete-button-${note.title}"),
                        colors = buttonColors,
                        border = buttonBorder,
                    ) {
                        Text(text = "Delete")
                    }
                    OutlinedButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("share-button-${note.title}"),
                        colors = buttonColors,
                        border = buttonBorder,
                    ) {
                        Text(text = "Share")
                    }
                }
            }
        }
    }
}
