package com.collage.new_strishakti.data.repository

import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.data.model.FriendListResponse
import com.collage.new_strishakti.data.model.friend.FriendRequestResponse
import com.collage.new_strishakti.data.model.friend.SearchFriendsResponse
import com.collage.new_strishakti.data.model.post.CommonResponse
import com.collage.new_strishakti.data.network.ApiService
import retrofit2.Response

class FriendRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getFriendsList(userId: Int): Response<FriendListResponse> {
        val token = sessionManager.getToken()
        return apiService.getFriendsList(userId, "Bearer $token")
    }

    suspend fun searchFriends(userName: String): Response<SearchFriendsResponse> {
        val token = sessionManager.getToken()
        return apiService.searchFriends(userName, "Bearer $token")
    }


    // NEW
    suspend fun getFriendRequests(receiverId: Int): retrofit2.Response<FriendRequestResponse> {
        val token = sessionManager.getToken()
        return apiService.getFriendRequests(receiverId, "Bearer $token")
    }

    suspend fun approveFriendRequest(friendRequestId: Int): Response<CommonResponse> {
        val token = sessionManager.getToken()
        return apiService.approveFriendRequest(friendRequestId, "Bearer $token")
    }

    // FriendRepository.kt
    suspend fun rejectFriendRequest(friendRequestId: Int): Response<CommonResponse> {
        val token = sessionManager.getToken()
        return apiService.rejectFriendRequest(friendRequestId, "Bearer $token")
    }

}


