package com.collage.new_strishakti.data.model.Reel

data class ReelUploadResponse(
    val homePostData: HomePostData?,
    val mediaFiles: List<MediaFile>?,
    val message: String?,
    val status: String?
)

data class HomePostData(
    val postId: Int?,
    val userId: Int?,
    val userProfileImageUrl: String?,
    val userName: String?,
    val postImageURl: String?,
    val postType: String?,
    val videoThumbnailUrl: String?,
    val postCreatedAt: String?,
    val postName: String?,
    val postLastReactedBy: String?,
    val totalCountOFReact: Int?,
    val totalCountOfComments: Int?,
    val description: String?,
    val commentsAndReacts: Any? // can replace with a model if you have structure
)

data class MediaFile(
    val mediaUrl: String?,
    val mediaType: String?,
    val id: Int?,
    val thumbnailUrl: String?
)

