package com.collage.empowermentstrishakti.data.model.Chat


import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class ChatSocketManager(
    private val wsUrl: String,
    private val token: String,
    private val onMessageReceived: (String) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit,
    private val onError: (Throwable) -> Unit
) {

    private val TAG = "ChatSocketManager"
    private val gson = Gson()

    private var webSocket: WebSocket? = null
    private var isConnected = false

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS) // ❤️ Heartbeat
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket requires this
        .retryOnConnectionFailure(true)
        .build()

    // --------------------------------------------------
    // CONNECT
    // --------------------------------------------------
    fun connect() {
        if (isConnected) return

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Connecting to $wsUrl")

        webSocket = client.newWebSocket(request, socketListener)
    }

    // --------------------------------------------------
    // SEND (STOMP)
    // --------------------------------------------------
    fun send(destination: String, body: String) {
        if (!isConnected) {
            Log.e(TAG, "Socket not connected")
            return
        }

        val frame = buildSendFrame(destination, body)
        webSocket?.send(frame)
    }

    // --------------------------------------------------
    // DISCONNECT
    // --------------------------------------------------
    fun disconnect() {
        if (!isConnected) return
        webSocket?.close(1000, "Client closed")
        isConnected = false
    }

    // --------------------------------------------------
    // STOMP FRAMES
    // --------------------------------------------------
    private fun buildConnectFrame(): String {
        return """
            CONNECT
            accept-version:1.2
            heart-beat:10000,10000
            authorization:Bearer $token

            ${'\u0000'}
        """.trimIndent()
    }

    private fun buildSubscribeFrame(): String {
        return """
            SUBSCRIBE
            id:sub-0
            destination:/user/queue/messages

            ${'\u0000'}
        """.trimIndent()
    }

    private fun buildSendFrame(destination: String, body: String): String {
        return """
            SEND
            destination:$destination
            content-type:application/json

            $body${'\u0000'}
        """.trimIndent()
    }

    // --------------------------------------------------
    // SOCKET LISTENER
    // --------------------------------------------------
    private val socketListener = object : WebSocketListener() {

        override fun onOpen(ws: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket OPEN")
            ws.send(buildConnectFrame())
        }

        override fun onMessage(ws: WebSocket, text: String) {
            Log.d(TAG, "RECEIVED: $text")

            when {
                text.startsWith("CONNECTED") -> {
                    isConnected = true
                    ws.send(buildSubscribeFrame())
                    onConnected()
                }

                text.startsWith("MESSAGE") -> {
                    val bodyIndex = text.indexOf("\n\n")
                    if (bodyIndex != -1) {
                        val body = text.substring(bodyIndex + 2).trimEnd('\u0000')
                        onMessageReceived(body)
                    }
                }

                text.startsWith("ERROR") -> {
                    onError(Throwable("STOMP ERROR"))
                }
            }
        }

        override fun onMessage(ws: WebSocket, bytes: ByteString) {
            // ignore
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Socket Failure", t)
            isConnected = false
            onError(t)
            reconnect()
        }

        override fun onClosed(ws: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Socket Closed: $reason")
            isConnected = false
            onDisconnected()
        }
    }

    // --------------------------------------------------
    // AUTO RECONNECT
    // --------------------------------------------------
    private fun reconnect() {
        Log.d(TAG, "Reconnecting...")
        try {
            Thread.sleep(3000)
            connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
