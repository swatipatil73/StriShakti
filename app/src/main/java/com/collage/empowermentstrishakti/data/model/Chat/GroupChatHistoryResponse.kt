package com.collage.empowermentstrishakti.data.model.Chat

data class GroupChatHistoryResponse(
    val details: List<GroupChatHistoryItem>,
    val totalPages: Int,
    val currentPage: Int,
    val totalElements: Int,
    val pageSize: Int,
    val hasNextPage: Boolean,
    val nextPageNo: Int
)

data class GroupChatHistoryItem(
    val chatId: Int,
    val groupId: Int,
    val groupName: String,
    val lastMessageContent: String,
    val lastMessageTimestamp: String,
    val lastMessageSenderId: Int,
    val lastMessageSenderName: String,
    val lastMessageTime: String
)
