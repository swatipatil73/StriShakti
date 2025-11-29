package com.collage.empowermentstrishakti



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Member
import de.hdodenhof.circleimageview.CircleImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FollowersAdapter(
    private val currentUserIsAdmin: Boolean = false,
    private val onRemoveClick: ((Member, Int) -> Unit)? = null
) : ListAdapter<Member, FollowersAdapter.FollowerVH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Member>() {
            override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class FollowerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: CircleImageView = itemView.findViewById(R.id.imgFollowerProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvFollowerName)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemoveFollower)

        fun bind(member: Member) {
            val fullName = listOfNotNull(member.userFirstName, member.userlastName).joinToString(" ").ifBlank { "User" }
            tvName.text = fullName

            Glide.with(itemView.context)
                .load(member.userProfileImagePath)
                .placeholder(R.drawable.user)
                .into(imgProfile)

            btnRemove.visibility = if (currentUserIsAdmin) View.VISIBLE else View.GONE
            btnRemove.setOnClickListener { onRemoveClick?.invoke(member, bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_follower, parent, false)
        return FollowerVH(v)
    }

    override fun onBindViewHolder(holder: FollowerVH, position: Int) {
        holder.bind(getItem(position))
    }

    // convenience to remove an item locally
    fun removeAt(position: Int) {
        val list = currentList.toMutableList()
        if (position in list.indices) {
            list.removeAt(position)
            submitList(list)
        }
    }
}
