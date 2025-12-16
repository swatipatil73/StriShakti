package com.collage.empowermentstrishakti.data.model.Chat

data class GroupChatMessage(
    val chatId: Int,
    val groupId: Int,
    val content: String,
    val senderId: Int,
    val senderName: String,
    val timestamp: String,
    val isMine: Boolean
)
