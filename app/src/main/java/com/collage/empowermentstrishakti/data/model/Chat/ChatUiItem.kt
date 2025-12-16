package com.collage.empowermentstrishakti.data.model.Chat

sealed class ChatUiItem {

    data class DateHeader(
        val title: String   // Today, Yesterday, 24 Oct 2025
    ) : ChatUiItem()

    data class MessageItem(
        val id: Int,
        val message: String,
        val time: String,
        val senderId: Int
    ) : ChatUiItem()
}
