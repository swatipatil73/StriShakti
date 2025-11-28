package com.collage.empowermentstrishakti.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.friend.FriendRequestItem
import de.hdodenhof.circleimageview.CircleImageView

class FriendRequestAdapter(
    private val items: MutableList<FriendRequestItem>,
    private val listener: Listener
) : RecyclerView.Adapter<FriendRequestAdapter.VH>() {

    interface Listener {
        fun onConfirm(item: FriendRequestItem, pos: Int)
        fun onDelete(item: FriendRequestItem, pos: Int)
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivUser: CircleImageView = v.findViewById(R.id.imgAvatar)
        val tvFullName: TextView = v.findViewById(R.id.tvFullName)
        val btnConfirm: Button = v.findViewById(R.id.btnConfirm)
        val btnDelete: Button = v.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.tvFullName.text = "${item.userFirstName} ${item.userLastName}".trim()

        val url = item.userProfileImagePath?.trim()
        if (url.isNullOrEmpty() || url == "/" || url.equals("null", true)) {
            Glide.with(h.itemView)
                .load(R.drawable.user) // put a placeholder in res/drawable
                .circleCrop()
                .into(h.ivUser)
        } else {
            Glide.with(h.itemView)
                .load(url)
                .placeholder(R.drawable.user)
                .error(R.drawable.strishaktilogo)
                .circleCrop()
                .into(h.ivUser)
        }

        h.btnConfirm.setOnClickListener { listener.onConfirm(item, h.bindingAdapterPosition) }
        h.btnDelete.setOnClickListener { listener.onDelete(item, h.bindingAdapterPosition) }
    }

    override fun getItemCount() = items.size

    fun setData(newItems: List<FriendRequestItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position in 0 until items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
