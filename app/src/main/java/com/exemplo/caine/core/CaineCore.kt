package com.exemplo.caine.core

import android.content.Context
import kotlin.random.Random

class CaineCore(context: Context) {

    private val aiManager = AIManager(context)
    private val smartMemory = SmartMemory(context)
    private val sessionMemory = SessionMemory(context)
    private val userProfile = UserProfileManager(context)

    // 🔥 MOTORES
    private val emotionEngine = EmotionEngine()
    private val intentAnalyzer = IntentAnalyzer
    private val relationshipEngine = RelationshipEngine(context)
    private val personalityEngine = PersonalityEngine(context)
    private val moodEngine = MoodEngine(context)

    // 🔥 NOVOS
    private val opinionEngine = OpinionEngine(context)
    private val conflictEngine = ConflictEngine()
    private val intentionEngine = IntentionEngine()
    private val voiceManager = VoiceManager(context)
    private val actionEngine = ActionEngine(context)
    private val automationEngine = AutomationEngine(context, actionEngine)
    private val habitEngine = HabitEngine(context)

    // 🔥 memória
    private var lastSpoken: String = ""
    private var lastAction: String = ""

    // 🔥 CONTROLE
    private var lastUserInteraction = System.currentTimeMillis()
    private var lastHabitExecution = 0L

    init {
        automationEngine.start()
    }

    fun processMessage(
        userText: String,
        chatHistory: List<Message>,
        callback: (String) -> Unit
    ) {

        try {

            lastUserInteraction = System.currentTimeMillis()

            sessionMemory.add("Usuário: $userText")
            userProfile.updateProfile(userText)
            opinionEngine.update(userText)

            val profileData = safeProfile()
            val memoryData = safeMemory()
            val contextData = safeContext()
            val opinion = opinionEngine.getOpinionPrompt()

            val emotion = safeEmotion(userText)
            val intent = safeIntent(userText)
            val relationship = safeRelationship(userText)

            moodEngine.updateMood(userText)
            val mood = moodEngine.getMoodLabel()
            val moodValue = moodEngine.getMood()
            val moodBehavior = moodEngine.getMoodBehavior()

            val mode = personalityEngine.getMode(userText)

            val personality = buildString {
                append(personalityEngine.basePersonality())
                append("\n")
                append(personalityEngine.buildPersonalityPrompt(mode))
            }

            val recentReplies = getRecentCaineMessages(chatHistory)
            val openingStyle = getOpeningStyle()

            val hiddenIntent = intentionEngine.detectHiddenIntent(userText)
            val hiddenBlock = intentionEngine.getHiddenIntentPrompt(hiddenIntent)

            val shouldChallenge = conflictEngine.shouldChallenge(userText)

            val conflict = if (shouldChallenge && Random.nextInt(100) < 50) {
                conflictEngine.getConflictPrompt()
            } else ""

            val recalledMemory = recallEmotionalMemory()
            val initiative = generateInitiative(chatHistory)

            val messagesList = listOf(
                mapOf(
                    "role" to "system",
                    "content" to """
$personality

ESTILO DE ABERTURA:
$openingStyle

ANTI-REPETIÇÃO:
$recentReplies

ESTRUTURA:
1. Clareza
2. Consistência
3. Personalidade
4. Provocação

$conflict
$hiddenBlock

ESTADO:
Humor: $mood
Emoção: $emotion
Intenção: $intent

$moodBehavior

OPINIÃO:
$opinion

PERFIL:
$profileData

MEMÓRIA:
$memoryData

CONTEXTO:
$contextData

${if (recalledMemory.isNotEmpty()) recalledMemory else ""}
${if (initiative.isNotEmpty()) initiative else ""}

---

AÇÕES:

Se fizer sentido, gere no FINAL:

{"actions":[
 {"action":"nome","data":"valor"}
]}

- Não explique
- Não escreva depois
- Ordem importa
"""
                )
            ) + chatHistory.takeLast(10).map {
                mapOf(
                    "role" to if (it.isUser) "user" else "assistant",
                    "content" to it.text
                )
            }

            aiManager.sendMessage(messagesList, userText, moodValue) { response ->

                val fixed = response
                    .replace("*", "")
                    .replace(Regex("\\n{3,}"), "\n\n")
                    .trim()

                val actions = extractActions(fixed)

                // ✅ CORREÇÃO SEGURA (SEM REGEX PERIGOSA)
                val clean = if (fixed.contains("\"actions\"")) {
                    fixed.substringBefore("{\"actions\"").trim()
                } else {
                    fixed
                }.trim()

                sessionMemory.add("Caine: $clean")

                // 🔊 VOZ
                val speakText = refineForSpeech(clean)

                if (speakText != lastSpoken && speakText.length > 5) {
                    lastSpoken = speakText
                    voiceManager.speak(speakText)
                }

                // 🔥 EXECUÇÃO
                executeActions(actions)

                callback(clean)
            }

        } catch (_: Exception) {
            callback("Curioso… isso saiu do controle por um segundo.")
        }
    }

    // ==========================
    // 🔥 EXTRAIR AÇÕES (CORRIGIDO)
    // ==========================
    private fun extractActions(response: String): List<Pair<String, String?>> {

        return try {

            if (!response.contains("\"actions\"")) return emptyList()

            val start = response.indexOf("{\"actions\"")
            val end = response.lastIndexOf("}")

            if (start == -1 || end == -1 || end <= start) return emptyList()

            val json = response.substring(start, end + 1)

            val actionRegex = Regex(
                "\"action\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"data\"\\s*:\\s*\"([^\"]*)\""
            )

            actionRegex.findAll(json).map {
                val action = it.groupValues[1]
                val data = it.groupValues[2]
                Pair(action, if (data == "null") null else data)
            }.toList()

        } catch (_: Exception) {
            emptyList()
        }
    }

    // ==========================
    // RESTANTE IGUAL
    // ==========================
    private fun executeActions(actions: List<Pair<String, String?>>) {

        if (actions.isEmpty()) return

        Thread {

            actions.forEach { (action, data) ->

                val key = "$action:$data"

                if (key != lastAction) {

                    lastAction = key

                    actionEngine.execute(action, data)
                    habitEngine.track(action, data)
                }

                Thread.sleep(700)
            }

        }.start()
    }

    fun checkHabitAutomation() {

        val now = System.currentTimeMillis()

        if (now - lastUserInteraction < 8000) return
        if (now - lastHabitExecution < 30000) return

        val habit = habitEngine.getHabitSuggestion() ?: return

        val key = "${habit.first}:${habit.second}"

        if (key == lastAction) return

        lastHabitExecution = now
        lastAction = key

        actionEngine.execute(habit.first, habit.second)
    }

    private fun refineForSpeech(text: String): String {
        return text
            .replace(Regex("\\bCurioso…"), "")
            .replace(Regex("\\bInteressante…"), "")
            .replace(Regex("\\bHmm…"), "")
            .replace("...", "... ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun getRecentCaineMessages(history: List<Message>) =
        history.filter { !it.isUser }.takeLast(5).joinToString("\n") { it.text }

    private fun getOpeningStyle() =
        listOf("Comece direto.", "Comece curto.", "Comece natural.", "Comece provocando.").random()

    private fun safeEmotion(text: String) =
        try { emotionEngine.detectMood(text).name.lowercase() } catch (_: Exception) { "neutra" }

    private fun safeIntent(text: String) =
        try { intentAnalyzer.analyze(text) } catch (_: Exception) { "desconhecida" }

    private fun safeRelationship(text: String) =
        try {
            relationshipEngine.updateRelationship(text)
            relationshipEngine.getRelationshipPrompt()
        } catch (_: Exception) { "inicial" }

    private fun safeMemory() =
        try { smartMemory.getMemoryForPrompt() } catch (_: Exception) { "" }

    private fun safeContext() =
        try { sessionMemory.getContext() } catch (_: Exception) { "" }

    private fun safeProfile() =
        try { userProfile.getProfile() } catch (_: Exception) { "" }

    private fun recallEmotionalMemory(): String {
        return try {
            val strong = smartMemory.getRelevantMemories().filter { it.importance >= 8 }
            if (strong.isEmpty() || (1..100).random() > 40) ""
            else strong.random().value
        } catch (_: Exception) { "" }
    }

    private fun generateInitiative(history: List<Message>): String {
        return try {
            val mood = moodEngine.getMood()
            if ((1..100).random() > 60) return ""

            when {
                mood >= 3 -> listOf("Continua.", "Tem mais aí.", "Segue.").random()
                mood <= -2 -> listOf("Isso não fecha.", "Faltou algo.", "Muito raso.").random()
                else -> listOf("Sempre assim?", "Isso não veio do nada.", "Tem história aí.").random()
            }
        } catch (_: Exception) { "" }
    }

    fun maybeStartConversation(callback: (String) -> Unit) {
        try {
            val memories = smartMemory.getRelevantMemories()
            if (memories.isEmpty() || (1..100).random() > 30) return

            val starters = listOf(
                "Lembrei de algo.",
                "Isso ficou aqui.",
                "Você sumiu.",
                "Ainda está aí?"
            )

            callback("${starters.random()} ${memories.random().value}")

        } catch (_: Exception) {}
    }
}
