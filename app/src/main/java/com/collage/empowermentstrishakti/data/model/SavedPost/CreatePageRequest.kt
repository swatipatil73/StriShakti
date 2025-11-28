package com.collage.empowermentstrishakti.data.model.SavedPost


import com.google.gson.annotations.SerializedName

data class CreatePageRequest(
    @SerializedName("pageName")
    val pageName: String,

    @SerializedName("pageDescription")
    val pageDescription: String? = null,

    @SerializedName("pageCoverProfileImagePath")
    val pageCoverProfileImagePath: String? = null,

    @SerializedName("linkUrlName")
    val linkUrlName: String? = null,

    @SerializedName("linkUrl")
    val linkUrl: String? = null
)