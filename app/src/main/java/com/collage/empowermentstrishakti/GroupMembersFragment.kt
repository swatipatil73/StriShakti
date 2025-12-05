package com.collage.empowermentstrishakti

import com.collage.empowermentstrishakti.Adapter.GroupMemberAdapter
import com.collage.empowermentstrishakti.data.model.Groups.GroupMember
import com.collage.empowermentstrishakti.databinding.FragmentGroupMembersBinding


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Common.SessionManager


class GroupMembersFragment : Fragment(), GroupMemberAdapter.Listener {

    private var _binding: FragmentGroupMembersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GroupMemberAdapter
    private var membersList: MutableList<GroupMember> = mutableListOf()

    // optional flag to control remove button visibility (e.g. only group admin can remove)
    private var isAdminMode = false

    companion object {
        private const val ARG_MEMBERS = "arg_members"
        private const val ARG_IS_ADMIN = "arg_is_admin"

        fun newInstance(members: ArrayList<GroupMember>, isAdmin: Boolean = false): GroupMembersFragment {
            val f = GroupMembersFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_MEMBERS, members)
            args.putBoolean(ARG_IS_ADMIN, isAdmin)
            f.arguments = args
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val incoming = it.getParcelableArrayList<GroupMember>(ARG_MEMBERS)
            if (!incoming.isNullOrEmpty()) membersList = incoming.toMutableList()
            isAdminMode = it.getBoolean(ARG_IS_ADMIN, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGroupMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecycler()
        showEmptyIfNeeded()
    }

    private fun setupRecycler() {
        // get current user id from your session manager
        // adapt this line to your SessionManager API; common names shown below:
        val sessionManager = SessionManager(requireContext())

        // Try common getters; change to the one present in your project:
        val currentUserId: Int = try {
            // example: SessionManager.userId (Int)
            sessionManager.getUserId()
        } catch (e: Exception) {
            try {
                // example: SessionManager.getUserId() : Int
                sessionManager.getUserId()
            } catch (e2: Exception) {
                // fallback: -1 (no user)
                -1
            }
        }

        // Create adapter passing admin flag and currentUserId
        adapter = GroupMemberAdapter(membersList, this, isAdminMode, currentUserId)
        binding.recyclerMembers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMembers.adapter = adapter
        binding.recyclerMembers.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        showEmptyIfNeeded()
    }

    private fun showRemoveButtons(show: Boolean) {
        // toggle admin mode at runtime if needed (adapter has setAdmin)
        val sessionManager = SessionManager(requireContext())
        val currentUserId = try {
            sessionManager.getUserId()
        } catch (e: Exception) {
            try { sessionManager.getUserId() } catch (e2: Exception) { -1 }
        }

        isAdminMode = show
        adapter.setAdmin(isAdminMode, currentUserId)
    }


    private fun showEmptyIfNeeded() {
        binding.tvEmptyMembers.visibility = if (membersList.isEmpty()) View.VISIBLE else View.GONE
    }




    // --- Adapter Listener callbacks ---
    override fun onMemberClicked(member: GroupMember, position: Int) {
        // open member detail or show profile
        Toast.makeText(requireContext(), "Clicked: ${member.userFirstName ?: "User"}", Toast.LENGTH_SHORT).show()

        // Example navigation stub (uncomment & replace with your nav action)
        // val action = GroupFragmentDirections.actionGroupToMemberDetail(member.userUUID)
        // findNavController().navigate(action)
    }

    override fun onRemoveClicked(member: GroupMember, position: Int) {
        // We won't perform removal here yet (will implement API/remove logic in next step).
        // For now, optimistic local removal to demonstrate UI:
        membersList.removeAt(position)
        adapter.removeAt(position)
        showEmptyIfNeeded()
        Toast.makeText(requireContext(), "Removed: ${member.userFirstName ?: "User"}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
