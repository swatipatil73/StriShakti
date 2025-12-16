package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.Chat.GroupChatHistoryResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import com.collage.empowermentstrishakti.data.network.ApiService

class GroupChatRepository {

    private val api = ApiClient.apiService

    suspend fun getGroupChatHistory(
        token: String,
        groupId: Int,
        page: Int,
        size: Int
    ): GroupChatHistoryResponse {

        return api.getGroupChatHistory(
            token = "Bearer $token",
            groupId = groupId,
            page = page,
            size = size
        )
    }
}

