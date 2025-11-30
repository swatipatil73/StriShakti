package com.collage.empowermentstrishakti.Common

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.collage.empowermentstrishakti.BookmarkActivity
import com.collage.empowermentstrishakti.EventListActivity
import com.collage.empowermentstrishakti.FriendListActivity
import com.collage.empowermentstrishakti.LoginActivity
import com.collage.empowermentstrishakti.NotificationActivity
import com.collage.empowermentstrishakti.PageListActivity
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.ReelActivity
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView


abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var baseSessionManager: SessionManager

    lateinit var toolbar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseSessionManager = SessionManager(this)

    }

    fun setupToolbar(
        title: String,
        showSearch: Boolean = false,
        showCreate: Boolean = false,
        @DrawableRes createIconRes: Int = R.drawable.baseline_add_circle_outline_24,
        @DrawableRes searchIconRes: Int = R.drawable.search
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
                R.id.nav_profile -> {
                    val intent = Intent(this, FriendListActivity::class.java)
                    startActivity(intent)
                }
                R.id.event -> {
                    val intent = Intent(this, EventListActivity::class.java)
                    startActivity(intent)
                }
                R.id.bookmark -> {
                val intent = Intent(this, BookmarkActivity::class.java)
                startActivity(intent)
            }

                R.id.page -> {
                    val intent = Intent(this, PageListActivity::class.java)
                    startActivity(intent)
                }
                R.id.notification -> {
                    val intent = Intent(this, NotificationActivity::class.java)
                    startActivity(intent)
                }
                R.id.delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to permanently delete your account?")
                        .setPositiveButton("Yes") { dialog, _ ->

                            val userId = baseSessionManager.getUserId().toString()
                            val token = "Bearer ${baseSessionManager.getToken()}"

                            // ✅ Call API using ApiClient.apiService
                            val call = ApiClient.apiService.deleteUser(userId, token)
                            call.enqueue(object : retrofit2.Callback<CommonResponse> {
                                override fun onResponse(
                                    call: retrofit2.Call<CommonResponse>,
                                    response: retrofit2.Response<CommonResponse>
                                ) {
                                    if (response.isSuccessful && response.body()?.status == "Success") {
                                        Toast.makeText(
                                            this@BaseActivity,
                                            response.body()?.message ?: "Account deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Clear session
                                        baseSessionManager.clear()

                                        // Redirect to LoginActivity
                                        val intent = Intent(this@BaseActivity, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@BaseActivity,
                                            "Failed to delete account",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: retrofit2.Call<CommonResponse>, t: Throwable) {
                                    Toast.makeText(
                                        this@BaseActivity,
                                        "Error: ${t.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })

                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }



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
