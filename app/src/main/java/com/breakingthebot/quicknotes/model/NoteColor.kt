/*
 * Defines color categories for note customization.
 * Connects to: Note model and NoteColorConverter.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.model

/**
 * Represents the customizable background color options for note cards.
 *
 * @property label The user-facing label for the color choice.
 */
enum class NoteColor(val label: String) {
    DEFAULT("Default"),
    MINT("Mint"),
    PEACH("Peach"),
    LAVENDER("Lavender"),
    BLUE("Blue")
}
