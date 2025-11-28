package com.collage.empowermentstrishakti.data.model

data class FriendListResponse(
    val friendListData: List<FriendData>
)

data class FriendData(
    val userId: Int,
    val userFirstName: String,
    val userLastName: String,
    val userProfileImagePath: String,
    val friendRequestId: Int,
    val status: String,
    val userUUID: String
)

