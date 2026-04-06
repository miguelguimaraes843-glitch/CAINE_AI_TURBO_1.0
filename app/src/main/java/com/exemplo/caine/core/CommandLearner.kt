package com.exemplo.caine.core

object CommandLearner {

    fun tryLearn(text: String): Boolean {

        if (text.contains("quando eu disser")) {

            val parts = text.split("faça")

            if (parts.size == 2) {
                val trigger = parts[0].replace("quando eu disser", "").trim()
                val action = parts[1].trim()

                MemoryManager.learn(trigger, action)
                return true
            }
        }

        return false
    }
}