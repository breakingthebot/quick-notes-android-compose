/*
 * Provides the singleton Room database instance for the app.
 * Connects to: Note entity, NoteDao, and MainActivity.
 * Created: 2026-07-03
 */
package com.breakingthebot.quicknotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.breakingthebot.quicknotes.model.Note

private const val DATABASE_NAME = "quick_notes.db"

/**
 * Room database that stores notes locally on the device.
 */
@Database(entities = [Note::class], version = 3, exportSchema = false)
@TypeConverters(TagListConverter::class)
abstract class QuickNotesDatabase : RoomDatabase() {
    /**
     * Returns the DAO used to query and mutate note records.
     *
     * @return Room DAO for notes.
     */
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: QuickNotesDatabase? = null

        /**
         * Returns the shared database instance.
         *
         * @param context Android context used to open the database file.
         * @return Singleton database instance.
         */
        fun getInstance(context: Context): QuickNotesDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuickNotesDatabase::class.java,
                    DATABASE_NAME,
                ).addMigrations(
                    DatabaseMigrations.migration1To2,
                    DatabaseMigrations.migration2To3,
                ).build().also { instance = it }
            }
        }
    }
}
