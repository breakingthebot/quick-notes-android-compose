/*
 * Applies the Material 3 theme used throughout the notes app.
 * Connects to: Color.kt and all Compose UI.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SlateBlue,
    secondary = Coral,
    background = Paper,
    surface = SoftBlue,
    surfaceVariant = Paper,
    onPrimary = Paper,
    onSecondary = Paper,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = SlateBlue,
)

/**
 * Wraps app content in the shared Material theme.
 *
 * @param content Composable subtree to theme.
 */
@Composable
fun QuickNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
