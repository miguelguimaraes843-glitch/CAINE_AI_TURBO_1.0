package com.exemplo.caine.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object MemoryManager {

    private lateinit var prefs: SharedPreferences

    var longTermMemory = mutableListOf<String>()
    var learnedCommands = mutableMapOf<String, String>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("caine_brain", Context.MODE_PRIVATE)
        load()
    }

    fun remember(text: String) {
        longTermMemory.add(text)
        persist()
    }

    fun learn(command: String, action: String) {
        learnedCommands[command.lowercase()] = action
        persist()
    }

    fun getLearnedAction(text: String): String? {
        return learnedCommands.entries.firstOrNull {
            text.lowercase().contains(it.key)
        }?.value
    }

    private fun persist() {
        prefs.edit()
            .putString("memory", Gson().toJson(longTermMemory))
            .putString("commands", Gson().toJson(learnedCommands))
            .apply()
    }

    private fun load() {
        val mem = prefs.getString("memory", null)
        val cmd = prefs.getString("commands", null)

        if (mem != null)
            longTermMemory = Gson().fromJson(mem, Array<String>::class.java).toMutableList()

        if (cmd != null)
            learnedCommands = Gson().fromJson(cmd, Map::class.java) as MutableMap<String, String>
    }
}