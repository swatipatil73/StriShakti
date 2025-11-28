package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Reel.Reel

class ReelAdapter(
    private val onItemClick: (Reel) -> Unit
) : PagingDataAdapter<Reel, ReelAdapter.ReelViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reel>() {
            override fun areItemsTheSame(old: Reel, new: Reel) = old.postId == new.postId
            override fun areContentsTheSame(old: Reel, new: Reel) = old == new
        }
        // height/width aspect ratios → gives the “masonry” effect
        private val ASPECTS = listOf(1f, 1.33f, 1.25f, 1.78f, 1.1f, 1.25f, 1.6f)
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val playIcon: ImageView = itemView.findViewById(R.id.ivPlay)

        fun bind(reel: Reel, position: Int) {
            Glide.with(itemView.context)
                .load(reel.videoThumbnailUrl ?: R.drawable.strishaktilogo)
                .placeholder(R.drawable.strishaktilogo)
                .into(thumbnail)

            // --- Masonry sizing (NO full-span) ---
            // Make sure we never set isFullSpan = true
            (itemView.layoutParams as? StaggeredGridLayoutManager.LayoutParams)?.let { lp ->
                lp.isFullSpan = false
                itemView.layoutParams = lp
            }

            val screenW = itemView.resources.displayMetrics.widthPixels
            val columns = 3
            val spanW = screenW / columns

            val aspect = ASPECTS[position % ASPECTS.size] // height = width * aspect
            val targetH = (spanW * aspect).toInt()

            thumbnail.layoutParams = thumbnail.layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = targetH.coerceAtLeast(screenW / 4) // small floor to avoid tiny tiles
            }

            itemView.setOnClickListener { onItemClick(reel) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel_grid, parent, false)
        return ReelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, position) }
    }
}
