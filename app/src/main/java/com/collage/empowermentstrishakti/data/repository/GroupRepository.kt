package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.data.model.FriendListResponse
import com.collage.empowermentstrishakti.data.model.Groups.CreateGroupRequest
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.network.ApiService
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class GroupRepository(
    private val api: ApiService,
    private val tokenOrEmpty: String
) {

    private fun bearer(): String {
        if (tokenOrEmpty.isBlank()) return ""
        return if (tokenOrEmpty.startsWith("Bearer ", ignoreCase = true)) {
            tokenOrEmpty
        } else {
            "Bearer $tokenOrEmpty"
        }
    }

    // -------------------------------------------------------------------------
    // FETCH GROUP LIST
    // -------------------------------------------------------------------------
    suspend fun getGroupsForUser(userId: Int): NetworkResult<List<GroupDetail>> {
        return try {
            val auth = bearer()
            val response = api.getGroups(auth, userId)

            if (response.isSuccessful) {
                val body = response.body()
                NetworkResult.Success(body?.groupDetails ?: emptyList())
            } else {
                val msg = response.errorBody()?.string().takeIf { !it.isNullOrBlank() }
                    ?: response.message()
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

    // -------------------------------------------------------------------------
    // FETCH GROUP DETAILS
    // -------------------------------------------------------------------------
    suspend fun fetchGroupDetails(
        userId: Int,
        groupUUID: String,
        page: Int,
        size: Int,
        token: String
    ): Response<GroupDetailsResponse> {
        return api.getGroupDetails(userId, groupUUID, page, size, token)
    }

    // -------------------------------------------------------------------------
    // FETCH FRIEND LIST
    // -------------------------------------------------------------------------
    suspend fun getFriendsList(userId: Int): Response<FriendListResponse> {
        return api.getFriendsList(userId, bearer())
    }

    // -------------------------------------------------------------------------
    // CREATE GROUP (FINAL FIXED FUNCTION)
    // -------------------------------------------------------------------------
    suspend fun createGroup(
        adminUserId: Int,
        membersIds: List<Int>,
        groupName: String,
        groupDescription: String
    ): NetworkResult<CommonResponse> {   // expected non-null

        return try {
            val token = bearer()

            val request = CreateGroupRequest(
                groupName = groupName,
                groupDescription = groupDescription
            )

            val response = api.createGroup(
                adminUserId = adminUserId,
                membersIds = membersIds,
                token = token,
                request = request
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)    // body is non-null
                } else {
                    NetworkResult.Error("Empty response", response.code())
                }
            } else {
                val msg = response.errorBody()?.string() ?: response.message()
                NetworkResult.Error(msg, response.code())
            }

        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    suspend fun deleteGroup(adminUserId: Int, groupId: Int): CommonResponse {
        return api.deleteGroup(
            token = bearer(),
            adminUserId = adminUserId,
            groupId = groupId
        )

    }

    }


