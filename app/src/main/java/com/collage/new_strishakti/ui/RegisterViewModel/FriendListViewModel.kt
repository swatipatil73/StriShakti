package com.collage.new_strishakti.ui.RegisterViewModel


import androidx.lifecycle.*
import com.collage.new_strishakti.data.model.FriendData
import com.collage.new_strishakti.data.model.friend.SearchedUser
import com.collage.new_strishakti.data.repository.FriendRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class FriendListViewModel(private val repository: FriendRepository) : ViewModel() {

    private val _friendsList = MutableLiveData<List<FriendData>>(emptyList())
    val friendsList: LiveData<List<FriendData>> = _friendsList

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // --- Search state ---
    private val _searchResults = MutableLiveData<List<SearchedUser>?>(null)
    val searchResults: LiveData<List<SearchedUser>?> = _searchResults

    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    fun fetchFriendsList(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.getFriendsList(userId)
                _friendsList.value = if (res.isSuccessful) res.body()?.friendListData ?: emptyList() else emptyList()
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
            // null means “show default friends adapter”
            _searchResults.value = null
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
                        if (res.isSuccessful) {
                            _searchResults.postValue(res.body()?.searchedData ?: emptyList())
                        } else {
                            _searchResults.postValue(emptyList())
                        }
                    } catch (_: Exception) {
                        _searchResults.postValue(emptyList())
                    } finally {
                        _isLoading.postValue(false)
                    }
                }
        }
    }
}
