package com.collage.empowermentstrishakti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.empowermentstrishakti.Adapter.GroupAdapter
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.CreateGroupViewModelFactory
import com.collage.empowermentstrishakti.Factory.GroupListViewModelFactory
import com.collage.empowermentstrishakti.data.model.Groups.CreateGroupBottomSheet
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import com.collage.empowermentstrishakti.databinding.ActivityGroupListBinding
import com.collage.empowermentstrishakti.ui.RegisterViewModel.CreateGroupViewModel
import com.collage.empowermentstrishakti.ui.RegisterViewModel.GroupListViewModel

class GroupListActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupListBinding
    lateinit var viewModel: GroupListViewModel
    private lateinit var adapter: GroupAdapter

    private lateinit var createGroupViewModel: CreateGroupViewModel
    private var deletedGroupTemp: GroupDetail? = null

    val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupToolbar(
            title = "Groups",
            showSearch = false,
            showCreate = true,
            searchIconRes = R.drawable.baseline_group_24
        )

        setupViewModel()
        setupRecycler()
        observeGroups()
        observeDeleteResult()

        val rawUserId = session.getUserId()
        val id = when (rawUserId) {
            is Int -> rawUserId
            is String -> rawUserId.toIntOrNull() ?: -1
            else -> -1
        }

        if (id > 0) {
            viewModel.fetchGroups(id)
        } else {
            Toast.makeText(this, "User not logged in or invalid id", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewModel() {
        val rawToken = session.getToken() ?: ""
        val repo = GroupRepository(ApiClient.apiService, rawToken)

        viewModel = ViewModelProvider(this, GroupListViewModelFactory(repo))[GroupListViewModel::class.java]

        createGroupViewModel = ViewModelProvider(
            this,
            CreateGroupViewModelFactory(repo)
        )[CreateGroupViewModel::class.java]
    }

    private fun setupRecycler() {
        adapter = GroupAdapter(
            onItemClick = { group -> openGroupDetails(group) },
            onDeleteClick = { group -> confirmDelete(group) },
            onFollowClick = { group -> Toast.makeText(this, "Follow ${group.groupName}", Toast.LENGTH_SHORT).show() },
            onShareClick = { group -> Toast.makeText(this, "Share ${group.groupName}", Toast.LENGTH_SHORT).show() }
        )

        binding.recyclerGroups.layoutManager = LinearLayoutManager(this)
        binding.recyclerGroups.adapter = adapter
    }

    private fun confirmDelete(group: GroupDetail) {
        AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete \"${group.groupName}\"?")
            .setPositiveButton("Delete") { _, _ -> performDelete(group) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete(group: GroupDetail) {
        val adminId = session.getUserId().toString().toInt()
        viewModel.deleteGroup(adminId, group.groupId)
        deletedGroupTemp = group
    }

    private fun observeGroups() {
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

    private fun observeDeleteResult() {
        viewModel.deleteResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> { /* optional */ }
                is NetworkResult.Success -> {
                    // Remove deleted group immediately from adapter
                    deletedGroupTemp?.let { group ->
                        val currentList = adapter.currentList.toMutableList()
                        currentList.remove(group)
                        adapter.submitList(currentList)
                        deletedGroupTemp = null
                    }
                    Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Delete failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGroupDetails(group: GroupDetail) {
        val intent = Intent(this, GroupDetailsActivity::class.java)
        intent.putExtra("groupId", group.groupId)
        intent.putExtra("groupUUID", group.groupUUID)
        intent.putExtra("groupname", group.groupName)
        startActivity(intent)
    }

    override fun onCreateClicked() {
        val rawUserId = session.getUserId()
        val userId = when (rawUserId) {
            is Int -> rawUserId
            is String -> rawUserId.toIntOrNull() ?: -1
            else -> -1
        }

        if (userId <= 0) {
            Toast.makeText(this, "Invalid user id", Toast.LENGTH_SHORT).show()
            return
        }

        val sheet = CreateGroupBottomSheet(viewModel = createGroupViewModel, adminUserId = userId)
        sheet.show(supportFragmentManager, "CreateGroup")
    }
}
