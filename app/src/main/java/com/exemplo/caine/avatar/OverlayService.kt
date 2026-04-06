package com.exemplo.caine.avatar

import android.app.Service
import android.content.Intent
import android.os.IBinder

class OverlayService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Aqui vai o personagem 3D flutuando depois

        return START_STICKY
    }
}