package com.collage.new_strishakti

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.collage.new_strishakti.Adapter.FriendListAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.ChatViewModelFactory
import com.collage.new_strishakti.data.model.FriendData
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.ChatRepository
import com.collage.new_strishakti.databinding.FragmentPersonalChatBinding
import com.collage.new_strishakti.ui.RegisterViewModel.ChatViewModel
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalChatFragment : Fragment() {

    private var _binding: FragmentPersonalChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: FriendListAdapter

    private var allFriends: List<FriendData> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupViewModel()
        setupSearch()

        loadFriends()
    }

    /** ---------------- RecyclerView ---------------- **/
    private fun setupRecyclerView() {
        adapter = FriendListAdapter(emptyList()) { friend ->
            openChatScreen(friend)
        }
        binding.rvFriends.layoutManager =
            LinearLayoutManager(requireContext())  // <-- This is critical!
        binding.rvFriends.adapter = adapter
    }


    /** ---------------- ViewModel ---------------- **/
    private fun setupViewModel() {
        val repository = ChatRepository(ApiClient.apiService)
        val factory = ChatViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[ChatViewModel::class.java]

        viewModel.friends.observe(viewLifecycleOwner) { list ->
            Log.d("PersonalChatFragment", "Friends loaded: ${list.size}")
            allFriends = list
            applyFilter("")
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadFriends() {
        val userId = sessionManager.getUserId()
        val token = sessionManager.getToken() ?: ""
        viewModel.loadFriends(userId, token)
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
            allFriends
        } else {
            allFriends.filter {
                val name = "${it.userFirstName} ${it.userLastName}".lowercase()
                name.contains(query.lowercase())
            }
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            ViewUtils.showEmptyState(binding.root, true, "No Friends Found")
        } else {
            ViewUtils.showEmptyState(binding.root, false, "")
        }
    }

    /** ---------------- OPEN CHAT SCREEN ---------------- **/
    private fun openChatScreen(friend: FriendData) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("USER_ID", friend.userId)
        intent.putExtra("UUID", friend.userUUID)
        intent.putExtra("NAME", "${friend.userFirstName} ${friend.userLastName}")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}