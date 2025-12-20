package com.collage.empowermentstrishakti.data.model.Chat

data class GroupChatMessage(
    val chatId: Int,
    val groupId: Int,
    val content: String,
    val senderId: Int,
    val lastMessageSenderName: String,
    val timestamp: String,
    val isMine: Boolean
)
