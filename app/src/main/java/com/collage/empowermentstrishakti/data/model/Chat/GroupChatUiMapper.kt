package com.collage.empowermentstrishakti.data.model.Chat

class GroupChatUiMapper {

    fun map(
        messages: List<GroupChatMessage>,
        myUserId: Int
    ): List<ChatUiItem> {

        val result = mutableListOf<ChatUiItem>()
        var lastDate: String? = null

        messages.forEach { msg ->
            val dateLabel =
                ChatDateUtils.getDateLabel(msg.timestamp)

            if (dateLabel != lastDate) {
                result.add(ChatUiItem.DateHeader(dateLabel))
                lastDate = dateLabel
            }

            result.add(
                ChatUiItem.MessageItem(
                    id = msg.chatId,
                    message = msg.content,
                    time = ChatDateUtils.getTime(msg.timestamp),
                    senderId = msg.senderId,

                )
            )
        }
        return result
    }
}
