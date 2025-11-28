package com.collage.empowermentstrishakti.data.model.Event

data class EventResponse(
    val hasNextPage: Boolean? = null,
    val totalPages: Int? = null,
    val pageSize: Int? = null,
    val nextPageNo: Int? = null,
    val allEventDetails: List<Event>? = null,
    val currentPage: Int? = null,
    val totalElements: Int? = null
)
