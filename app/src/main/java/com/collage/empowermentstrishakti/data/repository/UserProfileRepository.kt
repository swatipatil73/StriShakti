package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.Profile.UserProfileResponse
import com.collage.empowermentstrishakti.data.model.Reel.Reel
import com.collage.empowermentstrishakti.data.network.ApiService
import retrofit2.Response

class UserProfileRepository(
    val api: ApiService,
    val sessionManager: SessionManager
) {

    suspend fun getUserProfile(uuid: String): Response<UserProfileResponse> {
        val token = "Bearer ${sessionManager.getToken()}"
        return api.getUserProfile(uuid, token)
    }

    suspend fun getAllUserPosts(): List<Reel> {
        val token = "Bearer ${sessionManager.getToken()}"
        val response = api.getReels(token, page = 0, size = 1000)
        val posts = response.postsData ?: emptyList()
        return posts.map { reel ->
            reel.copy(
                userProfileImageUrl = reel.userProfileImageUrl ?: "",
                postType = reel.postType ?: "",
                description = reel.description ?: "",
                postImageURl = reel.postImageURl ?: "",
                userName = reel.userName ?: "",
                postUploadedAt = reel.postUploadedAt ?: "",
                videoThumbnailUrl = reel.videoThumbnailUrl ?: "",
                postName = reel.postName ?: "",
                userUUID = reel.userUUID ?: "",
                totalCountOFReact = reel.totalCountOFReact ?: 0,
                totalComments = reel.totalComments ?: 0,
                totalViews = reel.totalViews ?: 0,
                postSaved = reel.postSaved ?: false,
                userReactStatus = reel.userReactStatus ?: false,
                topComments = reel.topComments ?: emptyList(),
                commentsAndReacts = reel.commentsAndReacts ?: emptyList()
            )
        }
    }

}