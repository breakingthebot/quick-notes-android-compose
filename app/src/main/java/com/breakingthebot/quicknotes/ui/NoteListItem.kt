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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breakingthebot.quicknotes.model.Note
import com.breakingthebot.quicknotes.util.TimeFormatter

/**
 * Card for an individual note preview.
 *
 * @param note Note data to render.
 * @param onClick Callback for selecting the note for editing.
 * @param onDeleteClick Callback for deleting the note.
 */
@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                )
            }
            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            )
            OutlinedButton(onClick = onDeleteClick) {
                Text(text = "Delete")
            }
        }
    }
}
