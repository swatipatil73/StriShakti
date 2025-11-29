package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PostDetail
import de.hdodenhof.circleimageview.CircleImageView
import androidx.cardview.widget.CardView

class PagePostAdapter(
    private val onItemClick: ((PostDetail) -> Unit)? = null,
    private val onLikeClick: ((PostDetail, Int) -> Unit)? = null,
    private val onCommentClick: ((PostDetail, Int) -> Unit)? = null,
    private val onShareClick: ((PostDetail, Int) -> Unit)? = null,
    private val onMoreClick: ((PostDetail, Int, View) -> Unit)? = null
) : ListAdapter<PostDetail, PagePostAdapter.PostViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PostDetail>() {
            override fun areItemsTheSame(oldItem: PostDetail, newItem: PostDetail): Boolean {
                return (oldItem.postId != 0 && newItem.postId != 0 && oldItem.postId == newItem.postId)
                        || (!oldItem.userUUID.isNullOrBlank() && !newItem.userUUID.isNullOrBlank()
                        && oldItem.userUUID == newItem.userUUID
                        && oldItem.postCreatedAt == newItem.postCreatedAt)
            }

            override fun areContentsTheSame(oldItem: PostDetail, newItem: PostDetail): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: CircleImageView = itemView.findViewById(R.id.imgProfile)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val imgMore: ImageView = itemView.findViewById(R.id.imgMore)
        private val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        private val videoPost: PlayerView = itemView.findViewById(R.id.videoPost)
        private val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        private val imgShare: ImageView = itemView.findViewById(R.id.imgShare)
        private val tvLikedBy: TextView = itemView.findViewById(R.id.tvLikedBy)
        private val cardRoot: CardView = itemView as CardView

        fun bind(item: PostDetail) {
            // header
            tvUsername.text = item.userName ?: "Unknown"

            // profile image
            Glide.with(itemView.context)
                .load(item.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .into(imgProfile)

            // caption/description
            tvCaption.text = item.description ?: item.postName ?: ""

            // like/comment counts
            tvLikeCount.text = (item.totalCountOFReact).toString()
            tvLikedBy.text = when {
                item.totalCountOFReact > 0 -> "Liked by ${item.totalCountOFReact} people"
                else -> ""
            }

            // media: image vs video
            val imageUrl = item.postImageURl
            val isVideo = item.postType?.lowercase()?.contains("video") == true

            if (!imageUrl.isNullOrBlank() && !isVideo) {
                imgPost.visibility = View.VISIBLE
                videoPost.visibility = View.GONE
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .centerCrop()
                    .into(imgPost)
            } else if (isVideo && !imageUrl.isNullOrBlank()) {
                // show video container and initialize player if you want
                imgPost.visibility = View.GONE
                videoPost.visibility = View.VISIBLE
                // TODO: initialize ExoPlayer here and play the video thumbnail/URL
                // bindVideo(item.postImageURl, videoPost)
            } else {
                imgPost.visibility = View.GONE
                videoPost.visibility = View.GONE
            }

            // click listeners
            cardRoot.setOnClickListener { onItemClick?.invoke(item) }
            imgLike.setOnClickListener { onLikeClick?.invoke(item, bindingAdapterPosition) }
            imgComment.setOnClickListener { onCommentClick?.invoke(item, bindingAdapterPosition) }
            imgShare.setOnClickListener { onShareClick?.invoke(item, bindingAdapterPosition) }
            imgMore.setOnClickListener { onMoreClick?.invoke(item, bindingAdapterPosition, imgMore) }
        }

        // Optional: helper to initialize ExoPlayer for video playback
        // Implement this if you want inline video playback.
        private fun bindVideo(url: String?, playerView: PlayerView) {
            // Example stub:
            // val player = ExoPlayer.Builder(itemView.context).build()
            // playerView.player = player
            // val mediaItem = MediaItem.fromUri(url!!)
            // player.setMediaItem(mediaItem)
            // player.prepare()
            // player.playWhenReady = false // or true if you want auto-play
            //
            // Remember to release player when ViewHolder is recycled.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(v)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // convenience: update a single item (e.g., after like/unlike)
    fun updateItemAt(position: Int, updated: PostDetail) {
        val list = currentList.toMutableList()
        if (position >= 0 && position < list.size) {
            list[position] = updated
            submitList(list)
        }
    }
}
