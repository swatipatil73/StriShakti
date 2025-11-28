package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.model.post.ReportReason
import com.collage.empowermentstrishakti.data.network.ApiService
import com.google.gson.Gson

// PostActionsRepository.kt


class PostActionsRepository(
    private val api: ApiService
) {
    suspend fun deletePost(postId: Int, token: String): Result<Unit> {
        return try {
            val res = api.deletePost( postId,token)
            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(IllegalStateException(res.errorBody()?.string() ?: "Delete failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePost(userId: Int, postId: Int, token: String): Result<String> {
        return try {
            val response = api.savePost(userId, postId, token)

            // Parse JSON body in both success and failure
            val body: CommonResponse? = if (response.isSuccessful) {
                response.body()
            } else {
                // Convert errorBody to CommonResponse
                response.errorBody()?.string()?.let { json ->
                    Gson().fromJson(json, CommonResponse::class.java)
                }
            }

            if (body != null) {
                Result.success(body.message) // always return the message only
            } else {
                Result.failure(IllegalStateException("Save failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // com.collage.empowermentstrishakti.data.repository.PostActionsRepository.kt

    suspend fun reportPost(
        userId: Int,
        postId: Int,
        reason: ReportReason,
        token: String
    ): Result<String> {
        return try {
            val body = mapOf(
                "disputeType" to reason.title,
                "disputeDescription" to reason.description
            )

            val response = api.reportPost("Bearer $token", userId, postId, reason.id, body)

            val apiResponse: CommonResponse? = if (response.isSuccessful) {
                response.body()
            } else {
                response.errorBody()?.string()?.let { json ->
                    Gson().fromJson(json, CommonResponse::class.java)
                }
            }

            if (apiResponse != null) {
                val msg = apiResponse.message
                if (msg.contains("already been raised", ignoreCase = true) ||
                    msg.contains("duplicate report", ignoreCase = true) ||
                    msg.contains("already reported", ignoreCase = true)
                ) {
                    Result.failure(IllegalStateException("A dispute for this issue has already been raised"))
                } else {
                    Result.success(msg)
                }
            } else {
                Result.failure(IllegalStateException("Failed to report post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}
