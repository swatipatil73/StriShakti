package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.Comment.CommentModel
import com.collage.empowermentstrishakti.data.model.Comment.CommentResponse
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.network.ApiClient
import retrofit2.Response

object CommentRepository {

    suspend fun getComments(token: String, postId: Int): Response<CommentResponse> {
        return ApiClient.apiService.getComments("Bearer $token", postId)
    }

    suspend fun addComment(
        token: String,
        userId: Int,
        postId: Int,
        text: String
    ): Response<CommentModel> {
        val body = mapOf("comment" to text)
        return ApiClient.apiService.addComment("Bearer $token", userId, postId, body)
    }

    suspend fun addReply(
        token: String,
        userId: Int,
        postId: Int,
        parentCommentId: Int,
        text: String
    ): Response<CommonResponse> {
        val body = mapOf("comment" to text)
        return ApiClient.apiService.addReply("Bearer $token", userId, postId, parentCommentId, body)
    }

    suspend fun deleteComment(token: String, userId: Int, postCommentId: Int): Response<CommonResponse> {
        return ApiClient.apiService.deleteParentComment(token, userId, postCommentId)
    }
}

