package com.collage.new_strishakti

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.collage.new_strishakti.Adapter.ChatPagerAdapter
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.databinding.ActivityChatMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class ChatMainActivity : BaseActivity() {

    private lateinit var binding: ActivityChatMainBinding
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Initialize binding
        binding = ActivityChatMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2️⃣ Edge-to-edge support
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        setupForTab(0)
        setupViewPager()
    }


    private fun setupViewPager() {
        val adapter = ChatPagerAdapter(this)
        adapter.addFragment(PersonalChatFragment(), "Personal")
        adapter.addFragment(GroupChatFragment(), "Groups")

        binding.viewPagerChat.adapter = adapter

        TabLayoutMediator(binding.tabLayoutChat, binding.viewPagerChat) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Fix: correct callback
        binding.viewPagerChat.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    setupForTab(position)
                }
            }
        )
    }

    private fun setupForTab(tabIndex: Int) {
        when (tabIndex) {
            0 -> setupToolbar("Personal Chats", showSearch = true, showCreate = false)
            1 -> setupToolbar("Groups", showSearch = true, showCreate = true)
        }
    }

    override fun onSearchClicked() {
        val pos = binding.viewPagerChat.currentItem

        if (pos == 0) {
            Toast.makeText(this, "Search Personal", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Search Groups", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateClicked() {
        Toast.makeText(this, "Create Group Clicked", Toast.LENGTH_SHORT).show()
    }
}