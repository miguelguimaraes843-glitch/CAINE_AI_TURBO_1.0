package com.exemplo.caine.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.exemplo.caine.services.CaineAccessibilityService

object ActionManager {

    fun execute(context: Context, text: String) {

        val lower = text.lowercase()

        when {

            "youtube" in lower -> openApp(context, "com.google.android.youtube")

            "google" in lower || "pesquisar" in lower -> {
                val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=${Uri.encode(text)}"))
                context.startActivity(intent)
            }

            "scroll" in lower -> {
                CaineAccessibilityService.instance?.performSwipe(500f,1500f,500f,300f)
            }

            "voltar" in lower -> {
                CaineAccessibilityService.instance?.performGlobalBack()
            }

            "tocar" in lower -> {
                CaineAccessibilityService.instance?.performTap(500f,800f)
            }
        }
    }

    private fun openApp(context: Context, pkg: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        intent?.let { context.startActivity(it) }
    }
}