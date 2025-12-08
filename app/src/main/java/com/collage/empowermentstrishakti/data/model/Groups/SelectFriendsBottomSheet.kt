package com.collage.empowermentstrishakti.data.model.Groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.FriendAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.FriendData
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.databinding.BottomSheetSelectFriendsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import com.collage.empowermentstrishakti.data.model.Groups.GroupMember
class SelectFriendsBottomSheet(
    private val adminUserId: Int,
    private val groupId: Int,
    private val onMembersAdded: (List<GroupMember>) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSelectFriendsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendAdapter
    private var selectedFriends = mutableListOf<FriendData>()

    private val token by lazy { SessionManager(requireContext()).getToken() ?: "" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSelectFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        fetchFriends()
        setupAddButton()
    }

    private fun setupRecycler() {
        adapter = FriendAdapter { friend, isChecked ->
            if (isChecked) {
                selectedFriends.add(friend)
            } else {
                selectedFriends.remove(friend)
            }
        }
        binding.recyclerFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFriends.adapter = adapter
    }

    private fun fetchFriends() {
        // Call API to get friends list
        lifecycleScope.launch {
            val response = ApiClient.apiService.getFriendsList(adminUserId, "Bearer $token")
            if (response.isSuccessful) {
                val list = response.body()?.friendListData ?: emptyList()
                adapter.submitList(list)
            } else {
                Toast.makeText(requireContext(), "Failed to load friends", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAddButton() {
        binding.btnAddSelected.setOnClickListener {
            if (selectedFriends.isEmpty()) {
                Toast.makeText(requireContext(), "Select at least 1 friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addMembersToGroup()
        }
    }

    private fun addMembersToGroup() {
        val userIds = selectedFriends.map { it.userId }
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.addMembersToGroup(
                    adminUserId = adminUserId,
                    groupId = groupId,
                    userIds = userIds,
                    token = "Bearer $token"
                )

                if (response.isSuccessful && response.body()?.status == "Success") {
                    // convert FriendData to GroupMember to update list
                    val newMembers = selectedFriends.map {
                        GroupMember(
                            userId = it.userId,
                            userUUID = it.userUUID,
                            userFirstName = it.userFirstName,
                            userlastName = it.userLastName,  // match the lowercase 'l'
                            userProfileImagePath = it.userProfileImagePath
                        )
                    }


                    onMembersAdded(newMembers)
                    Toast.makeText(requireContext(), "Members added successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.localizedMessage ?: "Error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(adminUserId: Int, groupId: Int, onMembersAdded: (List<GroupMember>) -> Unit) =
            SelectFriendsBottomSheet(adminUserId, groupId, onMembersAdded)
    }
}
