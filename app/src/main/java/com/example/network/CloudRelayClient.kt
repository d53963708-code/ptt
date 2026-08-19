package com.example.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

class CloudRelayClient {

    private var client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false

    private var onDataReceivedListener: ((ByteArray) -> Unit)? = null

    fun setOnDataReceivedListener(listener: (ByteArray) -> Unit) {
        onDataReceivedListener = listener
    }

    fun connectToRoom(serverUrl: String, roomCode: String) {
        disconnect()

        val fullUrl = if (serverUrl.startsWith("ws://") || serverUrl.startsWith("wss://")) {
            "$serverUrl?room=${roomCode.replace("#", "")}"
        } else {
            "wss://ws.postman-echo.com/raw?room=${roomCode.replace("#", "")}"
        }

        try {
            val request = Request.Builder()
                .url(fullUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    isConnected = true
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    onDataReceivedListener?.invoke(bytes.toByteArray())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    onDataReceivedListener?.invoke(text.toByteArray(Charsets.UTF_8))
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    isConnected = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    isConnected = false
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            isConnected = false
        }
    }

    fun sendBinaryData(data: ByteArray): Boolean {
        return webSocket?.send(data.toByteString()) ?: false
    }

    fun isRelayConnected(): Boolean {
        return isConnected
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
            webSocket = null
            isConnected = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
