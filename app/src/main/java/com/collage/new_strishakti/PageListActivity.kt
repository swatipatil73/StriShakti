package com.collage.new_strishakti

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.PagesAdapter
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.EndlessRecyclerViewScrollListener
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.PagesViewModelFactory
import com.collage.new_strishakti.data.model.PageDetail
import com.collage.new_strishakti.data.network.ApiClient
import com.collage.new_strishakti.data.repository.PagesRepository
import com.collage.new_strishakti.ui.RegisterViewModel.PagesViewModel
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatDelegate
import com.collage.new_strishakti.Common.SessionManager


class PageListActivity : BaseActivity(), PagesAdapter.Callback {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PagesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var vm: PagesViewModel
    private lateinit var progressBarCenter: ProgressBar
    private lateinit var progressBarBottom: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var emptyText: TextView

    // keep full list for client-side filtering
    private var fullList: MutableList<PageDetail> = mutableListOf()

    // endless scroll listener reference so we can reset if needed
    private var endlessListener: EndlessRecyclerViewScrollListener? = null

    private val pageSize = 5
   private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_list)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

            // Toolbar from BaseActivity will be found by setupToolbar
        setupToolbar(title = "Pages", showSearch = true, showCreate = true)
        sessionManager = SessionManager(this)
        // find views
        recyclerView = findViewById(R.id.recyclerView)
        progressBarCenter = findViewById(R.id.progressBar)
        progressBarBottom = findViewById(R.id.bottomProgressBar)
        searchInput = findViewById(R.id.etSearch)
        emptyText = findViewById(R.id.emptyTextView)

        // init adapter
        adapter = PagesAdapter(this)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // repository & viewmodel
        val repo = PagesRepository(ApiClient.apiService)
        val factory = PagesViewModelFactory(repo)
        vm = ViewModelProvider(this, factory).get(PagesViewModel::class.java)

        // observe ViewModel
        vm.items.observe(this) { list ->
            fullList = list.toMutableList()
            applySearchFilter(searchInput.text?.toString() ?: "")
            // toggle empty state
            if (list.isEmpty()) {
                ViewUtils.showEmptyState(findViewById(R.id.main), true, "No pages available")
                emptyText.visibility = View.VISIBLE
                emptyText.text = "No pages available"
            } else {
                ViewUtils.showEmptyState(findViewById(R.id.main), false)
                emptyText.visibility = View.GONE
            }
        }

        vm.isInitialLoading.observe(this) { loading ->
            progressBarCenter.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.isLoading.observe(this) { loading ->
            // bottom progress shown only when appending pages (not initial)
            progressBarBottom.visibility = if (loading && (vm.items.value?.isNotEmpty() == true)) View.VISIBLE else View.GONE
        }

        vm.error.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // endless scroll
        endlessListener = EndlessRecyclerViewScrollListener(layoutManager, threshold = 3) {
            // Called when near end; only load if server has next
            val token = buildToken()
            val userId = sessionManager.getUserId()
            if (vm.hasNextPage.value == true && vm.isLoading.value == false) {
                vm.loadNext(userId, token)
            }
        }
        recyclerView.addOnScrollListener(endlessListener!!)

        // search text watcher (client-side filter)
        searchInput.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applySearchFilter(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // start initial load
        startLoading()
    }

    private fun startLoading() {
        // reset scroll listener state & viewmodel
        endlessListener?.resetState()
        val token = buildToken()
        val userId = sessionManager.getUserId()
        vm.loadInitial(userId = userId, token = token, size = pageSize)
    }

    private fun buildToken(): String {
        val raw = sessionManager.getToken() ?: ""
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun applySearchFilter(query: String) {
        if (query.isBlank()) {
            adapter.submitList(fullList.toList())
            return
        }
        val q = query.trim().lowercase()
        val filtered = fullList.filter { it.pageName?.lowercase()?.contains(q) == true }
        adapter.submitList(filtered)
    }

    // BaseActivity toolbar icons callbacks
    override fun onSearchClicked() {
        // focus the search input and open keyboard automatically (optional)
        searchInput.requestFocus()
    }

    override fun onCreateClicked() {
        // open create page screen or show create dialog (implement later)
        Toast.makeText(this, "Create clicked", Toast.LENGTH_SHORT).show()
    }

    // PagesAdapter callbacks
    override fun onPageClicked(page: PageDetail) {
        // open PageDetailsActivity later; pass puuid or parcelable
        Toast.makeText(this, "Open: ${page.pageName}", Toast.LENGTH_SHORT).show()
    }

    override fun onFollowClicked(page: PageDetail, position: Int) {
        // optimistic UI already toggled in adapter. Call follow/unfollow API here.
        // On success update ViewModel.updateItem(updatedPage)
        Toast.makeText(this, "Follow clicked: ${page.pageName}", Toast.LENGTH_SHORT).show()
    }

    override fun onShareClicked(page: PageDetail) {
        // share intent
        Toast.makeText(this, "Share: ${page.pageName}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // detach listener to avoid leaks
        endlessListener?.let { recyclerView.removeOnScrollListener(it) }
    }
}