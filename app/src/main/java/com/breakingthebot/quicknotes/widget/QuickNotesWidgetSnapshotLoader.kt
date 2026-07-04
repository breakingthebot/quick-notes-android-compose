/*
 * Loads Room-backed data for the Quick Notes home-screen widget.
 * Connects to: QuickNotesDatabase, NoteDao, and QuickNotesWidgetFormatter.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.widget

import android.content.Context
import android.util.Log
import com.breakingthebot.quicknotes.data.QuickNotesDatabase

private const val WIDGET_NOTE_LIMIT = 3
private const val WIDGET_LOAD_LOG_TAG = "QuickNotesWidget"

/**
 * Retrieves recent notes and builds widget display state.
 */
object QuickNotesWidgetSnapshotLoader {
    /**
     * Loads the current widget snapshot from local storage.
     *
     * @param context Android context used to open the database.
     * @return Snapshot ready for widget rendering.
     */
    suspend fun load(context: Context): QuickNotesWidgetSnapshot {
        return runCatching {
            val noteDao = QuickNotesDatabase.getInstance(context).noteDao()
            QuickNotesWidgetFormatter.format(
                notes = noteDao.getRecentActiveNotes(WIDGET_NOTE_LIMIT),
            )
        }.getOrElse { throwable ->
            Log.e(
                WIDGET_LOAD_LOG_TAG,
                "Failed to load widget notes from local Room storage.",
                throwable,
            )
            QuickNotesWidgetFormatter.format(emptyList())
        }
    }
}
