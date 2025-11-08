package com.collage.new_strishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.friend.SearchedUser

class SearchUserAdapter(
    private val onItemClick: (SearchedUser) -> Unit
) : ListAdapter<SearchedUser, SearchUserAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SearchedUser>() {
            override fun areItemsTheSame(oldItem: SearchedUser, newItem: SearchedUser) =
                oldItem.userUUID == newItem.userUUID
            override fun areContentsTheSame(oldItem: SearchedUser, newItem: SearchedUser) =
                oldItem == newItem
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        private val cb: CheckBox = itemView.findViewById(R.id.checkboxSelect)
        fun bind(item: SearchedUser) {
            tvName.text = item.displayName
            cb.isChecked = false
            Glide.with(ivProfile.context)
                .load(item.userProfileImagePath)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(ivProfile)
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
