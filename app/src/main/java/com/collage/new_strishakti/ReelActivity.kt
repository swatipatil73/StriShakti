package com.collage.new_strishakti

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.ReelAdapter
import android.content.Intent

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.collage.new_strishakti.Common.SessionManager

import com.collage.new_strishakti.Factory.ReelViewModelFactory
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.ReelRepository
import com.collage.new_strishakti.ui.RegisterViewModel.ReelViewModel

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ReelActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReelAdapter
    private lateinit var sessionManager: SessionManager
    private var userToken: String? = null

    // ViewModel is created later, after we have the token
    private val viewModel: ReelViewModel by viewModels {
        ReelViewModelFactory(ReelRepository(ApiClient.apiService, userToken ?: ""))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reel)

        // ✅ Initialize SessionManager before using it
        sessionManager = SessionManager(this)
        userToken = sessionManager.getToken() // Add "Bearer " prefix automatically

        recyclerView = findViewById(R.id.recyclerReels)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        adapter = ReelAdapter { reel ->
            openFullScreenReel(reel)
        }
        recyclerView.adapter = adapter

        // ✅ Load reels from the ViewModel using coroutine
        lifecycleScope.launch {
            viewModel.getReels().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun openFullScreenReel(reel: Reel) {
        val intent = Intent(this, ReelFullScreenActivity::class.java).apply {
            putExtra("reel_id", reel.postId)
            putExtra("reel_url", reel.postImageURl)
        }
        startActivity(intent)
    }
}