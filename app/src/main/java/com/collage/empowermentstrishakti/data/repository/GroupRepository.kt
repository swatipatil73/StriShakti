package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.network.ApiService
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class GroupRepository(
    private val api: ApiService,
    /**
     * tokenOrEmpty can be:
     *  - a raw token string like "eyJhbGci..." OR
     *  - already prefixed with "Bearer "
     */
    private val tokenOrEmpty: String
) {

    private fun bearer(): String {
        if (tokenOrEmpty.isBlank()) return ""
        return if (tokenOrEmpty.startsWith("Bearer ", ignoreCase = true)) tokenOrEmpty else "Bearer $tokenOrEmpty"
    }

    suspend fun getGroupsForUser(userId: Int): NetworkResult<List<GroupDetail>> {
        return try {
            val auth = bearer()
            val response = api.getGroups(auth, userId) // Retrofit method with @Header("Authorization")
            if (response.isSuccessful) {
                val body = response.body()
                NetworkResult.Success(body?.groupDetails ?: emptyList())
            } else {
                val msg = response.errorBody()?.string().takeIf { !it.isNullOrBlank() } ?: response.message()
                NetworkResult.Error(msg, response.code())
            }
        } catch (io: IOException) {
            NetworkResult.Error("Please check your internet connection")
        } catch (http: HttpException) {
            NetworkResult.Error(http.message ?: "Server error", http.code())
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }


    suspend fun fetchGroupDetails(
        userId: Int,
        groupUUID: String,
        page: Int,
        size: Int,
        token: String
    ): Response<GroupDetailsResponse> {
        return api.getGroupDetails(userId, groupUUID, page, size, token)
    }
}