/*
 * Resolves NoteColor categories into Compose Color tokens for light/dark themes.
 * Connects to: NoteColor model and NoteListItem.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.breakingthebot.quicknotes.model.NoteColor

/**
 * Maps NoteColor configurations to readable styling tokens.
 */
object NoteColorMapper {
    /**
     * Resolves the background color for a note card based on the active system theme.
     *
     * @param noteColor The selected note color choice.
     * @return Compose Color token.
     */
    @Composable
    fun getBackgroundColor(noteColor: NoteColor): Color {
        val isDark = isSystemInDarkTheme()
        return when (noteColor) {
            NoteColor.DEFAULT -> MaterialTheme.colorScheme.surface
            NoteColor.MINT -> if (isDark) Color(0xFF1B4332) else Color(0xFFE8F5E9)
            NoteColor.PEACH -> if (isDark) Color(0xFF4E2A14) else Color(0xFFFFEBD5)
            NoteColor.LAVENDER -> if (isDark) Color(0xFF2C1A3D) else Color(0xFFF3E8FF)
            NoteColor.BLUE -> if (isDark) Color(0xFF122C46) else Color(0xFFE3F2FD)
        }
    }

    /**
     * Resolves the text/icon foreground color for a note card based on the active system theme.
     *
     * @param noteColor The selected note color choice.
     * @return Compose Color token.
     */
    @Composable
    fun getOnBackgroundColor(noteColor: NoteColor): Color {
        val isDark = isSystemInDarkTheme()
        return when (noteColor) {
            NoteColor.DEFAULT -> MaterialTheme.colorScheme.onSurface
            NoteColor.MINT -> if (isDark) Color(0xFFD8F3DC) else Color(0xFF1B4332)
            NoteColor.PEACH -> if (isDark) Color(0xFFFFD2B2) else Color(0xFF4E2A14)
            NoteColor.LAVENDER -> if (isDark) Color(0xFFE9D5FF) else Color(0xFF2C1A3D)
            NoteColor.BLUE -> if (isDark) Color(0xFFB3E5FC) else Color(0xFF122C46)
        }
    }
}
