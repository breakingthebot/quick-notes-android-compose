/*
 * Provides a reusable in-memory NotebookDao implementation for local JVM tests.
 * Connects to: NoteRepository tests, Compose UI tests, and the NotebookDao contract.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.data

import com.breakingthebot.quicknotes.model.Notebook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory DAO used by JVM tests to avoid Room or emulator dependencies.
 */
class InMemoryNotebookDao : NotebookDao {
    private val notebooks = MutableStateFlow(emptyList<Notebook>())
    private var nextId = 1

    /**
     * Streams the current in-memory notebooks list.
     *
     * @return Observable notebook list sorted by name.
     */
    override fun observeNotebooks(): Flow<List<Notebook>> = notebooks

    /**
     * Adds or replaces a notebook in memory.
     *
     * @param notebook Notebook to store.
     */
    override suspend fun insert(notebook: Notebook): Long {
        val storedNotebook = if (notebook.id == 0) {
            notebook.copy(id = nextId++)
        } else {
            nextId = maxOf(nextId, notebook.id + 1)
            notebook
        }
        notebooks.value = (notebooks.value.filterNot { existing -> existing.id == storedNotebook.id } + storedNotebook)
            .sortedBy { existing -> existing.name }
        return storedNotebook.id.toLong()
    }

    /**
     * Replaces an existing notebook in memory.
     *
     * @param notebook Notebook to update.
     */
    override suspend fun update(notebook: Notebook) {
        insert(notebook)
    }

    /**
     * Removes a notebook from memory.
     *
     * @param notebook Notebook to delete.
     */
    override suspend fun delete(notebook: Notebook) {
        notebooks.value = notebooks.value.filterNot { existing -> existing.id == notebook.id }
    }
}
