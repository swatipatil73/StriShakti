package com.collage.empowermentstrishakti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.GroupAdapter
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.GroupListViewModelFactory
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.databinding.ActivityGroupListBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.GroupListViewModel


class GroupListActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupListBinding
    private lateinit var viewModel: GroupListViewModel
    private lateinit var adapter: GroupAdapter

    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If you have an edge-to-edge helper, keep it
        enableEdgeToEdge()

        // IMPORTANT: initialize view binding BEFORE using `binding`
        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets (uses the root view from binding)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupToolbar(
            title = "Groups",
            showSearch = false,
            showCreate = true,
            searchIconRes = R.drawable.baseline_group_24 // change as needed
        )

        setupViewModel()
        setupRecycler()
        observe()

        // Get user id from session manager
        val rawUserId = session.getUserId()
        if (rawUserId != null) {
            val id = when (rawUserId) {
                is Int -> rawUserId
                is String -> rawUserId.toIntOrNull() ?: -1
                else -> -1
            }
            if (id > 0) {
                viewModel.fetchGroups(id)
            } else {
                Toast.makeText(this, "Invalid user id", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val rawToken = session.getToken() // returns String?
        val tokenToUse = rawToken ?: ""
        val repo = GroupRepository(ApiClient.apiService, tokenToUse)
        val factory = GroupListViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory).get(GroupListViewModel::class.java)
    }



    private fun setupRecycler() {
        adapter = GroupAdapter(
            onItemClick = { group -> openGroupDetails(group) },
            onFollowClick = { group -> Toast.makeText(this, "Follow ${group.groupName}", Toast.LENGTH_SHORT).show() },
            onShareClick = { group -> Toast.makeText(this, "Share ${group.groupName}", Toast.LENGTH_SHORT).show() }
        )

        binding.recyclerGroups.layoutManager = LinearLayoutManager(this)
        binding.recyclerGroups.adapter = adapter
    }

    private fun observe() {
        viewModel.groups.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(result.data)
                    binding.tvEmpty.visibility = if (result.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGroupDetails(group: GroupDetail) {
        val intent = Intent(this, GroupDetailsActivity::class.java)
        intent.putExtra("groupId", group.groupId)
        intent.putExtra("groupUUID", group.groupUUID)
        startActivity(intent)
    }

    override fun onCreateClicked() {
        Toast.makeText(this, "Create Group clicked", Toast.LENGTH_SHORT).show()
    }

}