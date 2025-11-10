package com.collage.new_strishakti.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.model.post.ReelData



class PostGridAdapter : RecyclerView.Adapter<PostGridAdapter.PostViewHolder>() {

    private val posts = mutableListOf<Reel>()

    fun submitList(list: List<Reel>) {
        posts.clear()
        posts.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_grid, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Load correct media (image or video thumbnail)
        val mediaUrl = if (post.postType.equals("video", true)) post.videoThumbnailUrl else post.postImageURl

        Glide.with(holder.itemView.context)
            .load(mediaUrl)
            .placeholder(android.R.color.darker_gray)
            .centerCrop()
            .into(holder.imgPostMedia)

        // Show video icon if post is video
        holder.imgTypeIcon.visibility =
            if (post.postType.equals("video", true)) View.VISIBLE else View.GONE
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPostMedia: ImageView = itemView.findViewById(R.id.imgPostMedia)
        val imgTypeIcon: ImageView = itemView.findViewById(R.id.imgTypeIcon)
    }
}
