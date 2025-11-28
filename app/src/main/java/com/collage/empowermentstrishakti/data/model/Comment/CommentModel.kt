package com.collage.empowermentstrishakti.data.model.Comment

// CommentModel.kt
data class CommentModel(
    val userProfileImageUrl: String?,
    val commentTime: String?,
    val comment: String?,
    val userName: String?,
    val parentCommentId: Int,
    val childCommentId: Int,
    val children: List<CommentModel>?
)

// Response wrapper
data class CommentResponse(
    val commentsAndReacts: List<CommentModel>?
)

