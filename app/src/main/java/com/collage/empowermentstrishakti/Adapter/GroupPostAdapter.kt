package com.collage.empowermentstrishakti.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.InteractionManager
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionsViewModel
import com.collage.empowermentstrishakti.data.model.Groups.PostDetail
import com.collage.empowermentstrishakti.ui.RegisterViewModel.ReportBottomSheet
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GroupPostAdapter(
    private val savedIdsProvider: () -> List<String>,   // ADD THIS
    private val scope: CoroutineScope,
    private val interactionManager: InteractionManager,
    private val vm: PostActionsViewModel,
    private val onItemClick: ((PostDetail) -> Unit)? = null
) : ListAdapter<PostDetail, GroupPostAdapter.PostViewHolder>(DIFF){


    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PostDetail>() {
            override fun areItemsTheSame(oldItem: PostDetail, newItem: PostDetail): Boolean =
                oldItem.postId == newItem.postId

            override fun areContentsTheSame(oldItem: PostDetail, newItem: PostDetail): Boolean =
                oldItem == newItem
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: CircleImageView = itemView.findViewById(R.id.imgProfile)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val imgMore: ImageView = itemView.findViewById(R.id.imgMore)
        private val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        private val imgPost: ImageView = itemView.findViewById(R.id.imgPost)
        private val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        private val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val imgComment: ImageView = itemView.findViewById(R.id.imgComment)
        private val imgShare: ImageView = itemView.findViewById(R.id.imgShare)
        private val tvLikedBy: TextView = itemView.findViewById(R.id.tvLikedBy)
        private val cardRoot: CardView = itemView as CardView

        private val session = SessionManager(itemView.context)

        fun bind(item: PostDetail) {
            tvUsername.text = item.userName ?: "Unknown"
            tvCaption.text = item.description ?: item.postName ?: ""

            Glide.with(itemView.context)
                .load(item.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(imgProfile)

            if (!item.postImageURl.isNullOrBlank()) {
                imgPost.visibility = View.VISIBLE
                Glide.with(itemView.context).load(item.postImageURl).centerCrop().into(imgPost)
            } else imgPost.visibility = View.GONE

            updateLikeUi(item)

            // Optional: update saved icon if your layout has imgSaved
          //  val savedIcon: ImageView? = itemView.findViewById(R.id.imgSaved)
          //  savedIcon?.setImageResource(if (item.postSaved) R.drawable. else R.drawable.ic_unsaved)

            cardRoot.setOnClickListener { onItemClick?.invoke(item) }

            imgLike.setOnClickListener { handleLikeToggle(item) }

            imgComment.setOnClickListener {
                val ctx = itemView.context
                val intent = Intent(ctx, com.collage.empowermentstrishakti.CommentActivity::class.java)
                intent.putExtra("postId", item.postId)
                intent.putExtra("postOwnerUsername", item.userName)
                ctx.startActivity(intent)
            }

            imgShare.setOnClickListener {
                val ctx = itemView.context
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, item.postName ?: item.description ?: "")
                    type = "text/plain"
                }
                ctx.startActivity(Intent.createChooser(shareIntent, "Share post"))
                scope.launch {
                    val uid = session.getUserId() ?: return@launch
                    val token = "Bearer ${session.getToken() ?: ""}"
                    interactionManager.notifyShare(uid, item.postId, token)
                }
            }

            imgMore.setOnClickListener { anchor ->
                val ctx = anchor.context
                val popup = PopupMenu(ctx, imgMore)
                popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

                val currentUserId = session.getUserId()
                if (item.userId == currentUserId) {
                    popup.menu.findItem(R.id.action_save)?.isVisible = false
                    popup.menu.findItem(R.id.action_report)?.isVisible = false
                } else {
                    popup.menu.findItem(R.id.action_delete)?.isVisible = false
                }

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_save -> {
                            handleSaveToggle(item)
                            true
                        }
                        R.id.action_report -> {
                            val uid = session.getUserId() ?: 0
                            ReportBottomSheet.show(ctx, item.postId, uid)
                            true
                        }
                        R.id.action_delete -> {
                            val token = "Bearer ${session.getToken() ?: ""}"
                            vm.delete(item.postId, token)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        private fun updateLikeUi(item: PostDetail) {
            val liked = item.userReactStatus
            imgLike.setImageResource(if (liked) R.drawable.baseline_favorite_24_red else R.drawable.baseline_favorite_24)
            tvLikeCount.text = (item.totalCountOFReact).toString()
            tvLikedBy.text = when {
                item.totalCountOFReact == 0 -> "Be the first to like this"
                item.userReactStatus && item.totalCountOFReact == 1 -> "Liked by You"
                item.userReactStatus -> "Liked by You and ${item.totalCountOFReact - 1} others"
                else -> "Liked by ${item.totalCountOFReact} others"
            }
        }

        private fun handleLikeToggle(item: PostDetail) {
            val uid = session.getUserId() ?: return
            val token = "${session.getToken() ?: ""}"

            val prevLiked = item.userReactStatus
            val prevCount = item.totalCountOFReact

            item.userReactStatus = !prevLiked
            item.totalCountOFReact = if (item.userReactStatus) prevCount + 1 else (prevCount - 1).coerceAtLeast(0)
            updateLikeUi(item)

            scope.launch {
                val success = if (item.userReactStatus)
                    interactionManager.likePost(uid, item.postId, token)
                else
                    interactionManager.unlikePost(uid, item.postId, token)

                if (!success) {
                    item.userReactStatus = prevLiked
                    item.totalCountOFReact = prevCount
                    updateLikeUi(item)
                    Toast.makeText(itemView.context, "Failed to update like. Try again.", Toast.LENGTH_SHORT).show()
                } else {
                    val newList = currentList.toMutableList()
                    val idx = newList.indexOfFirst { it.postId == item.postId }
                    if (idx >= 0) {
                        newList[idx] = item
                        submitList(newList)
                    }
                }
            }
        }

        private fun handleSaveToggle(item: PostDetail) {
            val uid = session.getUserId() ?: return
            val token = "Bearer ${session.getToken() ?: ""}"

            val prevSaved = item.postSaved
            item.postSaved = !prevSaved
            val updated = currentList.toMutableList()
            val idx = updated.indexOfFirst { it.postId == item.postId }
            if (idx >= 0) {
                updated[idx] = item
                submitList(updated)
            }

            // update saved icon immediately if present
            //val savedIcon: ImageView? = itemView.findViewById(R.id.imgSaved)
           // savedIcon?.setImageResource(if (item.postSaved) R.drawable.ic_saved else R.drawable.ic_unsaved)

            scope.launch {
                val (success, message) = if (item.postSaved)
                    interactionManager.savePost(uid, item.postId, token)
                else
                    interactionManager.unsavePost(uid, item.postId, token)

                if (!success) {
                    item.postSaved = prevSaved
                    val rollback = currentList.toMutableList()
                    val j = rollback.indexOfFirst { it.postId == item.postId }
                    if (j >= 0) { rollback[j] = item; submitList(rollback) }
                    // restore icon
                  //  savedIcon?.setImageResource(if (item.postSaved) R.drawable.ic_saved else R.drawable.ic_unsaved)
                    Toast.makeText(itemView.context, message ?: "Save failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(v)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}
