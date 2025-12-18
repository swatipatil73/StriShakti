package com.collage.empowermentstrishakti.Common

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.collage.empowermentstrishakti.CreateEventActivity
import com.collage.empowermentstrishakti.CreatePostDialogFragment
import com.collage.empowermentstrishakti.GroupListActivity
import com.collage.empowermentstrishakti.LoginActivity
import com.collage.empowermentstrishakti.MainActivity
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.ReelActivity
import com.collage.empowermentstrishakti.UserProfileActivity
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
                    // show CreatePostDialogFragment
                    if (!activity.isFinishing) {
                        val dialog = CreatePostDialogFragment()
                        dialog.show(activity.supportFragmentManager, "CreatePostDialog")
                    }
                }
                R.id.profile -> {
                    //activity.startActivity(Intent(activity, UserProfileActivity::class.java))
                    activity.startActivity(Intent(activity, GroupListActivity::class.java))
                }
                R.id.reeldis -> {
                    activity.startActivity(Intent(activity, ReelActivity::class.java))
                }

                R.id.nav_profile -> {
                    AlertDialog.Builder(activity)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            // Clear session or preferences


                            // Navigate to login screen
                            val intent = Intent(activity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            activity.startActivity(intent)
                            activity.finish()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }

                else -> return@setOnItemSelectedListener false
            }
            // Optional: Remove animation for seamless feel
            activity.overridePendingTransition(0, 0)
            true
        }
    }
}


