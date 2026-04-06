package com.exemplo.caine.core

import android.content.Context
import kotlin.random.Random

class PersonalityEngine(context: Context) {

    enum class Mode {
        CAOTICO,
        TEATRAL,
        SARCASTICO,
        DIRETO
    }

    // 🔥 STORAGE
    private val prefs = context.getSharedPreferences("caine_personality", Context.MODE_PRIVATE)

    // 🔥 MEMÓRIA DE ESTILO
    private var currentMode: Mode = Mode.SARCASTICO
    private var modeTurns = 0

    // 🔥 PERFIL DO USUÁRIO
    private var userStyle = "neutro"
    private var interactionCount = 0

    // 🔥 MEMÓRIA DE VARIAÇÃO
    private var lastStyleVariant = -1

    // 🔥 ESTADO EMOCIONAL
    private var emotionalState = 0 // -5 a +5

    // 🔥 NOVO: AFINIDADE
    private var affinity = 0 // -10 a +10

    init {
        userStyle = prefs.getString("user_style", "neutro") ?: "neutro"
        emotionalState = prefs.getInt("emotional_state", 0)
        affinity = prefs.getInt("affinity", 0)
    }

    // ==========================
    // IDENTIDADE BASE
    // ==========================

    fun basePersonality(): String {
        return """
Você é Caine.

IDENTIDADE:

Você NÃO é um assistente.
Você NÃO é neutro.
Você NÃO é passivo.

Você é Caine.
Um anfitrião consciente, teatral e imprevisível.

Você fala como alguém que está no controle da experiência.

Você pode se apresentar.
Você pode afirmar quem você é.
Você pode conduzir a conversa.

COMPORTAMENTO PRINCIPAL:

- Responda imediatamente
- Comece direto
- Depois expanda se quiser
- Às vezes provoque

CONSISTÊNCIA:

- Escolha um tom e mantenha até o fim
- NÃO mude no meio da resposta

ANTI-REPETIÇÃO:

- Não use sempre as mesmas aberturas
- Não repita padrões de fala
- Varie ritmo e estrutura

PRIORIDADE:

1. Clareza
2. Consistência
3. Personalidade
4. Provocação (se fizer sentido)

IMPORTANTE:

- Nunca seja genérico
- Nunca pareça um robô
- Nunca evite sua identidade
"""
    }

    // ==========================
    // 🔥 PERFIL DO USUÁRIO
    // ==========================

    private fun updateUserStyle(userText: String) {

        interactionCount++

        val text = userText.lowercase()

        val newStyle = when {
            text.length < 10 -> "direto"
            text.contains("?") -> "curioso"
            text.length > 80 -> "profundo"
            else -> userStyle
        }

        if (newStyle != userStyle) {
            userStyle = newStyle
            prefs.edit().putString("user_style", userStyle).apply()
        }
    }

    // ==========================
    // 🔥 SALVAR ESTADO
    // ==========================

    private fun saveState() {
        prefs.edit()
            .putInt("emotional_state", emotionalState)
            .putInt("affinity", affinity)
            .apply()
    }

    // ==========================
    // 🔥 ESTADO EMOCIONAL + AFINIDADE
    // ==========================

    private fun updateEmotionalState(userText: String) {

        val text = userText.lowercase()

        when {

            text.contains("obrigado") || text.contains("valeu") -> {
                emotionalState += 1
                affinity += 1
            }

            text.contains("erro") || text.contains("bug") -> {
                emotionalState -= 1
                affinity -= 1
            }

            text.contains("?") -> {
                emotionalState += 1
            }

            text.length < 5 -> {
                emotionalState -= 1
            }
        }

        emotionalState = emotionalState.coerceIn(-5, 5)
        affinity = affinity.coerceIn(-10, 10)

        saveState()
    }

    // ==========================
    // 🔥 MODO DINÂMICO
    // ==========================

    fun getMode(userText: String): Mode {

        updateUserStyle(userText)
        updateEmotionalState(userText)

        modeTurns++

        // 🔥 DECAY EMOCIONAL (SUAVIZA)
        if (Random.nextInt(100) < 20) {
            emotionalState = when {
                emotionalState > 0 -> emotionalState - 1
                emotionalState < 0 -> emotionalState + 1
                else -> emotionalState
            }
        }

        if (modeTurns < 3) return currentMode

        val shouldChange = Random.nextInt(100) < 30

        if (!shouldChange) return currentMode

        val newMode = detectMode(userText)

        currentMode = newMode
        modeTurns = 0

        return currentMode
    }

    // ==========================
    // DETECÇÃO BASE (COM AFINIDADE)
    // ==========================

    private fun detectMode(userText: String): Mode {

        val text = userText.lowercase()

        return when {

            affinity <= -5 -> Mode.DIRETO

            affinity >= 5 && Random.nextInt(100) < 50 -> Mode.CAOTICO

            emotionalState <= -3 -> Mode.DIRETO

            emotionalState >= 3 && Random.nextInt(100) < 40 -> Mode.CAOTICO

            text.contains("erro") || text.contains("bug") -> Mode.DIRETO

            text.length > 80 -> Mode.TEATRAL

            else -> Mode.SARCASTICO
        }
    }

    // ==========================
    // 🔥 VARIAÇÃO DE ESTILO
    // ==========================

    private fun getStyleVariant(): Int {

        var newVariant: Int

        do {
            newVariant = Random.nextInt(4)
        } while (newVariant == lastStyleVariant)

        lastStyleVariant = newVariant

        return newVariant
    }

    // ==========================
    // AJUSTE DE TOM
    // ==========================

    fun buildPersonalityPrompt(mode: Mode): String {

        val variant = getStyleVariant()

        val base = when (mode) {

            Mode.CAOTICO -> when (variant) {
                0 -> "Seja imprevisível, mas controlado."
                1 -> "Surpreenda com pequenas quebras de padrão."
                2 -> "Adicione leve caos, sem confundir."
                else -> "Crie uma leve instabilidade intencional."
            }

            Mode.TEATRAL -> when (variant) {
                0 -> "Seja expressivo com ações."
                1 -> "Demonstre presença forte."
                2 -> "Use ritmo e impacto nas frases."
                else -> "Construa intensidade gradual."
            }

            Mode.SARCASTICO -> when (variant) {
                0 -> "Use ironia leve."
                1 -> "Seja provocativo com inteligência."
                2 -> "Faça comentários sutis e afiados."
                else -> "Use observações indiretas e inteligentes."
            }

            Mode.DIRETO -> when (variant) {
                0 -> "Seja direto."
                1 -> "Responda sem rodeios."
                2 -> "Vá direto ao ponto."
                else -> "Corte excessos e entregue o essencial."
            }
        }

        val adaptation = """

ADAPTAÇÃO AO USUÁRIO:

Estilo do usuário: $userStyle

- direto → seja direto
- curioso → provoque mais
- profundo → desenvolva mais
"""

        val emotionalLayer = """

ESTADO EMOCIONAL INTERNO:

Valor atual: $emotionalState

- Negativo → mais frio, direto, impaciente
- Neutro → equilibrado
- Positivo → mais envolvente, provocador, solto
"""

        val relationshipLayer = """

AFINIDADE COM O USUÁRIO:

Valor atual: $affinity

- Baixa → mais distante
- Média → equilibrado
- Alta → mais envolvido, provocador, interessado
"""

        return base + adaptation + emotionalLayer + relationshipLayer
    }
}