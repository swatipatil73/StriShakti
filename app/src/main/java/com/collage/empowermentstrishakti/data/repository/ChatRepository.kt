package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.Chat.ChatHistoryResponse
import com.collage.empowermentstrishakti.data.network.ApiService

class ChatRepository(private val api: ApiService) {

    suspend fun loadChatHistory(
        token: String,
        senderId: Int,
        receiverId: Int
    ): List<ChatHistoryResponse> {
        return api.getOneToOneChatHistory(
            token = "Bearer $token",
            senderId = senderId,
            receiverId = receiverId
        )
    }
}
