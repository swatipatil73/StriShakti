package com.collage.empowermentstrishakti.data.model.Chat

class GroupChatHistoryMapper {

    fun map(
        list: List<GroupChatHistoryItem>,
        myUserId: Int
    ): List<GroupChatMessage> {

        return list.map {
            GroupChatMessage(
                chatId = it.chatId,
                groupId = it.groupId,
                content = it.lastMessageContent,
                senderId = it.lastMessageSenderId,
                senderName = it.lastMessageSenderName,
                timestamp = it.lastMessageTimestamp,
                isMine = it.lastMessageSenderId == myUserId
            )
        }
    }
}
