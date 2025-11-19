package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.Event.Event
import com.collage.new_strishakti.data.model.Event.EventDetailResponse
import com.collage.new_strishakti.data.model.Event.EventResponse
import com.collage.new_strishakti.data.model.Event.Participant
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.repository.EventRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException


class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {

    private val _deleteLoading = MutableLiveData<Boolean>(false)
    val deleteLoading: LiveData<Boolean> = _deleteLoading

    private val _deleteResult = MutableLiveData<Pair<Boolean, String?>>()
    val deleteResult: LiveData<Pair<Boolean, String?>> = _deleteResult

    private val _districts = MutableLiveData<List<District>>()
    val districts: LiveData<List<District>> = _districts

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>(false)
    val loadingMore: LiveData<Boolean> = _loadingMore

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _details = MutableLiveData<EventDetailResponse?>()
    val details: LiveData<EventDetailResponse?> = _details

    private val _hasNextPage = MutableLiveData<Boolean>(false)
    val hasNextPage: LiveData<Boolean> = _hasNextPage
    private val _joinLoading = MutableLiveData<Boolean>(false)
    val joinLoading: LiveData<Boolean> = _joinLoading

    private val _joinResult = MutableLiveData<Pair<Boolean, String?>>()
    val joinResult: LiveData<Pair<Boolean, String?>> = _joinResult
    private val _participants = MutableLiveData<List<Participant>>()
    val participants: LiveData<List<Participant>> = _participants

    private val _participantsLoading = MutableLiveData<Boolean>(false)
    val participantsLoading: LiveData<Boolean> = _participantsLoading

    private val _participantsEmpty = MutableLiveData<Boolean>(false)
    val participantsEmpty: LiveData<Boolean> = _participantsEmpty


    // paging state
    private var currentPage = 0
    private var currentSize = 5
    private var lastDistrictId = 0
    private var lastUserId = -1
    private var lastToken: String? = null

    fun loadDistricts(stateId: Int) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val resp: Response<List<District>> = repository.getDistricts(stateId)
                if (resp.isSuccessful) {
                    _districts.value = resp.body() ?: emptyList()
                } else {
                    _error.value = parseError(resp)
                }
            } catch (t: Throwable) {
                _error.value = if (t is IOException) "Network error: ${t.message}" else "Unexpected error: ${t.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadEvents(userId: Int, districtId: Int, page: Int = 0, size: Int = 5, token: String? = null) {
        _loading.value = true
        _error.value = null

        currentPage = page
        currentSize = size
        lastDistrictId = districtId
        lastUserId = userId
        lastToken = token

        viewModelScope.launch {
            try {
                val resp: Response<EventResponse> = repository.getEvents(userId, districtId, page, size, token)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    _events.value = body?.allEventDetails ?: emptyList()
                    _hasNextPage.value = body?.hasNextPage ?: false
                } else {
                    _error.value = parseError(resp)
                }
            } catch (t: Throwable) {
                _error.value = if (t is IOException) "Network error: ${t.message}" else "Unexpected error: ${t.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadNextPage() {
        if (_loadingMore.value == true) return
        if (_hasNextPage.value != true) return

        _loadingMore.value = true
        _error.value = null
        val nextPage = currentPage + 1

        viewModelScope.launch {
            try {
                val resp = repository.getEvents(lastUserId, lastDistrictId, nextPage, currentSize, lastToken)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val newItems = body?.allEventDetails ?: emptyList()
                    val current = _events.value ?: emptyList()
                    val merged = ArrayList<Event>(current.size + newItems.size)
                    merged.addAll(current)
                    merged.addAll(newItems)
                    _events.value = merged
                    currentPage = body?.currentPage ?: nextPage
                    _hasNextPage.value = body?.hasNextPage ?: false
                } else {
                    _error.value = parseError(resp)
                }
            } catch (t: Throwable) {
                _error.value = if (t is IOException) "Network error: ${t.message}" else "Unexpected error: ${t.message}"
            } finally {
                _loadingMore.value = false
            }
        }
    }

    fun deleteEvent(hostUserId: Int, eventId: Int, token: String?) {
        _deleteLoading.value = true
        _deleteResult.value = null

        viewModelScope.launch {
            try {
                val resp = repository.deleteEvent(hostUserId, eventId, token)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    _deleteResult.value = Pair(true, body?.message ?: "Deleted")
                } else {
                    _deleteResult.value = Pair(false, "Delete failed: ${resp.code()} ${resp.message()}")
                }
            } catch (t: Throwable) {
                _deleteResult.value = Pair(false, "Error: ${t.message}")
            } finally {
                _deleteLoading.value = false
            }
        }
    }

    fun loadEventDetails(userId: Int, eventUUID: String, token: String?) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val resp = repository.getEventDetails(userId, eventUUID, token)
                if (resp.isSuccessful) {
                    _details.value = resp.body()
                } else {
                    _error.value = "API error: ${resp.code()} ${resp.message()}"
                }
            } catch (t: Throwable) {
                _error.value = if (t is IOException) "Network error: ${t.message}" else "Unexpected error: ${t.message}"
            } finally {
                _loading.value = false
            }
        }
    }



    fun joinEvent(eventId: Int, token: String) {
        _joinLoading.value = true
        _joinResult.value = null

        viewModelScope.launch {
            try {
                val resp = repository.joinEvent(eventId, token)
                if (resp.isSuccessful) {
                    _joinResult.value = Pair(true, resp.body()?.message ?: "Joined")
                } else {
                    _joinResult.value = Pair(false, "Join failed: ${resp.code()} ${resp.message()}")
                }
            } catch (t: Throwable) {
                _joinResult.value = Pair(false, "Error: ${t.message}")
            } finally {
                _joinLoading.value = false
            }
        }
    }


    fun loadParticipants(eventUUID: String, token: String) {
        _participantsLoading.value = true
        _participantsEmpty.value = false

        viewModelScope.launch {
            try {
                val resp = repository.getParticipants(eventUUID, 0, 50, token)

                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body()?.details ?: emptyList()
                    _participants.value = list
                    _participantsEmpty.value = list.isEmpty()
                } else {
                    _participants.value = emptyList()
                    _participantsEmpty.value = true
                }
            } catch (t: Throwable) {
                _participants.value = emptyList()
                _participantsEmpty.value = true
            } finally {
                _participantsLoading.value = false
            }
        }
    }


    // Add these inside EventViewModel




//    fun exitEvent(eventId: Int, userId: Int, token: String, callback: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            try {
//                val response = api.exitEvent(eventId, userId, "Bearer $token")
//                if (response.isSuccessful && response.body()?.status == "Success") {
//                    callback(true)
//                    // reload participants list after exit
//                    loadParticipants(eventId, token)
//                } else {
//                    callback(false)
//                }
//            } catch (e: Exception) {
//                callback(false)
//            }
//        }
//    }

//    // Delete participant (host only) can call the same API
//    fun deleteParticipant(eventId: Int, userId: Int, token: String, callback: (Boolean) -> Unit) {
//        exitEvent(eventId, userId, token, callback) // same API call
//    }

    private fun <T> parseError(response: Response<T>): String {
        return "API error: ${response.code()} ${response.message()}"
    }

    fun clearError() {
        _error.value = null
    }
}

