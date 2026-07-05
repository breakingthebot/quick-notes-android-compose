/*
 * Provides voice transcription capabilities using Android's native SpeechRecognizer.
 * Connects to: NotesViewModel and MainActivity.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

object VoiceTranscriptionService {

    private var speechRecognizer: SpeechRecognizer? = null
    
    // Test hook to allow injecting mock speech transcriptions during unit tests
    var mockTranscriptResult: String? = null

    /**
     * Starts listening to the microphone and transcribes speech.
     */
    fun startListening(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val mockText = mockTranscriptResult
        if (mockText != null) {
            onResult(mockText)
            return
        }

        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                onError("Speech recognition is not supported on this device.")
                return
            }

            stopListening()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_CLIENT -> "Client-side error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions."
                            SpeechRecognizer.ERROR_NETWORK -> "Network error."
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy."
                            SpeechRecognizer.ERROR_SERVER -> "Server error."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
                            else -> "Unknown speech recognition error."
                        }
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onResult(matches[0])
                        } else {
                            onError("No speech recognized.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Failed to initialize SpeechRecognizer.")
        }
    }

    /**
     * Cancels active speech recognition sessions.
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
