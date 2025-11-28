package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.FriendData
import com.collage.empowermentstrishakti.databinding.ItemFriendBinding
class FriendListAdapter(
    private var friendList: List<FriendData>,
    private val onClick: (FriendData) -> Unit
) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: FriendData) {
            binding.tvName.text = "${friend.userFirstName} ${friend.userLastName}"
            Glide.with(binding.ivAvatar.context)
                .load(friend.userProfileImagePath)
                .placeholder(R.drawable.user)
                .into(binding.ivAvatar)

            binding.root.setOnClickListener {
                onClick(friend) // Pass FriendData with UUID
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friendList[position])
    }

    override fun getItemCount(): Int = friendList.size

    fun updateList(newList: List<FriendData>) {
        friendList = newList
        notifyDataSetChanged()
    }
}

