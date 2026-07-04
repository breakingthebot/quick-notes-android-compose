/*
 * Swaps the main dispatcher during JVM tests so ViewModel coroutines run locally.
 * Connects to: NotesViewModel tests, Compose UI tests, and kotlinx-coroutines-test.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that replaces Dispatchers.Main with a test dispatcher.
 *
 * @property dispatcher Dispatcher installed as Dispatchers.Main for the test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    /**
     * Installs the test dispatcher before each test.
     *
     * @param description JUnit-provided test description.
     */
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Restores the platform main dispatcher after each test.
     *
     * @param description JUnit-provided test description.
     */
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
