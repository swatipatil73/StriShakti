package com.collage.empowermentstrishakti.Adapter

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.collage.empowermentstrishakti.AboutFragment
import com.collage.empowermentstrishakti.FollowersFragment
import com.collage.empowermentstrishakti.PostFragment
import com.collage.empowermentstrishakti.data.model.Member
import com.collage.empowermentstrishakti.data.model.PageAbout
import com.collage.empowermentstrishakti.data.model.PostDetail





class PageDetailPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private var puuid: String? = null
    private var isAdminForCurrentPage: Boolean = false
    private var currentUserId: Int = -1

    /** Provide only small primitives. Fragments should observe the activity ViewModel for lists/objects. */
    fun setPuuid(puuid: String?) {
        this.puuid = puuid
        notifyDataSetChanged()
    }

    /** Called by Activity after page data is loaded so adapter can mark admin flag for About / Posts UI. */
    fun setIsAdmin(isAdmin: Boolean) {
        this.isAdminForCurrentPage = isAdmin
        notifyDataSetChanged()
    }

    /** Optionally keep current user id if fragment needs it as primitive */
    fun setCurrentUserId(id: Int) {
        this.currentUserId = id
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        // safe small args only
        val args: Bundle = bundleOf(
            "puuid" to (puuid ?: ""),
            "isAdmin" to isAdminForCurrentPage,
            "currentUserId" to currentUserId
        )

        return when (position) {
            0 -> {
                val f = PostFragment()
                f.arguments = args
                f
            }
            1 -> {
                val f = AboutFragment()
                f.arguments = args
                f
            }
            else -> {
                val f = FollowersFragment()
                f.arguments = args
                f
            }
        }
    }
}
