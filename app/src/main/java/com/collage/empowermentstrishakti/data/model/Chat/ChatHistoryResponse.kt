package com.collage.empowermentstrishakti.data.model.Chat

data class ChatHistoryResponse(
    val id: Int,
    val content: String,
    val timestamp: String,
    val sender: ChatUser,
    val receiver: ChatUser,
    val group: Any?
)
