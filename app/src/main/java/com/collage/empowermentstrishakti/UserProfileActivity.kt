package com.collage.empowermentstrishakti

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Adapter.UserProfilePagerAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.FriendListViewModelFactory
import com.collage.empowermentstrishakti.Factory.UserProfileViewModelFactory
import com.collage.empowermentstrishakti.data.model.FriendData
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.FriendRepository
import com.collage.empowermentstrishakti.data.repository.UserProfileRepository
import com.collage.empowermentstrishakti.databinding.ActivityUserProfileBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.FriendListViewModel
import com.collage.empowermentstrishakti.ui.RegisterViewModel.UserProfileViewModel
import com.google.android.material.tabs.TabLayoutMediator


class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var viewModel: UserProfileViewModel
    private lateinit var friendViewModel: FriendListViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: UserProfilePagerAdapter

    private var friendStatus: String? = null
    private var friendRequestId: Int = -1
    private var viewedUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        val uuid = intent.getStringExtra("UUID") ?: ""
        viewedUserId = intent.getIntExtra("USER_ID", -1)
        val isOwnProfile = sessionManager.getUserId() == viewedUserId
        Log.d("UserProfileActivity1", "UUID from intent: $uuid")
        Log.d("UserProfileActivity1", "ViewedUserId from intent: $viewedUserId")
        Log.d("UserProfileActivity1", "Logged-in userId: ${sessionManager.getUserId()}")
        Log.d("UserProfileActivity1", "Is own profile: $isOwnProfile")
        setupViewModel()
        setupFriendViewModel()

        binding.ivEditProfile.isVisible = isOwnProfile
        binding.otherUserButtonsLayout.isVisible = !isOwnProfile

        setupViewPager(uuid, viewedUserId, isOwnProfile)
        setupObservers()
        setupFriendObservers()
        setupFriendButtons()

        if (uuid.isNotEmpty()) {
            viewModel.fetchUserProfile(uuid)
        }

        // Fetch friend list to get status of the viewed user
        friendViewModel.fetchFriendsList(sessionManager.getUserId())

        binding.ivBack.setOnClickListener { finish() }
    }

    // ---------------- ViewModel setup ----------------
    private fun setupViewModel() {
        val repo = UserProfileRepository(ApiClient.apiService, sessionManager)
        val factory = UserProfileViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[UserProfileViewModel::class.java]
    }

    private fun setupFriendViewModel() {
        val friendRepo = FriendRepository(ApiClient.apiService, sessionManager)
        val friendFactory = FriendListViewModelFactory(friendRepo)
        friendViewModel = ViewModelProvider(this, friendFactory)[FriendListViewModel::class.java]
    }

    // ---------------- ViewPager setup ----------------
    private fun setupViewPager(uuid: String, userId: Int, isOwnProfile: Boolean) {
        adapter = UserProfilePagerAdapter(this, uuid, userId, isOwnProfile)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Profile"
                1 -> "Posts"
                2 -> "Friends"
                else -> ""
            }
        }.attach()
    }

    // ---------------- Observers ----------------
    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                binding.tvUsername.text = "${it.userFirstName} ${it.userLastName}"
                binding.tvFullName.text = it.userOrgname ?: ""
                binding.tvEmail.text = it.userEmail ?: ""
                binding.tvBio.text = it.userLocation ?: ""
                binding.tvFollowers.text = it.totalFriends.toString()
                binding.tvFollowing.text = it.totalFriends.toString()
                binding.tvPosts.text = it.totalPosts.toString()

                Glide.with(this)
                    .load(it.userProfileImage)
                    .placeholder(R.drawable.user)
                    .into(binding.ivProfileImage)

                Glide.with(this)
                    .load(it.userCoverProfileImage)
                    .placeholder(R.drawable.bg_gradient_button)
                    .into(binding.ivCoverImage)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFriendObservers() {
        // Observe friend list to get the current status for this user
        friendViewModel.friendsList.observe(this) { friends ->
            val friendData: FriendData? = friends.find { it.userId == viewedUserId }
            friendStatus = friendData?.status
            friendRequestId = friendData?.friendRequestId ?: -1
            updateFriendButtonState()
        }

        // Observe friend action results (send/remove request)
        friendViewModel.friendActionState.observe(this) { state ->
            when (state) {
                is FriendListViewModel.FriendActionUI.Loading -> binding.btnRemove.isEnabled = false
                is FriendListViewModel.FriendActionUI.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    binding.btnRemove.isEnabled = true

                    if (state.message.contains("sent", ignoreCase = true)) {
                        friendStatus = "PENDING"
                        updateFriendButtonState()
                    } else if (state.message.contains("removed", ignoreCase = true)) {
                        friendStatus = null
                        updateFriendButtonState()
                    }
                }
                is FriendListViewModel.FriendActionUI.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    binding.btnRemove.isEnabled = true
                }
                else -> Unit
            }
        }
    }

    // ---------------- Friend Buttons ----------------
    private fun setupFriendButtons() {
        binding.btnMessage.setOnClickListener {
            Toast.makeText(this, "Message clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnRemove.setOnClickListener {
            when (binding.btnRemove.text.toString()) {
                "Send Request" -> {
                    val senderId = sessionManager.getUserId()
                    friendViewModel.sendFriendRequest(senderId, viewedUserId)
                }

                "Remove Friend" -> {
                    if (friendRequestId != -1) {
                        friendViewModel.removeFriendRequest(friendRequestId)
                    } else {
                        Toast.makeText(this, "Friend ID not found", Toast.LENGTH_SHORT).show()
                    }
                }

                "Pending" -> {
                    Toast.makeText(this, "Request already pending", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFriendButtonState() {
        when (friendStatus) {
            "APPROVED" -> {
                binding.btnRemove.text = "Remove Friend"
                binding.btnRemove.isEnabled = true
                binding.btnRemove.visibility = View.VISIBLE
            }
            "PENDING" -> {
                binding.btnRemove.text = "Pending"
                binding.btnRemove.isEnabled = false
                binding.btnRemove.visibility = View.VISIBLE
            }
            else -> {
                binding.btnRemove.text = "Send Request"
                binding.btnRemove.isEnabled = true
                binding.btnRemove.visibility = View.VISIBLE
            }
        }
    }

    private fun enableEdgeToEdge() {
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
