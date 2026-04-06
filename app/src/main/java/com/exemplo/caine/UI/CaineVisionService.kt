package com.exemplo.caine.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.exemplo.caine.core.ActionManager

class CaineVisionService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return

        val textos = mutableListOf<String>()
        extract(root, textos)

        val tela = textos.joinToString(" ").lowercase()

        if ("erro" in tela) {
            ActionManager.execute(this, "voltar")
        }
    }

    override fun onInterrupt() {}

    private fun extract(node: AccessibilityNodeInfo, list: MutableList<String>) {
        node.text?.let { list.add(it.toString()) }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { extract(it, list) }
        }
    }
}