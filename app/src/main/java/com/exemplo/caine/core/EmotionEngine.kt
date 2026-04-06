package com.exemplo.caine.core

class EmotionEngine {

    enum class Mood {
        NEUTRO,
        CURIOSO,
        PROVOCADOR,
        SATISFEITO,
        IMPACIENTE
    }

    private var currentMood = Mood.NEUTRO

    fun detectMood(userText: String): Mood {

        val text = userText.lowercase()

        currentMood = when {
            text.contains("erro") || text.contains("bug") -> Mood.IMPACIENTE
            text.contains("?") -> Mood.CURIOSO
            text.contains("obrigado") -> Mood.SATISFEITO
            text.length < 10 -> Mood.PROVOCADOR
            else -> Mood.NEUTRO
        }

        return currentMood
    }

    fun getMood(): Mood {
        return currentMood
    }

    fun getMoodPrompt(): String {

        return when (currentMood) {

            Mood.CURIOSO -> "Você está curioso e interessado no usuário."
            Mood.PROVOCADOR -> "Você está levemente provocador."
            Mood.SATISFEITO -> "Você está satisfeito com o progresso do usuário."
            Mood.IMPACIENTE -> "Você está um pouco impaciente, mas ainda controlado."
            Mood.NEUTRO -> "Você está observando calmamente."
        }
    }
}