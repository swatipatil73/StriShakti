package com.collage.new_strishakti.Adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.post.*
import de.hdodenhof.circleimageview.CircleImageView

class HomeFeedAdapter(
    private val sharedReelPlayer: ExoPlayer
) : ListAdapter<HomeFeedItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_REEL = 1
        private const val TYPE_AD = 2
        private const val TYPE_ANNOUNCEMENT = 3
        private const val TYPE_LOADING = 99

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HomeFeedItem>() {
            override fun areItemsTheSame(oldItem: HomeFeedItem, newItem: HomeFeedItem) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: HomeFeedItem, newItem: HomeFeedItem) =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeFeedItem.PostItem -> TYPE_POST
        is HomeFeedItem.ReelSection -> TYPE_REEL
        is HomeFeedItem.AdItem -> TYPE_AD
        is HomeFeedItem.AnnouncementItem -> TYPE_ANNOUNCEMENT
        is HomeFeedItem.LoadingItem -> TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_POST -> PostViewHolder(inflater.inflate(R.layout.item_post, parent, false))
            TYPE_REEL -> ReelsViewHolder(
                inflater.inflate(
                    R.layout.item_reels_section,
                    parent,
                    false


                )
            )

            TYPE_AD -> AdViewHolder(inflater.inflate(R.layout.item_ad, parent, false))
            TYPE_ANNOUNCEMENT -> AnnouncementViewHolder(
                inflater.inflate(
                    R.layout.dialog_announcement,
                    parent,
                    false
                )
            )

            TYPE_LOADING -> LoadingViewHolder(inflater.inflate(R.layout.item_loading, parent, false))
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeFeedItem.PostItem -> (holder as PostViewHolder).bind(item.post)
            is HomeFeedItem.ReelSection -> (holder as ReelsViewHolder).bind(item.reels)
            is HomeFeedItem.AdItem -> (holder as AdViewHolder).bind(item.ad)
            is HomeFeedItem.AnnouncementItem -> (holder as AnnouncementViewHolder).bind(item.announcement)
            is HomeFeedItem.LoadingItem -> {} // no binding needed
        }
    }

    // ---------------- POST VIEW HOLDER ----------------
    class PostViewHolder(
        itemView: View,
        private val sharedPlayer: ExoPlayer? = null
    ) : RecyclerView.ViewHolder(itemView) {

        private val imgProfile: CircleImageView = itemView.findViewById(R.id.imgProfile)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        private val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val tvLikedBy: TextView = itemView.findViewById(R.id.tvLikedBy)
        private val layoutSingleComment: LinearLayout =
            itemView.findViewById(R.id.layoutSingleComment)
        private val imgCommentUser: CircleImageView = itemView.findViewById(R.id.imgCommentUser)
        private val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
        private val tvViewAllComments: TextView = itemView.findViewById(R.id.tvViewAllComments)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        private val videoPost: PlayerView = itemView.findViewById(R.id.videoPost)

        private var player: ExoPlayer? = null
        private var mediaUrl: String? = null

        fun bind(post: PostData) {
            // --- Profile & Caption ---
            Glide.with(itemView.context)
                .load(post.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(imgProfile)

            tvUsername.text = post.userName ?: "Unknown User"
            tvCaption.text = post.postName ?: ""

            // --- Likes ---
            tvLikeCount.text = post.totalCountOFReact.toString()
            tvLikedBy.text =
                if (post.userReactStatus) "Liked by you and others" else "Liked by others"

            imgLike.setImageResource(
                if (post.isLikedByUser == 1)
                    R.drawable.baseline_favorite_24_red
                else
                    R.drawable.baseline_favorite_24
            )

            // --- Comments ---
            val comments = post.commentsAndReacts
            if (!comments.isNullOrEmpty()) {
                layoutSingleComment.visibility = View.VISIBLE
                val lastComment = comments.last()
                tvCommentText.text = lastComment.commentText ?: "Nice post!"
                tvViewAllComments.visibility =
                    if (comments.size > 1) View.VISIBLE else View.GONE
                tvViewAllComments.text = "View all ${comments.size} comments"
            } else {
                layoutSingleComment.visibility = View.GONE
                tvViewAllComments.visibility = View.GONE
            }

            // --- Media ---
            // --- Media ---
            if (post.mediaFiles.isNotEmpty()) {
                val media = post.mediaFiles[0]
                mediaUrl = media.mediaUrl

                when (media.mediaType) {

                    // ---------------- IMAGE ----------------
                    "image" -> {
                        videoPost.visibility = View.GONE
                        imgPost.visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(mediaUrl)
                            .placeholder(ColorDrawable(Color.LTGRAY))
                            .into(imgPost)
                    }

                    // ---------------- VIDEO ----------------
                    "video" -> {
                        videoPost.visibility = View.VISIBLE
                        imgPost.visibility = View.VISIBLE  // keep showing thumbnail till first frame

                        // Load thumbnail first
                        if (!media.thumbnailUrl.isNullOrEmpty()) {
                            Glide.with(itemView.context)
                                .load(media.thumbnailUrl)
                                .placeholder(ColorDrawable(Color.LTGRAY))
                                .into(imgPost)
                        } else {
                            imgPost.setImageDrawable(ColorDrawable(Color.LTGRAY))
                        }

                        // Prepare player
                        player?.release()
                        player = sharedPlayer ?: ExoPlayer.Builder(itemView.context).build().apply {
                            repeatMode = Player.REPEAT_MODE_ONE
                            playWhenReady = false
                        }

                        videoPost.player = player

                        // ✅ Prevent black background while preparing
                        videoPost.setShutterBackgroundColor(Color.TRANSPARENT)
                        videoPost.useController = true

                        mediaUrl?.let {
                            player?.setMediaItem(MediaItem.fromUri(it))
                            player?.prepare()
                        }

                        // ✅ Fade out thumbnail *only after* first frame rendered
                        player?.addListener(object : Player.Listener {
                            override fun onRenderedFirstFrame() {
                                imgPost.animate()
                                    .alpha(0f)
                                    .setDuration(250)
                                    .withEndAction {
                                        imgPost.visibility = View.GONE
                                        imgPost.alpha = 1f
                                    }
                                    .start()
                            }

                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_BUFFERING) {
                                    // show thumbnail again while buffering
                                    if (imgPost.visibility == View.GONE) {
                                        imgPost.alpha = 1f
                                        imgPost.visibility = View.VISIBLE
                                    }
                                }
                            }
                        })

                        // ✅ Tap to play/pause
                        videoPost.setOnClickListener {
                            player?.let { exo ->
                                if (exo.isPlaying) {
                                    exo.pause()
                                    imgPost.alpha = 1f
                                    imgPost.visibility = View.VISIBLE
                                } else {
                                    exo.play()
                                }
                            }
                        }
                    }

                }
            }

        }

        fun releasePlayer() {
            if (sharedPlayer == null) {
                player?.release()
            }
            player = null
        }

        fun pausePlayer() {
            player?.pause()
            imgPost.visibility = View.VISIBLE
        }

        fun playIfVisible() {
            if (mediaUrl != null && videoPost.visibility == View.VISIBLE) {
                player?.play()
                imgPost.visibility = View.GONE
            }
        }
    }

// ---------------- REELS SECTION ----------------
class ReelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val recyclerReels: RecyclerView = itemView.findViewById(R.id.recyclerReels)
    private val title: TextView? = itemView.findViewById(R.id.tvReelSectionTitle)

    fun bind(reels: List<ReelData>) {
        title?.text = "Suggested Reels"
        val adapter = ReelsAdapter(reels)
        recyclerReels.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerReels.adapter = adapter
        recyclerReels.post {
            recyclerReels.findViewHolderForAdapterPosition(0)?.itemView?.post {
                adapter.playVideoAt(recyclerReels, 0)
            }
        }
    }
}

    // ---------------- AD SECTION ----------------
    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val adImage: ImageView = itemView.findViewById(R.id.adImage)
        private val adTitle: TextView = itemView.findViewById(R.id.adTitle)

        fun bind(ad: AdPost) {
            val imageUrl = ad.videoThumbnailUrl ?: ad.postImageUrl
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.imageplacehoder)
                .into(adImage)

            val titleText = ad.advertisementDescription ?: ad.postName
            if (!titleText.isNullOrEmpty()) {
                adTitle.text = titleText
                adTitle.visibility = View.VISIBLE
            } else adTitle.visibility = View.GONE
        }
    }

    // ---------------- ANNOUNCEMENT SECTION ----------------
    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(announcement: Announcement) {
            val imageView = itemView.findViewById<ImageView>(R.id.announcementImage)
            Glide.with(itemView.context)
                .load(announcement.postImageUrl)
                .into(imageView)
        }
    }

    // ---------------- LOADING SECTION ----------------
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarItem)
    }

    // ---------------- Helper Methods ----------------
    fun showLoading() {
        val current = currentList.toMutableList()
        if (current.none { it is HomeFeedItem.LoadingItem }) {
            current.add(HomeFeedItem.LoadingItem)
            submitList(current)
        }
    }


    fun hideLoading() {
        val current = currentList.toMutableList()
        val removed = current.removeAll { it is HomeFeedItem.LoadingItem }
        if (removed) submitList(current)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is PostViewHolder) holder.releasePlayer()
    }
}