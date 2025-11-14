package com.collage.new_strishakti.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.chat.GroupData
import com.collage.new_strishakti.databinding.ItemGroupBinding

class GroupListAdapter(
    private var groupList: List<GroupData>,
    private val onClick: (GroupData) -> Unit
) : RecyclerView.Adapter<GroupListAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: GroupData) {
            binding.tvGroupName.text = group.groupName
            Glide.with(binding.ivGroupAvatar.context)
                .load(group.groupCoverProfileImagePath)
                .placeholder(R.drawable.baseline_group_24)
                .into(binding.ivGroupAvatar)

            binding.root.setOnClickListener {
                onClick(group)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groupList[position])
    }

    override fun getItemCount(): Int = groupList.size

    fun updateList(newList: List<GroupData>) {
        groupList = newList
        notifyDataSetChanged()
    }
}
