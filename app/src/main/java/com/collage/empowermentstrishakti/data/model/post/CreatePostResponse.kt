package com.collage.empowermentstrishakti.data.model.post

data class CreatePostResponse(
    val homePostData: HomePostData,
    val mediaFiles: List<MediaFile>,
    val message: String,
    val status: String
)

data class HomePostData(
    val postId: Int,
    val userId: Int,
    val userProfileImageUrl: String,
    val userName: String,
    val postImageURl: String,
    val postType: String,
    val videoThumbnailUrl: String,
    val postCreatedAt: String,
    val postName: String,
    val postLastReactedBy: String,
    val totalCountOFReact: Int,
    val totalCountOfComments: Int,
    val description: String?,            // nullable
    val commentsAndReacts: Any?          // nullable, unclear type, can be changed if known
)



