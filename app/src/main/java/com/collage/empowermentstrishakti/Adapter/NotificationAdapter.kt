package com.collage.empowermentstrishakti.Adapter



import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Notification.NotificationItem
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class NotificationAdapter(
    private var items: MutableList<NotificationItem> = mutableListOf(),
    private val listener: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.Holder>() {

    fun setItems(new: List<NotificationItem>) {
        items = new.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(private val v: View) : RecyclerView.ViewHolder(v) {
        // cache the child views
        private val tvMessage: TextView = v.findViewById(R.id.tvMessage)
        private val tvTime: TextView = v.findViewById(R.id.tvTime)
        private val ivAvatar: ImageView = v.findViewById(R.id.ivAvatar)
        private val ivChevron: ImageView = v.findViewById(R.id.ivChevron)

        fun bind(item: NotificationItem) {
            tvMessage.text = item.notificationMessage ?: "No message"

            // parse ISO date string to relative time (fallback: show raw)
            val created = item.notificationCreatedAt
            tvTime.text = try {
                if (!created.isNullOrBlank()) {
                    val odt = OffsetDateTime.parse(created, DateTimeFormatter.ISO_DATE_TIME)
                    val millis = odt.toInstant().toEpochMilli()
                    DateUtils.getRelativeTimeSpanString(millis).toString()
                } else ""
            } catch (e: DateTimeParseException) {
                created ?: ""
            }

            // Default avatar/icon - you can change to user's avatar if available
            Glide.with(v.context)
                .load(R.drawable.bell) // placeholder default
                .into(ivAvatar)

            v.setOnClickListener {
                listener(item)
            }
        }
    }
}
