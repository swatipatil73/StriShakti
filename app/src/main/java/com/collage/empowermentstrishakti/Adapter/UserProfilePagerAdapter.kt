package com.collage.empowermentstrishakti.Adapter


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.collage.empowermentstrishakti.FriendTabFragment
import com.collage.empowermentstrishakti.PostTabFragment
import com.collage.empowermentstrishakti.ProfileTabFragment


class UserProfilePagerAdapter(
    activity: FragmentActivity,
    private val uuid: String,
    private val userId: Int,
    private val isOwnProfile: Boolean
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileTabFragment.newInstance(uuid, isOwnProfile)
       1 -> PostTabFragment.newInstance(uuid, isOwnProfile)
  2-> FriendTabFragment.newInstance(uuid, userId, isOwnProfile)
            else -> Fragment()
        }
    }
}