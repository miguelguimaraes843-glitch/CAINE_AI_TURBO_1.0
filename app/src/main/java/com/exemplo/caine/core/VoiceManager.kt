package com.exemplo.caine.core

import android.content.Context
import android.media.MediaPlayer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class VoiceManager(private val context: Context) {

    private val client = OkHttpClient()

    private val API_KEY = "sk_4a7fd5086c0a3708caadf1e3a2595083e11c9906fea1c183"
    private val VOICE_ID = "sWaLPebpwGYPbUC23Bni"

    // ==========================
    // 🎤 GERAR VOZ (NORMAL)
    // ==========================
    fun speak(text: String, onDone: (() -> Unit)? = null) {

        val cleanText = prepareText(text)

        val json = """
        {
          "text": "$cleanText",
          "model_id": "eleven_multilingual_v2",
          "voice_settings": {
            "stability": 0.35,
            "similarity_boost": 0.75,
            "style": 0.9,
            "use_speaker_boost": true
          }
        }
        """

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$VOICE_ID")
            .addHeader("xi-api-key", API_KEY)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                onDone?.invoke()
            }

            override fun onResponse(call: Call, response: Response) {

                val bytes = response.body?.bytes()

                if (bytes != null) {
                    playAudio(bytes, onDone)
                } else {
                    onDone?.invoke()
                }
            }
        })
    }

    // ==========================
    // 🚀 STREAM (NOVO)
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
    // ✂️ QUEBRAR TEXTO (NOVO)
    // ==========================
    private fun splitText(text: String): List<String> {

        return text
            .split(Regex("(?<=[.!?])"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    // ==========================
    // 🔊 TOCAR ÁUDIO
    // ==========================
    private fun playAudio(bytes: ByteArray, onDone: (() -> Unit)?) {

        try {
            val file = File.createTempFile("caine_voice", ".mp3", context.cacheDir)
            file.writeBytes(bytes)

            val player = MediaPlayer()

            player.setDataSource(file.absolutePath)
            player.prepare()
            player.start()

            player.setOnCompletionListener {
                player.release()
                onDone?.invoke()
            }

        } catch (_: Exception) {
            onDone?.invoke()
        }
    }

    // ==========================
    // 🧠 PREPARAR TEXTO (MELHORADO)
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