package com.collage.new_strishakti.Adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.PageDetail

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * PagesAdapter
 *
 * - Uses your provided layout (card view) with ids:
 *   imgCover, imgAdmin, tvPageName, tvAdminName, tvFollowers,
 *   tvDescription, tvCreatedDate, btnFollow, btnShare
 *
 * - Exposes a Callback interface for actions (item click, follow, share).
 * - Use appendItems() to add pagination results.
 */
class PagesAdapter(
    private val callback: Callback
) : ListAdapter<PageDetail, PagesAdapter.PageViewHolder>(DIFF) {

    interface Callback {
        fun onPageClicked(page: PageDetail)
        fun onFollowClicked(page: PageDetail, position: Int)
        fun onShareClicked(page: PageDetail)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PageDetail>() {
            override fun areItemsTheSame(oldItem: PageDetail, newItem: PageDetail): Boolean {
                // pagesId or puuid uniquely identify page
                return (oldItem.pagesId != 0 && oldItem.pagesId == newItem.pagesId)
                        || (!oldItem.puuid.isNullOrBlank() && oldItem.puuid == newItem.puuid)
            }

            override fun areContentsTheSame(oldItem: PageDetail, newItem: PageDetail): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_page_card, parent, false)
        return PageViewHolder(v)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = getItem(position)
        holder.bind(page, position)
    }

    /**
     * Append new items for pagination.
     * Usage:
     *  val current = adapter.currentList.toMutableList()
     *  adapter.appendItems(newItems)
     *
     * This will submit a new combined list to ListAdapter.
     */
    fun appendItems(newItems: List<PageDetail>) {
        val combined = currentList.toMutableList()
        combined.addAll(newItems)
        submitList(combined)
    }

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        private val imgAdmin: ImageView = itemView.findViewById(R.id.imgAdmin)
        private val tvPageName: TextView = itemView.findViewById(R.id.tvPageName)
        private val tvAdminName: TextView = itemView.findViewById(R.id.tvAdminName)
        private val tvFollowers: TextView = itemView.findViewById(R.id.tvFollowers)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        private val btnFollow: Button = itemView.findViewById(R.id.btnFollow)
        private val btnShare: Button = itemView.findViewById(R.id.btnShare)

        fun bind(page: PageDetail, position: Int) {
            // Text fields
            tvPageName.text = page.pageName ?: "Untitled"
            tvAdminName.text = "Created by ${page.getAdminFullName()}"
            tvDescription.text = page.pageDescription ?: ""

            // Followers text: API doesn't return followers count; adapt if available
            tvFollowers.text = if (page.isPageFollowed) "Following" else "Follow"

            // Follow button UI: reflect state
            btnFollow.text = if (page.isPageFollowed) "Unfollow" else "Follow"
            btnFollow.isSelected = page.isPageFollowed

            // Created date formatting (ISO -> "dd MMM yyyy")
            tvCreatedDate.text = try {
                page.pageCreatedAt?.let { iso ->
                    val odt = OffsetDateTime.parse(iso)
                    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                    "Created on ${odt.format(fmt)}"
                } ?: "Created on -"
            } catch (_: Exception) {
                // fallback: show substring or the raw string
                "Created on ${page.pageCreatedAt?.substringBefore('T') ?: "-"}"
            }

            // Load images (cover + admin)
            val ctx = itemView.context
            Glide.with(ctx)
                .load(page.pageCoverProfileImagePath)
                .placeholder(R.drawable.strishaktilogo)
                .centerCrop()
                .into(imgCover)

            Glide.with(ctx)
                .load(page.adminUserProfileImagePath)
                .placeholder(R.drawable.user)
                .circleCrop()
                .into(imgAdmin)

            // Click listeners
            itemView.setOnClickListener { callback.onPageClicked(page) }

            btnFollow.setOnClickListener {
                // optimistic UI toggle (update view immediately). Make actual API call from Activity/ViewModel.
                val currentlyFollowed = page.isPageFollowed
                // update local object - Note: PageDetail is data class; if you want to mutate,
                // consider copying and replacing item in adapter list (or handle state in VM).
                // For quick visual feedback:
                btnFollow.text = if (!currentlyFollowed) "Unfollow" else "Follow"
                btnFollow.isSelected = !currentlyFollowed

                callback.onFollowClicked(page, bindingAdapterPosition)
            }

            btnShare.setOnClickListener { callback.onShareClicked(page) }
        }
    }
}
