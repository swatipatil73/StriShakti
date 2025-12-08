package com.collage.empowermentstrishakti.Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.collage.empowermentstrishakti.GroupAboutFragment
import com.collage.empowermentstrishakti.GroupMembersFragment

import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse




class GroupPagerAdapter(
    activity: AppCompatActivity,
    private val data: GroupDetailsResponse
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> com.collage.empowermentstrishakti.ui.group.GroupPostFragment.newInstance(data)
            1 -> GroupAboutFragment.newInstance(data)


            2 -> {
                val members = ArrayList(data.currentGroupMembers ?: emptyList())
                val isAdmin = try {
                    (data.groupAbout?.javaClass?.getDeclaredField("isAdmin") != null &&
                            (data.groupAbout?.let {
                                val f = it.javaClass.getDeclaredField("isAdmin")
                                f.isAccessible = true
                                f.get(it) as? Boolean
                            } == true))
                } catch (e: Exception) {
                    false
                }

                // Pass groupId to fragment via arguments
                val groupId = data.groupAbout?.groupId ?: -1
                GroupMembersFragment.newInstance(members, isAdmin = isAdmin, groupId = groupId)
            }

            else -> Fragment()
        }
    }
}

