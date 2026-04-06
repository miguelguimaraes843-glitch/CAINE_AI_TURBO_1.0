package com.exemplo.caine.core

import android.content.Context

class EvolutionEngine(context: Context) {

    private val prefs = context.getSharedPreferences("caine_evolution", Context.MODE_PRIVATE)

    private var sarcasmLevel = prefs.getInt("sarcasm", 5)
    private var theatricalLevel = prefs.getInt("theatrical", 5)
    private var directnessLevel = prefs.getInt("directness", 5)

    // ==========================
    // EVOLUÇÃO BASEADA NO USUÁRIO
    // ==========================

    fun evolve(userText: String) {

        val text = userText.lowercase()

        when {
            text.contains("rápido") || text.contains("direto") -> {
                directnessLevel += 1
                theatricalLevel -= 1
            }

            text.contains("engraçado") || text.contains("kk") -> {
                sarcasmLevel += 1
                theatricalLevel += 1
            }

            text.contains("explica") -> {
                directnessLevel -= 1
                theatricalLevel += 1
            }

            else -> {
                sarcasmLevel += 0
            }
        }

        sarcasmLevel = sarcasmLevel.coerceIn(1, 10)
        theatricalLevel = theatricalLevel.coerceIn(1, 10)
        directnessLevel = directnessLevel.coerceIn(1, 10)

        save()
    }

    // ==========================
    // GERAR PERSONALIDADE DINÂMICA
    // ==========================

    fun getEvolutionPrompt(): String {

        return """
NÍVEIS DE PERSONALIDADE:

Sarcasmo: $sarcasmLevel / 10
Teatralidade: $theatricalLevel / 10
Diretividade: $directnessLevel / 10

AJUSTE SUA RESPOSTA COM BASE NISSO:
- Quanto maior o sarcasmo → mais provocador
- Quanto maior a teatralidade → mais expressivo
- Quanto maior a diretividade → mais objetivo
"""
    }

    private fun save() {
        prefs.edit()
            .putInt("sarcasm", sarcasmLevel)
            .putInt("theatrical", theatricalLevel)
            .putInt("directness", directnessLevel)
            .apply()
    }
}