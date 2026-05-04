package com.collage.empowermentstrishakti

import android.app.Activity
import android.content.Context
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

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.collage.empowermentstrishakti.data.model.RefreshResponse
import com.collage.empowermentstrishakti.data.model.post.PostData
import com.collage.empowermentstrishakti.data.model.regi.LoginResponse
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import retrofit2.Call


class MainActivity : BaseActivity() {
    private var isSessionExpired = false
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
    private var sharedReelPlayer: ExoPlayer? = null

    private lateinit var postActionsVM: PostActionsViewModel   // <— add this
    private var nextCursor: Long? = 0L
    private var hasNextPage = true
    private var isLoading = false
    private val pageSize = 5
    private var pageSizeFirst = 10      // 👈 first page only 2
    private var pageSizeNext  = 15      // 👈 later pages as before
    private val REQUEST_CODE_UPDATE = 100
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

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom   // ✅ IMPORTANT FIX
            )
            insets
        }



        sessionManager = SessionManager(this)

// ✅ get saved values
     //   val savedToken = sessionManager.getToken()
        val savedToken = sessionManager.getToken()
        val expiry = sessionManager.getTokenExpiry()
      //  val expiry = "Apr 1, 2024, 4:59:15 PM"

        Log.d("SESSION_DEBUG", "Token: $savedToken")
        Log.d("SESSION_DEBUG", "Expiry: $expiry")



        val refreshToken = sessionManager.getRefreshToken()
        Log.d("SESSION_DEBUG", "Token: $refreshToken")
        if (savedToken.isNullOrEmpty()) {

            if (!refreshToken.isNullOrEmpty()) {

                // 🔥 try auto login using refresh token
                callRefreshToken(
                    sessionManager,
                    this,
                    onSuccess = { newToken ->

                        token = "Bearer $newToken"

                        // continue app
                        loadHomePosts()
                    },
                    onFailure = {

                        // refresh also failed → login
                      //  sessionManager.clearSession()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )

            } else {
                // ❌ no token + no refresh → login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            return
        }

// ✅ check login + expiry
//        if (savedToken.isNullOrEmpty() || isTokenExpired(expiry)) {
//
//            isSessionExpired = true
//
//            AlertDialog.Builder(this)
//
//                .setTitle("😔 Session Expired")
//                .setMessage("Your session has expired. Please login again.")
//                .setCancelable(false)
//                .setPositiveButton("OK") { _, _ ->
//
//                    sessionManager.clearSession()
//
//                    startActivity(Intent(this, LoginActivity::class.java))
//                    finish()
//                }
//                .show()
//
//            return
//        }
        token = "Bearer $savedToken"

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
        apiService = ApiClient.apiService

        setupToolbar(title = "Stri shakti", showSearch = false, showCreate = true)
        val postHintText = findViewById<TextView>(R.id.postHintText)

        postHintText.setOnClickListener {
            val dialog = CreatePostDialogFragment()
            dialog.show(supportFragmentManager, "CreatePostDialog")
        }

       checkForUpdate()

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



            // Start loading posts immediately (non-blocking)
            val postsJob = async { loadHomePosts() }

            lifecycleScope.launch {
                loadAnnouncementAndAdsAndShowPopup()
            }


            postsJob.await() // ensure posts visible before next load
        }
    }



    private fun checkForUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                // 🔥 Show dialog first
                AlertDialog.Builder(this)
                    .setTitle("Update Available")
                    .setMessage("A new version of the app is available. Please update to continue.")
                    .setCancelable(false)
                    .setPositiveButton("Update") { _, _ ->
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                REQUEST_CODE_UPDATE
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        }
                    }
                    .show()
            }
        }
    }


    private fun isTokenExpired(expiryDate: String?): Boolean {

        if (expiryDate.isNullOrEmpty()) return true

        return try {
            val format = java.text.SimpleDateFormat(
                "MMM dd, yyyy, hh:mm:ss a",
                java.util.Locale.ENGLISH
            )

            val expiry = format.parse(expiryDate)
            val now = java.util.Date()

            now.after(expiry)

        } catch (e: Exception) {
            true
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?

    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Update required to continue!", Toast.LENGTH_SHORT).show()

                // 🔁 Try again (force update)
                checkForUpdate()
            }
        }
    }
    // ------------------------------------------------------------------------------------------
    // 🔹 RecyclerView setup
    private fun setupRecyclerView() {
        adapter = HomeFeedAdapter(sharedReelPlayer!!, lifecycleScope,   postActionsVM
        ) // <-- pass lifecycleScope
        progressBar = findViewById(R.id.progressBar)
        emptyTextView = findViewById(R.id.emptyTextView)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(10)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisible >= totalItemCount - 1) {

                    // 🔁 If last page reached → restart
                    if (nextCursor == -1L) {
                        nextCursor = 0L
                    }

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


    // 🔹 Add these at class level (top of Activity)
    private val handler = Handler(Looper.getMainLooper())
    private var loadingToastShown = false
    private var toastRunnable: Runnable? = null


    private fun loadHomePosts() {
        if (isSessionExpired) return
        if (isLoading) return
        isLoading = true

        // ⏳ Delay toast setup
        loadingToastShown = false

        toastRunnable = Runnable {
            if (!loadingToastShown && isLoading) {
                Toast.makeText(
                    this,
                    "Loading posts, please wait...",
                    Toast.LENGTH_SHORT
                ).show()
                loadingToastShown = true
            }
        }

        handler.postDelayed(toastRunnable!!, 1000)

        val isFirstPage = nextCursor == 0L

        if (isFirstPage) {
            progressBar.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
        } else {
            Handler(Looper.getMainLooper()).post { adapter.showLoading() }
            Log.d("POST_FLOW", "API called, cursor=$nextCursor")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {

                val cursor = nextCursor ?: 0L
                val size = 4

                val result = safeApiCall {
                    apiService.getHomePosts(userId, cursor, size, token)
                }

                result.onFailure { error ->

                    if (error.message?.contains("401") == true) {

                        callRefreshToken(
                            sessionManager,
                            this@MainActivity,
                            onSuccess = { newToken ->

                                token = "Bearer $newToken"

                                // 🔥 retry same API
                                loadHomePosts()
                            },
                            onFailure = {

                              //  sessionManager.clearSession()
                                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                                finish()
                            }
                        )

                        return@onFailure   // 🔥 VERY IMPORTANT
                    }

                    Toast.makeText(
                        this@MainActivity,
                        error.message ?: "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val body = result.getOrNull()!!
                val posts = body.postsData ?: emptyList()

                // ✅ Pagination
                if (body.hasNextPage == true) {
                    nextCursor = body.nextCursor
                } else {
                    nextCursor = -1L   // 🔁 mark last page
                }

                withContext(Dispatchers.Main) {

                    val updated = if (cursor == 0L) {
                        // First load OR restart
                        posts.map { HomeFeedItem.PostItem(it) }.toMutableList()
                    } else {
                        // Append
                        val list = adapter.currentList.toMutableList()
                        list.removeAll { it is HomeFeedItem.LoadingItem }
                        list.addAll(posts.map { HomeFeedItem.PostItem(it) })
                        list
                    }

                    if (::adapter.isInitialized) {
                        adapter.submitList(updated)
                    }
                    progressBar.visibility = View.GONE
                    emptyTextView.visibility =
                        if (updated.isEmpty()) View.VISIBLE else View.GONE
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    adapter.hideLoading()
                    isLoading = false

                    // ❌ STOP delayed toast
                    toastRunnable?.let { handler.removeCallbacks(it) }
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
    // 🔹 Lifecycle
    override fun onDestroy() {
        super.onDestroy()

        // 🔥 Stop all pending handlers (important)
        if (isSessionExpired) {
            handler.removeCallbacksAndMessages(null)
        }

        sharedReelPlayer?.release()
    }

    override fun onStop() {
        super.onStop()
        sharedReelPlayer?.pause()
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
                Log.d("POST_FLOW", "Listener received — reloading feed")
                forceReloadFeed()   // now calls Activity method
            }
        }

        dialog.show(supportFragmentManager, "CreatePost")
    }

    private fun forceReloadFeed() {

        Log.d("POST_FLOW", "Force reload started")


        hasNextPage = true
        nextCursor = nextCursor?.minus(1)
        isLoading = false
        firstPageHandled = false

        adapter.submitList(emptyList())
        recyclerView.scrollToPosition(0)

        loadHomePosts()
    }




    private fun refreshFeed() {

        hasNextPage = true
        nextCursor = nextCursor?.minus(1)
        adapter.submitList(emptyList())
        progressBar.visibility = View.VISIBLE
        loadHomePosts()
    }



    override fun onResume() {
        super.onResume()

        // Reset pagination to reload fresh data

        hasNextPage = true
        nextCursor = nextCursor?.minus(1)

        loadHomePosts()
    }
    fun callRefreshToken(
        sessionManager: SessionManager,
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {

        val refreshToken = sessionManager.getRefreshToken()

        val body = mapOf("refreshToken" to (refreshToken ?: ""))

        ApiClient.apiService.refreshToken(body)
            .enqueue(object : retrofit2.Callback<RefreshResponse> {

                override fun onResponse(
                    call: retrofit2.Call<RefreshResponse>,
                    response: retrofit2.Response<RefreshResponse>
                ) {

                    val bodyRes = response.body()

                    if (response.isSuccessful && bodyRes != null) {

                        val newToken = bodyRes.token
                        val newRefresh = bodyRes.refreshToken

                        // ✅ Save new tokens
                        sessionManager.saveUserData(
                            sessionManager.getUserId(),
                            sessionManager.getUserName() ?: "",
                            newToken
                        )

                        sessionManager.saveRefreshToken(newRefresh)

                        onSuccess(newToken)

                    } else {
                        onFailure()
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<RefreshResponse>,
                    t: Throwable
                ) {
                    onFailure()
                }
            })
    }

}


