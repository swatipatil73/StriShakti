package com.collage.new_strishakti.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Reel.Reel
class ReelFullScreenAdapter(
    private val onLikeClick: (Reel) -> Unit,
    private val onCommentClick: (Reel) -> Unit,
    private val onShareClick: (Reel) -> Unit,
    private val onSaveClick: (Reel) -> Unit
) : PagingDataAdapter<Reel, ReelFullScreenAdapter.FullScreenViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Reel>() {
            override fun areItemsTheSame(oldItem: Reel, newItem: Reel) =
                oldItem.postId == newItem.postId

            override fun areContentsTheSame(oldItem: Reel, newItem: Reel) =
                oldItem == newItem
        }
    }

    inner class FullScreenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvViews: TextView = itemView.findViewById(R.id.tvViews)
        val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val ivComment: ImageView = itemView.findViewById(R.id.ivComment)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val ivShare: ImageView = itemView.findViewById(R.id.ivShare)
        val tvShareCount: TextView = itemView.findViewById(R.id.tvShareCount)
        val ivSave: ImageView = itemView.findViewById(R.id.ivSave)

        private var player: ExoPlayer? = null

        fun bind(reel: Reel) {
            // User Info
            tvUserName.text = reel.userName
            Glide.with(itemView.context)
                .load(reel.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .into(ivProfile)

            // Video Info
            tvDescription.text = reel.description ?: ""
            tvViews.text = "${reel.totalViews} views"
            tvLikeCount.text = "${reel.totalCountOFReact}"
            tvCommentCount.text = "${reel.totalComments}"
            tvShareCount.text = "0" // Can bind actual share count if available

            // Button click listeners
            ivLike.setOnClickListener { onLikeClick(reel) }
            ivComment.setOnClickListener { onCommentClick(reel) }
            ivShare.setOnClickListener { onShareClick(reel) }
            ivSave.setOnClickListener { onSaveClick(reel) }

            // Release previous player
            player?.release()

            // Initialize Media3 ExoPlayer
            player = ExoPlayer.Builder(itemView.context).build()
            playerView.player = player
            val mediaItem = MediaItem.fromUri(reel.postImageURl ?: "")
            player?.setMediaItem(mediaItem)

            player?.prepare()
            player?.playWhenReady = true
        }

        fun pausePlayer() {
            player?.pause()
        }

        fun playPlayer() {
            player?.play()
        }

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
        val reel = getItem(position)
        reel?.let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: FullScreenViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }
}

