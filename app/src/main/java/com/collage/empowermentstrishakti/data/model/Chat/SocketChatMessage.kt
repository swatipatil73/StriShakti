package com.collage.empowermentstrishakti.data.model.Chat
data class SocketChatMessage(
    val content: String,
    val timestamp: String?,
    val sender: SocketUser,
    val group: SocketGroup?
)

data class SocketUser(
    val userId: Int,
    val userName: String?
)

data class SocketGroup(
    val groupId: Int
)
