package com.collage.empowermentstrishakti

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.FriendListAdapter
import com.collage.empowermentstrishakti.Common.FriendClickType
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.FriendListViewModelFactory
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.FriendRepository
import com.collage.empowermentstrishakti.data.model.FriendData
import com.collage.empowermentstrishakti.databinding.EmptyStateLayoutBinding
import com.collage.empowermentstrishakti.databinding.FragmentFriendTabBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.FriendListViewModel

class FriendTabFragment : Fragment() {

    private var _binding: FragmentFriendTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var emptyBinding: EmptyStateLayoutBinding
    private lateinit var viewModel: FriendListViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var friendsAdapter: FriendListAdapter

    private var uuid: String? = null
    private var userId: Int = 0
    private var isOwnProfile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            uuid = it.getString(ARG_UUID)
            userId = it.getInt(ARG_USER_ID)
            isOwnProfile = it.getBoolean(ARG_IS_OWN_PROFILE, false)
        }

        sessionManager = SessionManager(requireContext())
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Disable dark mode (keep this before returning the view)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        _binding = FragmentFriendTabBinding.inflate(inflater, container, false)
        emptyBinding = binding.emptyStateLayout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        observeData()

        // Fetch friends of the profile being viewed
        viewModel.fetchFriendsList(userId)
    }

    private fun setupViewModel() {
        val repo = FriendRepository(ApiClient.apiService, sessionManager)
        val factory = FriendListViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[FriendListViewModel::class.java]
    }

    private fun setupRecycler() {
        // Choose click behavior:
        // - Use FriendClickType.PROFILE if clicking should open profile (default here)
        // - Use FriendClickType.CHAT if clicking should open chat screen directly
        val clickType = FriendClickType.PROFILE

        friendsAdapter = FriendListAdapter(
            friendList = emptyList(),
            clickType = clickType,
            onProfileClick = { friend ->
                // Open UserProfileActivity with extras
                val intent = Intent(requireContext(), UserProfileActivity::class.java).apply {
                    putExtra("UUID", friend.userUUID)
                    putExtra("USER_ID", friend.userId)
                }
                startActivity(intent)
            },
            onChatClick = { friend ->
                // TODO: replace with your chat activity intent
                Toast.makeText(requireContext(), "Open chat with: ${friend.userFirstName}", Toast.LENGTH_SHORT).show()
                // Example:
                // val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                //     putExtra("CHAT_USER_ID", friend.userId)
                // }
                // startActivity(intent)
            }
        )

        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.adapter = friendsAdapter
    }

    private fun observeData() {
        viewModel.friendsList.observe(viewLifecycleOwner) { list ->
            friendsAdapter.updateList(list)
            toggleEmpty(list.isEmpty(), "No friends found")
        }
    }

    private fun toggleEmpty(show: Boolean, message: String) {
        emptyBinding.tvEmpty.text = message
        emptyBinding.root.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            try {
                val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.f}")
                emptyBinding.emptyVideo.setVideoURI(uri)
                emptyBinding.emptyVideo.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                emptyBinding.emptyVideo.stopPlayback()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        // stop video to free resources
        try {
            emptyBinding.emptyVideo.stopPlayback()
        } catch (e: Exception) {
            // ignore
        }
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_UUID = "uuid"
        private const val ARG_USER_ID = "userId"
        private const val ARG_IS_OWN_PROFILE = "isOwnProfile"

        fun newInstance(uuid: String, userId: Int, isOwnProfile: Boolean): FriendTabFragment {
            val fragment = FriendTabFragment()
            val args = Bundle()
            args.putString(ARG_UUID, uuid)
            args.putInt(ARG_USER_ID, userId)
            args.putBoolean(ARG_IS_OWN_PROFILE, isOwnProfile)
            fragment.arguments = args
            return fragment
        }
    }
}
