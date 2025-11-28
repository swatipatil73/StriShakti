package com.collage.empowermentstrishakti.data.model.regi

data class Department(
    val id: Int,
    val uuid: String?,
    val name: String,
    val description: String?
)

data class StreamItem(
    val streamId: Int,
    val streamUuid: String?,
    val streamName: String,
    val createdAt: String?,
    val updatedAt: String?
)
