package com.collage.empowermentstrishakti

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.Adapter.ChatSocketManager
import com.collage.empowermentstrishakti.Adapter.GroupChatAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.Chat.GroupChatHistoryMapper
import com.collage.empowermentstrishakti.data.model.Chat.GroupChatMessage
import com.collage.empowermentstrishakti.data.model.Chat.SocketChatMessage
import com.collage.empowermentstrishakti.data.repository.GroupChatRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class GroupChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var ivSend: ImageView
    private lateinit var adapter: GroupChatAdapter

    private lateinit var socket: ChatSocketManager
    private val gson = Gson()

    private var groupId = -1
    private var myUserId = -1
    private lateinit var token: String

    private val wsUrl = "wss://backend.strishakti.org/ws-mobile"

    // Pagination
    private var page = 0
    private val size = 20
    private var isLoading = false
    private var hasNextPage = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        // Toolbar
        findViewById<Toolbar?>(R.id.toolbar)?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.setNavigationOnClickListener { onBackPressed() }
        }

        // Session
        val session = SessionManager(this)
        token = session.getToken() ?: ""
        myUserId = session.getUserId()

        // Intent
        groupId = intent.getIntExtra("GROUP_ID", -1)
        supportActionBar?.title =
            intent.getStringExtra("GROUP_NAME") ?: "Group Chat"

        if (groupId == -1) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Views
        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        ivSend = findViewById(R.id.ivSend)

        setupRecycler()
        loadHistory()
        setupSocket()

        ivSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendGroupMessage(text)
                etMessage.setText("")
            }
        }
    }

    // ---------------- Recycler ----------------

    private fun setupRecycler() {
        adapter = GroupChatAdapter(myUserId)

        val lm = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        rvChat.layoutManager = lm
        rvChat.adapter = adapter

        rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(-1) && !isLoading && hasNextPage) {
                    loadHistory()
                }
            }
        })
    }

    // ---------------- History API ----------------

    private fun loadHistory() {
        if (isLoading || !hasNextPage) return
        isLoading = true

        lifecycleScope.launch {
            try {
                val repo = GroupChatRepository()
                val mapper = GroupChatHistoryMapper()

                val response = repo.getGroupChatHistory(
                    token = token,
                    groupId = groupId,
                    page = page,
                    size = size
                )

                val messages: List<GroupChatMessage> =
                    mapper.map(response.details, myUserId)

                if (messages.isNotEmpty()) {
                    val lm = rvChat.layoutManager as LinearLayoutManager
                    val firstPos = lm.findFirstVisibleItemPosition()
                    val firstView = rvChat.getChildAt(0)
                    val offset = firstView?.top ?: 0

                    adapter.prependItems(messages)

                    lm.scrollToPositionWithOffset(
                        firstPos + messages.size,
                        offset
                    )
                }

                hasNextPage = response.hasNextPage
                page++

            } catch (e: Exception) {
                Toast.makeText(
                    this@GroupChatActivity,
                    "Failed to load history",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    // ---------------- WebSocket ----------------

    private fun setupSocket() {
        socket = ChatSocketManager(
            wsUrl = wsUrl,
            token = token,

            onMessageReceived = { payload ->
                val socketMsg =
                    gson.fromJson(payload, SocketChatMessage::class.java)

                if (socketMsg.group?.groupId == groupId) {

                    val msg = GroupChatMessage(
                        chatId = 0,
                        groupId = groupId,
                        content = socketMsg.content,
                        senderId = socketMsg.sender.userId,
                        senderName = socketMsg.sender.userName ?: "Member",
                        timestamp = socketMsg.timestamp ?: "",
                        isMine = socketMsg.sender.userId == myUserId
                    )

                    runOnUiThread {
                        adapter.addMessage(msg)
                        rvChat.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            },

            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "Group Connected", Toast.LENGTH_SHORT).show()
                }
            },

            onDisconnected = {
                runOnUiThread {
                    Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
                }
            },

            onError = { err ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Socket Error: ${err.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        socket.connect()
    }

    // ---------------- Send Message ----------------

    private fun sendGroupMessage(text: String) {

        val payload = mapOf(
            "content" to text,
            "sender" to mapOf("userId" to myUserId),
            "group" to mapOf("groupId" to groupId)
        )

        socket.send("/app/chat", gson.toJson(payload))

        val msg = GroupChatMessage(
            chatId = 0,
            groupId = groupId,
            content = text,
            senderId = myUserId,
            senderName = "You",
            timestamp = "",
            isMine = true
        )

        adapter.addMessage(msg)
        rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}
