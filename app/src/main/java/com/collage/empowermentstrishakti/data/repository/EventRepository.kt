package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.Event.CreateEventResponse
import com.collage.empowermentstrishakti.data.model.Event.DeleteResponse
import com.collage.empowermentstrishakti.data.model.Event.EventCategory
import com.collage.empowermentstrishakti.data.model.Event.EventDetailResponse
import com.collage.empowermentstrishakti.data.model.Event.EventResponse
import com.collage.empowermentstrishakti.data.model.Event.JoinEventResponse
import com.collage.empowermentstrishakti.data.model.Event.ParticipantResponse
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.model.regi.District
import com.collage.empowermentstrishakti.data.network.ApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

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
        size: Int = 115,
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
    suspend fun exitEvent(userId: Int, eventId: Int, authToken: String): Pair<Boolean, String?> {
        return try {
            val response = api.exitEvent(authToken, userId, eventId)
            if (response.isSuccessful) {
                val body: CommonResponse? = response.body()
                Pair(true, body?.message ?: "Success")
            } else {
                // try to read server error message if present
                val err = response.errorBody()?.string() ?: response.message()
                Pair(false, err)
            }
        } catch (e: IOException) {
            // network / timeout
            Pair(false, e.localizedMessage ?: "Network error")
        } catch (e: HttpException) {
            Pair(false, e.localizedMessage ?: "Server error")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Unknown error")
        }
    }

    suspend fun fetchEventCategories(authHeader: String): Result<List<EventCategory>> {
        return try {
            val resp = api.getEventCategories(authHeader)
            if (resp.isSuccessful) {
                val body = resp.body()
                Result.success(body?.eventCatgDetails ?: emptyList())
            } else {
                Result.failure(Exception("Server error: ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CREATE MULTIPART EVENT
    suspend fun createEventMultipart(
        authHeader: String,
        hostUserId: Int,
        districtId: Int,
        eventCatgId: Int,
        parts: Map<String, RequestBody>,
        imagePart: MultipartBody.Part?
    ): Result<CreateEventResponse> {
        return try {
            val resp = api.createEvent(
                authHeader,
                hostUserId,
                districtId,
                eventCatgId,
                parts,
                imagePart
            )
            if (resp.isSuccessful) {
                resp.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response"))
            } else {
                val msg = resp.errorBody()?.string() ?: "Server: ${resp.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHostEvents(
        hostUserId: Int,
        page: Int = 0,
        size: Int = 5,
        token: String? = null
    ): Response<EventResponse> {
        val authHeader = token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
        return api.getHostEvents(authHeader, hostUserId, page, size)
    }


}