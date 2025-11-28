package com.collage.empowermentstrishakti.data.model.friend

// SearchFriendsResponse.kt
data class SearchFriendsResponse(
    val searchedData: List<SearchedUser>?
)

data class SearchedUser(
    val userId: Int,
    val userFirstName: String,
    val userLastName: String,
    val userProfileImagePath: String?,
    val userUUID: String
) {
    val displayName: String get() = "${userFirstName.orEmpty().trim()} ${userLastName.orEmpty().trim()}".trim()
}




