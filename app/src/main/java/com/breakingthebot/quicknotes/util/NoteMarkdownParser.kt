/*
 * Parses inline markdown tags (bold, italic, headers) into Compose AnnotatedStrings.
 * Connects to: NoteListItem.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Utility to parse plain markdown text into stylized AnnotatedString objects.
 */
object NoteMarkdownParser {
    /**
     * Parses the note body, handling headers, bold, and italic markers.
     *
     * @param text Raw markdown text.
     * @return Stylized Compose AnnotatedString.
     */
    fun parse(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.lines()
            lines.forEachIndexed { index, line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("# ") -> {
                        val headerText = trimmed.substring(2)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parseInlineFormatting(headerText))
                        }
                    }
                    trimmed.startsWith("## ") -> {
                        val headerText = trimmed.substring(3)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parseInlineFormatting(headerText))
                        }
                    }
                    else -> {
                        append(parseInlineFormatting(line))
                    }
                }
                if (index < lines.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    /**
     * Parses bold (**text**) and italic (*text*) markers in a single line.
     */
    private fun parseInlineFormatting(text: String): AnnotatedString {
        return buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                when {
                    text.startsWith("**", i) -> {
                        val end = text.indexOf("**", i + 2)
                        if (end != -1) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(text.substring(i + 2, end))
                            }
                            i = end + 2
                        } else {
                            append("**")
                            i += 2
                        }
                    }
                    text.startsWith("*", i) -> {
                        val end = text.indexOf("*", i + 1)
                        if (end != -1) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(text.substring(i + 1, end))
                            }
                            i = end + 1
                        } else {
                            append("*")
                            i += 1
                        }
                    }
                    else -> {
                        append(text[i])
                        i++
                    }
                }
            }
        }
    }
}
