package com.exemplo.caine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.exemplo.caine.core.CaineCore

class CaineWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        val caine = CaineCore(applicationContext)

        var messageText: String? = null

        caine.maybeStartConversation {
            messageText = it
        }

        if (!messageText.isNullOrEmpty()) {
            showNotification(messageText!!)
        }

        return Result.success()
    }

    private fun showNotification(text: String) {

        val channelId = "caine_channel"

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Caine",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // 🔥 INTENT (abrir app ao clicar)
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("caine_message", text)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Caine")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher) // ✅ CORRIGIDO
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify((0..1000).random(), notification)
    }
}