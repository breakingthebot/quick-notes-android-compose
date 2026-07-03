/*
 * Creates NotesViewModel instances with injected repository dependencies.
 * Connects to: MainActivity, NotesViewModel, and NoteRepository.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.breakingthebot.quicknotes.data.NoteRepository

/**
 * Factory for constructing NotesViewModel with runtime dependencies.
 *
 * @property repository Repository supplied to new view model instances.
 */
class NotesViewModelFactory(
    private val repository: NoteRepository,
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
            return NotesViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
