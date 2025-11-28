package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.FriendListResponse
import com.collage.empowermentstrishakti.data.model.friend.FriendRequestResponse
import com.collage.empowermentstrishakti.data.model.friend.SearchFriendsResponse
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.network.ApiService
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

    suspend fun sendFriendRequest(senderId: Int, receiverId: Int): Response<CommonResponse> {
        val token = sessionManager.getToken()
        return apiService.sendFriendRequest(senderId, receiverId, "Bearer $token")
    }


}


