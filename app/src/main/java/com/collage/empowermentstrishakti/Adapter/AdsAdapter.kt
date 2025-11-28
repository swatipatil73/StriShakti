package com.collage.empowermentstrishakti.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R


import com.collage.empowermentstrishakti.data.model.post.AdPost

class AdsAdapter(private val adsList: List<AdPost>) :
    RecyclerView.Adapter<AdsAdapter.AdsViewHolder>() {

    inner class AdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adImageView: ImageView = itemView.findViewById(R.id.adImage)
        val adTitle: TextView = itemView.findViewById(R.id.adTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ad, parent, false)
        return AdsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdsViewHolder, position: Int) {
        val ad = adsList[position]
        Glide.with(holder.itemView.context)
            .load(ad.postImageUrl)
            .into(holder.adImageView)

        holder.adTitle.text = ad.postName ?: ""
    }

    override fun getItemCount() = adsList.size
}
