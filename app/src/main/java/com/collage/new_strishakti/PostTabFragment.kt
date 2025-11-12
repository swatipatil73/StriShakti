package com.collage.new_strishakti

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Factory.UserProfileViewModelFactory
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.UserProfileRepository
import com.collage.new_strishakti.databinding.FragmentPostTabBinding
import com.collage.new_strishakti.ui.RegisterViewModel.UserProfileViewModel

class PostTabFragment : Fragment() {

    private var _binding: FragmentPostTabBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PostGridAdapter

    private var uuid: String? = null
    private var isOwnProfile: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get fragment arguments
        arguments?.let {
            uuid = it.getString(ARG_UUID)
            isOwnProfile = it.getBoolean(ARG_IS_OWN_PROFILE, false)
        }

        // Initialize SessionManager and ViewModel
        sessionManager = SessionManager(requireContext())
        val repository = UserProfileRepository(ApiClient.apiService, sessionManager)
        val factory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[UserProfileViewModel::class.java]

        // Fetch all posts
        viewModel.fetchUserPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostTabBinding.inflate(inflater, container, false)
        return binding.root
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observePosts()
    }

    private fun setupRecyclerView() {
        adapter = PostGridAdapter()
        binding.recyclerPosts.layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns
        binding.recyclerPosts.adapter = adapter
    }

    private fun observePosts() {
        viewModel.userPosts.observe(viewLifecycleOwner) { postsList ->
            // Filter posts by the selected user's uuid
            val filteredPosts = postsList.filter { it.userUUID == uuid }

            if (filteredPosts.isNotEmpty()) {
                binding.recyclerPosts.visibility = View.VISIBLE
                binding.emptyStateLayout.root.visibility = View.GONE
                adapter.submitList(filteredPosts)
            } else {
                binding.recyclerPosts.visibility = View.GONE
                binding.emptyStateLayout.root.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_UUID = "uuid"
        private const val ARG_IS_OWN_PROFILE = "isOwnProfile"

        fun newInstance(uuid: String, isOwnProfile: Boolean): PostTabFragment {
            val fragment = PostTabFragment()
            val args = Bundle()
            args.putString(ARG_UUID, uuid)
            args.putBoolean(ARG_IS_OWN_PROFILE, isOwnProfile)
            fragment.arguments = args
            return fragment
        }
    }
}

