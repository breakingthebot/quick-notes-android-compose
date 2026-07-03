/*
 * Holds explicit Room migrations for persistent schema changes.
 * Connects to: QuickNotesDatabase and Note entity evolution.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Central list of database migrations used by Room.
 */
object DatabaseMigrations {
    /**
     * Adds the archive state column while preserving existing notes.
     */
    val migration1To2: Migration =
        object : Migration(1, 2) {
            /**
             * Updates the schema from version 1 to version 2.
             *
             * @param database Raw SQLite database being migrated.
             */
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
}
