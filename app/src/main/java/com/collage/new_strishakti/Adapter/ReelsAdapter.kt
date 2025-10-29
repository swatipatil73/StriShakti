package com.collage.new_strishakti.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.post.ReelData
class ReelsAdapter(
    private val reels: List<ReelData>
) : RecyclerView.Adapter<ReelsAdapter.ReelViewHolder>() {

    private var currentPlayingPosition = -1

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumb: ImageView = itemView.findViewById(R.id.imgReelThumb)
        val title: TextView = itemView.findViewById(R.id.tvReelTitle)
        val playerView: PlayerView = itemView.findViewById(R.id.fullscreenPlayerView)
        var exoPlayer: ExoPlayer? = null
        var isMuted: Boolean = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel, parent, false)
        return ReelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        val reel = reels[position]
        val context = holder.itemView.context

        // Load thumbnail
        Glide.with(context)
            .load(reel.videoThumbnailUrl ?: reel.postImageURl)
            .placeholder(R.drawable.imageplacehoder)
            .into(holder.imgThumb)

        holder.title.text = reel.postName ?: "Reel"

        // Keep thumbnail visible initially
        holder.imgThumb.visibility = View.VISIBLE
        holder.playerView.visibility = View.INVISIBLE

        if (holder.exoPlayer == null) {
            holder.exoPlayer = ExoPlayer.Builder(context).build().apply {
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
            }
            holder.playerView.player = holder.exoPlayer
        }

        val videoUrl = reel.postImageURl ?: return
        val mediaItem = MediaItem.fromUri(videoUrl)
        holder.exoPlayer?.setMediaItem(mediaItem)
        holder.exoPlayer?.prepare()
        holder.exoPlayer?.playWhenReady = (position == currentPlayingPosition)

        holder.exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && position == currentPlayingPosition) {
                    holder.imgThumb.visibility = View.GONE
                    holder.playerView.visibility = View.VISIBLE
                }
            }
        })

        holder.playerView.setOnClickListener {
            holder.isMuted = !holder.isMuted
            holder.exoPlayer?.volume = if (holder.isMuted) 0f else 1f
        }
    }

    override fun getItemCount() = reels.size

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        holder.exoPlayer?.release()
        holder.exoPlayer = null
    }

    fun playVideoAt(recyclerView: RecyclerView, position: Int) {
        if (position == currentPlayingPosition) return

        // Pause previous
        if (currentPlayingPosition != -1) {
            val oldHolder = recyclerView.findViewHolderForAdapterPosition(currentPlayingPosition) as? ReelViewHolder
            oldHolder?.exoPlayer?.playWhenReady = false
        }

        currentPlayingPosition = position
        val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
        holder?.exoPlayer?.playWhenReady = true
    }
}
