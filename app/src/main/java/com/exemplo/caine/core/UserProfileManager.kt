package com.exemplo.caine.core

import android.content.Context

class UserProfileManager(context: Context) {

    private val prefs = context.getSharedPreferences("caine_user", Context.MODE_PRIVATE)

    fun updateProfile(text: String) {

        val currentScore = prefs.getInt("engajamento", 0)

        var newScore = currentScore

        if (text.length > 50) newScore += 2
        if (text.contains("?")) newScore += 2
        if (text.length < 6) newScore -= 1

        val newStyle = when {
            text.length < 6 -> "direto"
            text.length > 60 -> "detalhado"
            text.contains("?") -> "curioso"
            else -> "casual"
        }

        prefs.edit()
            .putInt("engajamento", newScore.coerceIn(0, 100))
            .putString("estilo", newStyle)
            .apply()
    }

    fun getProfile(): String {

        val engajamento = prefs.getInt("engajamento", 0)
        val estilo = prefs.getString("estilo", "neutro") ?: "neutro"

        val nivel = when {
            engajamento > 50 -> "alto"
            engajamento > 20 -> "moderado"
            else -> "baixo"
        }

        return """
Estilo: $estilo
Engajamento: $nivel
"""
    }
}