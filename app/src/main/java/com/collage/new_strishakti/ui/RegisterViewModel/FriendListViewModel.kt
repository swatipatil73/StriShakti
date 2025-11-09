package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.FriendData
import com.collage.new_strishakti.data.model.friend.FriendRequestItem
import com.collage.new_strishakti.data.model.friend.SearchedUser
import com.collage.new_strishakti.data.repository.FriendRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class FriendListViewModel(private val repository: FriendRepository) : ViewModel() {

    // ---------------- Friends list ----------------
    private val _friendsList = MutableLiveData<List<FriendData>>(emptyList())
    val friendsList: LiveData<List<FriendData>> = _friendsList

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // ---------------- Search ----------------
    private val _searchResults = MutableLiveData<List<SearchedUser>?>(null)
    val searchResults: LiveData<List<SearchedUser>?> = _searchResults

    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    fun fetchFriendsList(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.getFriendsList(userId)
                _friendsList.value = if (res.isSuccessful)
                    res.body()?.friendListData ?: emptyList()
                else emptyList()
            } catch (_: Exception) {
                _friendsList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchTextChanged(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = null // show default friends adapter
            return
        }
        if (searchJob?.isActive == true) return
        searchJob = viewModelScope.launch {
            searchQuery
                .debounce(350)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isBlank()) {
                        _searchResults.postValue(null)
                        return@collect
                    }
                    _isLoading.postValue(true)
                    try {
                        val res = repository.searchFriends(q)
                        _searchResults.postValue(
                            if (res.isSuccessful) res.body()?.searchedData ?: emptyList()
                            else emptyList()
                        )
                    } catch (_: Exception) {
                        _searchResults.postValue(emptyList())
                    } finally {
                        _isLoading.postValue(false)
                    }
                }
        }
    }

    // ---------------- Friend Requests ----------------
    sealed class FriendReqUiState {
        object Idle : FriendReqUiState()
        object Loading : FriendReqUiState()
        data class Success(val list: List<FriendRequestItem>) : FriendReqUiState()
        object Empty : FriendReqUiState()
        data class Error(val message: String) : FriendReqUiState()
    }

    private val _friendReqState = MutableLiveData<FriendReqUiState>(FriendReqUiState.Idle)
    val friendReqState: LiveData<FriendReqUiState> = _friendReqState

    fun fetchFriendRequests(receiverId: Int) {
        _friendReqState.value = FriendReqUiState.Loading
        viewModelScope.launch {
            try {
                val res = repository.getFriendRequests(receiverId)
                if (res.isSuccessful) {
                    val list = res.body()?.friendRequestData.orEmpty()
                    _friendReqState.value =
                        if (list.isEmpty()) FriendReqUiState.Empty
                        else FriendReqUiState.Success(list)
                } else {
                    _friendReqState.value = FriendReqUiState.Error("Server ${res.code()}")
                }
            } catch (e: Exception) {
                _friendReqState.value =
                    FriendReqUiState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }

    sealed class ApproveUI {
        object Loading : ApproveUI()
        data class Success(val position: Int, val message: String) : ApproveUI()
        data class Error(val message: String) : ApproveUI()
    }

    private val _approveState = MutableLiveData<ApproveUI>()
    val approveState: LiveData<ApproveUI> = _approveState

    fun approveFriendRequest(friendRequestId: Int, position: Int) {
        _approveState.value = ApproveUI.Loading
        viewModelScope.launch {
            try {
                val res = repository.approveFriendRequest(friendRequestId)
                if (res.isSuccessful) {
                    val msg = res.body()?.message ?: "Friend request approved"
                    _approveState.value = ApproveUI.Success(position, msg)
                } else {
                    _approveState.value = ApproveUI.Error("Server ${res.code()}")
                }
            } catch (e: Exception) {
                _approveState.value = ApproveUI.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }
}