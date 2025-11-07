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
import android.view.View
import android.widget.Toast

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.BottomNavigationHelper
import com.collage.new_strishakti.Common.SessionManager

import com.collage.new_strishakti.Factory.ReelViewModelFactory
import com.collage.new_strishakti.R
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.ReelRepository
import com.collage.new_strishakti.ui.RegisterViewModel.ReelViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ReelActivity :BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReelAdapter
    private lateinit var sessionManager: SessionManager
    private var userToken: String? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: ReelViewModel by viewModels {
        ReelViewModelFactory(ReelRepository(ApiClient.apiService, userToken ?: ""))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reel)
//        setupToolbar(title = "Stri shakti", showSearch = false, showCreate = true)
        setupToolbar(
            title = "Stri Shakti",
            showSearch = false,
            showCreate = true,
            createIconRes = R.drawable.baseline_photo_camera_24 // 🎥 camera icon
        )


        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(this, bottomNavigationView, R.id.nav_home)
        sessionManager = SessionManager(this)
        userToken = sessionManager.getToken()

        recyclerView = findViewById(R.id.recyclerReels)

        val sglm = StaggeredGridLayoutManager(
            3, StaggeredGridLayoutManager.VERTICAL
        ).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }
        recyclerView.layoutManager = sglm
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(GridSpacingDecoration(2)) // 2dp gap

        adapter = ReelAdapter { reel -> openFullScreenReel(reel) }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.getReels().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun openFullScreenReel(reel: Reel) {
        startActivity(Intent(this, ReelFullScreenActivity::class.java).apply {
            putExtra("reel_id", reel.postId)
            putExtra("reel_url", reel.postImageURl)
        })
    }

    class GridSpacingDecoration(private val spaceDp: Int) : RecyclerView.ItemDecoration() {
        private fun Int.dp(v: View) = (this * v.resources.displayMetrics.density).toInt()
        override fun getItemOffsets(
            outRect: android.graphics.Rect, v: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val s = spaceDp.dp(v)
            outRect.set(s, s, s, s)
        }
    }
    override fun onCreateClicked() {
        val dialog = CreateReelDialogFragment()
        dialog.onReelCreatedListener = object : OnReelCreatedListener {
            override fun onReelCreated() {
                refreshReelFeed()
            }

            private fun refreshReelFeed() {
                adapter.refresh()
            }

        }
        dialog.show(supportFragmentManager, "CreateReel")
    }




}

