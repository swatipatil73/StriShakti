package com.collage.empowermentstrishakti


import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Member
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel

class FollowersFragment : Fragment() {

    private lateinit var recyclerFollowers: RecyclerView
    private lateinit var tvNoFollowers: TextView
    private lateinit var adapter: FollowersAdapter
    private lateinit var vm: PageDetailViewModel
    private val session by lazy { SessionManager(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = ViewModelProvider(requireActivity()).get(PageDetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_followers, container, false)
        recyclerFollowers = v.findViewById(R.id.recyclerFollowers)
        tvNoFollowers = v.findViewById(R.id.tvNoFollowers)

        recyclerFollowers.layoutManager = LinearLayoutManager(requireContext())

        // determine if current user is admin from VM
        val adminId = vm.pageDetails.value?.pageAbout?.adminId ?: vm.pageAbout.value?.adminId ?: -1
        val isAdmin = (adminId == session.getUserId())

        adapter = FollowersAdapter(currentUserIsAdmin = isAdmin, onRemoveClick = { member, pos ->
            confirmRemove(member, pos)
        })
        recyclerFollowers.adapter = adapter

        // Observe followers list
        vm.pageDetails.observe(viewLifecycleOwner) { resp ->
            val members = resp?.currentPageMembers ?: emptyList()
            adapter.submitList(members.toList())
            tvNoFollowers.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
        }

        // Also observe followers LiveData if used
//        vm.followers.observe(viewLifecycleOwner) { members ->
//            adapter.submitList(members.toList())
//            tvNoFollowers.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
//        }

        return v
    }

    private fun confirmRemove(member: Member, pos: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove follower")
            .setMessage("Remove ${member.userFirstName ?: "this user"} from followers?")
            .setPositiveButton("Remove") { _, _ ->
                // optimistic local removal
               // vm.removeFollowerLocally(member.userId)
                adapter.removeAt(pos)

                // TODO: call backend to remove follower (API). On failure, either refresh list or show error and re-add.
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
