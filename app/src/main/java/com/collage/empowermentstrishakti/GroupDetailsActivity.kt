package com.collage.empowermentstrishakti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Adapter.GroupPagerAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.GroupViewModelFactory
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.databinding.ActivityGroupDetailsBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.GroupViewModel
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Response

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailsBinding
    private lateinit var session: SessionManager
    private lateinit var viewModel: GroupViewModel

    private var groupId = -1
    private var groupUUID = ""
    private var groupname = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        session = SessionManager(this)

        groupId = intent.getIntExtra("groupId", -1)
        groupUUID = intent.getStringExtra("groupUUID") ?: ""
        groupname = intent.getStringExtra("groupname") ?: ""

        // after setContentView(...)
        binding.ivRightIcon.setOnClickListener {
            val intent = Intent(this, GroupChatActivity::class.java)
            intent.putExtra("GROUP_ID", groupId)
            intent.putExtra("GROUP_NAME", groupname)
            startActivity(intent)
        }



        // use binding.toolbar (safer)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = if (groupname.isNotBlank()) groupname else "Group"
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }

        setupViewModel()
        observeData()

        viewModel.loadGroupDetails(
            session.getUserId(),
            groupUUID,
            0,
            5,
            "Bearer ${session.getToken()}"
        )
    }


//    private fun setupToolbar() {
//        setSupportActionBar(binding.toolbar)
//        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
//    }

    private fun setupViewModel() {
        val rawToken = session.getToken() // returns String?
        val tokenToUse = rawToken ?: ""
        val repo = GroupRepository(ApiClient.apiService, tokenToUse)

        viewModel = ViewModelProvider(
            this,
            GroupViewModelFactory(repo)
        )[GroupViewModel::class.java]
    }


    private fun observeData() {

        viewModel.loading.observe(this) {
            binding.progressCenter.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.groupDetails.observe(this) { response ->

            // Cover image
            Glide.with(this)
                .load(response.groupAbout?.groupCoverProfileImagePath)
                .placeholder(R.drawable.baseline_group_24)
                .into(binding.imgPageCover)

            // Setup ViewPager
            val adapter = GroupPagerAdapter(this, response)
            binding.viewPager.adapter = adapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
                tab.text = listOf("Posts", "About", "Followers")[pos]
            }.attach()
        }
    }
}
