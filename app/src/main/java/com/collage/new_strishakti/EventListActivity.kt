package com.collage.new_strishakti

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.new_strishakti.Adapter.EventAdapter
import com.collage.new_strishakti.Common.BaseActivity
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Factory.EventViewModelFactory
import com.collage.new_strishakti.data.model.Event.Event
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.repository.EventRepository
import com.collage.new_strishakti.databinding.ActivityEventListBinding
import com.collage.new_strishakti.ui.RegisterViewModel.EventViewModel

class EventListActivity : BaseActivity() {

    private lateinit var binding: ActivityEventListBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var adapter: EventAdapter

    // session and user info
    private lateinit var session: SessionManager
    private var userId: Int = -1
    private var userToken: String? = null

    // defaults (change if needed)
    private val defaultStateId = 14
    private val initialPage = 0
    private val pageSize = 5

    // local copy for search filtering
    private var currentEventList: List<Event> = emptyList()

    // pagination local flag (mirrors viewModel.loadingMore)
    private var isLoadingMoreLocal = false

    // holds eventId we're currently deleting (so we can remove on success)
    private var pendingDeleteEventId: Int? = null

    // mode flag
    private var isShowingOwnEvents = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding
        binding = ActivityEventListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // optional: edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar(
            title = "Events",
            showSearch = true,
            showCreate = true,
            createIconRes = R.drawable.baseline_add_circle_outline_24
        )

        // session
        session = SessionManager(this)
        userId = session.getUserId()
        userToken = session.getToken()
        if (userToken.isNullOrBlank()) userToken = null

        setupViewModel()
        setupRecycler()
        setupObservers()
        setupSearch()

        // initial load
        viewModel.loadDistricts(defaultStateId)
    }

    override fun onSearchClicked() {
        // toggle between all events and own events
        isShowingOwnEvents = !isShowingOwnEvents

        if (isShowingOwnEvents) {
            // switch to own events
            binding.topAppBar.title = "My Events"
            binding.textInputLayoutSearch.visibility = View.GONE
            binding.spinnerDistrict.visibility = View.GONE

            // load events where hostUserId == current userId
            val auth = userToken ?: session.getToken()?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
            viewModel.loadHostEvents(userId, initialPage, pageSize, auth)
        } else {
            // switch back to full list
            binding.topAppBar.title = "Events"
            binding.textInputLayoutSearch.visibility = View.VISIBLE
            binding.spinnerDistrict.visibility = View.VISIBLE

            val selected = binding.spinnerDistrict.selectedItem as? District
            val districtId = selected?.districtId ?: 0
            viewModel.clearHostMode()
            viewModel.loadEvents(userId, districtId, initialPage, pageSize, userToken)
        }
    }

    override fun onCreateClicked() {
        val intent = Intent(this, CreateEventActivity::class.java)
        startActivity(intent)
    }

    private fun setupViewModel() {
        // Use constructor-injected repo (ApiClient + SessionManager) if you followed earlier suggestions.
        val repo = EventRepository()
        val factory = EventViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory).get(EventViewModel::class.java)
    }

    private fun setupRecycler() {
        adapter = EventAdapter(
            applicationContext,
            session.getUserId()
        ) { event ->
            // Confirm before deleting
            AlertDialog.Builder(this)
                .setTitle("Delete event")
                .setMessage("Are you sure you want to delete this event?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    // guard nulls
                    val hostId = event.hostUserId ?: -1
                    val evId = event.eventId ?: -1
                    if (hostId <= 0 || evId <= 0) {
                        Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // call ViewModel to delete
                    pendingDeleteEventId = evId
                    viewModel.deleteEvent(hostId, evId, session.getToken())
                }
                .show()
        }

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerEvents.layoutManager = layoutManager
        binding.recyclerEvents.adapter = adapter

        // Pagination scroll listener
        binding.recyclerEvents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return // only check when scrolling down

                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                // trigger when user scrolls near the end (2 items left)
                val loadMoreThreshold = 2
                if (!isLoadingMoreLocal
                    && viewModel.hasNextPage.value == true
                    && lastVisible >= totalItemCount - 1 - loadMoreThreshold
                ) {
                    // request next page
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupObservers() {
        // Districts loaded -> populate spinner
        viewModel.districts.observe(this) { districts ->
            populateDistrictSpinner(districts)
        }

        // Events loaded or appended
        viewModel.events.observe(this) { events ->
            currentEventList = events ?: emptyList()

            // ViewModel appends internally; set full list
            adapter.setItems(currentEventList)

            // Apply active search if needed (only when not in own-events mode)
            val q = binding.etSearch.text?.toString() ?: ""
            if (!isShowingOwnEvents && q.isNotBlank()) updateListForSearch(q)

            binding.tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        // loading flags
        viewModel.loading.observe(this) { /* optional UI */ }
        viewModel.loadingMore.observe(this) { loadingMore ->
            isLoadingMoreLocal = loadingMore
        }

        // delete result handling
        viewModel.deleteResult.observe(this) { result ->
            result?.let { (success, message) ->
                if (success) {
                    Toast.makeText(this, message ?: "Event deleted", Toast.LENGTH_SHORT).show()

                    // Remove the item using adapter.removeItemById() for smooth UX
                    val idToRemove = pendingDeleteEventId
                    if (idToRemove != null) {
                        adapter.removeItemById(idToRemove)
                        currentEventList = currentEventList.filterNot { it.eventId == idToRemove }
                        pendingDeleteEventId = null
                    } else {
                        // fallback: reload list
                        val selected = binding.spinnerDistrict.selectedItem as? District
                        val districtId = selected?.districtId ?: 0
                        viewModel.loadEvents(userId, districtId, initialPage, pageSize, userToken)
                    }
                } else {
                    Toast.makeText(this, message ?: "Delete failed", Toast.LENGTH_SHORT).show()
                    pendingDeleteEventId = null
                }
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun populateDistrictSpinner(districts: List<District>?) {
        val list = ArrayList<District>()
        list.add(District(0, "All Districts"))
        if (!districts.isNullOrEmpty()) list.addAll(districts)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistrict.adapter = spinnerAdapter

        // optionally auto-select
        for (i in list.indices) {
            if (list[i].districtId == 26) {
                binding.spinnerDistrict.setSelection(i)
                break
            }
        }

        val selected = binding.spinnerDistrict.selectedItem as? District
        val initialDistrictId = selected?.districtId ?: 0
        viewModel.loadEvents(userId, initialDistrictId, initialPage, pageSize, userToken)

        binding.spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val d = parent?.getItemAtPosition(position) as? District
                val district = d?.districtId ?: 0
                if (!isShowingOwnEvents) {
                    viewModel.loadEvents(userId, district, initialPage, pageSize, userToken)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateListForSearch(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* no-op */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* no-op */ }
        })
    }

    private fun updateListForSearch(query: String) {
        if (query.isBlank()) {
            adapter.setItems(currentEventList)
            binding.tvEmpty.visibility = if (currentEventList.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        val q = query.trim().lowercase()
        val filtered = currentEventList.filter { ev ->
            val title = (ev.eventName ?: ev.postName ?: "").lowercase()
            val district = (ev.districtName ?: "").lowercase()
            val category = (ev.categoryName ?: "").lowercase()
            title.contains(q) || district.contains(q) || category.contains(q)
        }

        adapter.setItems(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // refresh user info in case of login/logout changes while activity is paused
        val updatedUserId = session.getUserId()
        val updatedToken = session.getToken()
        if (updatedUserId != userId || updatedToken != userToken) {
            userId = updatedUserId
            userToken = if (updatedToken.isNullOrBlank()) null else updatedToken
            // reload current data depending on mode
            if (isShowingOwnEvents) {
                val auth = userToken ?: session.getToken()?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
                viewModel.loadHostEvents(userId, initialPage, pageSize, auth)
            } else {
                val selected = binding.spinnerDistrict.selectedItem as? District
                val districtId = selected?.districtId ?: 0
                viewModel.loadEvents(userId, districtId, initialPage, pageSize, userToken)
            }
        }
    }

    override fun onBackPressed() {
        if (isShowingOwnEvents) {
            // revert to main events
            isShowingOwnEvents = false
            binding.topAppBar.title = "Events"
            binding.textInputLayoutSearch.visibility = View.VISIBLE
            binding.spinnerDistrict.visibility = View.VISIBLE

            viewModel.clearHostMode()
            val selected = binding.spinnerDistrict.selectedItem as? District
            val districtId = selected?.districtId ?: 0
            viewModel.loadEvents(userId, districtId, initialPage, pageSize, userToken)
            return
        }
        super.onBackPressed()
    }
}
