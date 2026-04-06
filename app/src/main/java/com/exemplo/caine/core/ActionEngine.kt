package com.exemplo.caine.core

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Vibrator
import android.os.VibrationEffect

class ActionEngine(private val context: Context) {

    fun execute(action: String, data: String? = null) {

        when (action) {

            "abrir_app" -> openApp(data)

            "volume_up" -> adjustVolume(true)

            "volume_down" -> adjustVolume(false)

            "vibrar" -> vibrate()

            "abrir_camera" -> openCamera()

        }
    }

    // ==========================
    // 📱 AÇÕES
    // ==========================

    private fun openApp(packageName: String?) {

        if (packageName == null) return

        val intent = context.packageManager.getLaunchIntentForPackage(packageName)

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    private fun openCamera() {

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun adjustVolume(up: Boolean) {

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val direction = if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER

        audioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI)
    }

    private fun vibrate() {

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(300)
        }
    }
}