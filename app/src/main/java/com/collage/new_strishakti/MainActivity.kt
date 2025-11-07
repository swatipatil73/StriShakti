package com.collage.new_strishakti

import android.os.Bundle
import android.view.View

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import android.widget.ProgressBar
import android.widget.TextView

import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.HomeFeedAdapter
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.BottomNavigationHelper
import com.collage.new_strishakti.Common.OnPostCreatedListener
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.data.model.post.AnnouncementAdsPopupDialog
import com.collage.new_strishakti.data.model.post.HomeFeedItem
import com.collage.new_strishakti.data.model.post.HomeViewModelFactory
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.HomeRepository
import com.collage.new_strishakti.ui.RegisterViewModel.HomeViewModel
import com.collage.new_strishakti.data.network.ApiService

import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.collage.new_strishakti.data.model.post.AdsResponse
import com.collage.new_strishakti.data.model.post.AnnouncementResponse
import com.collage.new_strishakti.data.model.post.PostActionsVMFactory
import com.collage.new_strishakti.data.repository.PostActionsRepository
import com.collage.new_strishakti.ui.RegisterViewModel.PostActionEvent
import com.collage.new_strishakti.ui.RegisterViewModel.PostActionsViewModel
import org.json.JSONObject
import retrofit2.Response

class MainActivity : BaseActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var bottomProgressBar: ProgressBar   // ✅ Added
    private lateinit var emptyTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var adapter: HomeFeedAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private lateinit var token: String
    private var userId: Long = 0

    lateinit var sharedReelPlayer: ExoPlayer
    private lateinit var postActionsVM: PostActionsViewModel   // <— add this
    private var nextCursor: Long? = 0L
    private var hasNextPage = true
    private var isLoading = false
    private val pageSize = 5
    private val postCategory = "YOUR_POST_CATEGORY"
    // replace this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        sessionManager = SessionManager(this)
        apiService = ApiClient.apiService

        setupToolbar(title = "Stri shakti", showSearch = true, showCreate = true)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(this, bottomNavigationView, R.id.nav_home)

        // ✅ Shared ExoPlayer
        sharedReelPlayer = ExoPlayer.Builder(this).build().apply {
            val audioAttr = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            setAudioAttributes(audioAttr, true)
            setHandleAudioBecomingNoisy(true)
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_ONE
        }
        postActionsVM = ViewModelProvider(
            this,
            PostActionsVMFactory(PostActionsRepository(apiService))
        ).get(PostActionsViewModel::class.java)

        setupRecyclerView()
        setupViewModel()

        observeViewModel()
        observePostActions()
        observeDeleteEvents()
        lifecycleScope.launch {
            userId = sessionManager.getUserId().toLong()
            token = "Bearer ${sessionManager.getToken()}"

            // Start loading posts immediately (non-blocking)
            val postsJob = async { loadHomePosts() }

            // Parallel background task for popup + ads
            async { loadAnnouncementAndAdsAndShowPopup() }

            postsJob.await() // ensure posts visible before next load
        }
    }

    // ------------------------------------------------------------------------------------------
    // 🔹 RecyclerView setup
    private fun setupRecyclerView() {
        adapter = HomeFeedAdapter(sharedReelPlayer, lifecycleScope,   postActionsVM
        ) // <-- pass lifecycleScope
        progressBar = findViewById(R.id.progressBar)
        emptyTextView = findViewById(R.id.emptyTextView)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(10)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                // Trigger pagination when reaching near end
                if (!isLoading && hasNextPage && lastVisible >= totalItemCount - 2) {
                    loadHomePosts()
                }
            }
        })

    }

    // ------------------------------------------------------------------------------------------
    // 🔹 ViewModel setup
    private fun setupViewModel() {
        val repo = HomeRepository(ApiClient.apiService)
        val factory = HomeViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.homeFeedItems.observe(this) { items ->
            adapter.submitList(items)
            progressBar.visibility = View.GONE
            emptyTextView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun observePostActions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postActionsVM.events.collect { ev ->
                    when (ev) {
                        is PostActionEvent.Saved -> {
                            Toast.makeText(
                                this@MainActivity,
                                ev.message, // Only the message, e.g. "Post saved successfully"
                                Toast.LENGTH_SHORT
                            ).show()
                            // Re-enable the button for this post if needed
                            adapter.notifyItemChanged(ev.postId)
                        }

                        is PostActionEvent.Error -> {
                            Toast.makeText(
                                this@MainActivity,
                                ev.message, // Only the message, e.g. "This post has already been saved"
                                Toast.LENGTH_SHORT
                            ).show()
                            // Re-enable the button
                            adapter.notifyItemChanged(ev.postId)
                        }

                        is PostActionEvent.Deleted -> { /* handle delete if needed */ }
                    }
                }
            }
        }
    }








    private fun observeDeleteEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                postActionsVM.events.collect { event ->
                    if (event is PostActionEvent.Deleted) {
                        // Remove the deleted post from HomeViewModel feed immediately
                        viewModel.removePost(event.postId)

                        // Optional: show a Toast message
                        Toast.makeText(
                            this@MainActivity,
                            "Post deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }



    // ------------------------------------------------------------------------------------------
    // 🔹 Load Posts (Optimized)
    private fun loadHomePosts() {
        if (isLoading || !hasNextPage) return
        isLoading = true

        val isFirstPage = nextCursor == 0L

        if (isFirstPage) {
            progressBar.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
        } else {
            // 🧠 Delay ensures RecyclerView renders footer before API response arrives
            Handler(Looper.getMainLooper()).post {
                adapter.showLoading()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cursor = nextCursor ?: 0L
                val response = apiService.getHomePosts(userId, cursor, pageSize, token)

                if (response.isSuccessful) {
                    val body = response.body()
                    val posts = body?.postsData ?: emptyList()

                    nextCursor = body?.nextCursor
                    hasNextPage = body?.hasNextPage ?: false

                    val updatedList = adapter.currentList.toMutableList()

                    // remove loading if already visible
                    updatedList.removeAll { it is HomeFeedItem.LoadingItem }

                    val postItems = posts.map { HomeFeedItem.PostItem(it) }
                    updatedList.addAll(postItems)

                    // add reel section only after first 5 posts on first load
                    if (cursor == 0L) {
                        val reelsResponse = apiService.getAllReels(0, 10, token)
                        if (reelsResponse.isSuccessful) {
                            val reels = reelsResponse.body()?.postsData ?: emptyList()
                            if (reels.isNotEmpty()) {
                                val insertIndex = if (updatedList.size >= 5) 5 else updatedList.size
                                updatedList.add(insertIndex, HomeFeedItem.ReelSection(reels))
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        adapter.submitList(updatedList)
                        emptyTextView.visibility =
                            if (updatedList.isEmpty()) View.VISIBLE else View.GONE
                    }

                } else {
                    hasNextPage = false
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    adapter.hideLoading()
                    progressBar.visibility = View.GONE
                    isLoading = false
                }
            }
        }
    }



    // ------------------------------------------------------------------------------------------
    private suspend fun loadAnnouncementAndAdsAndShowPopup(): Boolean = suspendCancellableCoroutine { cont ->
        lifecycleScope.launch {
            try {
                val announcementResp = apiService.getAnnouncement("SUP_ADMIN_ANNOUNCEMENT", token)
                val adsSuperAdminResp = apiService.getAdsSuperAdmin(postCategory, token)
                val adsAdminResp = apiService.getAdsAdmin(postCategory, token)

                // Helper function to map API response to user-friendly error message
                fun getErrorMessageForAnnouncement(response: Response<AnnouncementResponse>): String? {
                    return try {
                        if (!response.isSuccessful) {
                            val errorString = response.errorBody()?.string()
                            val json = errorString?.let { JSONObject(it) }
                            val message = json?.optString("message", "")
                            if (message.isNullOrEmpty() || !message.contains("No posts found", ignoreCase = true)) {
                                "Failed to load Admin announcements"
                            } else {
                                "\uD83D\uDCE2 No new admin announcements at the moment"
                            }
                        } else if (response.body()?.postData == null) {
                            "\uD83D\uDCE2 No new admin announcements at the moment"
                        } else {
                            null // Valid response
                        }
                    } catch (e: Exception) {
                        "Failed to load Admin announcements"
                    }
                }

                fun getErrorMessageForAds(response: Response<AdsResponse>, type: String): String? {
                    return try {
                        if (!response.isSuccessful) {
                            val errorString = response.errorBody()?.string()
                            val json = errorString?.let { JSONObject(it) }
                            val message = json?.optString("message", "")
                            if (message.isNullOrEmpty() || !message.contains("No posts found", ignoreCase = true)) {
                                "Failed to load $type announcements"
                            } else {
                                "No $type announcements available"
                            }
                        } else if (response.body()?.postData.isNullOrEmpty()) {
                            "No $type announcements available"
                        } else {
                            null // Valid response
                        }
                    } catch (e: Exception) {
                        "Failed to load $type announcements"
                    }
                }

                // Announcement error
                getErrorMessageForAnnouncement(announcementResp)?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                    cont.resume(false) {}
                    return@launch
                }

                val announcement = announcementResp.body()!!.postData!!

                // Super Admin Ads error
                getErrorMessageForAds(adsSuperAdminResp, "Super Admin")?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }

                // Admin Ads error
                getErrorMessageForAds(adsAdminResp, "Admin")?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }

                // Show popup dialog (only if announcement exists)
                recyclerView.isEnabled = false
                recyclerView.alpha = 0.4f

                val dialog = AnnouncementAdsPopupDialog.newInstance(
                    announcement,
                    adsSuperAdminResp.body(),
                    adsAdminResp.body()
                )
                dialog.show(supportFragmentManager, "AnnouncementAdsPopup")

                dialog.setOnDismissListener {
                    recyclerView.isEnabled = true
                    recyclerView.alpha = 1f
                    cont.resume(true) {}
                }

                delay(5000)
                if (dialog.isAdded && dialog.isVisible) dialog.dismiss()

            } catch (e: Exception) {
                recyclerView.isEnabled = true
                recyclerView.alpha = 1f
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                cont.resume(false) {}
            }
        }
    }







    // ------------------------------------------------------------------------------------------
    // 🔹 Lifecycle
    override fun onDestroy() {
        super.onDestroy()
        sharedReelPlayer.release()
    }

    override fun onStop() {
        super.onStop()
        sharedReelPlayer.pause()
    }

    // ------------------------------------------------------------------------------------------
    // 🔹 Toolbar actions
    override fun onSearchClicked() {
        Toast.makeText(this, "Search posts or users", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateClicked() {



        val dialog = CreatePostDialogFragment()
        dialog.onPostCreatedListener = object : OnPostCreatedListener {
            override fun onPostCreated() {
                refreshFeed()
            }
        }
        dialog.show(supportFragmentManager, "CreatePost")
    }

    private fun refreshFeed() {
        nextCursor = 0L
        hasNextPage = true
        adapter.submitList(emptyList())
        progressBar.visibility = View.VISIBLE
        loadHomePosts()
    }
}