package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.Event.CreateEventResponse
import com.collage.new_strishakti.data.model.Event.Event
import com.collage.new_strishakti.data.model.Event.EventCategory
import com.collage.new_strishakti.data.model.Event.EventDetailResponse
import com.collage.new_strishakti.data.model.Event.EventResponse
import com.collage.new_strishakti.data.model.Event.Participant
import com.collage.new_strishakti.data.model.regi.District
import com.collage.new_strishakti.data.repository.EventRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.IOException


class EventViewModel(
    private val repository: EventRepository
) : ViewModel() {
    private var currentHostId: Int? = null
    private val _categories = MutableLiveData<List<EventCategory>>()
    val categories: LiveData<List<EventCategory>> = _categories

    private val _createResult = MutableLiveData<Result<CreateEventResponse>?>()
    val createResult: LiveData<Result<CreateEventResponse>?> = _createResult

    private val _loadingCategories = MutableLiveData<Boolean>(false)
    val loadingCategories: LiveData<Boolean> = _loadingCategories

    private val _deleteLoading = MutableLiveData<Boolean>(false)
    val deleteLoading: LiveData<Boolean> = _deleteLoading

    private val _deleteResult = MutableLiveData<Pair<Boolean, String?>>()
    val deleteResult: LiveData<Pair<Boolean, String?>> = _deleteResult

    private val _districts = MutableLiveData<List<District>>()
    val districts: LiveData<List<District>> = _districts

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events
    private var currentQueryHostId: Int? = null
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

    private val _exitLoading = MutableLiveData<Boolean>(false)
    val exitLoading: LiveData<Boolean> = _exitLoading

    // Pair(success, message)
    private val _exitResult = MutableLiveData<Pair<Boolean, String?>>()
    val exitResult = _exitResult


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

    fun loadCategories(token: String?) {
        val auth = token ?: return
        viewModelScope.launch {
            _loadingCategories.value = true
            val res = repository.fetchEventCategories(auth)
            _loadingCategories.value = false
            if (res.isSuccess) {
                _categories.value = res.getOrDefault(emptyList())
            } else {
                // You may want to expose error LiveData too
                _categories.value = emptyList()
            }
        }
    }

    fun createEvent(
        token: String,
        hostUserId: Int,
        districtId: Int,
        eventCatgId: Int,
        fields: Map<String, RequestBody>,
        imagePart: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _createResult.value = Result.failure(Exception("loading"))
            val res = repository.createEventMultipart(token, hostUserId, districtId, eventCatgId, fields, imagePart)
            _createResult.value = res
        }
    }

    fun clearCreateResult() {
        _createResult.value = null
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

//    fun loadNextPage() {
//        if (_loadingMore.value == true) return
//        if (_hasNextPage.value != true) return
//
//        _loadingMore.value = true
//        _error.value = null
//        val nextPage = currentPage + 1
//
//        viewModelScope.launch {
//            try {
//                val resp = repository.getEvents(lastUserId, lastDistrictId, nextPage, currentSize, lastToken)
//                if (resp.isSuccessful) {
//                    val body = resp.body()
//                    val newItems = body?.allEventDetails ?: emptyList()
//                    val current = _events.value ?: emptyList()
//                    val merged = ArrayList<Event>(current.size + newItems.size)
//                    merged.addAll(current)
//                    merged.addAll(newItems)
//                    _events.value = merged
//                    currentPage = body?.currentPage ?: nextPage
//                    _hasNextPage.value = body?.hasNextPage ?: false
//                } else {
//                    _error.value = parseError(resp)
//                }
//            } catch (t: Throwable) {
//                _error.value = if (t is IOException) "Network error: ${t.message}" else "Unexpected error: ${t.message}"
//            } finally {
//                _loadingMore.value = false
//            }
//        }
//    }

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

    fun exitEvent(userId: Int, eventId: Int, authToken: String, isHost: Boolean) {
        _exitLoading.value = true
        viewModelScope.launch {
            try {
                val (success, msg) = repository.exitEvent(userId, eventId, authToken)
                _exitResult.value = Pair(success, msg)

                if (success) {
                    // Use the backing MutableLiveData (_participants) to modify the list
                    val current = _participants.value?.toMutableList() ?: mutableListOf()
                    val removed = current.removeAll { it.userId == userId }
                    if (removed) {
                        _participants.value = current
                    }

                    // Update EventDetailResponse via the backing MutableLiveData (_details)
                    val about = _details.value?.about?.firstOrNull()
                    if (!isHost && about != null) {
                        about.isParticipant = false
                        // reassign the same object to trigger observers
                        _details.value = _details.value
                    }
                }
            } catch (t: Throwable) {
                // optional: surface error via _exitResult
                _exitResult.value = Pair(false, t.localizedMessage ?: "Error")
            } finally {
                _exitLoading.value = false
            }
        }
    }

    // NEW: load host-created events (shows only events where hostUserId == host)
    fun loadHostEvents(hostUserId: Int, page: Int = 0, size: Int = 5, token: String? = null) {
        _loading.value = true
        _error.value = null

        currentPage = page
        currentSize = size
        currentHostId = hostUserId
        lastToken = token

        viewModelScope.launch {
            try {
                val resp: Response<EventResponse> = repository.getHostEvents(hostUserId, page, size, token)
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

    // Update loadNextPage to handle both normal and host modes
    fun loadNextPage() {
        if (_loadingMore.value == true) return
        if (_hasNextPage.value != true) return

        _loadingMore.value = true
        _error.value = null
        val nextPage = currentPage + 1

        viewModelScope.launch {
            try {
                if (currentHostId != null) {
                    // host-events pagination
                    val resp = repository.getHostEvents(currentHostId!!, nextPage, currentSize, lastToken)
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
                } else {
                    // normal events pagination (existing behaviour)
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
                }
            } catch (t: Throwable) {
                _error.value = if (t is IOException) "Network error: ${t.message}" else "Unexpected error: ${t.message}"
            } finally {
                _loadingMore.value = false
            }
        }
    }

    // Optional helper
    fun isShowingHostEvents(): Boolean = currentHostId != null

    // Optional: call this to go back to normal mode and clear host filter
    fun clearHostMode() {
        currentHostId = null
    }

    private fun <T> parseError(response: Response<T>): String {
        return "API error: ${response.code()} ${response.message()}"
    }

    fun clearError() {
        _error.value = null
    }
}

