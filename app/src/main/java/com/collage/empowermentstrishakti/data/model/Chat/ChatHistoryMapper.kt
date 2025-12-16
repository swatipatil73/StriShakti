package com.collage.empowermentstrishakti.data.model.Chat

class ChatHistoryMapper {

    fun map(
        list: List<ChatHistoryResponse>
    ): List<ChatUiItem> {

        val result = mutableListOf<ChatUiItem>()
        var lastDateHeader: String? = null

        list.sortedBy { it.timestamp }.forEach { msg ->

            val header = DateUtils.getDateHeader(msg.timestamp)

            if (header != lastDateHeader) {
                result.add(ChatUiItem.DateHeader(header))
                lastDateHeader = header
            }

            result.add(
                ChatUiItem.MessageItem(
                    id = msg.id,
                    message = msg.content,
                    time = DateUtils.parseTime(msg.timestamp),
                    senderId = msg.sender.userId
                )
            )
        }
        return result
    }
}
