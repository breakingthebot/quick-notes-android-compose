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

    /**
     * Adds persisted note tags while preserving existing notes.
     */
    val migration2To3: Migration =
        object : Migration(2, 3) {
            /**
             * Updates the schema from version 2 to version 3.
             *
             * @param database Raw SQLite database being migrated.
             */
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''",
                )
            }
        }

    /**
     * Adds the isDeleted state column while preserving existing notes.
     */
    val migration3To4: Migration =
        object : Migration(3, 4) {
            /**
             * Updates the schema from version 3 to version 4.
             *
             * @param database Raw SQLite database being migrated.
             */
             override fun migrate(database: SupportSQLiteDatabase) {
                 database.execSQL(
                     "ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0",
                 )
             }
        }

    /**
     * Adds the isPinned state column while preserving existing notes.
     */
    val migration4To5: Migration =
        object : Migration(4, 5) {
            /**
             * Updates the schema from version 4 to version 5.
             *
             * @param database Raw SQLite database being migrated.
             */
             override fun migrate(database: SupportSQLiteDatabase) {
                 database.execSQL(
                     "ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0",
                 )
             }
        }

    /**
     * Adds the isChecklist state column while preserving existing notes.
     */
    val migration5To6: Migration =
        object : Migration(5, 6) {
            /**
             * Updates the schema from version 5 to version 6.
             *
             * @param database Raw SQLite database being migrated.
             */
             override fun migrate(database: SupportSQLiteDatabase) {
                 database.execSQL(
                     "ALTER TABLE notes ADD COLUMN isChecklist INTEGER NOT NULL DEFAULT 0",
                 )
             }
        }
}
