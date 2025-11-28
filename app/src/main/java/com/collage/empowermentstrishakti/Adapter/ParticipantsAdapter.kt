package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Event.Participant

// File: ParticipantsAdapter.kt (replace current adapter)
class ParticipantsAdapter(
    private val hostUserId: Int,
    private val currentUserId: Int,
    private val onActionClick: (participant: Participant) -> Unit
) : RecyclerView.Adapter<ParticipantsAdapter.Holder>() {

    private val list = ArrayList<Participant>()

    fun submitList(newList: List<Participant>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvName)
        val img = view.findViewById<ImageView>(R.id.imgProfile)
        val imgAction = view.findViewById<ImageView>(R.id.imgAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return Holder(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val p = list[position]
        holder.name.text = "${p.userFirstName ?: ""} ${p.userLastName ?: ""}".trim()
        Glide.with(holder.view).load(p.userProfileImagePath).into(holder.img)

        // Decide when to show action icon:
        // - If current viewer is host, show delete icon for participants except host themself
        // - If current viewer is NOT host, hide the action here (Exit is done from main button)
        if (currentUserId == hostUserId) {
            val isSelf = (p.userId == hostUserId)
            holder.imgAction.visibility = if (isSelf) View.GONE else View.VISIBLE
            holder.imgAction.setImageResource(R.drawable.baseline_delete_24)
            holder.imgAction.contentDescription = "Remove participant"
            holder.imgAction.setOnClickListener {
                onActionClick(p)
            }
        } else {
            holder.imgAction.visibility = View.GONE
        }
    }
}

