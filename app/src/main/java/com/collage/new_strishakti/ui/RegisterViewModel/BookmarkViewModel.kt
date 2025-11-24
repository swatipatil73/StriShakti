package com.collage.new_strishakti.ui.RegisterViewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.SavedPost.SavedPostData
import com.collage.new_strishakti.data.repository.SavedPostsRepository
import com.collage.new_strishakti.data.repository.ApiResult
import kotlinx.coroutines.launch

class BookmarkViewModel(private val repository: SavedPostsRepository) : ViewModel() {

    private val _deleteResult = MutableLiveData<Pair<Long, String?>>()
    val deleteResult: LiveData<Pair<Long, String?>> = _deleteResult

    private val _savedPosts = MutableLiveData<List<SavedPostData>>(emptyList())
    val savedPosts: LiveData<List<SavedPostData>> = _savedPosts

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Paging state
    private var currentPage = 0
    private var pageSize = 10
    private var hasNextPage = true
    private var isFetching = false

    fun loadInitial(userId: Int, token: String, size: Int = 10) {
        currentPage = 0
        pageSize = size
        hasNextPage = true
        _savedPosts.value = emptyList()
        fetch(userId, token, reset = true)
    }

    fun loadNext(userId: Int, token: String) {
        if (!hasNextPage || isFetching) return
        fetch(userId, token, reset = false)
    }

    private fun fetch(userId: Int, token: String, reset: Boolean) {
        viewModelScope.launch {
            isFetching = true
            _loading.value = true
            _error.value = null

            when (val res = repository.getSavedPosts(userId, pageSize, currentPage, token)) {
                is ApiResult.Success -> {
                    val response = res.data
                    val newList = response.savedPostData

                    if (reset) {
                        _savedPosts.value = newList
                    } else {
                        _savedPosts.value = (_savedPosts.value ?: emptyList()) + newList
                    }

                    hasNextPage = response.hasNextPage
                    currentPage = response.nextPageNo
                }
                is ApiResult.Error -> {
                    _error.value = res.message
                }
                else -> { }
            }

            _loading.value = false
            isFetching = false
        }
    }



    fun deleteSavedPost(userId: Long, postId: Long, token: String) {
        // Optimistic delete: remove from UI immediately
        val oldList = _savedPosts.value ?: emptyList()
        _savedPosts.value = oldList.filter { it.postId != postId }

        viewModelScope.launch {
            when (val res = repository.deleteSavedPost(userId, postId, token)) {

                is ApiResult.Success -> {
                    // Tell UI delete succeeded
                    _deleteResult.value = Pair(postId, null)
                }

                is ApiResult.Error -> {
                    // Restore old list on server failure
                    _savedPosts.value = oldList
                    _deleteResult.value = Pair(postId, res.message)
                }

                else -> {}
            }
        }
    }
}
