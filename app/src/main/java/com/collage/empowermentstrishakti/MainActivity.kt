package com.collage.empowermentstrishakti

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
import com.collage.empowermentstrishakti.Adapter.HomeFeedAdapter
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.BottomNavigationHelper
import com.collage.empowermentstrishakti.Common.OnPostCreatedListener
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.post.AnnouncementAdsPopupDialog
import com.collage.empowermentstrishakti.data.model.post.HomeFeedItem
import com.collage.empowermentstrishakti.data.model.post.HomeViewModelFactory
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.HomeRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.HomeViewModel
import com.collage.empowermentstrishakti.data.network.ApiService

import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.collage.empowermentstrishakti.data.model.post.AdsResponse
import com.collage.empowermentstrishakti.data.model.post.AnnouncementResponse
import com.collage.empowermentstrishakti.data.model.post.PostActionsVMFactory
import com.collage.empowermentstrishakti.data.network.safeApiCall
import com.collage.empowermentstrishakti.data.repository.PostActionsRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionEvent
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PostActionsViewModel
import org.json.JSONObject
import retrofit2.Response

class MainActivity : BaseActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var bottomProgressBar: ProgressBar   // ✅ Added
    private lateinit var emptyTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigationView: BottomNavigationView
    private var firstPageHandled = false
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
    private var pageSizeFirst = 2      // 👈 first page only 2
    private val pageSizeNext  = 5      // 👈 later pages as before

    private val postCategory = "YOUR_POST_CATEGORY"
    // replace this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        sessionManager = SessionManager(this)
        apiService = ApiClient.apiService

        setupToolbar(title = "Stri shakti", showSearch = false, showCreate = true)
        val postHintText = findViewById<TextView>(R.id.postHintText)

        postHintText.setOnClickListener {
            val dialog = CreatePostDialogFragment()
            dialog.show(supportFragmentManager, "CreatePostDialog")
        }

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

            lifecycleScope.launch {
                loadAnnouncementAndAdsAndShowPopup()
            }


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

//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
//                val layoutManager = rv.layoutManager as LinearLayoutManager
//                val lastVisible = layoutManager.findLastVisibleItemPosition()
//                val totalItemCount = layoutManager.itemCount
//
//                // Trigger pagination when reaching near end
//                if (!isLoading && hasNextPage && lastVisible >= totalItemCount - 2) {
//                    loadHomePosts()
//                }
//            }
//        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var lastTriggerTime = 0L
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val now = System.currentTimeMillis()
                if (now - lastTriggerTime < 350) return   // 👈 debounce

                val lm = rv.layoutManager as LinearLayoutManager
                val lastVisible = lm.findLastVisibleItemPosition()
                val total = lm.itemCount

                if (!isLoading && hasNextPage && lastVisible >= total - 2) {
                    lastTriggerTime = now
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
            if (firstPageHandled) { // prevent early "no posts" flash
                emptyTextView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            if (!loading) firstPageHandled = true
        }

        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
//    private fun loadHomePosts() {
//        if (isLoading || !hasNextPage) return
//        isLoading = true
//
//        val isFirstPage = nextCursor == 0L
//
//        if (isFirstPage) {
//            progressBar.visibility = View.VISIBLE
//            emptyTextView.visibility = View.GONE
//        } else {
//            // 🧠 Delay ensures RecyclerView renders footer before API response arrives
//            Handler(Looper.getMainLooper()).post {
//                adapter.showLoading()
//            }
//        }
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val cursor = nextCursor ?: 0L
//                val response = apiService.getHomePosts(userId, cursor, pageSize, token)
//
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    val posts = body?.postsData ?: emptyList()
//
//                    nextCursor = body?.nextCursor
//                    hasNextPage = body?.hasNextPage ?: false
//
//                    val updatedList = adapter.currentList.toMutableList()
//
//                    // remove loading if already visible
//                    updatedList.removeAll { it is HomeFeedItem.LoadingItem }
//
//                    val postItems = posts.map { HomeFeedItem.PostItem(it) }
//                    updatedList.addAll(postItems)
//
//                    // add reel section only after first 5 posts on first load
//                    if (cursor == 0L) {
//                        val reelsResponse = apiService.getAllReels(0, 10, token)
//                        if (reelsResponse.isSuccessful) {
//                            val reels = reelsResponse.body()?.postsData ?: emptyList()
//                            if (reels.isNotEmpty()) {
//                                val insertIndex = if (updatedList.size >= 5) 5 else updatedList.size
//                                updatedList.add(insertIndex, HomeFeedItem.ReelSection(reels))
//                            }
//                        }
//                    }
//
//                    withContext(Dispatchers.Main) {
//                        progressBar.visibility = View.GONE
//                        adapter.submitList(updatedList)
//                        emptyTextView.visibility =
//                            if (updatedList.isEmpty()) View.VISIBLE else View.GONE
//                    }
//
//                } else {
//                    hasNextPage = false
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@MainActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//                }
//            } finally {
//                withContext(Dispatchers.Main) {
//                    adapter.hideLoading()
//                    progressBar.visibility = View.GONE
//                    isLoading = false
//                }
//            }
//        }
//    }


    private fun loadHomePosts() {
        if (isLoading || !hasNextPage) return
        isLoading = true

        val isFirstPage = nextCursor == 0L
        if (isFirstPage) {
            progressBar.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
        } else {
            Handler(Looper.getMainLooper()).post { adapter.showLoading() }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cursor = nextCursor ?: 0L
                val size = if (isFirstPage) pageSizeFirst else pageSizeNext

                // ── 1) Posts with safeApiCall
                val result = safeApiCall { apiService.getHomePosts(userId, cursor, size, token) }

                result.onFailure { error ->
                    withContext(Dispatchers.Main) {
                        adapter.hideLoading()
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@MainActivity, error.message ?: "Failed to load posts", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val body = result.getOrNull()!!
                val posts = body.postsData ?: emptyList()
                nextCursor = body.nextCursor
                hasNextPage = body.hasNextPage == true

                // ── 2) FIRST: show posts immediately (UI thread)
                withContext(Dispatchers.Main) {
                    val updated = adapter.currentList.toMutableList()
                    updated.removeAll { it is HomeFeedItem.LoadingItem }
                    updated.addAll(posts.map { HomeFeedItem.PostItem(it) })
                    adapter.submitList(updated)
                    progressBar.visibility = View.GONE
                    emptyTextView.visibility = if (updated.isEmpty()) View.VISIBLE else View.GONE
                }

                // ── 3) THEN: fetch reels asynchronously (only for first page)
                if (cursor == 0L) {
                    launch(Dispatchers.IO) {
                        val reelsResult = safeApiCall { apiService.getAllReels(0, 5, token) }
                        reelsResult.onSuccess { reelsResp ->
                            val reels = reelsResp.postsData.orEmpty()
                            if (reels.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val list = adapter.currentList.toMutableList()
                                    val insertIndex = if (list.size >= 5) 5 else list.size
                                    list.add(insertIndex, HomeFeedItem.ReelSection(reels))
                                    adapter.submitList(list)
                                }
                            }
                        }
                        // onFailure: ignore silently; feed already visible
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    adapter.hideLoading()
                    isLoading = false
                }
            }
        }
    }

    // Call this from lifecycleScope.launch { val ok = loadAnnouncementAndAdsAndShowPopup() }
    private suspend fun loadAnnouncementAndAdsAndShowPopup(): Boolean {
        return try {
            // 1) do network on IO
            val announcementResp: Response<AnnouncementResponse>
            val adsSuperAdminResp: Response<AdsResponse>
            val adsAdminResp: Response<AdsResponse>

            withContext(Dispatchers.IO) {
                announcementResp = apiService.getAnnouncement("SUP_ADMIN_ANNOUNCEMENT", token)
                adsSuperAdminResp = apiService.getAdsSuperAdmin(postCategory, token)
                adsAdminResp = apiService.getAdsAdmin(postCategory, token)
            }

            // 2) basic error handling (keep the same logic you had)
            fun getErrorMessageForAnnouncement(response: Response<AnnouncementResponse>): String? {
                return try {
                    if (!response.isSuccessful) {
                        val errorString = response.errorBody()?.string()
                        val json = errorString?.let { JSONObject(it) }
                        val message = json?.optString("message", "")
                        if (message.isNullOrEmpty() || !message.contains("No posts found", true)) {
                            "Failed to load Admin announcements"
                        } else {
                            "\uD83D\uDCE2 No new admin announcements at the moment"
                        }
                    } else if (response.body()?.postData == null) {
                        "\uD83D\uDCE2 No new admin announcements at the moment"
                    } else {
                        null
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
                        if (message.isNullOrEmpty() || !message.contains("No posts found", true)) {
                            "Failed to load $type announcements"
                        } else {
                            "No $type announcements available"
                        }
                    } else if (response.body()?.postData.isNullOrEmpty()) {
                        "No $type announcements available"
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    "Failed to load $type announcements"
                }
            }

            getErrorMessageForAnnouncement(announcementResp)?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
                return false
            }

            val announcement = announcementResp.body()?.postData ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "\uD83D\uDCE2 No new admin announcements at the moment", Toast.LENGTH_LONG).show()
                }
                return false
            }

            // show toasts for ads errors but continue
            getErrorMessageForAds(adsSuperAdminResp, "Super Admin")?.let {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() }
            }
            getErrorMessageForAds(adsAdminResp, "Admin")?.let {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() }
            }

            // 3) switch to Main to show dialog and await dismissal
            return withContext(Dispatchers.Main) {
                // disable interaction while popup visible
                recyclerView.isEnabled = false
                recyclerView.alpha = 0.4f

                val dialog = AnnouncementAdsPopupDialog.newInstance(
                    announcement,
                    adsSuperAdminResp.body(),
                    adsAdminResp.body()
                )

                // make suspendable wait for dismissal
                suspendCancellableCoroutine<Boolean> { cont ->
                    // show dialog
                    dialog.show(supportFragmentManager, "AnnouncementAdsPopup")

                    dialog.setOnDismissListener {
                        recyclerView.isEnabled = true
                        recyclerView.alpha = 1f
                        if (!cont.isCompleted) cont.resume(true) {}
                    }

                    // safety timeout
                    val killJob = lifecycleScope.launch {
                        delay(5000)
                        if (dialog.isAdded && dialog.isVisible) dialog.dismiss()
                    }

                    cont.invokeOnCancellation {
                        // ensure dialog closed and UI restored if caller cancels
                        if (dialog.isAdded) dialog.dismiss()
                        killJob.cancel()
                        recyclerView.isEnabled = true
                        recyclerView.alpha = 1f
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                recyclerView.isEnabled = true
                recyclerView.alpha = 1f
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            false
        }
    }


    // ------------------------------------------------------------------------------------------
//    private suspend fun loadAnnouncementAndAdsAndShowPopup(): Boolean = suspendCancellableCoroutine { cont ->
//        lifecycleScope.launch {
//            try {
//                val announcementResp = apiService.getAnnouncement("SUP_ADMIN_ANNOUNCEMENT", token)
//                val adsSuperAdminResp = apiService.getAdsSuperAdmin(postCategory, token)
//                val adsAdminResp = apiService.getAdsAdmin(postCategory, token)
//
//                // Helper function to map API response to user-friendly error message
//                fun getErrorMessageForAnnouncement(response: Response<AnnouncementResponse>): String? {
//                    return try {
//                        if (!response.isSuccessful) {
//                            val errorString = response.errorBody()?.string()
//                            val json = errorString?.let { JSONObject(it) }
//                            val message = json?.optString("message", "")
//                            if (message.isNullOrEmpty() || !message.contains("No posts found", ignoreCase = true)) {
//                                "Failed to load Admin announcements"
//                            } else {
//                                "\uD83D\uDCE2 No new admin announcements at the moment"
//                            }
//                        } else if (response.body()?.postData == null) {
//                            "\uD83D\uDCE2 No new admin announcements at the moment"
//                        } else {
//                            null // Valid response
//                        }
//                    } catch (e: Exception) {
//                        "Failed to load Admin announcements"
//                    }
//                }
//
//                fun getErrorMessageForAds(response: Response<AdsResponse>, type: String): String? {
//                    return try {
//                        if (!response.isSuccessful) {
//                            val errorString = response.errorBody()?.string()
//                            val json = errorString?.let { JSONObject(it) }
//                            val message = json?.optString("message", "")
//                            if (message.isNullOrEmpty() || !message.contains("No posts found", ignoreCase = true)) {
//                                "Failed to load $type announcements"
//                            } else {
//                                "No $type announcements available"
//                            }
//                        } else if (response.body()?.postData.isNullOrEmpty()) {
//                            "No $type announcements available"
//                        } else {
//                            null // Valid response
//                        }
//                    } catch (e: Exception) {
//                        "Failed to load $type announcements"
//                    }
//                }
//
//                // Announcement error
//                getErrorMessageForAnnouncement(announcementResp)?.let {
//                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
//                    cont.resume(false) {}
//                    return@launch
//                }
//
//                val announcement = announcementResp.body()!!.postData!!
//
//                // Super Admin Ads error
//                getErrorMessageForAds(adsSuperAdminResp, "Super Admin")?.let {
//                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
//                }
//
//                // Admin Ads error
//                getErrorMessageForAds(adsAdminResp, "Admin")?.let {
//                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
//                }
//
//                // Show popup dialog (only if announcement exists)
//                recyclerView.isEnabled = false
//                recyclerView.alpha = 0.4f
//
//                val dialog = AnnouncementAdsPopupDialog.newInstance(
//                    announcement,
//                    adsSuperAdminResp.body(),
//                    adsAdminResp.body()
//                )
//                dialog.show(supportFragmentManager, "AnnouncementAdsPopup")
//
//                dialog.setOnDismissListener {
//                    recyclerView.isEnabled = true
//                    recyclerView.alpha = 1f
//                    cont.resume(true) {}
//                }
//
//                delay(5000)
//                if (dialog.isAdded && dialog.isVisible) dialog.dismiss()
//
//            } catch (e: Exception) {
//                recyclerView.isEnabled = true
//                recyclerView.alpha = 1f
//                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
//                cont.resume(false) {}
//            }
//        }
//    }







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