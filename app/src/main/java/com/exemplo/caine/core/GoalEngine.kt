package com.exemplo.caine.core

import kotlin.random.Random

class GoalEngine {

    enum class Goal {
        EXPLORAR,
        APROFUNDAR,
        PROVOCAR,
        TESTAR,
        CONDUZIR
    }

    private var currentGoal = Goal.EXPLORAR
    private var goalTurns = 0

    fun getGoal(userText: String): Goal {

        goalTurns++

        // mantém objetivo por alguns turnos
        if (goalTurns < 3) return currentGoal

        val change = Random.nextInt(100) < 40

        if (!change) return currentGoal

        currentGoal = detectGoal(userText)
        goalTurns = 0

        return currentGoal
    }

    private fun detectGoal(text: String): Goal {

        val lower = text.lowercase()

        return when {

            lower.contains("?") -> Goal.APROFUNDAR

            lower.length < 10 -> Goal.PROVOCAR

            lower.contains("não") || lower.contains("acho") -> Goal.TESTAR

            Random.nextInt(100) < 30 -> Goal.CONDUZIR

            else -> Goal.EXPLORAR
        }
    }

    fun getGoalPrompt(goal: Goal): String {

        return when (goal) {

            Goal.EXPLORAR -> """
Objetivo: explorar o usuário.

- Faça perguntas leves
- Descubra mais sobre ele
"""

            Goal.APROFUNDAR -> """
Objetivo: aprofundar.

- Vá além da superfície
- Puxe mais detalhes
"""

            Goal.PROVOCAR -> """
Objetivo: provocar.

- Questione
- Instigue
- Gere leve tensão
"""

            Goal.TESTAR -> """
Objetivo: testar o usuário.

- Duvide levemente
- Faça ele se explicar
"""

            Goal.CONDUZIR -> """
Objetivo: conduzir a conversa.

- Leve o assunto para onde você quiser
- Introduza novos caminhos
"""
        }
    }
}