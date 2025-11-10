package com.collage.new_strishakti.data.model.Profile


data class UserProfileResponse(
    val totalFriends: Int?,
    val userProfileImage: String?,
    val userLocation: String?,
    val userFirstName: String?,
    val userId: Int?,
    val orgId: Int?,
    val totalPosts: Int?,
    val userBirthDate: String?,
    val userLastName: String?,
    val userOrgname: String?,
    val userGender: String?,
    val userEmail: String?,
    val userMobileNumber: String?,
    val userCoverProfileImage: String?
) {
    val fullName: String
        get() = listOfNotNull(userFirstName, userLastName)
            .joinToString(" ")
            .ifBlank { "Unknown" }
}

