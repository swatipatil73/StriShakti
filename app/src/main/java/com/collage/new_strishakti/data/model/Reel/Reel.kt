package com.collage.new_strishakti.data.model.Reel

data class Reel(
    val userProfileImageUrl: String?,
    val postCreatedAt: String?,
    val postType: String?,
    val trendingScore: Int?,
    val description: String?,
    val postId: Int,
    val postImageURl: String?,
    val userName: String?,
    val userId: Int?,
    val postUploadedAt: String?,
    val totalCountOFReact: Int?,
    val topComments: List<TopComment>?,
    val videoThumbnailUrl: String?,
    val totalComments: Int?,
    val postSaved: Boolean?,
    val postName: String?,
    val totalViews: Int?,
    val userUUID: String?,
    val commentsAndReacts: List<Any>?,
    val userReactStatus: Boolean?
)

data class TopComment(
    val commentedAt: String?,
    val commentId: Int?,
    val commentedBy: String?,
    val commentText: String?
)
