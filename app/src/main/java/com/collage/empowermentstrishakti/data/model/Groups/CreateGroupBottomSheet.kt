package com.collage.empowermentstrishakti.data.model.Groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.collage.empowermentstrishakti.GroupListActivity
import com.collage.empowermentstrishakti.databinding.BottomSheetCreateGroupBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.CreateGroupViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateGroupBottomSheet(
    private val viewModel: CreateGroupViewModel,
    private val adminUserId: Int
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreateGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetCreateGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddMembers.setOnClickListener {
            showSelectMembersSheet()
        }

        binding.btnCreateGroup.setOnClickListener {
            val name = binding.etGroupName.text.toString().trim()
            val desc = binding.etGroupDesc.text.toString().trim()

            if (name.isEmpty()) {
                binding.etGroupName.error = "Enter group name"
                return@setOnClickListener
            }

            viewModel.createGroup(adminUserId, name, desc)
        }

        // Observe creation result
        viewModel.createGroupResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is NetworkResult.Loading -> {
                    Toast.makeText(requireContext(), "Creating group...", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Success -> {
                    Toast.makeText(requireContext(), result.data?.message ?: "Group created", Toast.LENGTH_SHORT).show()

                    // --- NEW: refresh group list in parent activity ---
                    (activity as? GroupListActivity)?.let { activity ->
                        val rawUserId = activity.session.getUserId()
                        val userId = when (rawUserId) {
                            is Int -> rawUserId
                            is String -> rawUserId.toIntOrNull() ?: -1
                            else -> -1
                        }
                        if (userId > 0) {
                            activity.viewModel.fetchGroups(userId)
                        }
                    }

                    dismiss()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSelectMembersSheet() {
        val selectMembersSheet = SelectMembersBottomSheet(viewModel)
        selectMembersSheet.show(parentFragmentManager, "SelectMembers")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
