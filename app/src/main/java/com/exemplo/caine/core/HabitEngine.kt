package com.exemplo.caine.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class HabitEngine(context: Context) {

    private val prefs = context.getSharedPreferences("caine_habits", Context.MODE_PRIVATE)

    // ==========================
    // 📊 REGISTRAR AÇÃO
    // ==========================
    fun track(action: String, data: String?) {

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        val key = "$action:$data:$hour"

        val count = prefs.getInt(key, 0) + 1
        prefs.edit().putInt(key, count).apply()
    }

    // ==========================
    // 🧠 DETECTAR HÁBITO
    // ==========================
    fun getHabitSuggestion(): Pair<String, String?>? {

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        val all = prefs.all

        val candidates = all.filter {
            it.key.endsWith(":$hour") && (it.value as Int) >= 3
        }

        if (candidates.isEmpty()) return null

        val selected = candidates.maxByOrNull { it.value as Int } ?: return null

        val parts = selected.key.split(":")

        val action = parts[0]
        val data = parts.getOrNull(1)

        return Pair(action, if (data == "null") null else data)
    }
}