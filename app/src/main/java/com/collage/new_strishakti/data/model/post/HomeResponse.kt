package com.collage.new_strishakti.data.model.post

import com.google.gson.annotations.SerializedName

data class HomeResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("nextCursor") val nextCursor: Long,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("nextPageNo") val nextPageNo: Int,
    @SerializedName("postsData") val postsData: List<PostData>
)

data class PostData(
    val postId: Int,
    val userId: Int,
    val userProfileImageUrl: String?,
    val userName: String?,
    val postImageURl: String?,
    val postType: String?,
    val videoThumbnailUrl: String?,
    val postCreatedAt: String?,
    val postName: String?,
    val totalCountOFReact: Int,
    val userReactStatus: Boolean,
    val postUploadedAt: String?,
    val userUUID: String?,
    val description: String?,
    val commentsAndReacts: List<CommentData>?,
    val postSaved: Boolean,
    val isLikedByUser: Int,
    val mediaFiles: List<MediaFile>
)

data class MediaFile(
    val mediaUrl: String?,
    val mediaType: String?,
    val id: Int,
    val thumbnailUrl: String?
)

