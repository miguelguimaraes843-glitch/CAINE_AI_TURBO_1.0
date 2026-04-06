package com.exemplo.caine.core

import kotlin.random.Random

class IntentionEngine {

    fun detectHiddenIntent(userText: String): String {

        val text = userText.lowercase()

        val signals = mutableListOf<String>()

        if (text.contains("não sei")) {
            signals.add("incerteza ou evasão")
        }

        if (text.contains("tanto faz") || text.contains("qualquer")) {
            signals.add("falta de interesse real")
        }

        if (text.contains("deixa") || text.contains("depois")) {
            signals.add("evitação")
        }

        if (text.length < 8) {
            signals.add("resposta superficial")
        }

        if (signals.isEmpty()) return ""

        if (Random.nextInt(100) > 60) return "" // 🔥 não usar sempre

        val interpretations = listOf(
            "Talvez isso seja $signals.",
            "Isso parece $signals.",
            "Tem algo aí além do que você disse.",
            "Isso não soa completo.",
            "Você não falou tudo."
        )

        return interpretations.random()
    }

    fun getHiddenIntentPrompt(hidden: String): String {

        if (hidden.isEmpty()) return ""

        return """
LEITURA DE INTENÇÃO OCULTA:

- Perceba além do que foi dito
- Se houver algo implícito → traga à tona
- Não afirme como verdade absoluta
- Use tom curioso ou provocativo

Exemplo de abordagem:

- "Isso não parece completo…"
- "Você não falou tudo."
- "Tem algo aí por trás, não tem?"

Interpretação atual:
$hidden
"""
    }
}