package com.collage.empowermentstrishakti.data.model.Groups



import com.google.gson.annotations.SerializedName

data class GroupDetail(
    @SerializedName("groupId")
    val groupId: Int = 0,

    @SerializedName("groupName")
    val groupName: String? = null,

    @SerializedName("groupDescription")
    val groupDescription: String? = null,

    @SerializedName("groupCoverProfileImagePath")
    val groupCoverProfileImagePath: String? = null,

    @SerializedName("adminId")
    val adminId: Int = 0,

    @SerializedName("adminUserFirstName")
    val adminUserFirstName: String? = null,

    @SerializedName("adminUserLastName")
    val adminUserLastName: String? = null,

    @SerializedName("adminUserProfileImagePath")
    val adminUserProfileImagePath: String? = null,

    // ISO datetime string (parse later when formatting)
    @SerializedName("groupCreatedAt")
    val groupCreatedAt: String? = null,

    @SerializedName("groupUUID")
    val groupUUID: String? = null
) {
    // convenience property
    val adminFullName: String
        get() {
            val first = adminUserFirstName.orEmpty()
            val last = adminUserLastName.orEmpty()
            return listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
        }
}
