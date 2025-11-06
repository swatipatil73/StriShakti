package com.collage.new_strishakti

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.ReelFullScreenAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Factory.ReelViewModelFactory
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.ReelRepository
import com.collage.new_strishakti.ui.RegisterViewModel.ReelViewModel
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_reel_full_screen)
        // ✅ Initialize SessionManager and get real token
        sessionManager = SessionManager(this)
        userToken = sessionManager.getToken()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerFullScreenReels)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        // ✅ Pass empty click handlers for now
        adapter = ReelFullScreenAdapter(
            onLikeClick = { reel -> /* TODO */ },
            onCommentClick = { reel -> /* TODO */ },
            onShareClick = { reel -> /* TODO */ },
            onSaveClick = { reel -> /* TODO */ }
        )
        recyclerView.adapter = adapter

        // Initial reel scroll target
        initialReelId = intent.getIntExtra("reel_id", -1)

        // Collect paging data
        lifecycleScope.launch {
            viewModel.getReels().collectLatest { pagingData ->
                adapter.submitData(pagingData)
                scrollToInitialReel()
            }
        }

        // Auto-play visible reel
        setupAutoPlayOnScroll()
    }

    private fun scrollToInitialReel() {
        if (initialReelId == -1) return
        val position = adapter.snapshot().indexOfFirst { it?.postId == initialReelId }
        if (position != -1) {
            recyclerView.scrollToPosition(position)
        }
    }

    private fun setupAutoPlayOnScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visiblePos = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (visiblePos != RecyclerView.NO_POSITION) {
                        val holder = recyclerView.findViewHolderForAdapterPosition(visiblePos)
                                as? ReelFullScreenAdapter.FullScreenViewHolder
                        holder?.playPlayer()
                    }
                } else {
                    // Pause all other players when scrolling
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

    override fun onPause() {
        super.onPause()
        for (i in 0 until recyclerView.childCount) {
            val childHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
            if (childHolder is ReelFullScreenAdapter.FullScreenViewHolder) {
                childHolder.pausePlayer()
            }
        }
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
