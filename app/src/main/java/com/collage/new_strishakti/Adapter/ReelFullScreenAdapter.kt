package com.example.app.reels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Reel.Reel
class ReelFullScreenAdapter(
    private val onLikeClick: (Reel, Int) -> Unit,
    private val onCommentClick: (Reel) -> Unit,
    private val onShareClick: (Reel) -> Unit,
    private val onDeleteClick: (Reel) -> Unit
) : PagingDataAdapter<Reel, ReelFullScreenAdapter.FullScreenViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reel>() {
            override fun areItemsTheSame(oldItem: Reel, newItem: Reel): Boolean =
                oldItem.postId == newItem.postId

            override fun areContentsTheSame(oldItem: Reel, newItem: Reel): Boolean =
                oldItem == newItem
        }

        // 🔊 Global mute state shared by all reels
        var isGlobalMuted: Boolean = true
    }

    inner class FullScreenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        private val ivSoundToggle: ImageView = itemView.findViewById(R.id.ivSoundToggle)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvViews: TextView = itemView.findViewById(R.id.tvViews)
        private val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val ivComment: ImageView = itemView.findViewById(R.id.ivComment)
        private val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        private val ivShare: ImageView = itemView.findViewById(R.id.ivShare)
        private val tvShareCount: TextView = itemView.findViewById(R.id.tvShareCount)
        private val ivDelete: ImageView = itemView.findViewById(R.id.deleet)

        private var player: ExoPlayer? = null


        fun bind(reel: Reel) {
            // --- User Info ---
            tvUserName.text = reel.userName ?: "Unknown"
            Glide.with(itemView.context)
                .load(reel.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .into(ivProfile)

            // --- Video Info ---
            tvDescription.text = reel.description ?: ""
            tvViews.text = "${reel.totalViews ?: 0} views"
            tvLikeCount.text = (reel.totalCountOFReact ?: 0).toString()
            tvCommentCount.text = (reel.totalComments ?: 0).toString()
            tvShareCount.text = "0"

            // --- Like Icon Update ---
            val isLiked = reel.userReactStatus == true
            ivLike.setImageResource(
                if (isLiked) R.drawable.baseline_favorite_24_red
                else R.drawable.baseline_favorite_24
            )

            // --- Click Listeners ---
            ivLike.setOnClickListener { onLikeClick(reel, bindingAdapterPosition) }
            ivComment.setOnClickListener { onCommentClick(reel) }
            ivShare.setOnClickListener { onShareClick(reel) }
            ivDelete.setOnClickListener { onDeleteClick(reel) }


            // --- ExoPlayer Setup ---
            player?.release()
            player = ExoPlayer.Builder(itemView.context).build()
            playerView.player = player

            val videoUrl = reel.postImageURl ?: ""
            if (videoUrl.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(videoUrl)
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.playWhenReady = true
            }

            // --- Apply global mute state ---
            applyMuteState(ReelFullScreenAdapter.isGlobalMuted)

            // --- Toggle sound on icon click ---
            ivSoundToggle.setOnClickListener {
                ReelFullScreenAdapter.isGlobalMuted = !ReelFullScreenAdapter.isGlobalMuted
                notifyGlobalMuteChange()
            }

            // --- Optional: tap video to toggle sound ---
            playerView.setOnClickListener {
                ReelFullScreenAdapter.isGlobalMuted = !ReelFullScreenAdapter.isGlobalMuted
                notifyGlobalMuteChange()
            }
        }

        private fun applyMuteState(mute: Boolean) {
            player?.volume = if (mute) 0f else 1f
            ivSoundToggle.setImageResource(
                if (mute) R.drawable.baseline_volume_off_24
                else R.drawable.outline_volume_up_24
            )
        }

        // 🔔 Notify all visible reels to update their sound state
        private fun notifyGlobalMuteChange() {
            val recyclerView = itemView.parent as? RecyclerView ?: return
            for (i in 0 until recyclerView.childCount) {
                val holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
                if (holder is FullScreenViewHolder) {
                    holder.applyMuteState(ReelFullScreenAdapter.isGlobalMuted)
                }
            }
        }

        fun pausePlayer() = player?.pause()
        fun playPlayer() = player?.play()
        fun releasePlayer() {
            player?.release()
            player = null
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullScreenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel_fullscreen, parent, false)
        return FullScreenViewHolder(view)
    }

    override fun onBindViewHolder(holder: FullScreenViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onBindViewHolder(
        holder: FullScreenViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val updatedReel = payloads[0] as Reel
            holder.bind(updatedReel)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewRecycled(holder: FullScreenViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    fun updateReelAt(position: Int, updatedReel: Reel) {
        if (position in 0 until itemCount) {
            notifyItemChanged(position, updatedReel)
        }
    }


}
