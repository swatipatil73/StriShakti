package com.collage.empowermentstrishakti.data.model.Profile
import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("userDateOfBirth")
    val userDateOfBirth: String? = null,

    @SerializedName("userAddress")
    val userAddress: String? = null,

    @SerializedName("userProfileImagePath")
    val userProfileImagePath: String? = null,

    @SerializedName("userCoverProfileImagePath")
    val userCoverProfileImagePath: String? = null,

    @SerializedName("userFirstName")
    val userFirstName: String? = null,

    @SerializedName("userLastName")
    val userLastName: String? = null,

    @SerializedName("orgId")
    val orgId: Int? = null,

    @SerializedName("subRole")
    val subRole: String? = null
)
