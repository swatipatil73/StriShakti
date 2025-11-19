package com.collage.new_strishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Event.DiscussionItem

class DiscussionAdapter : RecyclerView.Adapter<DiscussionAdapter.VH>() {

    private val items = ArrayList<DiscussionItem>()

    fun submitList(list: List<DiscussionItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_discussion, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val tvDesc: TextView = view.findViewById(R.id.tvDesc)

        fun bind(item: DiscussionItem) {
            tvName.text = item.userName ?: "Unknown"
            tvTime.text = item.postUploadedAt ?: ""
            tvDesc.text = item.description ?: ""
            val url = item.userProfileImageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(url)
                    .circleCrop()
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.user) // fallback drawable
            }

            // Optional: click listeners
            itemView.setOnClickListener {
                // open post / profile etc.
            }
        }
    }
}