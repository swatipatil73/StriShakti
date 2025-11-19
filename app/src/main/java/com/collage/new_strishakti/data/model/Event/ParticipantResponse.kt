package com.collage.new_strishakti.data.model.Event

data class ParticipantResponse(
    val details: List<Participant>?,
    val totalPages: Int?,
    val currentPage: Int?,
    val totalElements: Int?,
    val pageSize: Int?,
    val hasNextPage: Boolean?,
    val nextPageNo: Int?
)
data class Participant(
    val userId: Int,
    val userFirstName: String?,
    val userLastName: String?,
    val userProfileImagePath: String?,
    val userEmail: String?,
    val userCreatedAt: String?,
    val uuid: String?
)
