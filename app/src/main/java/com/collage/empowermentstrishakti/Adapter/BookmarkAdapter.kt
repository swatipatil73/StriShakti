package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.SavedPost.SavedPostData

class BookmarkAdapter(
    private val items: MutableList<SavedPostData> = mutableListOf(),
    private val listener: Listener
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkVH>() {

    interface Listener {
        fun onItemClick(item: SavedPostData, position: Int)
    }

    fun setItems(newItems: List<SavedPostData>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItems(newItems: List<SavedPostData>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun removeByPostId(postId: Long) {
        val idx = items.indexOfFirst { it.postId == postId }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_bookeritem, parent, false)
        return BookmarkVH(v)
    }

    override fun onBindViewHolder(holder: BookmarkVH, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class BookmarkVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePost: ImageView = itemView.findViewById(R.id.imagePost)
        private val textPostBox: TextView = itemView.findViewById(R.id.textPostBox)
        private val iconVideo: ImageView = itemView.findViewById(R.id.iconVideo)
        private val card: CardView? = itemView as? CardView

        fun bind(item: SavedPostData, pos: Int) {
            // Decide type: video, image, or text
            val isVideo = item.postType?.contains("video", ignoreCase = true) == true
            val isImage = item.postType?.contains("image", ignoreCase = true) == true
            val isText = (item.postType == null || item.postImageUrl == "emptyPostImageUrl") && (item.description != null || (item.postName?.isNotBlank() == true))

            // Reset visibilities
            iconVideo.visibility = View.GONE
            textPostBox.visibility = View.GONE
            imagePost.visibility = View.VISIBLE

            when {
                isVideo -> {
                    // show thumbnail if available, else placeholder
                    val thumb = item.videoThumbnailUrl ?: item.postImageUrl
                    Glide.with(imagePost.context)
                        .load(thumb)
                        .centerCrop()
                        .into(imagePost)
                    iconVideo.visibility = View.VISIBLE
                }
                isImage -> {
                    Glide.with(imagePost.context)
                        .load(item.postImageUrl)
                        .centerCrop()
                        .into(imagePost)
                }
                isText -> {
                    // show gradient overlay + text
                    imagePost.setImageResource(R.drawable.ic_text_post_placeholder) // provide placeholder icon
                    textPostBox.visibility = View.VISIBLE
                    textPostBox.text = item.postName ?: item.description ?: "Text Post"
                }
                else -> {
                    // fallback to image url or placeholder
                    Glide.with(imagePost.context)
                        .load(item.postImageUrl)
                        .centerCrop()
                        .into(imagePost)
                }
            }

            itemView.setOnClickListener {
                listener.onItemClick(item, pos)
            }
        }
    }
}