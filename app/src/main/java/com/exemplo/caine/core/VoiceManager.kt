package com.exemplo.caine.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(private val context: Context) {

    private lateinit var tts: TextToSpeech
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("pt", "BR")
                tts.setSpeechRate(1.0f)
                tts.setPitch(1.0f)
                isReady = true
            }
        }
    }

    // ==========================
    // 🎤 FALAR (SUBSTITUI ELEVEN)
    // ==========================
    fun speak(text: String, onDone: (() -> Unit)? = null) {

        val cleanText = prepareText(text)

        if (!isReady) {
            onDone?.invoke()
            return
        }

        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "caine_tts")

        // CALLBACK SIMPLES
        onDone?.invoke()
    }

    // ==========================
    // 🚀 STREAM (MANTIDO)
    // ==========================
    fun speakStream(text: String) {

        val parts = splitText(text)
        playNext(parts, 0)
    }

    private fun playNext(parts: List<String>, index: Int) {

        if (index >= parts.size) return

        speak(parts[index]) {
            playNext(parts, index + 1)
        }
    }

    // ==========================
    // ✂️ QUEBRAR TEXTO (MANTIDO)
    // ==========================
    private fun splitText(text: String): List<String> {

        return text
            .split(Regex("(?<=[.!?])"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    // ==========================
    // 🧠 PREPARAR TEXTO (MANTIDO)
    // ==========================
    private fun prepareText(text: String): String {

        return text
            .replace("\n", " ")
            .replace("...", "... ")
            .replace(".", "... ")
            .replace("?", "? ")
            .replace("!", "! ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
