package com.collage.empowermentstrishakti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.data.model.Reel.Reel


class PostGridAdapter : RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Reel>()

    fun submitList(list: List<Reel>) {
        posts.clear()
        posts.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_grid, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPostMedia: ImageView = itemView.findViewById(R.id.imgPostMedia)
        private val imgTypeIcon: ImageView = itemView.findViewById(R.id.imgTypeIcon)

        fun bind(reel: Reel) {
            // Load image or video thumbnail
            Glide.with(imgPostMedia.context)
                .load(reel.postImageURl)
                .centerCrop()
                .placeholder(R.drawable.strishaktilogo) // optional placeholder
                .into(imgPostMedia)

            // Show video icon if postType contains "video"
            imgTypeIcon.visibility = if (reel.postType?.contains("video") == true) View.VISIBLE else View.GONE
        }
    }
}