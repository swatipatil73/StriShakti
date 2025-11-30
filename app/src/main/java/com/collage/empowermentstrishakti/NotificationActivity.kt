package com.collage.empowermentstrishakti

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.empowermentstrishakti.Adapter.NotificationAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.NotificationViewModelFactory
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Notification.NotificationItem

import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.network.ApiService
import com.collage.empowermentstrishakti.data.repository.NotificationRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.NotificationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: NotificationAdapter
    private lateinit var viewModel: NotificationViewModel

    private lateinit var rvNotifications: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sessionManager = SessionManager(this)
        rvNotifications = findViewById(R.id.rvNotifications)
        progress = findViewById(R.id.progress)
        tvEmpty = findViewById(R.id.tvEmpty)

        val api = ApiClient.apiService
        val repo = NotificationRepository(api)
        val factory = NotificationViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)



        viewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)

        adapter = NotificationAdapter(mutableListOf()) { item ->
            onNotificationClicked(item)
        }

        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        viewModel.notifications.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                tvEmpty.visibility = android.view.View.VISIBLE
            } else {
                tvEmpty.visibility = android.view.View.GONE
            }
            adapter.setItems(list ?: emptyList())
        }

        viewModel.isLoading.observe(this) { loading ->
            progress.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.error.observe(this) { err ->
            if (!err.isNullOrBlank()) {
                android.widget.Toast.makeText(this, err, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        val token = getAuthTokenOrEmpty()
        val userId = getUserIdOrZero()

        if (token.isBlank() || userId == 0L) {
            android.widget.Toast.makeText(this, "Auth missing", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.loadNotifications(token, userId)
    }

    private fun onNotificationClicked(item: NotificationItem) {
        val token = getAuthTokenOrEmpty()
        val userId = getUserIdOrZero()

        if (token.isBlank() || userId == 0L) {
            showMessageOrImage(item)
            return
        }

        // Call server: mark ALL notifications READ
        viewModel.markAllRead(token, userId).observe(this) {
            // even if failed, still show content
            showMessageOrImage(item)
        }
    }

    private fun showMessageOrImage(item: NotificationItem) {
        val url = extractFirstUrl(item.notificationMessage)
        if (url != null) showImageDialog(url)
        else showTextDialog(item)
    }

    private fun showTextDialog(item: NotificationItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Notification")
            .setMessage(item.notificationMessage ?: "")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showImageDialog(imageUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_image_fullscreen)

        val imageView = dialog.findViewById<ImageView>(R.id.ivFull)
        imageView.adjustViewBounds = true

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.imageplacehoder)
            .into(imageView)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.show()
    }

    private fun extractFirstUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val regex = "(https?://\\S+)".toRegex()
        return regex.find(text)?.value
    }

    private fun getAuthTokenOrEmpty(): String {
        val rawToken = sessionManager.getToken() ?: ""

        if (rawToken.isBlank()) return ""

        return if (rawToken.startsWith("Bearer")) rawToken else "Bearer $rawToken"
    }


    private fun getUserIdOrZero(): Long {
        val id = sessionManager.getUserId()
        return if (id <= 0) 0L else id.toLong()
    }

}
