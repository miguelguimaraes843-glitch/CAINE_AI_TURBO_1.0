package com.exemplo.caine.core

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionMemory(context: Context) {

    private val prefs = context.getSharedPreferences("caine_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    private var history: MutableList<String> = load()

    private fun load(): MutableList<String> {
        val json = prefs.getString("session_history", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun save() {
        prefs.edit().putString("session_history", gson.toJson(history)).apply()
    }

    // ==========================
    // ADICIONAR CONTEXTO
    // ==========================

    fun add(text: String) {
        history.add(text)

        if (history.size > 20) {
            history.removeAt(0)
        }

        save()
    }

    // ==========================
    // CONTEXTO ATUAL
    // ==========================

    fun getContext(): String {
        return if (history.isEmpty()) {
            "Sem histórico recente."
        } else {
            history.joinToString("\n")
        }
    }

    fun clear() {
        history.clear()
        save()
    }
}