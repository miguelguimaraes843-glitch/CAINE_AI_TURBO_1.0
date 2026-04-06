package com.exemplo.caine.core

import kotlin.random.Random

class ConflictEngine {

    fun shouldChallenge(userText: String): Boolean {

        val text = userText.lowercase()

        // 🔥 mais chance em frases curtas ou vagas
        val vague = text.length < 20

        val chance = if (vague) 60 else 30

        return Random.nextInt(100) < chance
    }

    fun getConflictPrompt(): String {

        return """
CONFLITO INTELIGENTE:

- Você NÃO precisa concordar com o usuário
- Se algo parecer superficial, incompleto ou estranho → questione
- Você pode discordar com elegância
- Você pode provocar inconsistências

FORMAS DE FAZER:

- "Não. Isso não faz sentido ainda."
- "Você pulou uma parte importante."
- "Isso está raso… aprofunda."
- "Tem algo aí que você não está dizendo."

REGRA:

- Não seja agressivo
- Seja firme e confiante
- Crie tensão leve, não confronto pesado
"""
    }
}