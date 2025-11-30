package com.collage.empowermentstrishakti


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Adapter.PageDetailPagerAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.PageDetailViewModelFactory
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PageDetailViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PageDetailsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PUUID = "extra_puuid"
        private const val EXTRA_PNAME = "extra_pname"

        fun start(context: Context, puuid: String, pageName: String) {
            val i = Intent(context, PageDetailsActivity::class.java)
            i.putExtra(EXTRA_PUUID, puuid)
            i.putExtra(EXTRA_PNAME, pageName)
            context.startActivity(i)
        }
    }

    // views
    private lateinit var imgPageCover: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    // adapter + vm + session
    private lateinit var pagerAdapter: PageDetailPagerAdapter
    private lateinit var vm: PageDetailViewModel
    private lateinit var session: SessionManager

    // extras/state
    private var puuid: String? = null
    private var pageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_details)

        // read extras FIRST so toolbar can show the passed title immediately
        puuid = intent.getStringExtra(EXTRA_PUUID)
        pageName = intent.getStringExtra(EXTRA_PNAME)

        // handle insets (optional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // toolbar setup (your layout already has a Toolbar inside the AppBarLayout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true) // let ActionBar show title
            title = pageName ?: "" // show page name beside back arrow (from intent)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // views
        imgPageCover = findViewById(R.id.imgPageCover)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        session = SessionManager(this)

        // ViewModel initialization (use factory if your PagesRepository needs injection)
        val repo = PagesRepository(ApiClient.apiService)
        val sessionManager = SessionManager(this)
        val factory = PageDetailViewModelFactory(repo, sessionManager)
        vm = androidx.lifecycle.ViewModelProvider(this, factory).get(PageDetailViewModel::class.java)

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

                // update toolbar title from server if available (fallback to intent pageName)
                val serverName = r.pageAbout?.pageName
                supportActionBar?.title = serverName ?: pageName ?: ""

                // compute isAdmin and update adapter so fragments get this flag in their args
                val isAdmin = (r.pageAbout?.adminId == session.getUserId())
                pagerAdapter.setIsAdmin(isAdmin)

                // (fragments should observe vm.postDetails / vm.followers for lists)
            }
        }

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
