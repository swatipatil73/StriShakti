package com.collage.new_strishakti.data.repository

import com.collage.new_strishakti.data.model.FriendListResponse
import com.collage.new_strishakti.data.model.chat.GroupListResponse
import com.collage.new_strishakti.data.network.ApiService
import retrofit2.Response

class ChatRepository(private val apiService: ApiService) {

    // Fetch Friend List
    suspend fun getFriends(userId: Int, token: String): Response<FriendListResponse> {
        return apiService.getFriendsList(userId, token)
    }

    // Fetch Group List
    suspend fun getGroups(userId: Int, token: String): Response<GroupListResponse> {
        return apiService.getGroupList(userId, token)
    }
}
