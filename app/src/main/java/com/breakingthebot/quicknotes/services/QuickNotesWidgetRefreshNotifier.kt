/*
 * Refreshes installed Quick Notes home-screen widgets after note mutations.
 * Connects to: NotesChangeNotifier, QuickNotesWidget, and Android app context.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.services

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.breakingthebot.quicknotes.widget.QuickNotesWidget

private const val WIDGET_REFRESH_LOG_TAG = "QuickNotesWidget"

/**
 * Widget refresh side effect implementation for note changes.
 *
 * @property appContext Application context used to refresh widget instances.
 */
class QuickNotesWidgetRefreshNotifier(
    context: Context,
) : NotesChangeNotifier {
    private val appContext = context.applicationContext

    /**
     * Refreshes every installed widget instance.
     */
    override suspend fun onNotesChanged() {
        runCatching {
            QuickNotesWidget().updateAll(appContext)
        }.onFailure { throwable ->
            Log.e(
                WIDGET_REFRESH_LOG_TAG,
                "Failed to refresh Quick Notes widgets after a note mutation.",
                throwable,
            )
        }
    }
}
