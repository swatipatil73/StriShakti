package com.collage.empowermentstrishakti.data.model


import com.google.gson.annotations.SerializedName

// ---------- Request ----------
data class CreatePagePostRequest(
    @SerializedName("postName")
    val postName: String,

    @SerializedName("postType")
    val postType: String, // "image" | "video" | "text"

    @SerializedName("videoThumbnailUrl")
    val videoThumbnailUrl: String? = null,

    @SerializedName("postImage")
    val postImage: List<String>? = null, // server URLs OR base64 strings depending on API

    @SerializedName("hashtag")
    val hashtag: List<String>? = null,

    @SerializedName("mentionId")
    val mentionId: List<Int>? = null
)

// ---------- Response ----------
data class CreatePagePostResponse(
    @SerializedName("homePostData")
    val homePostData: HomePostData? = null,

    @SerializedName("mediaFiles")
    val mediaFiles: List<MediaFile>? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("status")
    val status: String? = null
)

data class HomePostData(
    @SerializedName("postId")
    val postId: Int = 0,

    @SerializedName("userId")
    val userId: Int = 0,

    @SerializedName("userProfileImageUrl")
    val userProfileImageUrl: String? = null,

    @SerializedName("userName")
    val userName: String? = null,

    @SerializedName("postImageURl")
    val postImageURl: String? = null,

    @SerializedName("postType")
    val postType: String? = null,

    @SerializedName("videoThumbnailUrl")
    val videoThumbnailUrl: String? = null,

    @SerializedName("postCreatedAt")
    val postCreatedAt: String? = null,

    @SerializedName("postName")
    val postName: String? = null,

    @SerializedName("postLastReactedBy")
    val postLastReactedBy: String? = null,

    @SerializedName("totalCountOFReact")
    val totalCountOFReact: Int = 0,

    @SerializedName("totalCountOfComments")
    val totalCountOfComments: Int = 0,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("commentsAndReacts")
    val commentsAndReacts: List<Any>? = null
)

data class MediaFile(
    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,

    @SerializedName("mediaType")
    val mediaType: String? = null, // "image"|"video"

    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null
)

// ---------- Mapper: convert HomePostData -> your PostDetail ----------
/**
 * Use this to convert server response homePostData into your existing PostDetail model.
 * Adjust field mapping if your PostDetail has different names.
 */
fun HomePostData.toPostDetail(): PostDetail {
    return PostDetail(
        postId = this.postId,
        userId = this.userId,
        userProfileImageUrl = this.userProfileImageUrl,
        userName = this.userName,
        postImageURl = this.postImageURl,
        postType = this.postType,
        videoThumbnailUrl = this.videoThumbnailUrl,
        postCreatedAt = this.postCreatedAt,
        postName = this.postName,
        totalCountOFReact = this.totalCountOFReact,
        totalComments = this.totalCountOfComments,
        description = this.description,
        // keep other fields default/null as needed
        postSaved = false
    )
}
