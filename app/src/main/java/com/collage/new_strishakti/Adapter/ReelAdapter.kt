package com.collage.new_strishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Reel.Reel




class ReelAdapter(


    private val onItemClick: (Reel) -> Unit
) : PagingDataAdapter<Reel, ReelAdapter.ReelViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reel>() {
            override fun areItemsTheSame(oldItem: Reel, newItem: Reel): Boolean =
                oldItem.postId == newItem.postId

            override fun areContentsTheSame(oldItem: Reel, newItem: Reel): Boolean =
                oldItem == newItem
        }
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val playIcon: ImageView = itemView.findViewById(R.id.ivPlay)

        fun bind(reel: Reel) {
            Glide.with(itemView.context)
                .load(reel.videoThumbnailUrl ?: reel.postImageURl)
                .placeholder(R.drawable.strishaktilogo)
                .into(thumbnail)

            itemView.setOnClickListener {
                onItemClick(reel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel_grid, parent, false)
        return ReelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        val reel = getItem(position)
        reel?.let { holder.bind(it) }
    }
}