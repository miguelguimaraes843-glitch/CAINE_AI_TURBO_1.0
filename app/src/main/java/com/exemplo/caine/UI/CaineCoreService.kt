package com.exemplo.caine.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.exemplo.caine.core.MemoryManager

class CaineCoreService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Thread {
            while (true) {
                Thread.sleep(5000)

                val last = MemoryManager.longTermMemory.lastOrNull()
                Log.d("CAINE", "Pensando sobre: $last")
            }
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}