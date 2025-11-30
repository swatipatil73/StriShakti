package com.collage.empowermentstrishakti.data.repository


import com.collage.empowermentstrishakti.data.model.CreatePagePostRequest
import com.collage.empowermentstrishakti.data.model.CreatePagePostResponse
import com.collage.empowermentstrishakti.data.model.PageDetailsResponse
import com.collage.empowermentstrishakti.data.model.PageListResponse
import com.collage.empowermentstrishakti.data.model.SavedPost.CreatePageRequest
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.network.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class PagesRepository(
    private val api: ApiService
) {

    suspend fun fetchPages(
        userId: Int,
        page: Int,
        size: Int,
        token: String
    ): Response<PageListResponse> {
        return api.getAllPages(userId = userId, page = page, size = size, token = token)
    }

    suspend fun fetchOwnPages(
        userId: Int,
        page: Int,
        size: Int,
        token: String
    ): Response<PageListResponse> {
        return api.getOwnPages(userId = userId, page = page, size = size, token = token)
    }

    suspend fun fetchPagesByMode(
        userId: Int,
        page: Int,
        size: Int,
        token: String,
        showOwn: Boolean
    ): Response<PageListResponse> {
        return if (showOwn) fetchOwnPages(userId, page, size, token)
        else fetchPages(userId, page, size, token)
    }

    suspend fun followPage(
        userId: Int,
        pagesId: Int,
        token: String
    ): Response<CommonResponse> {
        return api.followPage(userId = userId, pagesId = pagesId, token = token)
    }

    suspend fun unfollowPage(
        userId: Int,
        pagesId: Int,
        pageAdminUserId: Int,
        token: String
    ): Response<CommonResponse> {
        return api.unfollowPage(
            pageAdminUserId = pageAdminUserId,
            pagesId = pagesId,
            userId = userId,
            token = token
        )
    }

    suspend fun deletePage(
        pageAdminUserId: Int,
        pagesId: Int,
        token: String
    ): Response<CommonResponse> {
        return api.deletePage(
            pageAdminUserId = pageAdminUserId,
            pagesId = pagesId,
            token = token
        )
    }

    suspend fun createPage(
        adminUserId: Int,
        body: CreatePageRequest,
        token: String
    ): Response<CommonResponse> {
        return api.createPage(adminUserId = adminUserId, body = body, token = token)
    }

    suspend fun getPageDetails(puuid: String, userId: Int, page: Int, size: Int, token: String): Response<PageDetailsResponse> {
        return api.getPageDetails(puuid = puuid, userId = userId, page = page, size = size, authorization = token)
    }

    suspend fun addPagePostMultipart(
        pageAdminUserId: Int,
        pageId: Int,
        postName: RequestBody,
        postType: RequestBody,
        videoThumbnailUrl: RequestBody,
        hashtags: RequestBody?,
        mentionIds: RequestBody?,
        postImage: List<MultipartBody.Part>?,
        authorization: String
    ): Response<CreatePagePostResponse> {
        // ensure token header format: "Bearer <token>" (if your API expects that). Adjust if different.
        val authHeader = if (authorization.startsWith("Bearer")) authorization else "Bearer $authorization"
        return api.addPagePostMultipart(
            pageAdminUserId,
            pageId,
            postName,
            postType,
            videoThumbnailUrl,
            hashtags,
            mentionIds,
            postImage,
            authHeader
        )
    }

    /**
     * Helper that creates a page and throws on non-success.
     */
    suspend fun createPageOrThrow(
        adminUserId: Int,
        body: CreatePageRequest,
        token: String
    ): CommonResponse {
        val resp = createPage(adminUserId, body, token)
        if (resp.isSuccessful) {
            return resp.body() ?: CommonResponse(message = "No response body", status = "Error")
        } else {
            throw RuntimeException("CreatePage API error: ${resp.code()} ${resp.message()}")
        }
    }

    /**
     * Update page on server - returns Result<CommonResponse>
     */

    suspend fun updatePageOnServer(
        jwtToken: String,                 // the JWT string (may already include "Bearer " or not)
        rawToken: String,                 // the raw token string (as backend expects in "token" header)
        pageAdminUserId: Int,
        pageId: Int,
        pageName: String,
        pageDescription: String?,
        linkUrlName: String?,
        linkUrl: String?,
        coverImagePart: MultipartBody.Part? = null
    ): Result<CommonResponse> {
        return try {
            val textMediaType = "text/plain".toMediaType()
            fun strPart(value: String?) = (value ?: "").toRequestBody(textMediaType)

            // Ensure Authorization header is "Bearer <token>"
            val authHeader = if (jwtToken.startsWith("Bearer ", ignoreCase = true)) jwtToken else "Bearer $jwtToken"

            val resp: Response<CommonResponse> = api.updatePage(
                pageAdminUserId = pageAdminUserId,
                pageId = pageId,
                authorization = authHeader,
                token = rawToken,
                pageName = strPart(pageName),
                pageDescription = strPart(pageDescription),
                linkUrlName = strPart(linkUrlName),
                linkUrl = strPart(linkUrl),
                coverImage = coverImagePart
            )

            if (resp.isSuccessful) {
                resp.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val err = resp.errorBody()?.string()
                Result.failure(Exception(err ?: "Server error: ${resp.code()} ${resp.message()}"))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }



    // Optional helper if you like throwing instead of checking isSuccessful
    suspend fun deletePageOrThrow(
        pageAdminUserId: Int,
        pagesId: Int,
        token: String
    ): CommonResponse {
        val resp = deletePage(pageAdminUserId, pagesId, token)
        if (resp.isSuccessful) {
            return resp.body() ?: CommonResponse(
                message = "No message from server",
                status = "Error"
            )
        } else {
            throw RuntimeException("Delete page API error: ${resp.code()} ${resp.message()}")
        }
    }

    /** Optional helpers that unwrap response or throw (useful if you want simpler call-sites) */
    suspend fun followPageOrThrow(userId: Int, pagesId: Int, token: String): CommonResponse {
        val resp = followPage(userId, pagesId, token)
        if (resp.isSuccessful) return resp.body() ?: CommonResponse(
            message = "No message",
            status = "Error"
        )
        throw RuntimeException("Follow API error: ${resp.code()} ${resp.message()}")
    }

    suspend fun unfollowPageOrThrow(
        userId: Int,
        pagesId: Int,
        pageAdminUserId: Int,
        token: String
    ): CommonResponse {
        val resp = unfollowPage(userId, pagesId, pageAdminUserId, token)
        if (resp.isSuccessful) return resp.body() ?: CommonResponse(
            message = "No message",
            status = "Error"
        )
        throw RuntimeException("Unfollow API error: ${resp.code()} ${resp.message()}")
    }

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

    suspend fun fetchPagesByModeOrThrow(
        userId: Int,
        page: Int,
        size: Int,
        token: String,
        showOwn: Boolean
    ): PageListResponse {
        val response = fetchPagesByMode(userId, page, size, token, showOwn)
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
            throw RuntimeException("API error: ${response.code()} - ${response.message()}")
        }
    }
}
