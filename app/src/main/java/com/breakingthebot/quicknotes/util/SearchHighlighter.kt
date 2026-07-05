/*
 * Highlights occurrences of search queries in AnnotatedString objects.
 * Connects to: ui/NoteListItem.kt
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Helper to apply search match highlights onto AnnotatedString items.
 */
object SearchHighlighter {
    /**
     * Highlights occurrences of [query] in [annotated] using [highlightColor] and [textColor].
     *
     * @param annotated Original AnnotatedString.
     * @param query Search query to match (case-insensitive).
     * @param highlightColor Background highlight color.
     * @param textColor Color for highlighted text to ensure accessibility.
     * @return AnnotatedString with highlighted search term ranges.
     */
    fun highlight(
        annotated: AnnotatedString,
        query: String,
        highlightColor: Color = Color(0xFFFFD54F),
        textColor: Color = Color.Black
    ): AnnotatedString {
        if (query.isBlank()) return annotated
        val builder = AnnotatedString.Builder(annotated)
        val text = annotated.text
        val lowercaseText = text.lowercase()
        val lowercaseQuery = query.lowercase()

        var startIndex = lowercaseText.indexOf(lowercaseQuery)
        while (startIndex >= 0) {
            val endIndex = startIndex + query.length
            builder.addStyle(
                style = SpanStyle(
                    background = highlightColor,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                start = startIndex,
                end = endIndex
            )
            startIndex = lowercaseText.indexOf(lowercaseQuery, endIndex)
        }
        return builder.toAnnotatedString()
    }

    /**
     * Helper overload that highlights a plain string text.
     */
    fun highlight(
        text: String,
        query: String,
        highlightColor: Color = Color(0xFFFFD54F),
        textColor: Color = Color.Black
    ): AnnotatedString {
        return highlight(AnnotatedString(text), query, highlightColor, textColor)
    }
}
