package com.collage.new_strishakti.Common

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.collage.new_strishakti.R
import com.google.android.material.appbar.MaterialToolbar



    abstract class BaseActivity : AppCompatActivity() {

        lateinit var toolbar: MaterialToolbar

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Child activities will setContentView()
        }

        // 🔧 Call this from child activity to setup toolbar
        fun setupToolbar(
            title: String,
            showSearch: Boolean = false,
            showCreate: Boolean = false
        ) {
            toolbar = findViewById(R.id.topAppBar)
            toolbar.title = title

            // ☰ Menu icon on the left
            toolbar.setNavigationIcon(R.drawable.outline_line_weight_24)
            toolbar.setNavigationOnClickListener {
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
            }

            // Clear right-side icons
            val menu = toolbar.menu
            menu.clear()

            // 🔍 Search icon on right (if enabled)
            if (showSearch) {
                val searchItem = menu.add("Search")
                searchItem.setIcon(R.drawable.outline_search_24)
                searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                searchItem.setOnMenuItemClickListener {
                    Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
                    onSearchClicked() // C
                    true
                }
            }

            // ➕ Create icon on right (if enabled)
            if (showCreate) {
                val createItem = menu.add("Create")
                createItem.setIcon(R.drawable.baseline_add_circle_outline_24)
                createItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                createItem.setOnMenuItemClickListener {
                    Toast.makeText(this, "Create clicked", Toast.LENGTH_SHORT).show()
                    onCreateClicked() // C
                    true
                }
            }
        }

        // Open functions to be overridden in child activities
        open fun onSearchClicked() {}
        open fun onCreateClicked() {}


        // 📡 Check internet on start
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
                    recreate() // Restart activity
                }
                .show()
        }
    }
