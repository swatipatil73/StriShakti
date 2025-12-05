package com.collage.empowermentstrishakti.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.CommentActivity
import com.collage.empowermentstrishakti.Common.InteractionManager
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PostDetail
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionsViewModel
import com.collage.empowermentstrishakti.ui.RegisterViewModel.ReportBottomSheet

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// optimistic local state maps (postId -> state)


class PagePostAdapter(


    private val scope: CoroutineScope,
    private val interactionManager: InteractionManager,
    private val vm: PostActionsViewModel,
    private val onItemClick: ((PostDetail) -> Unit)? = null
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

    private val likedState = mutableMapOf<Int, Boolean>()        // override for liked (true = liked)
    private val savedState = mutableMapOf<Int, Boolean>()        // override for saved (true = saved)
    private val likeCountOverrides = mutableMapOf<Int, Int>()   // override for like counts

    inner class PostViewHolder(itemView: View,  private val vm: PostActionsViewModel) : RecyclerView.ViewHolder(itemView) {


        private val imgProfile: de.hdodenhof.circleimageview.CircleImageView = itemView.findViewById(R.id.imgProfile)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val imgMore: ImageView = itemView.findViewById(R.id.imgMore)
        private val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        private val videoPost: androidx.media3.ui.PlayerView = itemView.findViewById(R.id.videoPost)
        private val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        private val imgShare: ImageView = itemView.findViewById(R.id.imgShare)
        private val tvLikedBy: TextView = itemView.findViewById(R.id.tvLikedBy)
        private val cardRoot: CardView = itemView as CardView


        private val sessionManager = SessionManager(itemView.context)

        fun bind(item: PostDetail) {
            tvUsername.text = item.userName ?: "Unknown"

            Glide.with(itemView.context)
                .load(item.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .into(imgProfile)

            tvCaption.text = item.description ?: item.postName ?: ""

            // Determine effective liked/saved/count using overrides if present
            val effectiveLiked = likedState[item.postId] ?: item.userReactStatus
            val effectiveSaved = savedState[item.postId] ?: item.postSaved
            val effectiveCount = likeCountOverrides[item.postId] ?: item.totalCountOFReact

            updateLikeUi(effectiveLiked, effectiveCount)

            // media
            val imageUrl = item.postImageURl
            val isVideo = item.postType?.lowercase()?.contains("video") == true
            if (!imageUrl.isNullOrBlank() && !isVideo) {
                imgPost.visibility = View.VISIBLE
                videoPost.visibility = View.GONE
                Glide.with(itemView.context).load(imageUrl).centerCrop().into(imgPost)
            } else if (isVideo && !imageUrl.isNullOrBlank()) {
                imgPost.visibility = View.GONE
                videoPost.visibility = View.VISIBLE
                // optional: init ExoPlayer if desired
            } else {
                imgPost.visibility = View.GONE
                videoPost.visibility = View.GONE
            }

            // clicks
            cardRoot.setOnClickListener { onItemClick?.invoke(item) }

            imgLike.setOnClickListener { handleLikeClick(item, bindingAdapterPosition) }
            imgComment.setOnClickListener {
                val intent = Intent(itemView.context, CommentActivity::class.java)
                intent.putExtra("postId", item.postId)
                intent.putExtra("postOwnerUsername", item.userName)
                itemView.context.startActivity(intent)
            }
            imgShare.setOnClickListener { handleShare(item) }

            imgMore.setOnClickListener { view ->
                val context = view.context
                val popupMenu = android.widget.PopupMenu(context, imgMore)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

                val currentUserId = sessionManager.getUserId()
                if (item.userId == currentUserId) {
                    popupMenu.menu.findItem(R.id.action_save)?.isVisible = false
                    popupMenu.menu.findItem(R.id.action_report)?.isVisible = false
                } else {
                    popupMenu.menu.findItem(R.id.action_delete)?.isVisible = false
                }

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_save -> {
                            handleSaveClick(item, bindingAdapterPosition)
                            true
                        }
                        R.id.action_report -> {
                            ReportBottomSheet.show(context, item.postId, currentUserId)
                            true
                        }
                        R.id.action_delete -> {
                            val token = "Bearer ${SessionManager(context).getToken() ?: ""}"
                            vm.delete(item.postId, token)   // ✅ call ViewModel (PostActionsViewModel)

                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

        // Update UI given explicit liked state and like count
        private fun updateLikeUi(liked: Boolean, likeCount: Int) {
            imgLike.setImageResource(if (liked) R.drawable.baseline_favorite_24_red else R.drawable.baseline_favorite_24)
            tvLikeCount.text = likeCount.toString()
            tvLikedBy.text = when {
                likeCount == 0 -> "Be the first to like this"
                liked && likeCount == 1 -> "Liked by You"
                liked -> "Liked by You and ${likeCount - 1} others"
                else -> "Liked by $likeCount others"
            }
        }

        private fun handleLikeClick(item: PostDetail, position: Int) {
            if (position == RecyclerView.NO_POSITION) return

            val userIdStr = sessionManager.getUserId() ?: return
            val token = "${sessionManager.getToken() ?: ""}"

            // original server values
            val serverLiked = item.userReactStatus
            val serverCount = item.totalCountOFReact

            // current effective from overrides or server
            val currentlyLiked = likedState[item.postId] ?: serverLiked
            val currentlyCount = likeCountOverrides[item.postId] ?: serverCount

            // compute optimistic new state
            val newLiked = !currentlyLiked
            val newCount = if (newLiked) currentlyCount + 1 else (currentlyCount - 1).coerceAtLeast(0)

            // apply optimistic overrides & update UI
            likedState[item.postId] = newLiked
            likeCountOverrides[item.postId] = newCount
            updateLikeUi(newLiked, newCount)

            // call server via interaction manager
            scope.launch {
                val ok = if (newLiked) {
                    interactionManager.likePost(userIdStr, item.postId, token)
                } else {
                    interactionManager.unlikePost(userIdStr, item.postId, token)
                }

                if (!ok) {
                    // rollback overrides to previous effective values
                    if (serverLiked == currentlyLiked) {
                        // no server/user difference previously -> remove override
                        likedState.remove(item.postId)
                    } else {
                        // restore to server's value
                        likedState[item.postId] = serverLiked
                    }

                    if (serverCount == currentlyCount) {
                        likeCountOverrides.remove(item.postId)
                    } else {
                        likeCountOverrides[item.postId] = serverCount
                    }

                    // notify user and refresh UI
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        updateLikeUi(likedState[item.postId] ?: serverLiked, likeCountOverrides[item.postId] ?: serverCount)
                        Toast.makeText(itemView.context, "Failed to update like. Try again.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // server accepted: update the concrete PostDetail in the adapter list
                    val updated = item.copy(
                        userReactStatus = newLiked,
                        totalCountOFReact = newCount
                    )
                    updateItemAt(position, updated)

                    // clean up overrides (server is now source-of-truth)
                    likedState.remove(item.postId)
                    likeCountOverrides.remove(item.postId)
                }
            }
        }

        private fun handleSaveClick(item: PostDetail, position: Int) {
            if (position == RecyclerView.NO_POSITION) return
            val userId = sessionManager.getUserId() ?: return
            val token = "Bearer ${sessionManager.getToken() ?: ""}"

            val serverSaved = item.postSaved
            val currentlySaved = savedState[item.postId] ?: serverSaved
            val newSaved = !currentlySaved

            // optimistic update
            savedState[item.postId] = newSaved
            updateLikeUi(savedState[item.postId] ?: serverSaved, likeCountOverrides[item.postId] ?: item.totalCountOFReact)
            // update UI of save icon if you have one (imgMore menu only here; if you have save icon, update it similarly)

            scope.launch {
                val (success, message) = if (newSaved) {
                    interactionManager.savePost(userId, item.postId, token)
                } else {
                    interactionManager.unsavePost(userId, item.postId, token)
                }

                if (!success) {
                    // rollback
                    if (serverSaved == currentlySaved) savedState.remove(item.postId) else savedState[item.postId] = serverSaved
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        updateLikeUi(savedState[item.postId] ?: serverSaved, likeCountOverrides[item.postId] ?: item.totalCountOFReact)
                        Toast.makeText(itemView.context, message ?: "Save failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // update underlying model with server-confirmed saved flag
                    val updated = item.copy(postSaved = newSaved)
                    updateItemAt(position, updated)
                    savedState.remove(item.postId)
                }
            }
        }

        private fun handleShare(item: PostDetail) {
            val context = itemView.context
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, item.postName ?: item.description ?: "")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share post"))

            val userIdStr = sessionManager.getUserId() ?: return
            val token = "Bearer ${sessionManager.getToken() ?: ""}"
            scope.launch {
                interactionManager.notifyShare(userIdStr, item.postId, token)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(v, vm) // <-- ps
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateItemAt(position: Int, updated: PostDetail) {
        val list = currentList.toMutableList()
        if (position >= 0 && position < list.size) {
            list[position] = updated
            submitList(list)
        }
    }
}
