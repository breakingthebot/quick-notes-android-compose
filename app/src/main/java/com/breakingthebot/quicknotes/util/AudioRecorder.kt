/*
 * Manages audio recording sessions using MediaRecorder.
 * Connects to: NotesViewModel and NoteEditorCard.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

object AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    /**
     * Starts recording audio and saves to a file in the app's external files directory.
     *
     * @return Absolute file path of the recording, or null if failed.
     */
    fun startRecording(context: Context): String? {
        return try {
            val directory = File(context.getExternalFilesDir(null), "Audio")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "REC_${System.currentTimeMillis()}.mp3")
            outputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder = recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Stops the active recording session.
     */
    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
