package com.collage.empowermentstrishakti.Adapter

import android.util.Base64
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

    private val gson = Gson()
    private var username: String? = null
    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    fun connect() {
        username = extractUsernameFromJWT(token)
        if (username == null) {
            onError(Exception("Invalid JWT"))
            return
        }

        Log.d("WS_CUSTOM", "Connecting… username=$username")

        val request = Request.Builder()
            .url(wsUrl)
            .header("Sec-WebSocket-Protocol", "v10.stomp") // MUST for Spring STOMP
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WS_CUSTOM", "WebSocket OPEN ✓ response=${response.code} ${response.message}")

                // Send STOMP CONNECT frame
                val connectFrame = buildConnectFrame()
                ws.send(connectFrame)

                onConnected()
                subscribeUserQueue()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WS_CUSTOM", "MESSAGE: $text")
                // Filter MESSAGE frames
                if (text.startsWith("MESSAGE")) {
                    val payload = extractPayload(text)
                    onMessageReceived(payload)
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w("WS_CUSTOM", "WebSocket CLOSED: $code $reason")
                onDisconnected()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WS_CUSTOM", "WebSocket ERROR", t)
                onError(t)
            }
        })
    }

    private fun buildConnectFrame(): String {
        return buildString {
            append("CONNECT\n")
            append("accept-version:1.2\n")
            append("heart-beat:10000,10000\n")

            append("\n\u0000") // Null terminator
        }
    }

    private fun subscribeUserQueue() {
        val dest = "/user/$username/queue/messages"
        val frame = buildString {
            append("SUBSCRIBE\n")
            append("id:sub-0\n")
            append("destination:$dest\n")
            append("\n\u0000")
        }
        webSocket?.send(frame)
        Log.d("WS_CUSTOM", "Subscribed to $dest")
    }

    fun send(destination: String, json: String) {
        val frame = buildString {
            append("SEND\n")
            append("destination:$destination\n")
            append("content-type:application/json\n")
            append("\n")
            append(json)
            append("\u0000")
        }
        webSocket?.send(frame)
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
    }

    private fun extractPayload(frame: String): String {
        val idx = frame.indexOf("\n\n")
        return if (idx != -1) frame.substring(idx + 2).trimEnd('\u0000') else frame
    }

    // Extract `sub` from JWT
    private fun extractUsernameFromJWT(token: String): String? {
        return try {
            val parts = token.split(".")
            val payload = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val map = gson.fromJson(String(payload), Map::class.java)
            map["sub"] as String?
        } catch (e: Exception) {
            null
        }
    }
}
