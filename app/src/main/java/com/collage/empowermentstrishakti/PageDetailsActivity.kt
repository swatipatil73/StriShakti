package com.collage.empowermentstrishakti



import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.PageDetailViewModelFactory
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.PagesRepository

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.lifecycle.ViewModelProvider
import com.collage.empowermentstrishakti.Adapter.PageDetailPagerAdapter
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel

class PageDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PUUID = "extra_puuid"

        fun start(context: Context, puuid: String) {
            val i = Intent(context, PageDetailsActivity::class.java)
            i.putExtra(EXTRA_PUUID, puuid)
            context.startActivity(i)
        }
    }

    private lateinit var imgPageCover: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: PageDetailPagerAdapter
    private lateinit var vm: PageDetailViewModel
    private lateinit var session: SessionManager
    private var puuid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_details)

        // handle insets (optional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // read puuid from the single canonical extra
        puuid = intent.getStringExtra(EXTRA_PUUID)

        imgPageCover = findViewById(R.id.imgPageCover)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        session = SessionManager(this)

        // ViewModel initialization (use factory if your PagesRepository needs injection)
        val repo = PagesRepository(ApiClient.apiService)
        val factory = PageDetailViewModelFactory(repo)
        vm = ViewModelProvider(this, factory).get(PageDetailViewModel::class.java)

        // after creating pagerAdapter:
        pagerAdapter = PageDetailPagerAdapter(this)
        viewPager.adapter = pagerAdapter

// pass puuid primitive immediately (adapter creates fragments with puuid arg)
        pagerAdapter.setPuuid(puuid)
        pagerAdapter.setCurrentUserId(session.getUserId())

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Posts"
                1 -> "About"
                else -> "Followers"
            }
        }.attach()

// Observe page data and update UI / adapter
        vm.pageDetails.observe(this) { resp ->
            resp?.let { r ->
                // Set cover image safely
                Glide.with(this)
                    .load(r.pageAbout?.pageCoverProfileImagePath)
                    .centerCrop()
                    .into(imgPageCover)

                // compute isAdmin and update adapter so fragments get this flag in their args
                val isAdmin = (r.pageAbout?.adminId == session.getUserId())
                pagerAdapter.setIsAdmin(isAdmin)

                // NOTE: fragments should observe vm.postDetails / vm.followers for lists
                // No need to pass post list or members via fragment args anymore
            }
        }

//        vm.error.observe(this) { err ->
//            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
//        }

        // Load details (use session userId + token)
        val currentUserId = session.getUserId()
        val token = "Bearer ${session.getToken()}"
        puuid?.let { vm.loadPageDetails(it, currentUserId, page = 0, size = 50, token = token) }
            ?: run {
                Toast.makeText(this, "Page id missing", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
