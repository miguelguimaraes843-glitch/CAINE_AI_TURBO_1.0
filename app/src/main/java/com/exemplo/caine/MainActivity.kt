package com.exemplo.caine

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.exemplo.caine.core.CaineCore
import com.exemplo.caine.core.Message
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var inputText: EditText
    private lateinit var sendButton: Button

    private val messages = mutableListOf<Message>()
    private lateinit var caineCore: CaineCore

    private var lastAutoMessageTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        caineCore = CaineCore(this)

        // 🔥 PERMISSÃO DE NOTIFICAÇÃO
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        // 🔥 RECEBER MENSAGEM DA NOTIFICAÇÃO
        intent.getStringExtra("caine_message")?.let { message ->
            addMessage(Message(message, false))
        }

        recyclerView = findViewById(R.id.recyclerView)
        inputText = findViewById(R.id.inputText)
        sendButton = findViewById(R.id.sendButton)

        adapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addMessage(Message("Caine ativo… observando 😈", false))

        sendButton.setOnClickListener {

            val text = inputText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            addMessage(Message(text, true))
            inputText.text.clear()

            caineCore.processMessage(text, messages) { response ->
                runOnUiThread {
                    addMessage(Message(response, false))
                }
            }
        }

        startAutoConversation()
        startCaineBackground()
    }

    private fun addMessage(message: Message) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun startAutoConversation() {

        Handler(Looper.getMainLooper()).postDelayed({

            if (canSendAutoMessage()) {

                lastAutoMessageTime = System.currentTimeMillis()

                caineCore.maybeStartConversation { message ->
                    runOnUiThread {
                        addMessage(Message(message, false))
                    }
                }
            }

            startAutoConversation()

        }, 15000)
    }

    private fun startCaineBackground() {

        val work = PeriodicWorkRequestBuilder<CaineWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "caine_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            work
        )
    }

    private fun canSendAutoMessage(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastAutoMessageTime) > 60000
    }
}