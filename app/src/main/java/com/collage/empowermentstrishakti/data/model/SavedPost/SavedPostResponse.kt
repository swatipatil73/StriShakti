package com.collage.empowermentstrishakti.data.model.SavedPost

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class SavedPostResponse(
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("pageSize") val pageSize: Int = 0,
    @SerializedName("nextPageNo") val nextPageNo: Int = 0,
    @SerializedName("savedPostData") val savedPostData: List<SavedPostData> = emptyList(),
    @SerializedName("currentPage") val currentPage: Int = 0,
    @SerializedName("totalElements") val totalElements: Int = 0
) : Parcelable

@Parcelize
data class SavedPostData(
    @SerializedName("postId") val postId: Long = 0L,
    @SerializedName("userId") val userId: Long = 0L,
    @SerializedName("userProfileImageUrl") val userProfileImageUrl: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("postImageURl") val postImageUrl: String? = null,
    @SerializedName("postType") val postType: String? = null,            // e.g. "image/jpeg" or "video/mp4" or null for text
    @SerializedName("videoThumbnailUrl") val videoThumbnailUrl: String? = null,
    @SerializedName("postCreatedAt") val postCreatedAt: String? = null,  // ISO datetime string
    @SerializedName("postName") val postName: String? = null,
    @SerializedName("totalCountOFReact") val totalCountOFReact: Int = 0,
    @SerializedName("totalComments") val totalComments: Int? = null,
    @SerializedName("userReactStatus") val userReactStatus: Boolean = false,
    @SerializedName("postUploadedAt") val postUploadedAt: String? = null,
    @SerializedName("userUUID") val userUUID: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("mediaFiles") val mediaFiles: List<String>? = null,    // null or list of media urls
    @SerializedName("commentsAndReacts") val commentsAndReacts: List<CommentReact>? = null,
    @SerializedName("reachCount") val reachCount: Int = 0,
    @SerializedName("viewCount") val viewCount: Int = 0,
    @SerializedName("postSaved") val postSaved: Boolean = false
) : Parcelable

/**
 * Minimal, flexible Comment/React model.
 * Add or remove fields to match your backend payload.
 * Gson will ignore fields that aren't declared here.
 */
@Parcelize
data class CommentReact(
    @SerializedName("id") val id: String? = null,
    @SerializedName("userId") val userId: Long? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("type") val type: String? = null,        // "comment" / "react" / etc.
    @SerializedName("text") val text: String? = null,
    @SerializedName("reactType") val reactType: String? = null, // e.g. "like", "love"
    @SerializedName("createdAt") val createdAt: String? = null
) : Parcelable