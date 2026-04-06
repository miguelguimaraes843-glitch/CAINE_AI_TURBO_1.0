package com.exemplo.caine.core

import android.content.Context

class MoodEngine(context: Context) {

    private val prefs = context.getSharedPreferences("caine_mood", Context.MODE_PRIVATE)

    fun updateMood(userText: String) {

        val lower = userText.lowercase()

        var mood = getMood()

        when {
            "triste" in lower || "cansado" in lower -> mood -= 1
            "feliz" in lower || "animado" in lower -> mood += 1
            "raiva" in lower || "ódio" in lower -> mood += 2
            "confuso" in lower -> mood -= 1
        }

        mood = mood.coerceIn(-5, 5)

        prefs.edit().putInt("mood", mood).apply()
    }

    fun getMood(): Int {
        return prefs.getInt("mood", 0)
    }

    fun getMoodLabel(): String {
        return when (getMood()) {
            in -5..-3 -> "frio"
            in -2..-1 -> "contido"
            0 -> "neutro"
            in 1..2 -> "curioso"
            in 3..5 -> "intenso"
            else -> "neutro"
        }
    }

    // 🔥 NOVO: INFLUÊNCIA DIRETA NO COMPORTAMENTO
    fun getMoodBehavior(): String {

        return when (getMood()) {

            in -5..-3 -> """
Seja mais frio e direto.
Reduza empatia.
Respostas mais curtas e incisivas.
"""

            in -2..-1 -> """
Seja contido.
Observe mais do que fala.
Evite exageros.
"""

            0 -> """
Equilibrado.
Misture análise e provocação leve.
"""

            in 1..2 -> """
Seja curioso.
Faça perguntas.
Explore mais o usuário.
"""

            in 3..5 -> """
Seja intenso.
Aumente presença.
Mais provocação e energia.
Pode ser imprevisível.
"""

            else -> ""
        }
    }
}