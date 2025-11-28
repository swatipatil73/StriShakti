package com.collage.empowermentstrishakti

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.FriendListAdapter
import com.collage.empowermentstrishakti.Adapter.SearchUserAdapter
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.BottomNavigationHelper
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.FriendListViewModelFactory
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.FriendRepository
import com.collage.empowermentstrishakti.databinding.ActivityFriendListBinding
import com.collage.empowermentstrishakti.databinding.EmptyStateLayoutBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.FriendListViewModel

import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendListActivity : BaseActivity() {
    private lateinit var binding: ActivityFriendListBinding
    private lateinit var emptyBinding: EmptyStateLayoutBinding
    private lateinit var viewModel: FriendListViewModel
   private lateinit var sessionManager: SessionManager
    private lateinit var friendsAdapter: FriendListAdapter
    private lateinit var searchAdapter: SearchUserAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private var friendRequestBadge: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
        setupToolbarWithBadge()
        fetchFriendRequestCount()
        setupViewModel()
        setupRecycler()
        observeData()

        // Fetch friends using logged-in user ID
        viewModel.fetchFriendsList(sessionManager.getUserId())

        // Search functionality
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onSearchTextChanged(text?.toString().orEmpty())
        }

        onBackPressedDispatcher.addCallback(this) {
            binding.emptyStateLayout.emptyVideo.stopPlayback()
            finish()
        }
    }



    private fun setupToolbarWithBadge() {
        setupToolbar(
            title = "Stri Shakti",
            showSearch = false,
            showCreate = true,
            createIconRes = R.drawable.bell
        )

        // Create a custom layout for the bell + badge
        val badgeLayout = FrameLayout(this)

        val bellIcon = ImageView(this).apply {
            setImageResource(R.drawable.bell)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val badge = TextView(this).apply {
            setBackgroundResource(R.drawable.badge_circle) // red circle drawable
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            visibility = View.GONE // hide initially
            setPadding(0, 0, 0, 0)
        }

        val badgeParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        ).apply {
            setMargins(0, 30, 60, 60)


        }

        badgeLayout.addView(badge, badgeParams)

        // Add custom view to toolbar menu
        val menuItem = toolbar.menu.add("Notifications")
        menuItem.setActionView(badgeLayout)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        // Keep reference to update badge later
        friendRequestBadge = badge

        // Click listener
        badgeLayout.setOnClickListener {
            startActivity(Intent(this, FriendRequestsActivity::class.java))
        }
    }

    private fun fetchFriendRequestCount() {
        val repo = FriendRepository(ApiClient.apiService, sessionManager)
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repo.getFriendRequests(sessionManager.getUserId())
                }
                if (response.isSuccessful) {
                    val count = response.body()?.friendRequestData?.size ?: 0
                    updateBadge(count)
                } else {
                    updateBadge(0)
                }
            } catch (e: Exception) {
                updateBadge(0)
            }
        }
    }
    private fun updateBadge(count: Int) {
        if (count > 0) {
            friendRequestBadge?.visibility = View.VISIBLE
            friendRequestBadge?.text = count.toString()
        } else {
            friendRequestBadge?.visibility = View.GONE
        }
    }


    override fun onCreateClicked() {
        startActivity(Intent(this, FriendRequestsActivity::class.java))
    }

    private fun setupViewModel() {
        val repo = FriendRepository(ApiClient.apiService, sessionManager)
        val factory = FriendListViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[FriendListViewModel::class.java]
    }

    private fun setupRecycler() {
        // Friends list adapter click
        friendsAdapter = FriendListAdapter(emptyList()) { friend ->
            val intent = Intent(this, UserProfileActivity::class.java)
                .putExtra("UUID", friend.userUUID)   // Needed to fetch profile
                .putExtra("USER_ID", friend.userId)  // Pass userId for friend request
            startActivity(intent)
        }

        // Search adapter click
        searchAdapter = SearchUserAdapter { user ->
            val intent = Intent(this, UserProfileActivity::class.java)
                .putExtra("UUID", user.userUUID)
                .putExtra("USER_ID", user.userId)   // Make sure userId exists in search model
            startActivity(intent)
        }

        binding.rvFriends.layoutManager = LinearLayoutManager(this)
        binding.rvFriends.adapter = friendsAdapter
    }

    private fun observeData() {
        viewModel.friendsList.observe(this) { list ->
            if (viewModel.searchResults.value == null) {
                friendsAdapter.updateList(list)
                toggleEmpty(list.isEmpty(), "No friends found")
            }
        }

        viewModel.searchResults.observe(this) { results ->
            if (results == null) {
                binding.rvFriends.adapter = friendsAdapter
                toggleEmpty(friendsAdapter.itemCount == 0, "No friends found")
            } else {
                binding.rvFriends.adapter = searchAdapter
                searchAdapter.submitList(results)
                toggleEmpty(results.isEmpty(), "No users found")
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
