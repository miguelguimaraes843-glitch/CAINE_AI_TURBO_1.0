package com.exemplo.caine.core

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    private val queue: MutableList<String> = mutableListOf()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(Locale("pt", "BR"))

            if (result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
            ) {

                isReady = true

                tts?.setPitch(1.2f)
                tts?.setSpeechRate(1.05f)

                // executa fila
                queue.forEach { speakInternal(it) }
                queue.clear()
            }
        }
    }

    fun speak(text: String) {

        if (text.isBlank()) return

        if (!isReady) {
            queue.add(text)
            return
        }

        speakInternal(text)
    }

    private fun speakInternal(text: String) {
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}