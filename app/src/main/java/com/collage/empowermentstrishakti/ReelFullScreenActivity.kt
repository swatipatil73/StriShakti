package com.collage.empowermentstrishakti

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.ReelViewModelFactory
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.LikeRepository
import com.collage.empowermentstrishakti.data.repository.ReelRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.ReelViewModel
import com.example.app.reels.ReelFullScreenAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReelFullScreenActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReelFullScreenAdapter
    private lateinit var sessionManager: SessionManager
    private var userToken: String? = null

    private val viewModel: ReelViewModel by viewModels {
        ReelViewModelFactory(ReelRepository(ApiClient.apiService, userToken ?: ""))
    }

    private var initialReelId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reel_full_screen)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // --- Session and Token Setup ---
        sessionManager = SessionManager(this)
        userToken = sessionManager.getToken()

        // --- Handle Edge Insets (Full Screen UI) ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- RecyclerView Setup ---
        recyclerView = findViewById(R.id.recyclerFullScreenReels)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        // --- Adapter Setup ---
        adapter = ReelFullScreenAdapter(
            onLikeClick = { reel, position ->
                lifecycleScope.launch {
                    try {
                        val token = sessionManager.getToken() ?: ""
                        val userId = sessionManager.getUserId()
                        var updatedReel = reel.copy()

                        if (reel.userReactStatus == true) {
                            val response = LikeRepository.unlikePost(userId.toString(), reel.postId.toString(), token)
                            if (response.isSuccessful && response.body()?.status == "Success") {
                                updatedReel = updatedReel.copy(
                                    userReactStatus = false,
                                    totalCountOFReact = (reel.totalCountOFReact ?: 1) - 1
                                )
                            }
                        } else {
                            val response = LikeRepository.likePost(userId.toString(), reel.postId.toString(), token)
                            if (response.isSuccessful && response.body()?.status == "Success") {
                                updatedReel = updatedReel.copy(
                                    userReactStatus = true,
                                    totalCountOFReact = (reel.totalCountOFReact ?: 0) + 1
                                )
                            }
                        }

                        // 🔥 Refresh UI immediately
                        adapter.updateReelAt(position, updatedReel)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            ,
            onCommentClick = { reel ->
                val intent = Intent(this, CommentActivity::class.java)
                intent.putExtra("postId", reel.postId)
                intent.putExtra("postOwnerUsername", reel.userName)
                startActivity(intent)
            },
            onShareClick = { reel ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, reel.postImageURl)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Reel"))
            },
            onDeleteClick = { reel ->
                AlertDialog.Builder(this@ReelFullScreenActivity)
                    .setTitle("Delete Reel")
                    .setMessage("Are you sure you want to delete this reel?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.dismiss()



                        lifecycleScope.launch {
                            try {
                                val token = sessionManager.getToken() ?: ""
                                val repository = ReelRepository(ApiClient.apiService, token)
                                val result = repository.deleteReel(reel.postId, token)


                                if (result.isSuccess) {
                                    val response = result.getOrNull()
                                    if (response?.status == "Success") {
                                        Toast.makeText(
                                            this@ReelFullScreenActivity,
                                            response.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        adapter.refresh()
                                    } else {
                                        Toast.makeText(
                                            this@ReelFullScreenActivity,
                                            "Failed to delete reel",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@ReelFullScreenActivity,
                                        "Error deleting reel",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {

                                e.printStackTrace()
                                Toast.makeText(
                                    this@ReelFullScreenActivity,
                                    "Something went wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss() // ❌ Cancel deletion
                    }
                    .show()
            }



        )

        recyclerView.adapter = adapter

        // --- Get Initial Reel to Scroll To ---
        initialReelId = intent.getIntExtra("reel_id", -1)

        // --- Load Data from ViewModel ---
        lifecycleScope.launch {
            viewModel.getReels().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // --- Scroll to initial reel after first load ---
        adapter.addLoadStateListener { loadState ->
            val isNotLoading = loadState.source.refresh is LoadState.NotLoading
            if (isNotLoading) {
                scrollToInitialReel()
            }
        }

        // --- Auto Play Feature ---
        setupAutoPlayOnScroll()
    }

    private fun scrollToInitialReel() {
        if (initialReelId == -1) return
        val position = adapter.snapshot().indexOfFirst { it?.postId == initialReelId }
        if (position != -1) recyclerView.scrollToPosition(position)
    }

    private fun setupAutoPlayOnScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Pause all
                    for (i in 0 until recyclerView.childCount) {
                        val childHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
                        if (childHolder is ReelFullScreenAdapter.FullScreenViewHolder) {
                            childHolder.pausePlayer()
                        }
                    }

                    // Play visible reel
                    val visiblePos = layoutManager.findFirstVisibleItemPosition()
                    if (visiblePos != RecyclerView.NO_POSITION) {
                        val holder =
                            recyclerView.findViewHolderForAdapterPosition(visiblePos)
                                    as? ReelFullScreenAdapter.FullScreenViewHolder
                        holder?.playPlayer()
                    }
                } else {
                    // Pause all while scrolling
                    for (i in 0 until recyclerView.childCount) {
                        val childHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
                        if (childHolder is ReelFullScreenAdapter.FullScreenViewHolder) {
                            childHolder.pausePlayer()
                        }
                    }
                }
            }
        })



    }




    override fun onDestroy() {
        super.onDestroy()
        for (i in 0 until recyclerView.childCount) {
            val childHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
            if (childHolder is ReelFullScreenAdapter.FullScreenViewHolder) {
                childHolder.releasePlayer()
            }
        }
    }
}
