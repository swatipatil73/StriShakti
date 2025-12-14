package com.collage.empowermentstrishakti

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.Adapter.ChatAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.Chat.ChatMessage
import com.collage.empowermentstrishakti.data.model.Chat.User
import com.google.gson.Gson
import com.collage.empowermentstrishakti.Adapter.ChatSocketManager
import com.collage.empowermentstrishakti.Common.WebSocketClient

class ChatActivity : AppCompatActivity() {

    private lateinit var socket: ChatSocketManager
    private lateinit var adapter: ChatAdapter
    private lateinit var rv: RecyclerView
    private lateinit var et: EditText
    private lateinit var btnSend: Button

    private val gson = Gson()

    private var myUserId = 0
    private var receiverId = 0
    private var token: String = ""

    // ✔ Correct Mobile WebSocket endpoint
    val wsUrl = "wss://backend.strishakti.org/ws-mobile"
    private lateinit var wsClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)



//        wsClient = WebSocketClient()
//        wsClient.connect()

        myUserId = SessionManager(this).getUserId()
        token = SessionManager(this).getToken() ?: ""
        receiverId = intent.getIntExtra("USER_ID", 0)

        rv = findViewById(R.id.rvMessages)
        et = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        adapter = ChatAdapter(mutableListOf(), myUserId)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        setupSocket()

        btnSend.setOnClickListener { sendMessage() }
    }

    private fun setupSocket() {

        socket = ChatSocketManager(
            wsUrl = wsUrl,
            token = token,

            onMessageReceived = { payload ->
                val msg = gson.fromJson(payload, ChatMessage::class.java)

                runOnUiThread {
                    adapter.addMessage(msg)
                    rv.scrollToPosition(adapter.itemCount - 1)
                }
            },

            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }
            },

            onDisconnected = {
                runOnUiThread {
                    Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
                }
            },

            onError = { err ->
                runOnUiThread {
                    Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        socket.connect()
    }

    private fun sendMessage() {

        val text = et.text.toString().trim()
        if (text.isEmpty()) return

        val msg = ChatMessage(
            sender = User(myUserId),
            receiver = User(receiverId),
            content = text
        )

        val json = gson.toJson(msg)

        // ✔ Correct STOMP send
        socket.send("/app/chat", json)

        // Add message to UI instantly
        adapter.addMessage(msg)
        rv.scrollToPosition(adapter.itemCount - 1)

        et.setText("")
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}