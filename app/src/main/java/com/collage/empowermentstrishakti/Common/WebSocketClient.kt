package com.collage.empowermentstrishakti.Common

import android.util.Log
import okhttp3.*

class WebSocketClient {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder()
            .url("wss://backend.strishakti.org/ws-mobile/websocket")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WS_TEST", "Connected OK")
                // Example: send a test message
                ws.send("Hello from Kotlin WebSocket!")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WS_TEST", "Received: $text")
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d("WS_TEST", "Closing: $code / $reason")
                ws.close(1000, null)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("WS_TEST", "Closed: $code / $reason")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
                Log.e("WS_TEST", "Failure", t)
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
}