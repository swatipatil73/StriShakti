package com.collage.new_strishakti.data.repository

import com.collage.new_strishakti.data.model.PageListResponse
import com.collage.new_strishakti.data.network.ApiService
import retrofit2.Response


class PagesRepository(
    private val api: ApiService
) {

    /**
     * Fetch a page of pages.
     *
     * @param userId - userId to pass in path (use sessionManager.getUserId().toString())
     * @param page - zero-based page index
     * @param size - page size (e.g., 5)
     * @param token - "Bearer <token>"
     *
     * Returns Retrofit Response so ViewModel can handle errors / codes.
     */
    suspend fun fetchPages(
        userId: Int,
        page: Int,
        size: Int,
        token: String
    ): Response<PageListResponse> {
        return api.getAllPages(userId = userId, page = page, size = size, token = token)
    }

    /**
     * Optional helper that unwraps body or throws an exception.
     * Use only if you want simpler call-site code.
     */
    suspend fun fetchPagesOrThrow(
        userId: Int,
        page: Int,
        size: Int,
        token: String
    ): PageListResponse {
        val response = fetchPages(userId, page, size, token)
        if (response.isSuccessful) {
            return response.body() ?: PageListResponse(
                hasNextPage = false,
                pagesDetail = emptyList(),
                totalPages = 0,
                pageSize = size,
                nextPageNo = page,
                currentPage = page,
                totalElements = 0
            )
        } else {
            // Throw an exception that the ViewModel / caller can catch and handle
            throw RuntimeException("API error: ${response.code()} - ${response.message()}")
        }
    }
}