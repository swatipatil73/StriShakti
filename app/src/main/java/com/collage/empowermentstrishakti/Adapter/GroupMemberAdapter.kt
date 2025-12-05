package com.collage.empowermentstrishakti.Adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Groups.GroupMember
import de.hdodenhof.circleimageview.CircleImageView



class GroupMemberAdapter(
    private var items: MutableList<GroupMember>,
    private val listener: Listener? = null,
    private var isAdminMode: Boolean = false,
    private var currentUserId: Int = -1 // default unknown
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    interface Listener {
        fun onMemberClicked(member: GroupMember, position: Int)
        fun onRemoveClicked(member: GroupMember, position: Int)
    }

    fun updateList(newList: List<GroupMember>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position in 0 until items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /** Call this from fragment/activity to toggle admin mode without recreating adapter */
    fun setAdmin(isAdmin: Boolean, currentUserId: Int) {
        this.isAdminMode = isAdmin
        this.currentUserId = currentUserId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follower, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = items[position]
        holder.bind(member, position)
    }

    override fun getItemCount(): Int = items.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: CircleImageView = itemView.findViewById(R.id.imgFollowerProfile)
        private val tvName: TextView = itemView.findViewById(R.id.tvFollowerName)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemoveFollower)

        fun bind(member: GroupMember, position: Int) {
            val fullName = buildString {
                if (!member.userFirstName.isNullOrBlank()) append(member.userFirstName)
                if (!member.userlastName.isNullOrBlank()) {
                    if (isNotEmpty()) append(" ")
                    append(member.userlastName)
                }
            }.ifBlank { "Unknown" }

            tvName.text = fullName

            // Load profile image with Glide (fallback to placeholder)
            val ctx = itemView.context
            val imageUrl = member.userProfileImagePath
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(ctx)
                    .load(imageUrl)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.user)
            }

            // ---- NEW: show Remove only if current user is admin AND not trying to remove themself ----
            val canRemove = isAdminMode && (member.userId != currentUserId)
            btnRemove.visibility = if (canRemove) View.VISIBLE else View.GONE

            // Click listeners
            itemView.setOnClickListener {
                listener?.onMemberClicked(member, position)
            }

            btnRemove.setOnClickListener {
                listener?.onRemoveClicked(member, position)
            }
        }
    }
}
