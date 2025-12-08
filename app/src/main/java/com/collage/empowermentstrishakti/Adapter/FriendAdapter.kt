package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.FriendData
import com.collage.empowermentstrishakti.databinding.GroupmenteItemBinding
import com.collage.empowermentstrishakti.databinding.ItemFriendBinding

class FriendAdapter(
    private val onChecked: (FriendData, Boolean) -> Unit
) : ListAdapter<FriendData, FriendAdapter.FriendViewHolder>(DiffCallback()) {

    // Map to keep track of selected friends
    private val selectedMap = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = GroupmenteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FriendViewHolder(private val binding: GroupmenteItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: FriendData) {
            binding.tvName.text = "${friend.userFirstName} ${friend.userLastName}"
            Glide.with(binding.ivProfile.context)
                .load(friend.userProfileImagePath)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(binding.ivProfile)

            // Remove previous listener
            binding.checkbox.setOnCheckedChangeListener(null)

            // Set checked state
            binding.checkbox.isChecked = selectedMap[friend.userId] ?: false

            // Set listener
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                selectedMap[friend.userId] = isChecked
                onChecked(friend, isChecked)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FriendData>() {
        override fun areItemsTheSame(oldItem: FriendData, newItem: FriendData) =
            oldItem.userId == newItem.userId

        override fun areContentsTheSame(oldItem: FriendData, newItem: FriendData) =
            oldItem == newItem
    }

    fun getSelectedFriends(): List<FriendData> =
        currentList.filter { selectedMap[it.userId] == true }

    fun clearSelection() {
        selectedMap.clear()
        notifyDataSetChanged()
    }
}
