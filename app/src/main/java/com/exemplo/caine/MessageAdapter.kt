package com.exemplo.caine

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.exemplo.caine.core.Message

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.messageText)
        val container: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val message = messages[position]
        holder.text.text = message.text

        val params = holder.text.layoutParams as LinearLayout.LayoutParams

        if (message.isUser) {
            // 👉 Mensagem do usuário (direita)
            holder.container.gravity = Gravity.END
            holder.text.setBackgroundResource(R.drawable.bg_message_user)
            holder.text.setTextColor(0xFFFFFFFF.toInt())
        } else {
            // 👉 Mensagem do Caine (esquerda)
            holder.container.gravity = Gravity.START
            holder.text.setBackgroundResource(R.drawable.bg_message_ai)
            holder.text.setTextColor(0xFF000000.toInt())
        }

        holder.text.layoutParams = params
    }

    override fun getItemCount() = messages.size
}