package com.collage.new_strishakti.data.repository

import com.collage.new_strishakti.data.model.Event.DeleteResponse
import com.collage.new_strishakti.data.model.Event.EventDetailResponse
import com.collage.new_strishakti.data.model.Event.EventResponse
import com.collage.new_strishakti.data.model.Event.JoinEventResponse
import com.collage.new_strishakti.data.model.Event.ParticipantResponse
import com.collage.new_strishakti.data.model.post.CommonResponse
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.network.ApiClient
import retrofit2.Response

class EventRepository {

    private val api = ApiClient.apiService

    /**
     * Suspend function to fetch districts for a given stateId.
     * Caller should call from a coroutine (eg. viewModelScope).
     */
    suspend fun getDistricts(stateId: Int): Response<List<District>> {
        return api.getDistricts(stateId)
    }

    /**
     * Suspend function to fetch events for the given params.
     * - userId: host/user id
     * - districtId: district id (0 or specific)
     * - page, size: pagination
     * - token: raw token (pass "Bearer <token>" from caller if needed)
     *
     * Caller should call from a coroutine (eg. viewModelScope).
     */
    suspend fun getEvents(
        userId: Int,
        districtId: Int,
        page: Int = 0,
        size: Int = 5,
        token: String? = null
    ): Response<EventResponse> {
        // If you store token without "Bearer ", build it here or pass already formatted
        val authHeader = token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
        return api.getEvents(userId = userId, districtId = districtId, page = page, size = size, authorization = authHeader)
    }

    suspend fun deleteEvent(hostUserId: Int, eventId: Int, token: String?): Response<DeleteResponse> {
        val authHeader = token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
        return ApiClient.apiService.deleteEvent(hostUserId, eventId, authHeader)
    }

    suspend fun getEventDetails(userId: Int, eventUUID: String, token: String?): Response<EventDetailResponse> {
        val authHeader = token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
        return api.getEventDetails(userId = userId, eventUUID = eventUUID, token = authHeader)
    }

    suspend fun joinEvent(eventId: Int, token: String): Response<JoinEventResponse> {
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return ApiClient.apiService.joinEvent(authHeader, eventId)
    }

    suspend fun getParticipants(
        eventUUID: String,
        page: Int = 0,
        size: Int = 20,
        token: String
    ): Response<ParticipantResponse> {
        val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
        return ApiClient.apiService.getParticipants(authHeader, eventUUID, page, size)
    }
    suspend fun exitEvent(userId: Int, eventId: Int, token: String): Response<CommonResponse> {
        return api.exitEvent(userId, eventId, "Bearer $token") // add Bearer if needed
    }


}