package com.collage.empowermentstrishakti



import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.WindowCompat
import com.collage.empowermentstrishakti.Adapter.PagesAdapter
import com.collage.empowermentstrishakti.Common.BaseActivity
import com.collage.empowermentstrishakti.Common.EndlessRecyclerViewScrollListener
import com.collage.empowermentstrishakti.Common.ViewUtils
import com.collage.empowermentstrishakti.Factory.PagesViewModelFactory
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.repository.PagesRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.PagesViewModel
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.PageDetail

import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog

class PageListActivity : BaseActivity(), PagesAdapter.Callback, CreatePageDialogFragment.Callback {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PagesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var vm: PagesViewModel
    private lateinit var progressBarCenter: ProgressBar
    private lateinit var progressBarBottom: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var emptyText: TextView

    private var fullList: MutableList<PageDetail> = mutableListOf()
    private var endlessListener: EndlessRecyclerViewScrollListener? = null
    private val pageSize = 5

    // single-session helper
    private val session by lazy { SessionManager(this) }
    private data class PendingAction(val pagesId: Int, val prevFollowState: Boolean, val position: Int)
    private var pendingAction: PendingAction? = null
    var callback: CreatePageDialogFragment.Callback? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_list)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Let content draw behind system bars and handle insets manually
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // apply insets to toolbar / bottom nav / content
        val root = findViewById<View>(R.id.main)
        val topAppBar = findViewById<View>(R.id.topAppBar)
        val bottomNav = findViewById<View>(R.id.bottomNavigationView)
        val fragmentContainer = findViewById<View>(R.id.fragment_container)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        WindowInsetsControllerCompat(window, root).isAppearanceLightNavigationBars = false

        // Toolbar from BaseActivity
        setupToolbar(title = "Pages", showSearch = true, showCreate = true)

        // find views AFTER setContentView
        recyclerView = findViewById(R.id.recyclerView)
        progressBarCenter = findViewById(R.id.progressBar)
        progressBarBottom = findViewById(R.id.bottomProgressBar)
        searchInput = findViewById(R.id.etSearch)
        emptyText = findViewById(R.id.emptyTextView)

        // adapter & recycler
        adapter = PagesAdapter(
            callback = this,
            currentUserId = session.getUserId()   // logged-in userId
        )

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // viewmodel & repo
        val repo = PagesRepository(ApiClient.apiService)
        val factory = PagesViewModelFactory(repo)
        vm = ViewModelProvider(this, factory).get(PagesViewModel::class.java)
        val uid = session.getUserId()
        val token = buildToken()
        vm.setMode(showOwnMode = true, userId = uid, token = token, size = pageSize)

        // Observe "createdPage" so we insert new page at top instantly
        vm.createdPage.observe(this) { created ->
            created?.let { newPage ->
                // Don't insert duplicate if server list already contains it
                val already = fullList.any { it.pagesId != 0 && it.pagesId == newPage.pagesId
                        || (!it.puuid.isNullOrBlank() && it.puuid == newPage.puuid) }

                if (!already) {
                    // Prepend new page as newest-first
                    fullList.add(0, newPage)

                    // Submit new snapshot
                    adapter.submitList(fullList.toList())

                    // Scroll to top to make it visible
                    recyclerView.post { layoutManager.scrollToPosition(0) }
                } else {
                    // If server already has it, optionally move it to top:
                    fullList = fullList.filterNot {
                        (it.pagesId != 0 && it.pagesId == newPage.pagesId) ||
                                (!it.puuid.isNullOrBlank() && it.puuid == newPage.puuid)
                    }.toMutableList().apply { add(0, newPage) }
                    adapter.submitList(fullList.toList())
                    recyclerView.post { layoutManager.scrollToPosition(0) }
                }

                // Consume the single-event
                vm.clearCreatedPage()
            }
        }


        // Observe items
        // Observe action results to show feedback and revert optimistic UI on failure


        // Observe items (add this)
        vm.items.observe(this) { list ->
            // Convert API list into newest-first if API returns oldest-first
            // If your API already returns newest-first, remove `.asReversed()`
            val incoming = list.asReversed().toMutableList()

            // If we have locally-inserted created item at index 0 (from vm.createdPage observer),
            // ensure we don't duplicate it: keep the local item and merge server list excluding duplicates.
            val localFirst = fullList.firstOrNull()
            if (localFirst != null && incoming.isNotEmpty()) {
                val duplicateExists = incoming.any { it.pagesId != 0 && it.pagesId == localFirst.pagesId
                        || (!it.puuid.isNullOrBlank() && it.puuid == localFirst.puuid) }
                if (duplicateExists) {
                    // remove any server-side duplicate to keep our local-first ordering
                    incoming.removeAll { srv ->
                        (localFirst.pagesId != 0 && srv.pagesId == localFirst.pagesId) ||
                                (!localFirst.puuid.isNullOrBlank() && srv.puuid == localFirst.puuid)
                    }
                    // ensure local stays first, then append rest
                    fullList = mutableListOf(localFirst).apply { addAll(incoming) }
                } else {
                    // no duplicate, just replace fullList with server data (fresh)
                    fullList = incoming
                }
            } else {
                // no local-first special case, use server data
                fullList = incoming
            }

            val currentQuery = searchInput.text?.toString() ?: ""
            if (currentQuery.isBlank()) {
                adapter.submitList(fullList.toList())
            } else {
                applySearchFilter(currentQuery)
            }

            // empty state handling
            if (fullList.isEmpty()) {
                ViewUtils.showEmptyState(findViewById(R.id.main), true, "No pages available")
                emptyText.visibility = View.VISIBLE
            } else {
                ViewUtils.showEmptyState(findViewById(R.id.main), false)
                emptyText.visibility = View.GONE
            }
        }



        vm.actionStatus.observe(this) { pair ->
            val (success, message) = pair
            message?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }

            if (success) {
                // keep optimistic update — clear pending
                pendingAction = null
            } else {
                // failure -> revert optimistic update if possible
                pendingAction?.let { pa ->
                    val item = adapter.getItemAt(pa.position)
                    if (item != null && item.pagesId == pa.pagesId) {
                        // revert item to previous follow state
                        val reverted = item.copy(isPageFollowed = pa.prevFollowState)
                        adapter.updateItemAt(pa.position, reverted)
                    } else {
                        // fallback: refresh list
                        vm.loadInitial(userId = session.getUserId(), token = buildToken(), size = pageSize)
                    }
                }
                pendingAction = null
            }
        }

        vm.isActionLoading.observe(this) { loading ->
            // optional: disable scrolling/interactions while action running
            recyclerView.isEnabled = !loading
        }

        // Loading indicators
        vm.isInitialLoading.observe(this) { loading ->
            progressBarCenter.visibility = if (loading) View.VISIBLE else View.GONE
        }
        vm.isLoading.observe(this) { loading ->
            progressBarBottom.visibility = if (loading && (vm.items.value?.isNotEmpty() == true)) View.VISIBLE else View.GONE
        }

        // Errors
        vm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        // Observe mode (all vs own) to update UI hint or give feedback
        vm.showOwn.observe(this) { showingOwn ->
            if (showingOwn) {
                searchInput.hint = "Showing: My Pages"
                // optionally change toolbar icon state (not implemented here)
            } else {
                searchInput.hint = "Search pages"
            }
        }

        // endless scroll: call vm.loadNext with Int userId and token
        endlessListener = EndlessRecyclerViewScrollListener(layoutManager, threshold = 3) {
            val uid = session.getUserId()
            val token = buildToken()
            if (vm.hasNextPage.value == true && vm.isLoading.value == false) {
                vm.loadNext(uid, token)
            }
        }
        recyclerView.addOnScrollListener(endlessListener!!)

        // search watcher - client-side filtering of loaded list
        searchInput.imeOptions = EditorInfo.IME_ACTION_SEARCH
        searchInput.addTextChangedListener { editable: Editable? ->
            applySearchFilter(editable?.toString() ?: "")
        }

        // initial load (default mode = all pages)
        startLoading()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val drawer = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)

                // hide keyboard if visible
                try {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    currentFocus?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
                } catch (_: Exception) { /* ignore */ }

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    // disable this callback and let the system handle the back press (will call Activity.onBackPressed)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

// attach with lifecycle of the activity (automatically removed)
        onBackPressedDispatcher.addCallback(this, backCallback)

    }

    override fun onPageCreated(newPage: PageDetail) {
        // Keep fullList as newest-first (index 0 = newest)
        // If fullList is empty (or null), ensure it's initialized
        if (fullList == null) fullList = mutableListOf()
        fullList.add(0, newPage)

        // Submit to adapter (ListAdapter + DiffUtil will animate)
        adapter.submitList(fullList.toList())

        // ensure top is visible
        recyclerView.post {
            layoutManager.scrollToPosition(0)
        }

        Toast.makeText(this, "Page created successfully!", Toast.LENGTH_SHORT).show()
    }



    private fun startLoading() {
        endlessListener?.resetState()
        val token = buildToken()
        val userId = session.getUserId()
        vm.loadInitial(userId = userId, token = token, size = pageSize)
    }

    private fun buildToken(): String {
        val raw = session.getToken() ?: ""
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun applySearchFilter(query: String) {
        if (query.isBlank()) {
            adapter.submitList(fullList.toList())
            return
        }
        val q = query.trim().lowercase()
        val filtered = fullList.filter { it.pageName?.lowercase()?.contains(q) == true }
        // filtered is already newest-first because fullList is newest-first
        adapter.submitList(filtered)
    }

    // Toolbar action (in BaseActivity toolbar): toggle between all/own pages
    // Toolbar action (in BaseActivity toolbar): toggle to show ALL pages and focus search
    override fun onSearchClicked() {
        val uid = session.getUserId()
        val token = buildToken()
        // ensure we show ALL pages when search is tapped
        vm.setMode(showOwnMode = false, userId = uid, token = token, size = pageSize)

        // clear any previous query and focus the input
        searchInput.setText("")
        searchInput.requestFocus()

        // optionally open keyboard
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        } catch (_: Exception) { }
    }


    override fun onCreateClicked() {
        val dialog = CreatePageDialogFragment()
        dialog.callback = this   // <-- IMPORTANT
        dialog.show(supportFragmentManager, "CreatePageDialog")
    }



    override fun onDeleteClicked(page: PageDetail, position: Int) {
        val pageId = page.pagesId
        val adminId = page.adminId   // API param (pageAdminUserId)

        AlertDialog.Builder(this)
            .setTitle("Delete Page")
            .setMessage("Do you want to delete this page?")
            .setPositiveButton("Delete") { _, _ ->

                // Store pending action if you want to revert later on failure
                pendingAction = PendingAction(
                    pagesId = pageId,
                    prevFollowState = page.isPageFollowed,
                    position = position
                )

                val token = buildToken()

                // <--- FIX: use 'pagesId' param name (not pageId) ----
                vm.deletePage(
                    pageAdminUserId = adminId,
                    pagesId = pageId,
                    token = token
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    // Adapter callbacks
    override fun onPageClicked(page: PageDetail) {
        // TODO: open PageDetailsActivity, pass puuid or parcelable PageDetail
        Toast.makeText(this, "Open: ${page.pageName}", Toast.LENGTH_SHORT).show()
    }

    override fun onFollowClicked(page: PageDetail, position: Int) {
        if (position < 0) return

        val currentlyFollowed = page.isPageFollowed
        val title = if (!currentlyFollowed) "Follow page" else "Unfollow page"
        val msg = if (!currentlyFollowed) "Do you want to follow this page?" else "Do you want to unfollow this page?"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(if (!currentlyFollowed) "Follow" else "Unfollow") { _, _ ->
                // optimistic UI update performed only after user confirms
                val updated = page.copy(isPageFollowed = !currentlyFollowed)
                adapter.updateItemAt(position, updated)

                // small visible animation for feedback (only if visible)
                val vh = recyclerView.findViewHolderForAdapterPosition(position) as? PagesAdapter.PageViewHolder
                vh?.let { holder ->
                    holder.itemView.animate().alpha(0.85f).setDuration(100).withEndAction {
                        holder.itemView.animate().alpha(1f).setDuration(120).start()
                    }.start()
                    try {
                        val btn = holder.itemView.findViewById<View>(R.id.btnFollow)
                        btn.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                            btn.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                        }.start()
                    } catch (_: Exception) { }
                }

                // store pending action to revert on failure
                pendingAction = PendingAction(pagesId = page.pagesId, prevFollowState = currentlyFollowed, position = position)

                // call ViewModel API
                val uid = session.getUserId()
                val token = buildToken()
                if (!currentlyFollowed) {
                    vm.followPage(userId = uid, pagesId = page.pagesId, token = token)
                } else {
                    vm.unfollowPage(userId = uid, pagesId = page.pagesId, pageAdminUserId = page.adminId, token = token)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }



    override fun onShareClicked(page: PageDetail) {
        // share intent or copy link
        Toast.makeText(this, "Share: ${page.pageName}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        endlessListener?.let { recyclerView.removeOnScrollListener(it) }
    }


}
