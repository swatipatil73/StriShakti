package com.collage.new_strishakti.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.Paging.ReelPagingSource
import com.collage.new_strishakti.data.model.Profile.UserProfileResponse
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.model.Reel.ReelResponse
import com.collage.new_strishakti.data.model.post.ReelData
import com.collage.new_strishakti.data.model.post.ReelsResponse
import com.collage.new_strishakti.data.network.ApiService
import kotlinx.coroutines.flow.Flow
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