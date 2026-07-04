/*
 * Creates NotesViewModel instances with injected repository dependencies.
 * Connects to: MainActivity, NotesViewModel, and NoteRepository.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.breakingthebot.quicknotes.data.NoteRepository
import com.breakingthebot.quicknotes.services.NotesChangeNotifier

/**
 * Factory for constructing NotesViewModel with runtime dependencies.
 *
 * @property repository Repository supplied to new view model instances.
 * @property notesChangeNotifier Mutation side effects supplied to new view model instances.
 */
class NotesViewModelFactory(
    private val repository: NoteRepository,
    private val notesChangeNotifier: NotesChangeNotifier,
) : ViewModelProvider.Factory {
    /**
     * Creates a view model instance of the requested class.
     *
     * @param modelClass Requested view model class.
     * @return Instantiated NotesViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(repository, notesChangeNotifier) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
