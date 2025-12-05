package com.collage.empowermentstrishakti.data.model.Groups

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostDetail(
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
    val totalComments: Int?,
    val userReactStatus: Boolean,
    val postUploadedAt: String?,
    val userUUID: String?,
    val description: String?,
    val mediaFiles: List<MediaFile>?,       // <-- comma added
    val commentsAndReacts: List<CommentReact>,  // <-- comma added
    val reachCount: Int,
    val viewCount: Int,
    val postSaved: Boolean
) : Parcelable

@Parcelize
data class MediaFile(
    val url: String?,
    val type: String?
) : Parcelable

@Parcelize
data class CommentReact(
    val userId: Int,
    val comment: String?
) : Parcelable
