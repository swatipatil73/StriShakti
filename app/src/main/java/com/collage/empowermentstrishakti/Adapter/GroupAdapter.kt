package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater

import android.view.ViewGroup


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.toPrettyDate
import com.collage.empowermentstrishakti.databinding.ItemGroupCardBinding

class GroupAdapter(
    private val onItemClick: (GroupDetail) -> Unit,
    private val onFollowClick: (GroupDetail) -> Unit = {},
    private val onShareClick: (GroupDetail) -> Unit = {}
) : ListAdapter<GroupDetail, GroupAdapter.GroupViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(private val binding: ItemGroupCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: GroupDetail) {

            binding.tvName.text = group.groupName.orEmpty()
            binding.tvSubtitle.text =
                group.adminFullName.takeIf { !it.isNullOrBlank() } ?: "Admin"

            binding.tvDate.text = group.groupCreatedAt.toPrettyDate()

            Glide.with(binding.imgAdmin.context)
                .load(group.adminUserProfileImagePath)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(binding.imgAdmin)

            binding.root.setOnClickListener { onItemClick(group) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GroupDetail>() {
        override fun areItemsTheSame(oldItem: GroupDetail, newItem: GroupDetail): Boolean =
            oldItem.groupUUID == newItem.groupUUID

        override fun areContentsTheSame(oldItem: GroupDetail, newItem: GroupDetail): Boolean =
            oldItem == newItem
    }
}
