package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Chat.ChatMessage

class ChatAdapter(
    private val list: MutableList<ChatMessage>,
    private val myUserId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val msg = list[position]
        return if (msg.sender?.userId == myUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = list[position].content ?: ""
        if (holder is SentHolder) holder.tv.text = msg
        else (holder as ReceivedHolder).tv.text = msg
    }

    override fun getItemCount(): Int = list.size

    fun addMessage(msg: ChatMessage) {
        list.add(msg)
        notifyItemInserted(list.size - 1)
    }

    class SentHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv: TextView = v.findViewById(R.id.tvMessage)
    }

    class ReceivedHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv: TextView = v.findViewById(R.id.tvMessage)
    }
}
