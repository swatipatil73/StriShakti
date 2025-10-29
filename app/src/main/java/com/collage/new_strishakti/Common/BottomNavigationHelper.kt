package com.collage.new_strishakti.Common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.collage.new_strishakti.MainActivity
import com.collage.new_strishakti.R
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavigationHelper {

    fun setupBottomNavigation(
        activity: AppCompatActivity,
        bottomNavigationView: BottomNavigationView,
        currentItemId: Int
    ) {
        // Set current selected item
        bottomNavigationView.selectedItemId = currentItemId

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) {
                // Already on this activity, do nothing
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.nav_home -> {
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                }
                R.id.nav_search -> {
//activity.startActivity(Intent(activity, SearchActivity::class.java))
                }
                R.id.nav_add -> {
//activity.startActivity(Intent(activity, AddActivity::class.java))
                }
                R.id.nav_notifications -> {
                   // activity.startActivity(Intent(activity, NotificationsActivity::class.java))
                }
                R.id.nav_profile -> {
                    //activity.startActivity(Intent(activity, ProfileActivity::class.java))
                }
                else -> return@setOnItemSelectedListener false
            }
            // Optional: Remove animation for seamless feel
            activity.overridePendingTransition(0, 0)
            true
        }
    }
}
