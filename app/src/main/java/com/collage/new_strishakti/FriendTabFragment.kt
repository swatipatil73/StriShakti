package com.collage.new_strishakti

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.new_strishakti.Adapter.FriendListAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Factory.FriendListViewModelFactory
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.FriendRepository
import com.collage.new_strishakti.databinding.EmptyStateLayoutBinding
import com.collage.new_strishakti.databinding.FragmentFriendTabBinding
import com.collage.new_strishakti.ui.RegisterViewModel.FriendListViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


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
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendTabBinding.inflate(inflater, container, false)
        emptyBinding = binding.emptyStateLayout
        return binding.root
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
        friendsAdapter = FriendListAdapter(emptyList()) { friend ->
            // On friend click
            Toast.makeText(requireContext(), "Clicked: ${friend.userFirstName}", Toast.LENGTH_SHORT)
                .show()
        }

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
            val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.f}")
            emptyBinding.emptyVideo.setVideoURI(uri)
            emptyBinding.emptyVideo.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.start()
            }
        } else {
            emptyBinding.emptyVideo.stopPlayback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
