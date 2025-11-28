package com.collage.empowermentstrishakti.data.model.friend

// models/FriendRequestModels.kt
data class FriendRequestItem(
    val userId: Int,
    val userFirstName: String,
    val userLastName: String,
    val userProfileImagePath: String?,
    val friendRequestSentDate: String,
    val friendRequestId: Int
)

data class FriendRequestResponse(
    val friendRequestData: List<FriendRequestItem>?
)
