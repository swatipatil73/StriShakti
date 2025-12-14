package com.collage.empowermentstrishakti.data.model.Chat

data class ChatMessage(
    var sender: User? = null,
    var receiver: User? = null,
    var group: Group? = null,
    var content: String? = null,
    var timestamp: String? = null
)
