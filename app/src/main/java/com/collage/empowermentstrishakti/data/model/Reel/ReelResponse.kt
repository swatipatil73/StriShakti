package com.collage.empowermentstrishakti.data.model.Reel



data class ReelResponse(
    val hasNextPage: Boolean,
    val totalPages: Int,
    val pageSize: Int,
    val nextPageNo: Int?,
    val postsData: List<Reel>,
    val currentPage: Int,
    val totalElements: Int
)

