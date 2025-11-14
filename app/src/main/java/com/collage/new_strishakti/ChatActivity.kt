package com.collage.new_strishakti

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.collage.new_strishakti.Adapter.ChatMessageAdapter
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.data.ChatMessageResponse
import com.collage.new_strishakti.data.UserResponse
import com.collage.new_strishakti.databinding.ActivityChatBinding
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var stompClient: StompClient
    private lateinit var adapter: ChatMessageAdapter
    private val disposables = CompositeDisposable()

    private val TAG = "ChatActivity1"
    private lateinit var sessionManager: SessionManager
    private var userId: Int = -1
    private val receiverId = 722658  // Replace with dynamic receiver if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()

        adapter = ChatMessageAdapter(mutableListOf(), userId.toLong())
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        connectToSocket()

        binding.btnSend.setOnClickListener {
            val msg = binding.etMessage.text.toString().trim()
            if (msg.isNotBlank()) {
                sendMessage(msg)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun connectToSocket() {
        Toast.makeText(this, "Starting connection...", Toast.LENGTH_SHORT).show()
        val backendSockJSUrl = "wss://dev.strishakti.org/ws-mobile"

        // Use JWS transport (WebSocket)
        stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, backendSockJSUrl)

        val headers = listOf(StompHeader("Authorization", "Bearer ${getTokenFromPrefs()}"))

        // Handle connection lifecycle
        val lifecycleDisp = stompClient.lifecycle().subscribe({ event ->
            lifecycleScope.launch(Dispatchers.Main) {
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.i(TAG, "✅ STOMP Connected")
                        Toast.makeText(this@ChatActivity, "✅ Connected to chat", Toast.LENGTH_SHORT).show()
                        subscribeToMessages()
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.w(TAG, "⚠️ STOMP Closed - reconnecting in 3s...")
                        Toast.makeText(this@ChatActivity, "⚠️ Connection closed. Reconnecting...", Toast.LENGTH_SHORT).show()
                        delay(3000)
                        connectToSocket()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "❌ STOMP Connection error", event.exception)
                        Toast.makeText(
                            this@ChatActivity,
                            "❌ Connection error: ${event.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        delay(5000)
                        connectToSocket()
                    }
                    else -> {}
                }
            }
        }, { err ->
            Log.e(TAG, "Lifecycle subscription error", err)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    this@ChatActivity,
                    "❌ Lifecycle subscription error: ${err.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        disposables.add(lifecycleDisp)
        stompClient.connect(headers)
    }

    private fun subscribeToMessages() {
        val topicPath = "/user/$userId/queue/messages" // Dynamic per user
        val topicDisp = stompClient.topic(topicPath).subscribe({ message ->
            Log.d(TAG, "📩 Received: ${message.payload}")
            try {
                val json = JSONObject(message.payload)
                val senderJson = json.optJSONObject("sender")

                val chatMessage = ChatMessageResponse(
                    id = json.optLong("id"),
                    content = json.optString("content"),
                    timestamp = json.optString("timestamp"),
                    sender = senderJson?.let {
                        UserResponse(
                            userId = it.optInt("userId"),
                            userFirstName = it.optString("userFirstName"),
                            fullName = it.optString("fullName")
                        )
                    },
                    receiver = null,
                    group = null,
                    files = null
                )

                lifecycleScope.launch(Dispatchers.Main) {
                    adapter.addMessage(chatMessage)
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                    Toast.makeText(this@ChatActivity, "📩 New message received", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Parse error", e)
            }
        }, { err ->
            Log.e(TAG, "Topic subscribe error", err)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@ChatActivity, "❌ Topic subscribe error: ${err.message}", Toast.LENGTH_LONG).show()
            }
        })

        disposables.add(topicDisp)
    }

    private fun sendMessage(text: String) {
        try {
            val json = JSONObject().apply {
                put("content", text)
                put("sender", JSONObject().put("userId", userId))
                put("receiver", JSONObject().put("userId", receiverId))
            }

            val localMsg = ChatMessageResponse(
                id = null,
                content = text,
                timestamp = null,
                sender = UserResponse(userId = userId, userFirstName = null, fullName = "You"),
                receiver = null,
                group = null,
                files = null
            )

            // Show message locally immediately
            lifecycleScope.launch(Dispatchers.Main) {
                adapter.addMessage(localMsg)
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }

            stompClient.send("/app/chat", json.toString()).subscribe({
                Log.d(TAG, "📤 Sent message: $text")
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Message sent ✅", Toast.LENGTH_SHORT).show()
                }
            }, { err ->
                Log.e(TAG, "Send error", err)
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Send failed ❌: ${err.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Send exception", e)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@ChatActivity, "Send exception ❌: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTokenFromPrefs(): String {
        return sessionManager.getToken() ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        if (::stompClient.isInitialized) {
            stompClient.disconnect()
        }
    }
}
