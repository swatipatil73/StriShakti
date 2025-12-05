package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Groups.PostDetail
import de.hdodenhof.circleimageview.CircleImageView

class GroupPostAdapter(
    private val list: List<PostDetail>
) : RecyclerView.Adapter<GroupPostAdapter.PostHolder>() {

    inner class PostHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imgProfile: CircleImageView = view.findViewById(R.id.imgProfile)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvCaption: TextView = view.findViewById(R.id.tvCaption)
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostHolder(view)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val item = list[position]

        holder.tvUsername.text = item.userName ?: ""
        holder.tvCaption.text = item.postName ?: ""

        Glide.with(holder.itemView.context)
            .load(item.userProfileImageUrl)
            .placeholder(R.drawable.user)
            .into(holder.imgProfile)

        holder.imgPost.visibility = View.VISIBLE
        Glide.with(holder.itemView.context)
            .load(item.postImageURl)
            .into(holder.imgPost)
    }

    override fun getItemCount(): Int = list.size
}
