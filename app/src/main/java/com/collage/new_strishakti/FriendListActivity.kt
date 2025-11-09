package com.collage.new_strishakti

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.new_strishakti.Adapter.FriendListAdapter
import com.collage.new_strishakti.Adapter.SearchUserAdapter
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.BottomNavigationHelper
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.FriendListViewModelFactory
import com.collage.new_strishakti.data.model.FriendData
import com.collage.new_strishakti.data.model.friend.SearchedUser
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.FriendRepository
import com.collage.new_strishakti.databinding.ActivityFriendListBinding
import com.collage.new_strishakti.databinding.EmptyStateLayoutBinding
import com.collage.new_strishakti.ui.RegisterViewModel.FriendListViewModel

import com.google.android.material.bottomnavigation.BottomNavigationView

class FriendListActivity : BaseActivity() {

    private lateinit var binding: ActivityFriendListBinding
    private lateinit var emptyBinding: EmptyStateLayoutBinding
    private lateinit var viewModel: FriendListViewModel
    private lateinit var sessionManager: SessionManager

    private lateinit var friendsAdapter: FriendListAdapter
    private lateinit var searchAdapter: SearchUserAdapter
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(
            title = "Stri Shakti",
            showSearch = false,
            showCreate = true,
            createIconRes = R.drawable.bell
        )

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(this, bottomNavigationView, R.id.nav_home)

        emptyBinding = binding.emptyStateLayout
        sessionManager = SessionManager(this)

        setupViewModel()
        setupRecycler()
        observeData()

        val userId = sessionManager.getUserId()
        viewModel.fetchFriendsList(userId)

        // search-as-you-type
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onSearchTextChanged(text?.toString().orEmpty())
        }

        // Optional: handle back press when empty-state video is playing
        onBackPressedDispatcher.addCallback(this) {
            binding.emptyStateLayout.emptyVideo.stopPlayback()
            finish()
        }
    }

    /** Bell click from BaseActivity */
    override fun onCreateClicked() {
        startActivity(Intent(this, FriendRequestsActivity::class.java))
    }

    private fun setupViewModel() {
        val repo = FriendRepository(ApiClient.apiService, SessionManager(this))
        val factory = FriendListViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[FriendListViewModel::class.java]
    }

    private fun setupRecycler() {
        friendsAdapter = FriendListAdapter(emptyList()) { friend ->
            startActivity(
                Intent(this, UserProfileActivity::class.java)
                    .putExtra("USER_ID", friend.userId)
            )
        }

        searchAdapter = SearchUserAdapter { user ->
            startActivity(
                Intent(this, UserProfileActivity::class.java)
                    .putExtra("USER_ID", user.userId)
            )
        }

        binding.rvFriends.layoutManager = LinearLayoutManager(this)
        binding.rvFriends.adapter = friendsAdapter
    }

    private fun observeData() {
        // screen state
        viewModel.isLoading.observe(this) { /* no progress bar here */ }

        viewModel.friendsList.observe(this) { list ->
            if (viewModel.searchResults.value == null) {
                friendsAdapter.updateList(list)
                toggleEmpty(list.isEmpty(), "No friends found")
            }
        }

        viewModel.searchResults.observe(this) { resultsOrNull ->
            if (resultsOrNull == null) {
                binding.rvFriends.adapter = friendsAdapter
                toggleEmpty(friendsAdapter.itemCount == 0, "No friends found")
            } else {
                binding.rvFriends.adapter = searchAdapter
                searchAdapter.submitList(resultsOrNull)
                toggleEmpty(resultsOrNull.isEmpty(), "No users found")
            }
        }
    }

    private fun toggleEmpty(show: Boolean, message: String) {
        emptyBinding.tvEmpty.text = message
        binding.emptyStateLayout.root.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            val uri = Uri.parse("android.resource://${packageName}/${R.raw.f}")
            emptyBinding.emptyVideo.setVideoURI(uri)
            emptyBinding.emptyVideo.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.start()
            }
        } else {
            emptyBinding.emptyVideo.stopPlayback()
        }
    }
}
