package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.toPrettyDate
import com.collage.empowermentstrishakti.databinding.ItemGroupCardBinding

class GroupAdapter(
    private val onItemClick: (GroupDetail) -> Unit,
    private val onDeleteClick: (GroupDetail) -> Unit,
    private val onFollowClick: (GroupDetail) -> Unit,
    private val onShareClick: (GroupDetail) -> Unit
) : ListAdapter<GroupDetail, GroupAdapter.GroupViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GroupDetail>() {
            override fun areItemsTheSame(oldItem: GroupDetail, newItem: GroupDetail) =
                oldItem.groupId == newItem.groupId

            override fun areContentsTheSame(oldItem: GroupDetail, newItem: GroupDetail) =
                oldItem == newItem
        }
    }

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
            val loggedInUserId = SessionManager(binding.root.context).getUserId().toString()
            val isAdmin = group.adminId == loggedInUserId.toInt()

            binding.imgDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
            binding.imgDelete.setOnClickListener { onDeleteClick(group) }

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
}
