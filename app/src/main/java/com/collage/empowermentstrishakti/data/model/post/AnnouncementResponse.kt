package com.collage.empowermentstrishakti.data.model.post



import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class AnnouncementResponse(
    @SerializedName("postData") val postData: Announcement?,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String?
)

@Parcelize
data class Announcement(
    val postName: String?,
    val selectFile: String?,
    val postImageUrl: String?,
    val postType: String?,
    val videoThumbnailUrl: String?,
    val advertisementDescription: String?,
    val postCreatedAt: String?,
    val postId: Int,
    val postCategory: String?
) : Parcelable
