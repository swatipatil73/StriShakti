package com.collage.new_strishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Event.Participant

class ParticipantsAdapter : RecyclerView.Adapter<ParticipantsAdapter.Holder>() {

    private val list = ArrayList<Participant>()

    fun submitList(newList: List<Participant>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvName)
        val img = view.findViewById<ImageView>(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return Holder(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val p = list[position]
        holder.name.text = "${p.userFirstName} ${p.userLastName}"
        Glide.with(holder.view).load(p.userProfileImagePath).into(holder.img)
    }
}
