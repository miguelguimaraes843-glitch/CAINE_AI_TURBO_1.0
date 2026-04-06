package com.exemplo.caine.core

object IntentAnalyzer {

    fun analyze(text: String): String {

        val lower = text.lowercase()

        return when {
            "abrir" in lower -> "OPEN"
            "pesquisar" in lower -> "SEARCH"
            "rolar" in lower -> "SCROLL"
            "voltar" in lower -> "BACK"
            else -> "CHAT"
        }
    }
}