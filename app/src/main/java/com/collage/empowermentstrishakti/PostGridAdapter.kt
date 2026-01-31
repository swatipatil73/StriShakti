package com.collage.empowermentstrishakti

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.data.model.Profile.VideoPopupFragment
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
            // Load thumbnail
            Glide.with(imgPostMedia.context)
                .load(reel.videoThumbnailUrl ?: reel.postImageURl)
                .centerCrop()
                .placeholder(R.drawable.strishaktilogo)
                .into(imgPostMedia)

            // Show video icon if it's a video
            imgTypeIcon.visibility = if (reel.postType?.contains("video") == true) View.VISIBLE else View.GONE

            // Click to open video popup
            itemView.setOnClickListener {
                if (reel.postType?.contains("video") == true && !reel.postImageURl.isNullOrEmpty()) {
                    val fragmentManager = (itemView.context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
                    fragmentManager?.let { fm ->
                        val videoPopup = VideoPopupFragment.newInstance(reel.postImageURl!!)
                        videoPopup.show(fm, "video_popup")
                    }
                }
            }

        }
    }

}