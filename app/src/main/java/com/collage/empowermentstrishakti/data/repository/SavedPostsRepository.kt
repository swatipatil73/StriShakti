package com.collage.empowermentstrishakti.data.repository


import com.collage.empowermentstrishakti.data.model.SavedPost.SavedPostResponse
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.network.ApiService

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavedPostsRepository(private val apiService: ApiService) {


        suspend fun getSavedPosts(
            userId: Int,
            size: Int,
            page: Int,
            token: String
        ): ApiResult<SavedPostResponse> {

            return withContext(Dispatchers.IO) {
                try {
                    val response = apiService.getSavedPosts(userId, size, page, token)

                    if (response.isSuccessful) {
                        response.body()?.let {
                            ApiResult.Success(it)
                        } ?: ApiResult.Error("Empty response body")
                    } else {
                        ApiResult.Error("Server error: ${response.code()} ${response.message()}")
                    }

                } catch (e: Exception) {
                    ApiResult.Error("Network error: ${e.localizedMessage}", e)
                }
            }
        }

    suspend fun deleteSavedPost(
        userId: Long,
        postId: Long,
        token: String
    ): ApiResult<CommonResponse> {

        return try {
            val response = apiService.deleteSavedPost(
                userId = userId,
                postId = postId,
                token = token
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Server error: ${response.code()} ${response.message()}")
            }

        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}", e)
        }
    }


}

