package com.collage.new_strishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.ChatFile
import com.collage.new_strishakti.data.ChatMessageResponse

class ChatMessageAdapter(
    private val items: MutableList<ChatMessageResponse>,
    private val currentUserId: Long
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        val senderId = msg.sender?.userId ?: -1L
        return if (senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        if (holder is SentVH) holder.bind(msg)
        else if (holder is ReceivedVH) holder.bind(msg)
    }

    override fun getItemCount(): Int = items.size

    fun setMessages(list: List<ChatMessageResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun addMessage(msg: ChatMessageResponse) {
        items.add(msg)
        notifyItemInserted(items.size - 1)
    }

    inner class SentVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvText: TextView = view.findViewById(R.id.tvSentText)
        private val tvTime: TextView = view.findViewById(R.id.tvSentTime)
        private val img: ImageView = view.findViewById(R.id.imgSent)

        fun bind(m: ChatMessageResponse) {
            tvText.text = m.content ?: ""
            tvTime.text = formatTime(m.timestamp)
            bindImage(m.files)
        }

        private fun bindImage(files: List<ChatFile>?) {
            if (!files.isNullOrEmpty()) {
                val url = files[0].fileUrl
                img.visibility = View.VISIBLE
                Glide.with(img.context).load(url).centerCrop().into(img)
            } else img.visibility = View.GONE
        }
    }

    inner class ReceivedVH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvText: TextView = view.findViewById(R.id.tvReceivedText)
        private val tvTime: TextView = view.findViewById(R.id.tvReceivedTime)
        private val img: ImageView = view.findViewById(R.id.imgReceived)

        fun bind(m: ChatMessageResponse) {
            tvText.text = m.content ?: ""
            tvTime.text = formatTime(m.timestamp)
            bindImage(m.files)
        }

        private fun bindImage(files: List<ChatFile>?) {
            if (!files.isNullOrEmpty()) {
                val url = files[0].fileUrl
                img.visibility = View.VISIBLE
                Glide.with(img.context).load(url).centerCrop().into(img)
            } else img.visibility = View.GONE
        }
    }

    private fun formatTime(ts: String?): String {
        // You can parse timestamp and format nicer; keep simple for now
        return ts ?: "Just now"
    }
}