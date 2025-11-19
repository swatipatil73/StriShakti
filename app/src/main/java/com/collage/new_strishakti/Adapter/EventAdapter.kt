package com.collage.new_strishakti.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.EventDetailActivity
import com.collage.new_strishakti.databinding.ItemEventBinding
import com.collage.new_strishakti.data.model.Event.Event


class EventAdapter(
    private val context: Context,
    private val userId: Int? = null, // current logged-in user id
    private val onDeleteClicked: ((event: Event) -> Unit)? = null // callback for delete
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val items = ArrayList<Event>()

    fun setItems(list: List<Event>?) {
        items.clear()
        if (list != null) items.addAll(list)
        notifyDataSetChanged()
    }

    fun addItems(list: List<Event>?) {
        if (list == null || list.isEmpty()) return
        val start = items.size
        items.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }

    fun removeItemById(eventId: Int) {
        val idx = items.indexOfFirst { it.eventId == eventId }
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    fun getItemAt(pos: Int): Event? = items.getOrNull(pos)

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) = with(binding) {
            // Title / meta
            tvEventName.text = event.eventName ?: event.postName ?: "Untitled Event"
            tvDistrict.text = buildString {
                append(event.districtName ?: "")
                if (!event.categoryName.isNullOrEmpty()) {
                    append(" - ")
                    append(event.categoryName)
                }
            }
            tvStartDate.text = "Start: ${event.startDate ?: "-"}"
            tvEndDate.text = "End: ${event.endDate ?: "-"}"

            // Glide: clear previous then load with placeholder/error
            Glide.with(imgEvent)
                .clear(imgEvent)
            Glide.with(imgEvent)
                .load(event.postImageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .into(imgEvent)

            // Debug log
            android.util.Log.d(
                "EVENT_DELETE_CHECK",
                "CurrentUserID = $userId | EventHostUserID = ${event.hostUserId}"
            )

            // Show delete only if userId equals hostUserId (handle nulls safely)
            val showDelete = (userId != null) &&
                    (event.hostUserId != null) &&
                    (userId == event.hostUserId)
            btnDelete.visibility = if (showDelete) View.VISIBLE else View.GONE

            // Prevent multiple quick taps
            btnDelete.setOnClickListener {
                btnDelete.isEnabled = false
                try {
                    if (onDeleteClicked != null) {
                        onDeleteClicked.invoke(event)
                    } else {
                        Toast.makeText(context, "Delete clicked for event ${event.eventId}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    // re-enable after a short delay to avoid accidental double taps.
                    btnDelete.postDelayed({ btnDelete.isEnabled = true }, 500)
                }
            }

            // Item click -> open details (use root.context)
            root.setOnClickListener {
                val ctx = root.context
                val intent = Intent(ctx, EventDetailActivity::class.java).apply {
                    putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.eventId)

                    putExtra(EventDetailActivity.EXTRA_EVENT_USERID, event.hostUserId)
                    putExtra(EventDetailActivity.EXTRA_EVENT_UUID, event.uuid)
                    putExtra(EventDetailActivity.EXTRA_EVENT_NAME, event.eventName)
                    putExtra(EventDetailActivity.EXTRA_EVENT_IMAGE, event.postImageUrl)
                    putExtra(EventDetailActivity.EXTRA_EVENT_DESC, event.eventDescription)
                    putExtra(EventDetailActivity.EXTRA_EVENT_ADDRESS, event.eventAddress)
                    putExtra(EventDetailActivity.EXTRA_EVENT_START, event.startDate + " " + (event.startTime ?: ""))
                    putExtra(EventDetailActivity.EXTRA_EVENT_END, event.endDate + " " + (event.endTime ?: ""))
                    putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, event.categoryName)
                }
                ctx.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
