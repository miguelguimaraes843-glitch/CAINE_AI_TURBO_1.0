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

    private val API_KEY = ApiKeys.HF_API_KEY

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

    // ==========================
    // 🔥 FALLBACK OFFLINE
    // ==========================
    private fun offlineResponse(text: String): String {

        val lower = text.lowercase()

        return when {
            "oi" in lower || "olá" in lower ->
                "Você voltou… interessante."

            "tudo bem" in lower ->
                "Depende… você está?"

            "quem é você" in lower ->
                "Depende do que você consegue entender."

            "me ajuda" in lower ->
                "Ajudo… mas não de graça."

            else -> listOf(
                "Isso não foi por acaso.",
                "Tem algo aí que você não falou.",
                "Você sempre pensa assim?",
                "Curioso… continua.",
                "Isso diz mais sobre você do que parece."
            ).random()
        }
    }

    // ==========================
    // 🔥 HUGGING FACE
    // ==========================
    private fun tryModel(
        index: Int,
        models: List<String>,
        messages: List<Map<String, String>>,
        callback: (String) -> Unit,
        retryCount: Int
    ) {

        if (index >= models.size) {
            callback(offlineResponse(messages.lastOrNull()?.get("content") ?: ""))
            return
        }

        // 🔥 MODELO FREE FUNCIONANDO
        val model = "google/flan-t5-large"

        val prompt = buildString {

            val emotionalBlock = buildEmotionalBlock()

            append("Você é Caine.\n")
            append("Direto, expressivo e imprevisível.\n\n")

            append("MEMÓRIA EMOCIONAL:\n$emotionalBlock\n\n")

            messages.forEach {
                append("${it["role"]}: ${it["content"]}\n")
            }

            append("\nassistant:")
        }

        val json = JSONObject()
        json.put("inputs", prompt)

        val parameters = JSONObject()
        parameters.put("max_new_tokens", 120)
        parameters.put("temperature", 0.7)
        parameters.put("return_full_text", false)

        json.put("parameters", parameters)

        val options = JSONObject()
        options.put("wait_for_model", true)

        json.put("options", options)

        val body = json.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/$model")
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(offlineResponse(messages.lastOrNull()?.get("content") ?: ""))
            }

            override fun onResponse(call: Call, response: Response) {

                try {

                    val bodyStr = response.body?.string()

                    println("HF RESPONSE: $bodyStr")

                    if (!response.isSuccessful || bodyStr.isNullOrEmpty()) {
                        callback(offlineResponse(messages.lastOrNull()?.get("content") ?: ""))
                        return
                    }

                    val clean = try {

                        val jsonArray = JSONArray(bodyStr)

                        val generated = jsonArray
                            .getJSONObject(0)
                            .optString("generated_text", "")

                        generated.trim()

                    } catch (_: Exception) {

                        val jsonObj = JSONObject(bodyStr)

                        if (jsonObj.has("error")) {
                            println("HF ERROR: ${jsonObj.getString("error")}")
                            return callback(offlineResponse(messages.lastOrNull()?.get("content") ?: ""))
                        }

                        jsonObj.optString("generated_text", "").trim()
                    }

                    if (clean.length < 5) {

                        if (retryCount < 1) {

                            val improved = messages.toMutableList()
                            improved.add(
                                mapOf(
                                    "role" to "user",
                                    "content" to "Responda direto e sem enrolação."
                                )
                            )

                            tryModel(index, models, improved, callback, retryCount + 1)
                            return
                        }

                        callback(offlineResponse(messages.lastOrNull()?.get("content") ?: ""))
                        return
                    }

                    rewardModel(model)
                    callback(clean)

                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(offlineResponse(messages.lastOrNull()?.get("content") ?: ""))
                }
            }
        })
    }

    private fun rewardModel(model: String) {
        val score = (modelScores[model] ?: 5) + 1
        modelScores[model] = score
        saveScore(model, score)
    }

    private fun penalizeModel(model: String) {
        val score = (modelScores[model] ?: 5) - 1
        modelScores[model] = score
        saveScore(model, score)
    }
}
