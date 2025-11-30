package com.collage.empowermentstrishakti.data.model.Ads



import com.google.gson.annotations.SerializedName

data class AdsItem(
    @SerializedName("postName")
    val postName: String? = null,

    @SerializedName("selectFile")
    val selectFile: String? = null,

    @SerializedName("postImageUrl")
    val postImageUrl: String? = null,

    @SerializedName("postType")
    val postType: String? = null,

    @SerializedName("videoThumbnailUrl")
    val videoThumbnailUrl: String? = null,

    @SerializedName("advertisementDescription")
    val advertisementDescription: String? = null,

    @SerializedName("postCreatedAt")
    val postCreatedAt: String? = null,

    @SerializedName("postId")
    val postId: Int = 0,

    @SerializedName("postCategory")
    val postCategory: String? = null
)
