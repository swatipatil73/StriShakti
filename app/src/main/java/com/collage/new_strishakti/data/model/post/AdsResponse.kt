package com.collage.new_strishakti.data.model.post



import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdsResponse(
    @SerializedName("postData") val postData: List<AdPost>?,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String
) : Parcelable

@Parcelize
data class AdPost(
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



