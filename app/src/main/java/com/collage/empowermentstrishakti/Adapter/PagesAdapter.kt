package com.collage.empowermentstrishakti.Adapter



import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.PageDetail

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
    private val callback: Callback,
    private val currentUserId: Int  // <-- ADD THIS
) : ListAdapter<PageDetail, PagesAdapter.PageViewHolder>(DIFF) {

    interface Callback {
        fun onPageClicked(page: PageDetail)
        fun onFollowClicked(page: PageDetail, position: Int)
        fun onShareClicked(page: PageDetail)
        fun onDeleteClicked(page: PageDetail, position: Int) // new
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


    fun appendItems(newItems: List<PageDetail>) {
        val combined = currentList.toMutableList()
        combined.addAll(newItems)
        submitList(combined)
    }

    fun updateItemAt(position: Int, item: PageDetail) {
        val list = currentList.toMutableList()
        if (position in list.indices) {
            list[position] = item
            submitList(list)
        }
    }

    fun getItemAt(position: Int): PageDetail? {
        return currentList.getOrNull(position)
    }
    // Add helper to remove / insert item (for optimistic delete / revert)
    fun removeItemAt(position: Int) {
        val list = currentList.toMutableList()
        if (position in list.indices) {
            list.removeAt(position)
            submitList(list)
        }
    }

    fun insertItemAt(position: Int, item: PageDetail) {
        val list = currentList.toMutableList()
        val insertPos = position.coerceIn(0, list.size)
        list.add(insertPos, item)
        submitList(list)
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
        private val btnDelete: ImageView? = itemView.findViewById(R.id.btnDelete)

        fun bind(page: PageDetail, position: Int) {
            // Text fields
            tvPageName.text = page.pageName ?: "Untitled"
            tvAdminName.text = "Created by ${page.getAdminFullName()}"
            tvDescription.text = page.pageDescription ?: ""

            // Followers text: show following/follow
            tvFollowers.text = if (page.isPageFollowed) "Following" else "Follow"

            // Owner check: show delete icon instead of followers
            val isOwner = page.adminId == currentUserId

            if (isOwner) {
                tvFollowers.visibility = View.GONE
                btnDelete?.visibility = View.VISIBLE
            } else {
                tvFollowers.visibility = View.VISIBLE
                btnDelete?.visibility = View.GONE
            }


            // Follow button UI (colors + text)
            val isFollowed = page.isPageFollowed
            btnFollow.text = if (isFollowed) "Unfollow" else "Follow"
            val bgColor = if (isFollowed) {
                ContextCompat.getColor(itemView.context, R.color.page_unfollow_red)
            } else {
                ContextCompat.getColor(itemView.context, R.color.page_follow_blue)
            }
            btnFollow.backgroundTintList = ColorStateList.valueOf(bgColor)
            btnFollow.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            btnFollow.isSelected = isFollowed

            // Created date formatting
            tvCreatedDate.text = try {
                page.pageCreatedAt?.let { iso ->
                    val odt = OffsetDateTime.parse(iso)
                    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                    "Created on ${odt.format(fmt)}"
                } ?: "Created on -"
            } catch (_: Exception) {
                "Created on ${page.pageCreatedAt?.substringBefore('T') ?: "-"}"
            }

            // Load images
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

            // Delete click (only visible for owner)
            btnDelete?.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                callback.onDeleteClicked(page, pos)
            }

            // Follow click -> Activity handles confirmation + API
            btnFollow.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                callback.onFollowClicked(page, pos)
            }

            // Share click
            btnShare.setOnClickListener { callback.onShareClicked(page) }
        }
    }
}
