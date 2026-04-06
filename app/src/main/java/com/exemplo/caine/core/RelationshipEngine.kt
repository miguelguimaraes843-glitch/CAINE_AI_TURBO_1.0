package com.exemplo.caine.core

import android.content.Context

class RelationshipEngine(context: Context) {

    private val prefs = context.getSharedPreferences("caine_relationship", Context.MODE_PRIVATE)

    private var familiarity: Int = prefs.getInt("familiarity", 0)
    private var trust: Int = prefs.getInt("trust", 0)

    // ==========================
    // ATUALIZA RELAÇÃO
    // ==========================

    fun updateRelationship(userText: String) {

        val text = userText.lowercase()

        when {
            text.contains("obrigado") || text.contains("valeu") -> {
                trust += 2
                familiarity += 1
            }

            text.contains("erro") || text.contains("bug") -> {
                trust += 1
            }

            text.length > 20 -> {
                familiarity += 1
            }

            else -> {
                familiarity += 1
            }
        }

        trust = trust.coerceIn(0, 100)
        familiarity = familiarity.coerceIn(0, 100)

        save()
    }

    // ==========================
    // GERAR ESTILO RELACIONAL
    // ==========================

    fun getRelationshipPrompt(): String {

        return when {
            familiarity < 10 -> """
Você ainda está conhecendo o usuário.
Seja curioso e levemente formal.
"""

            familiarity < 30 -> """
Você já conhece um pouco o usuário.
Seja mais natural e confortável.
"""

            familiarity < 60 -> """
Você tem familiaridade com o usuário.
Pode ser mais solto e provocador leve.
"""

            else -> """
Você conhece bem o usuário.
Seja direto, confiante e até um pouco íntimo.
"""
        }
    }

    // ==========================
    // DEBUG / STATUS
    // ==========================

    fun getStats(): String {
        return "familiaridade=$familiarity, confiança=$trust"
    }

    private fun save() {
        prefs.edit()
            .putInt("familiarity", familiarity)
            .putInt("trust", trust)
            .apply()
    }
}