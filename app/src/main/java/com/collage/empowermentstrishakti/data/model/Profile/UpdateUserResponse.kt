package com.collage.empowermentstrishakti.data.model.Profile

import com.google.gson.annotations.SerializedName
data class UpdateUserResponse(
    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    // If API returns the updated user object, map it here (optional)
    @SerializedName("data")
    val data: UserProfileResponse? = null
)