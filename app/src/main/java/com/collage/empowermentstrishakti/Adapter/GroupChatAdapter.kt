package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Chat.GroupChatMessage
class GroupChatAdapter(
    private val myUserId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<GroupChatMessage>()

    companion object {
        private const val VIEW_LEFT = 1
        private const val VIEW_RIGHT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isMine) VIEW_RIGHT else VIEW_LEFT
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == VIEW_RIGHT) {
            val view = inflater.inflate(
                R.layout.item_group_chat_right,
                parent,
                false
            )
            RightVH(view)
        } else {
            val view = inflater.inflate(
                R.layout.item_group_chat_left,
                parent,
                false
            )
            LeftVH(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val msg = items[position]

        when (holder) {
            is LeftVH -> {
                holder.tvSender.text = msg.lastMessageSenderName
                holder.tvMessage.text = msg.content
                holder.tvTime.text = formatTime(msg.timestamp)
            }

            is RightVH -> {
                holder.tvMessage.text = msg.content
                holder.tvSender.text = msg.lastMessageSenderName
                holder.tvTime.text = formatTime(msg.timestamp)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ---------------- DATA ----------------

    fun setHistory(list: List<GroupChatMessage>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun addMessage(msg: GroupChatMessage) {
        items.add(msg)
        notifyItemInserted(items.size - 1)
    }

    fun prependItems(list: List<GroupChatMessage>) {
        prependMessages(list)
    }

    fun prependMessages(list: List<GroupChatMessage>) {
        if (list.isEmpty()) return
        items.addAll(0, list)
        notifyItemRangeInserted(0, list.size)
    }

    // ---------------- UTIL ----------------

    private fun formatTime(ts: String): String {
        return try {
            ts.substring(11, 16) // HH:mm
        } catch (e: Exception) {
            ""
        }
    }

    // ---------------- VIEW HOLDERS ----------------

    class LeftVH(v: View) : RecyclerView.ViewHolder(v) {
        val tvSender: TextView = v.findViewById(R.id.tvSender)
        val tvMessage: TextView = v.findViewById(R.id.tvMessage)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
    }

    class RightVH(v: View) : RecyclerView.ViewHolder(v) {
        val tvSender: TextView = v.findViewById(R.id.tvSender)
        val tvMessage: TextView = v.findViewById(R.id.tvMessage)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
    }
}
