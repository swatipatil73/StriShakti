package com.collage.empowermentstrishakti.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Comment.CommentModel
import de.hdodenhof.circleimageview.CircleImageView

// CommentAdapter.kt
class CommentAdapter(
    private val context: Context,
    private var comments: MutableList<CommentModel>,
    private val listener: CommentClickListener,
    private val postOwnerUsername: String,   //
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PARENT = 1
        private const val VIEW_TYPE_CHILD = 2
    }

    override fun getItemViewType(position: Int): Int =
        if (comments[position].parentCommentId == 0) VIEW_TYPE_PARENT else VIEW_TYPE_CHILD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_PARENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_comment_parent, parent, false)
            ParentViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_comment_child, parent, false)
            ChildViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val comment = comments[position]
        if (holder is ParentViewHolder) {
            holder.bind(comment)
        } else if (holder is ChildViewHolder) {
            holder.bind(comment)
        }
    }

    override fun getItemCount(): Int = comments.size

    fun updateList(newList: List<CommentModel>) {
        comments = newList.toMutableList()
        notifyDataSetChanged()
    }

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUser: CircleImageView = itemView.findViewById(R.id.imgUser)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvReply: TextView = itemView.findViewById(R.id.tvReply)
        private val imgDelete: ImageView = itemView.findViewById(R.id.imgDelete)

        fun bind(comment: CommentModel) {
            tvUserName.text = comment.userName
            tvComment.text = comment.comment
            tvTime.text = comment.commentTime
            Glide.with(context).load(comment.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(imgUser)
            val session = SessionManager(context)
            val loggedInUsername = session.getUserName()


            // ✅ check both conditions (comment owner OR post owner)
            if (comment.userName?.trim().equals(loggedInUsername?.trim(), true) ||
                loggedInUsername?.trim().equals(postOwnerUsername?.trim(), true)
            ) {
                imgDelete.visibility = View.VISIBLE
            } else {
                imgDelete.visibility = View.GONE
            }


            tvReply.setOnClickListener {
                listener.onReplyClicked(comment.childCommentId)
            }

            imgDelete.setOnClickListener {
                listener.onDeleteClicked(comment)
            }
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUser: CircleImageView = itemView.findViewById(R.id.imgChildUser)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvChildUserName)
        private val tvComment: TextView = itemView.findViewById(R.id.tvChildComment)
        private val tvTime: TextView = itemView.findViewById(R.id.tvChildTime)

        fun bind(comment: CommentModel) {
            tvUserName.text = comment.userName
            tvComment.text = comment.comment
            tvTime.text = comment.commentTime
            Glide.with(context).load(comment.userProfileImageUrl)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(imgUser)
        }
    }

    interface CommentClickListener {
        fun onReplyClicked(commentId: Int)
        fun onDeleteClicked(comment: CommentModel)
    }

}
