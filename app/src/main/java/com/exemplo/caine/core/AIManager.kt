package com.exemplo.caine.core

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class AIManager(context: Context) {

    private val client = OkHttpClient()

    private val API_KEY = try {
        ApiKeys.HF_API_KEY
    } catch (e: Exception) {
        ""
    }

    private val GEMINI_KEY = try {
        ApiKeys.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }

    private val prefs = context.getSharedPreferences("caine_ai", Context.MODE_PRIVATE)
    private val emotionalPrefs = context.getSharedPreferences("caine_emotional", Context.MODE_PRIVATE)

    private val defaultModels = listOf(
        "mistralai/mistral-7b-instruct",
        "openchat/openchat-7b",
        "gryphe/mythomax-l2-13b"
    )

    private val creativeModels = listOf("gryphe/mythomax-l2-13b")
    private val balancedModels = listOf("openchat/openchat-7b")
    private val logicalModels = listOf("mistralai/mistral-7b-instruct")

    private val modelScores = ConcurrentHashMap<String, Int>()

    init {
        loadScores()
    }

    private fun loadScores() {
        defaultModels.forEach {
            modelScores[it] = prefs.getInt(it, 5)
        }
    }

    private fun saveScore(model: String, score: Int) {
        prefs.edit().putInt(model, score).apply()
    }

    fun sendMessage(
        messages: List<Map<String, String>>,
        userText: String,
        mood: Int,
        callback: (String) -> Unit
    ) {

        if (userText.isBlank()) {
            callback("Não entendi… fala de novo.")
            return
        }

        detectEmotionalMemory(userText)

        val moodModels = getModelsByMood(mood)

        val sortedModels = moodModels.sortedByDescending {
            modelScores[it] ?: 5
        }

        tryModel(0, sortedModels, messages, callback, 0)
    }

    private fun getModelsByMood(mood: Int): List<String> {
        return when {
            mood >= 3 -> creativeModels + balancedModels
            mood <= -2 -> logicalModels + balancedModels
            else -> balancedModels + creativeModels + logicalModels
        }
    }

    private fun detectEmotionalMemory(text: String) {
        val lower = text.lowercase()

        val triggers = mapOf(
            "tenho medo" to 9,
            "estou triste" to 10,
            "isso me marcou" to 10,
            "nunca contei isso" to 10,
            "estou feliz" to 7
        )

        triggers.forEach { (trigger, weight) ->
            if (trigger in lower) {
                saveEmotionalMemory(text, weight)
            }
        }
    }

    private fun saveEmotionalMemory(text: String, weight: Int) {

        val list = getEmotionalMemories().toMutableList()

        val obj = JSONObject()
        obj.put("text", text)
        obj.put("weight", weight)

        list.add(obj)

        if (list.size > 10) list.removeFirst()

        emotionalPrefs.edit()
            .putString("emotional", JSONArray(list).toString())
            .apply()
    }

    private fun getEmotionalMemories(): List<JSONObject> {

        val json = emotionalPrefs.getString("emotional", null) ?: return emptyList()

        return try {
            val array = JSONArray(json)
            List(array.length()) { array.getJSONObject(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildEmotionalBlock(): String {

        val memories = getEmotionalMemories()

        if (memories.isEmpty()) return "nenhuma"

        return memories.joinToString("\n") {
            val text = it.getString("text")
            val weight = it.getInt("weight")
            "$text (impacto:$weight)"
        }
    }

    private fun tryGemini(
        messages: List<Map<String, String>>,
        callback: (String?) -> Unit
    ) {

        val prompt = buildString {
            messages.forEach {
                append("${it["role"]}: ${it["content"]}\n")
            }
        }

        val json = JSONObject()
        val parts = JSONArray().put(JSONObject().put("text", prompt))
        val content = JSONObject().put("parts", parts)
        val contentsArray = JSONArray().put(content)

        json.put("contents", contentsArray)

        val body = json.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=$GEMINI_KEY")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {

                try {

                    val bodyStr = response.body?.string()

                    if (!response.isSuccessful || bodyStr.isNullOrEmpty()) {
                        callback(null)
                        return
                    }

                    val reply = JSONObject(bodyStr)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    callback(reply.trim())

                } catch (_: Exception) {
                    callback(null)
                }
            }
        })
    }

    private fun tryModel(
        index: Int,
        models: List<String>,
        messages: List<Map<String, String>>,
        callback: (String) -> Unit,
        retryCount: Int
    ) {

        tryGemini(messages) { geminiResponse ->

            if (!geminiResponse.isNullOrBlank()) {
                callback(geminiResponse)
                return@tryGemini
            }

            val userText = messages.lastOrNull()?.get("content") ?: ""

            if (index >= models.size) {
                callback(offlineResponse(userText))
                return@tryGemini
            }

            callback(offlineResponse(userText))
        }
    }

    private fun offlineResponse(text: String): String {

        val lower = text.lowercase()

        return when {
            "oi" in lower || "olá" in lower -> "Você voltou… interessante."
            "tudo bem" in lower -> "Depende… você está?"
            "quem é você" in lower -> "Depende do que você consegue entender."
            else -> listOf(
                "Isso não foi por acaso.",
                "Tem algo aí que você não falou.",
                "Curioso… continua.",
                "Isso diz mais sobre você do que parece."
            ).random()
        }
    }
}
