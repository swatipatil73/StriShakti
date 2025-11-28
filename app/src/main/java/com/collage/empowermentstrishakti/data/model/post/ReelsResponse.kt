package com.collage.empowermentstrishakti.data.model.post

import com.google.gson.annotations.SerializedName

data class ReelsResponse(
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("nextPageNo") val nextPageNo: Int,
    @SerializedName("postsData") val postsData: List<ReelData>
)

data class ReelData(
    val userProfileImageUrl: String?,
    val postCreatedAt: String?,
    val postType: String?,
    val trendingScore: Int,
    val description: String?,
    val postId: Int,
    val postImageURl: String?,
    val userName: String?,
    val userId: Int,
    val postUploadedAt: String?,
    val totalCountOFReact: Int,
    val topComments: List<CommentData>?,
    val videoThumbnailUrl: String?,
    val totalComments: Int,
    val postSaved: Boolean,
    val postName: String?,
    val totalViews: Int,
    val userUUID: String?,
    val commentsAndReacts: List<CommentData>?,
    val userReactStatus: Boolean
)
