package com.collage.new_strishakti.data.repository

import com.collage.new_strishakti.data.model.post.CommonResponse
import com.collage.new_strishakti.data.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Response

object LikeRepository {

    /**
     * Call this to add a like (reaction) on post
     */
    suspend fun likePost(
        userId: String,
        postId: String,
        token: String,
        reactName: String = "Like"
    ): Response<CommonResponse> {

        val jsonObject = JSONObject().apply {
            put("postReactName", reactName)
            put("postReactImageUrl", "string")
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        return ApiClient.apiService.addReactOnPost(userId, postId, requestBody, "Bearer $token")
    }

    /**
     * Call this to remove a like (unlike) from post
     */
    suspend fun unlikePost(
        userId: String,
        postId: String,
        token: String
    ): Response<CommonResponse> {
        return ApiClient.apiService.deleteReactOnPost(userId, postId, "Bearer $token")
    }
}