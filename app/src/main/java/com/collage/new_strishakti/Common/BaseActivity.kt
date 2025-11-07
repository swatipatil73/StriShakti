package com.collage.new_strishakti.Common

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.collage.new_strishakti.R
import com.collage.new_strishakti.ReelActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView


abstract class BaseActivity : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setupToolbar(
        title: String,
        showSearch: Boolean = false,
        showCreate: Boolean = false,
        createIconRes: Int = R.drawable.baseline_add_circle_outline_24 // default icon
    ) {
        toolbar = findViewById(R.id.topAppBar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        toolbar.title = title

        // ☰ Menu icon opens drawer
        toolbar.setNavigationIcon(R.drawable.outline_line_weight_24)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle drawer item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.reel -> {
                    val intent = Intent(this, ReelActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Clear right-side icons
        val menu = toolbar.menu
        menu.clear()

        // 🔍 Search icon on right
        if (showSearch) {
            val searchItem = menu.add("Search")
            searchItem.setIcon(R.drawable.outline_search_24)
            searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            searchItem.setOnMenuItemClickListener {
                onSearchClicked()
                true
            }
        }

        // ➕ or 🎥 Create icon on right
        if (showCreate) {
            val createItem = menu.add("Create")
            createItem.setIcon(createIconRes)
            createItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            createItem.setOnMenuItemClickListener {
                onCreateClicked()
                true
            }
        }
    }


    open fun onSearchClicked() {}
    open fun onCreateClicked() {}

    override fun onStart() {
        super.onStart()
        checkInternet()
    }

    private fun checkInternet() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork == null || !activeNetwork.isConnected) {
            showNoInternetDialog()
        }
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Internet")
            .setMessage("Please check your internet connection.")
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                recreate()
            }
            .show()
    }
}
