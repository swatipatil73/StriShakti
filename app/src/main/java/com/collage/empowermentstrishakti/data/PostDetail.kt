package com.collage.empowermentstrishakti.data





import android.os.Parcelable
import com.collage.empowermentstrishakti.data.model.SavedPost.CommentReact
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostDetail(
    val postId: Int = 0,
    val userId: Int = 0,
    val userProfileImageUrl: String? = null,
    val userName: String? = null,
    val postImageURl: String? = null,
    val postType: String? = null,
    val videoThumbnailUrl: String? = null,
    val postCreatedAt: String? = null,
    val postName: String? = null,
    val totalCountOFReact: Int = 0,
    val totalComments: Int? = null,
    val userReactStatus: Boolean = false,
    val postUploadedAt: String? = null,
    val userUUID: String? = null,
    val description: String? = null,
    val mediaFiles: String? = null,                      // keep as String if backend sends a JSON string or URL
    val commentsAndReacts: List<CommentReact>? = null,   // concrete Parcelable type
    val reachCount: Int = 0,
    val viewCount: Int = 0,
    val postSaved: Boolean = false
) : Parcelable
