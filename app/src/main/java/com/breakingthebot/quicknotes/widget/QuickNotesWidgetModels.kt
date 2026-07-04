/*
 * Defines lightweight display models used by the home-screen widget.
 * Connects to: QuickNotesWidgetFormatter and QuickNotesWidget.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.widget

/**
 * Immutable snapshot rendered by the Quick Notes widget.
 *
 * @property noteCountLabel Summary of the visible active note count.
 * @property notes Recent note rows shown in the widget.
 * @property emptyMessage Fallback copy shown when there are no active notes.
 */
data class QuickNotesWidgetSnapshot(
    val noteCountLabel: String,
    val notes: List<QuickNotesWidgetNote>,
    val emptyMessage: String,
)

/**
 * Compact widget row model for a single note preview.
 *
 * @property title Note title shown in bold.
 * @property preview One-line body preview.
 */
data class QuickNotesWidgetNote(
    val title: String,
    val preview: String,
)
