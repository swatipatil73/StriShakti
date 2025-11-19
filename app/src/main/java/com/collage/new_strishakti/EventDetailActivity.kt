package com.collage.new_strishakti
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.collage.new_strishakti.Adapter.DiscussionAdapter
import com.collage.new_strishakti.Adapter.ParticipantsAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Common.ViewUtils
import com.collage.new_strishakti.Factory.EventViewModelFactory
import com.collage.new_strishakti.data.model.Event.EventAbout
import com.collage.new_strishakti.data.model.Event.EventDetailResponse
import com.collage.new_strishakti.data.repository.EventRepository
import com.collage.new_strishakti.ui.RegisterViewModel.EventViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


class EventDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_USERID = "extra_event_userid"


        const val EXTRA_EVENT_UUID = "extra_event_uuid"
        const val EXTRA_EVENT_NAME = "extra_event_name"
        const val EXTRA_EVENT_IMAGE = "extra_event_image"
        const val EXTRA_EVENT_DESC = "extra_event_desc"
        const val EXTRA_EVENT_ADDRESS = "extra_event_address"
        const val EXTRA_EVENT_START = "extra_event_start"
        const val EXTRA_EVENT_END = "extra_event_end"
        const val EXTRA_EVENT_CATEGORY = "extra_event_category"
    }

    private lateinit var btnAction: Button
    private lateinit var tvName: TextView
    private lateinit var imgHeader: ImageView
    private lateinit var rvDiscussion: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private lateinit var viewModel: EventViewModel
    private lateinit var discussAdapter: DiscussionAdapter

    // Bottom sheet
    private lateinit var participantsAdapter: ParticipantsAdapter
    private lateinit var bottomSheetView: View
    private lateinit var progressBarParticipants: ProgressBar

    private var eventUUID = ""
    private var eventId = -1
    private var hostUserId = -1
    private var currentUserId = -1
    private var token = ""
    private var participantsDialog: BottomSheetDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        bindViews()
        initBasics()
        setupRecycler()
        setupViewModel()
        setupObservers()
        loadEventDetails()
        setupClicks()
    }

    private fun bindViews() {
        btnAction = findViewById(R.id.btnJoin)
        tvName = findViewById(R.id.tvEventName)
        imgHeader = findViewById(R.id.imgEventHeader)
        rvDiscussion = findViewById(R.id.rvDiscussion)
        progressBar = findViewById(R.id.progressLoading)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun initBasics() {
        eventUUID = intent.getStringExtra(EXTRA_EVENT_UUID) ?: ""
        eventId = intent.getIntExtra(EXTRA_EVENT_ID, -1)
        hostUserId = intent.getIntExtra(EXTRA_EVENT_USERID, -1)  // <-- get host from intent
        currentUserId = SessionManager(this).getUserId()
        token = "Bearer " + SessionManager(this).getToken()
    }


    private fun setupRecycler() {
        discussAdapter = DiscussionAdapter()
        rvDiscussion.layoutManager = LinearLayoutManager(this)
        rvDiscussion.adapter = discussAdapter
    }

    private fun setupViewModel() {
        val repo = EventRepository()
        val factory = EventViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupObservers() {
        // Event detail response
        viewModel.details.observe(this) { resp ->
            resp?.let { applyDetails(it) }
        }

        // Join event loading
        viewModel.joinLoading.observe(this) { loading ->
            btnAction.isEnabled = !loading
        }

        // Join event result
        viewModel.joinResult.observe(this) { result ->
            result?.let {
                val (success, msg) = it
                Toast.makeText(this, msg ?: "", Toast.LENGTH_SHORT).show()
                if (success) {
                    btnAction.text = "Joined"
                    btnAction.isEnabled = false
                    // Update local EventAbout to reflect joined
                    viewModel.details.value?.about?.firstOrNull()?.isParticipant = true
                }
            }
        }

        // Participant list observer (bottom sheet)
        viewModel.participants.observe(this) { list ->
            if (::participantsAdapter.isInitialized) {
                participantsAdapter.submitList(list)
            }
        }

        // participant empty state
        viewModel.participantsEmpty.observe(this) { empty ->
            if (::bottomSheetView.isInitialized) {
                ViewUtils.showEmptyState(bottomSheetView, empty, "No participants yet")
            }
        }

        // loading indicator bottom sheet
        viewModel.participantsLoading.observe(this) { loading ->
            if (::progressBarParticipants.isInitialized) {
                progressBarParticipants.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
        // Observe exit loading (show/hide bottom sheet progress)
        viewModel.exitLoading.observe(this) { loading ->
            if (::progressBarParticipants.isInitialized) {
                progressBarParticipants.visibility = if (loading) View.VISIBLE else View.GONE
            }
            // optionally disable adapter clicks while loading
            rvDiscussion.isEnabled = !loading
        }

// Observe exit result
        viewModel.exitResult.observe(this) { pair ->
            pair?.let { (success, msg) ->
                Toast.makeText(this, msg ?: "", Toast.LENGTH_SHORT).show()
                if (success) {
                    // If current user left the event, dismiss bottom sheet and update UI
                    val about = viewModel.details.value?.about?.firstOrNull()
                    val isHost = currentUserId == hostUserId
                    if (!isHost) {
                        // current user exited - update main UI
                        btnAction.text = "Join"
                        btnAction.isEnabled = true
                        // dismiss bottom sheet if visible
                        participantsDialog?.dismiss()
                    } else {
                        // host removed someone - participants LiveData already updated by ViewModel
                        // adapter will update automatically
                    }
                }
            }
        }
    }

    private fun loadEventDetails() {
        if (eventUUID.isNotBlank()) {
            viewModel.loadEventDetails(currentUserId, eventUUID, token)
        }
    }


    private fun applyDetails(resp: EventDetailResponse) {
        val about = resp.about.firstOrNull() ?: return
        val dics = resp.discussion.firstOrNull() ?: return

        // Update class-level variables
        eventId = about.eventId ?: eventId
        val isParticipant = about.isParticipant
        val isHost = currentUserId == hostUserId

        tvName.text = about.eventName ?: ""
        about.postImageUrl?.let { Glide.with(this).load(it).into(imgHeader) }

        // Set button text and state
        btnAction.text = when {
            isHost -> "View Participants"
            isParticipant -> "Exit"         // allow participant to exit
            else -> "Join"
        }
        // button enabled when:
        // - host (to view participants)
        // - user not participant (to join)
        // - participant (to exit) -> enabled
        btnAction.isEnabled = true

        // Discussion list (unchanged)
        if (resp.discussion.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvDiscussion.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvDiscussion.visibility = View.VISIBLE
            discussAdapter.submitList(resp.discussion)
        }
    }

    private fun setupClicks() {
        btnAction.setOnClickListener {
            val about = viewModel.details.value?.about?.firstOrNull()
            val isParticipant = about?.isParticipant == true
            when {
                currentUserId == hostUserId -> {
                    // Host → Show participants
                    showParticipantsBottomSheet()
                }
                isParticipant -> {
                    // Current user is a participant → Exit event
                    // eventId and token already available
                    viewModel.exitEvent(currentUserId, eventId, token, isHost = false)
                }
                else -> {
                    // Not participant → Join
                    viewModel.joinEvent(eventId, token)
                }
            }
        }
    }


    private fun showParticipantsBottomSheet() {
        participantsDialog = BottomSheetDialog(this)
        bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_participants, null)
        participantsDialog?.setContentView(bottomSheetView)

        val rv = bottomSheetView.findViewById<RecyclerView>(R.id.rvParticipants)
        progressBarParticipants = bottomSheetView.findViewById(R.id.progressParticipants)

        // adapter with callback
        participantsAdapter = ParticipantsAdapter(
            hostUserId = hostUserId,
            currentUserId = currentUserId
        ) { participant ->
            // This callback is when host taps delete on a participant.
            // Call viewModel.exitEvent(participant.userId, eventId, token, isHost = true)
            viewModel.exitEvent(participant.userId, eventId, token, isHost = true)
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = participantsAdapter

        // Load participants from ViewModel
        viewModel.loadParticipants(eventUUID, token)

        participantsDialog?.show()
    }
}
