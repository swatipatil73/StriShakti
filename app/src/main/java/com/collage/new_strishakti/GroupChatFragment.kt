package com.collage.new_strishakti

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.Adapter.GroupListAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.ChatViewModelFactory
import com.collage.new_strishakti.data.model.chat.GroupData
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.ChatRepository
import com.collage.new_strishakti.databinding.FragmentGroupChatBinding
import com.collage.new_strishakti.ui.RegisterViewModel.ChatViewModel

import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupChatFragment : Fragment() {

    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: GroupListAdapter

    private var allGroups: List<GroupData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupViewModel()
        setupSearch()

        loadGroups()
    }

    /** ---------------- RecyclerView ---------------- **/
    private fun setupRecyclerView() {
        adapter = GroupListAdapter(emptyList()) { group ->
            openGroupChat(group)
        }
        binding.rvGroups.layoutManager =
            LinearLayoutManager(requireContext())  //
        binding.rvGroups.adapter = adapter
    }

    /** ---------------- ViewModel ---------------- **/
    private fun setupViewModel() {
        val repository = ChatRepository(ApiClient.apiService)
        val factory = ChatViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[ChatViewModel::class.java]

        viewModel.groups.observe(viewLifecycleOwner) { list ->
            allGroups = list
            applyFilter("")
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadGroups() {
        val userId = sessionManager.getUserId()
        val token = sessionManager.getToken() ?: ""
        viewModel.loadGroups(userId, token)
    }

    /** ---------------- SEARCH FILTER ---------------- **/
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun applyFilter(query: String) {
        val filtered = if (query.isEmpty()) {
            allGroups
        } else {
            allGroups.filter {
                it.groupName.lowercase().contains(query.lowercase())
            }
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            ViewUtils.showEmptyState(binding.root, true, "No Groups Found")
        } else {
            ViewUtils.showEmptyState(binding.root, false, "")
        }
    }

    /** ---------------- OPEN GROUP CHAT SCREEN ---------------- **/
    private fun openGroupChat(group: GroupData) {
        val intent = Intent(requireContext(), GroupchattActivity::class.java)
        intent.putExtra("GROUP_ID", group.groupId)
      intent.putExtra("TITLE", group.groupName)
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
