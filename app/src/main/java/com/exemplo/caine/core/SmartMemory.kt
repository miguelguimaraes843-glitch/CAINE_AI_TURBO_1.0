package com.exemplo.caine.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SmartMemory(context: Context) {

    private val prefs = context.getSharedPreferences("caine_memory", Context.MODE_PRIVATE)
    private val memoryList = mutableListOf<Memory>()

    init {
        loadMemory()
    }

    // ==========================
    // 💾 SALVAR
    // ==========================
    private fun saveMemory() {

        val jsonArray = JSONArray()

        memoryList.forEach {
            val obj = JSONObject()
            obj.put("key", it.key)
            obj.put("value", it.value)
            obj.put("importance", it.importance)
            jsonArray.put(obj)
        }

        prefs.edit().putString("memory_data", jsonArray.toString()).apply()
    }

    // ==========================
    // 📥 CARREGAR
    // ==========================
    private fun loadMemory() {

        val jsonString = prefs.getString("memory_data", null) ?: return

        try {

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {

                val obj = jsonArray.getJSONObject(i)

                memoryList.add(
                    Memory(
                        obj.getString("key"),
                        obj.getString("value"),
                        obj.getInt("importance")
                    )
                )
            }

        } catch (_: Exception) {}
    }

    // ==========================
    fun addMemory(key: String, value: String, importance: Int) {

        val existing = memoryList.find { it.key == key }

        if (existing != null) {
            memoryList.remove(existing)
        }

        memoryList.add(Memory(key, value, importance))

        // 🔥 limite pra não explodir
        if (memoryList.size > 50) {
            memoryList.sortByDescending { it.importance }
            memoryList.removeLast()
        }

        saveMemory()
    }

    // ==========================
    fun getRelevantMemories(): List<Memory> {
        return memoryList.sortedByDescending { it.importance }.take(15)
    }

    // ==========================
    fun getMemoryForPrompt(): String {

        return getRelevantMemories().joinToString("\n") {
            "${it.key}: ${it.value}"
        }
    }

    // ==========================
    fun applyReflection(newMemories: List<Memory>) {

        memoryList.clear()
        memoryList.addAll(newMemories)
        saveMemory()
    }
}