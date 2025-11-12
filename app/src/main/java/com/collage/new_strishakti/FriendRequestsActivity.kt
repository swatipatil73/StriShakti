package com.collage.new_strishakti

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.new_strishakti.Adapter.FriendRequestAdapter
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.FriendRequestsViewModelFactory
import com.collage.new_strishakti.data.model.friend.FriendRequestItem
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.FriendRepository
import com.collage.new_strishakti.databinding.ActivityFriendRequestsBinding
import com.collage.new_strishakti.databinding.EmptyStateLayoutBinding
import com.collage.new_strishakti.ui.RegisterViewModel.FriendRequestsViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendRequestsActivity : BaseActivity() {

    private lateinit var binding: ActivityFriendRequestsBinding
    private lateinit var emptyBinding: EmptyStateLayoutBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: FriendRequestAdapter

    private val viewModel: FriendRequestsViewModel by viewModels {
        val repo = FriendRepository(ApiClient.apiService, SessionManager(this))
        FriendRequestsViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        emptyBinding = binding.emptyStateLayout

        setupToolbar(
            title = "Friend Requests",
            showSearch = false,
            showCreate = false
        )

        sessionManager = SessionManager(this)

        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        val repo = FriendRepository(ApiClient.apiService, sessionManager)

        adapter = FriendRequestAdapter(mutableListOf(), object : FriendRequestAdapter.Listener {
            override fun onConfirm(item: FriendRequestItem, pos: Int) {
                val reqId = item.friendRequestId ?: run {
                    Toast.makeText(this@FriendRequestsActivity, "Invalid request id", Toast.LENGTH_SHORT).show()
                    return
                }

                val vh = binding.rvRequests.findViewHolderForAdapterPosition(pos)
                val row = vh?.itemView
                val btnConfirm = row?.findViewById<Button>(R.id.btnConfirm)
                val btnDelete  = row?.findViewById<Button>(R.id.btnReject)
                btnConfirm?.isEnabled = false
                btnDelete?.isEnabled  = false

                lifecycleScope.launch {
                    try {
                        val res = withContext(Dispatchers.IO) { repo.approveFriendRequest(reqId) }
                        val ok = res.isSuccessful && res.body()?.status.equals("Success", true)

                        if (ok) {
                            adapter.removeAt(pos)
                            Toast.makeText(
                                this@FriendRequestsActivity,
                                res.body()?.message ?: "Friend request approved",
                                Toast.LENGTH_SHORT
                            ).show()

                            if (adapter.itemCount == 0) {
                                binding.rvRequests.visibility = View.GONE
                                ViewUtils.showEmptyState(binding.root, true, "Data is not available")
                            }
                        } else {
                            btnConfirm?.isEnabled = true
                            btnDelete?.isEnabled  = true
                            Toast.makeText(
                                this@FriendRequestsActivity,
                                res.body()?.message ?: "Failed: ${res.code()} ${res.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        btnConfirm?.isEnabled = true
                        btnDelete?.isEnabled  = true
                        Toast.makeText(
                            this@FriendRequestsActivity,
                            "Error: ${e.localizedMessage ?: "Something went wrong"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onDelete(item: FriendRequestItem, pos: Int) {
                val reqId = item.friendRequestId ?: run {
                    Toast.makeText(this@FriendRequestsActivity, "Invalid request id", Toast.LENGTH_SHORT).show()
                    return
                }

                val vh = binding.rvRequests.findViewHolderForAdapterPosition(pos)
                val row = vh?.itemView
                val btnConfirm = row?.findViewById<Button>(R.id.btnConfirm)
                val btnDelete  = row?.findViewById<Button>(R.id.btnReject)
                btnConfirm?.isEnabled = false
                btnDelete?.isEnabled  = false

                lifecycleScope.launch {
                    try {
                        val res = withContext(Dispatchers.IO) { repo.rejectFriendRequest(reqId) }
                        val ok = res.isSuccessful && res.body()?.status.equals("Success", true)

                        if (ok) {
                            adapter.removeAt(pos)
                            Toast.makeText(
                                this@FriendRequestsActivity,
                                res.body()?.message ?: "Friend request deleted",
                                Toast.LENGTH_SHORT
                            ).show()

                            if (adapter.itemCount == 0) {
                                binding.rvRequests.visibility = View.GONE
                                ViewUtils.showEmptyState(binding.root, true, "Data is not available")
                            }
                        } else {
                            btnConfirm?.isEnabled = true
                            btnDelete?.isEnabled  = true
                            Toast.makeText(
                                this@FriendRequestsActivity,
                                res.body()?.message ?: "Failed: ${res.code()} ${res.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        btnConfirm?.isEnabled = true
                        btnDelete?.isEnabled  = true
                        Toast.makeText(
                            this@FriendRequestsActivity,
                            "Error: ${e.localizedMessage ?: "Something went wrong"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

        binding.rvRequests.adapter = adapter

        observeData()

        val receiverId = sessionManager.getUserId()
        viewModel.fetchFriendRequests(receiverId)
    }

    private fun observeData() {
        viewModel.state.observe(this, Observer { st ->
            when (st) {
                FriendRequestsViewModel.UI.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.rvRequests.visibility = View.GONE
                    ViewUtils.showEmptyState(binding.root, false, "")
                }
                is FriendRequestsViewModel.UI.Success -> {
                    binding.progress.visibility = View.GONE
                    binding.rvRequests.visibility = View.VISIBLE
                    ViewUtils.showEmptyState(binding.root, false, "")
                    adapter.setData(st.list)
                }
                FriendRequestsViewModel.UI.Empty -> {
                    binding.progress.visibility = View.GONE
                    binding.rvRequests.visibility = View.GONE
                    ViewUtils.showEmptyState(binding.root, true, "Data is not available")
                }
                is FriendRequestsViewModel.UI.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.rvRequests.visibility = View.GONE
                    ViewUtils.showEmptyState(binding.root, true, "Failed to load requests")
                    Toast.makeText(this, st.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
