package com.collage.empowermentstrishakti.Adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.post.AdPost
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class AllAdsAdapter(
    private val onClick: ((AdPost) -> Unit)? = null
) : ListAdapter<AdPost, AllAdsAdapter.AllAdsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAdsViewHolder {
        // If your layout file is named "allitem_ad.xml", use R.layout.allitem_ad
        // If it's "item_ad.xml", replace below with R.layout.item_ad
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.allitem_ad, parent, false)
        return AllAdsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllAdsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AllAdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.adImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.adTitle)
        // optional date TextView if present in your layout:
        // private val tvDate: TextView = itemView.findViewById(R.id.adDate)

        fun bind(item: AdPost) {
            tvTitle.text = if (item.postName.isNullOrBlank()) "Advertisement" else item.postName

            // If you want to show date, uncomment and format:
            // tvDate.text = item.postCreatedAt?.let { formatDate(it) } ?: ""

            if (!item.postImageUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(item.postImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.imageplacehoder)
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.strishaktilogo)
            }

            itemView.setOnClickListener { onClick?.invoke(item) }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val odt = OffsetDateTime.parse(dateString)
                val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                odt.format(fmt)
            } catch (e: Exception) {
                dateString
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AdPost>() {
        override fun areItemsTheSame(oldItem: AdPost, newItem: AdPost): Boolean =
            oldItem.postId == newItem.postId

        override fun areContentsTheSame(oldItem: AdPost, newItem: AdPost): Boolean =
            oldItem == newItem
    }
}
