/*
 * Hosts the Compose UI for the notes application.
 * Connects to: QuickNotesApp, NotesViewModel, and Android activity lifecycle.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakingthebot.quicknotes.data.NoteRepository
import com.breakingthebot.quicknotes.data.QuickNotesDatabase
import com.breakingthebot.quicknotes.services.QuickNotesWidgetRefreshNotifier
import com.breakingthebot.quicknotes.ui.QuickNotesApp
import com.breakingthebot.quicknotes.ui.theme.QuickNotesTheme
import com.breakingthebot.quicknotes.viewmodel.NotesViewModel
import com.breakingthebot.quicknotes.viewmodel.NotesViewModelFactory

/**
 * Android entry activity that wires the database-backed view model into Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Creates the Compose content tree for the app.
     *
     * @param savedInstanceState Previously saved activity state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = QuickNotesDatabase.getInstance(applicationContext)
        val repository = NoteRepository(database.noteDao())
        val widgetRefreshNotifier = QuickNotesWidgetRefreshNotifier(applicationContext)

        setContent {
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModelFactory(
                    repository = repository,
                    notesChangeNotifier = widgetRefreshNotifier,
                ),
            )

            QuickNotesTheme {
                QuickNotesApp(viewModel = notesViewModel)
            }
        }
    }
}
