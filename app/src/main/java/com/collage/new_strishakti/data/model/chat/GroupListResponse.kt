package com.collage.new_strishakti.data.model.chat

data class GroupListResponse(
    val groupDetails: List<GroupData>
)

data class GroupData(
    val groupId: Int,
    val groupName: String,
    val groupDescription: String,
    val groupCoverProfileImagePath: String,
    val adminId: Int,
    val adminUserFirstName: String,
    val adminUserLastName: String,
    val adminUserProfileImagePath: String,
    val groupCreatedAt: String,
    val groupUUID: String
)

