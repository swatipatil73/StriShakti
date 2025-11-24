package com.collage.new_strishakti

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.BookmarkAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.BookmarkViewModelFactory
import com.collage.new_strishakti.data.model.SavedPost.SavedPostData
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.SavedPostsRepository
import com.collage.new_strishakti.databinding.ActivityBookmarkBinding
import com.collage.new_strishakti.ui.RegisterViewModel.BookmarkViewModel

class BookmarkActivity : AppCompatActivity(),
    BookmarkAdapter.Listener,
    FullScreenPopupDialog.PopupListener {

    private lateinit var binding: ActivityBookmarkBinding
    private lateinit var viewModel: BookmarkViewModel
    private lateinit var adapter: BookmarkAdapter
    private lateinit var session: SessionManager

    private var isLoadingMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // edge-to-edge support (optional)
        enableEdgeToEdge()

        // initialize view binding BEFORE using binding.*
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // apply system bar insets to root padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        session = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupViewModel()

        // Initial load: session.getUserId() returns Int -> convert to Long
        val userIdLong = session.getUserId()
        val token = "Bearer ${session.getToken()}"

        viewModel.loadInitial(userIdLong, token, size = 9)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = BookmarkAdapter(mutableListOf(), this)
        val gridLayoutManager = GridLayoutManager(this, 3)
        binding.recyclerBookmark.layoutManager = gridLayoutManager
        binding.recyclerBookmark.adapter = adapter

        // Pagination
        binding.recyclerBookmark.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                if (isLoadingMore) return

                val lm = rv.layoutManager as GridLayoutManager
                val total = lm.itemCount
                val last = lm.findLastVisibleItemPosition()
                if (total <= last + 6) {
                    val userIdLong = session.getUserId()
                    val token = "Bearer ${session.getToken()}"
                    viewModel.loadNext(userIdLong, token)
                }
            }
        })
    }

    private fun setupViewModel() {
        val repo = SavedPostsRepository(ApiClient.apiService)
        val factory = BookmarkViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory).get(BookmarkViewModel::class.java)

        viewModel.savedPosts.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                ViewUtils.showEmptyState(binding.root, true, "No saved posts")
            } else {
                ViewUtils.showEmptyState(binding.root, false)
                adapter.setItems(list)
            }
        }

        viewModel.loading.observe(this) {
            isLoadingMore = it
        }

        viewModel.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    // OPEN POPUP
    override fun onItemClick(item: SavedPostData, position: Int) {
        val dialog = FullScreenPopupDialog(item, this)
        dialog.show(supportFragmentManager, "fullscreen")
    }



    // delete callback (not implemented server-side here)
    override fun onDeleteClicked(post: SavedPostData) {
        showDeleteAlert(post)
    }


    fun showDeleteAlert(post: SavedPostData) {
        AlertDialog.Builder(this)
            .setTitle("Delete Saved Post")
            .setMessage("Are you sure you want to remove this saved post?")
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()

                // Perform delete here
                val userId = session.getUserId().toLong()
                val token = "Bearer ${session.getToken()}"
                viewModel.deleteSavedPost(userId, post.postId, token)

                Toast.makeText(this, "Deleting…", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

}
