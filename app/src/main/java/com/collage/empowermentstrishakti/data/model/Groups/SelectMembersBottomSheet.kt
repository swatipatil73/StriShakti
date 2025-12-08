package com.collage.empowermentstrishakti.data.model.Groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.FriendAdapter
import com.collage.empowermentstrishakti.GroupListActivity
import com.collage.empowermentstrishakti.databinding.BottomSheetSelectMembersBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.CreateGroupViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectMembersBottomSheet(
    private val viewModel: CreateGroupViewModel
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSelectMembersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetSelectMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FriendAdapter { friend, isChecked ->
            viewModel.toggleMemberSelection(friend.userId, isChecked)
        }

        binding.recyclerFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFriends.adapter = adapter

        // Load friends
        val userId = (activity as? GroupListActivity)?.session?.getUserId() ?: 0
        viewModel.fetchFriends(userId)

        viewModel.friendList.observe(viewLifecycleOwner) { result ->
            when(result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(result.data)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDone.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
