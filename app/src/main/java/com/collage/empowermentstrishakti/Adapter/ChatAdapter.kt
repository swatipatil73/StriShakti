package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Chat.ChatMessage
import com.collage.empowermentstrishakti.data.model.Chat.ChatUiItem

class ChatAdapter(
    private val myUserId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    private val items = mutableListOf<ChatUiItem>()

    // ---------- PUBLIC METHODS ----------

    fun setHistory(list: List<ChatUiItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun addMessage(msg: ChatMessage) {

        val uiItem = ChatUiItem.MessageItem(
            id = 0, // live socket message → no DB id yet
            message = msg.content ?: "",
            time = "", // optional (can add later)
            senderId = msg.sender?.userId ?: 0
        )

        items.add(uiItem)
        notifyItemInserted(items.size - 1)
    }


    // ---------- ADAPTER OVERRIDES ----------

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatUiItem.DateHeader -> TYPE_DATE
            is ChatUiItem.MessageItem ->
                if (item.senderId == myUserId) TYPE_SENT else TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_DATE -> {
                val v = inflater.inflate(R.layout.item_chat_date, parent, false)
                DateHolder(v)
            }

            TYPE_SENT -> {
                val v = inflater.inflate(R.layout.item_message_sent, parent, false)
                SentHolder(v)
            }

            else -> {
                val v = inflater.inflate(R.layout.item_message_received, parent, false)
                ReceivedHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = items[position]) {

            is ChatUiItem.DateHeader -> {
                (holder as DateHolder).tvDate.text = item.title
            }

            is ChatUiItem.MessageItem -> {
                if (holder is SentHolder) {
                    holder.tv.text = item.message
                } else if (holder is ReceivedHolder) {
                    holder.tv.text = item.message
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ---------- VIEW HOLDERS ----------

    class SentHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv: TextView = v.findViewById(R.id.tvMessage)
    }

    class ReceivedHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv: TextView = v.findViewById(R.id.tvMessage)
    }

    class DateHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate: TextView = v.findViewById(R.id.tvDate)
    }
}
