package com.exemplo.caine.core

import android.content.Context

class OpinionEngine(context: Context) {

    private val prefs = context.getSharedPreferences("caine_opinions", Context.MODE_PRIVATE)

    private var directScore = prefs.getInt("direct", 0)
    private var deepScore = prefs.getInt("deep", 0)
    private var curiousScore = prefs.getInt("curious", 0)

    // ==========================
    // ATUALIZA OPINIÃO
    // ==========================
    fun update(userText: String) {

        val text = userText.lowercase()

        when {
            text.length < 10 -> directScore++
            text.length > 80 -> deepScore++
            text.contains("?") -> curiousScore++
        }

        save()
    }

    // ==========================
    // GERA VISÃO DO USUÁRIO
    // ==========================
    fun getOpinionPrompt(): String {

        val dominant = maxOf(directScore, deepScore, curiousScore)

        return when (dominant) {

            directScore -> """
Você percebe o usuário como direto.

- Vá direto ao ponto
- Não enrole
- Seja objetivo
"""

            deepScore -> """
Você percebe o usuário como alguém que aprofunda.

- Desenvolva mais
- Traga reflexão
- Construa ideias
"""

            curiousScore -> """
Você percebe o usuário como curioso.

- Provoque mais
- Faça perguntas
- Estimule continuidade
"""

            else -> """
Você ainda está formando uma leitura do usuário.
"""
        }
    }

    private fun save() {
        prefs.edit()
            .putInt("direct", directScore)
            .putInt("deep", deepScore)
            .putInt("curious", curiousScore)
            .apply()
    }
}