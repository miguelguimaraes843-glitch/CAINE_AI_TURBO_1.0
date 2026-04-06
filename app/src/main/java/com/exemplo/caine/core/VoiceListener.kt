package com.exemplo.caine.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.*

class VoiceListener(
    private val context: Context,
    private val onResult: (String) -> Unit
) {

    private var recognizer: SpeechRecognizer? = null
    private var isListening = false

    fun toggle() {
        if (isListening) stop()
        else start()
    }

    fun start() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle?) {

                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()

                if (!text.isNullOrEmpty()) {
                    onResult(text)
                }

                // 🔥 continua ouvindo automaticamente
                restart()
            }

            override fun onError(error: Int) {
                // 🔥 evita travar
                restart()
            }

            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onEndOfSpeech() {}

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer?.startListening(intent)
    }

    private fun restart() {
        if (isListening) {
            recognizer?.cancel()
            start()
        }
    }

    fun stop() {
        isListening = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}
