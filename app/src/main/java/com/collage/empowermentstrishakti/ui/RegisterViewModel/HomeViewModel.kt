package com.collage.empowermentstrishakti.ui.RegisterViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.post.HomeFeedItem
import com.collage.empowermentstrishakti.data.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    // region 🔹 LiveData States
    private val _homeFeedItems = MutableLiveData<List<HomeFeedItem>>(emptyList())
    val homeFeedItems: LiveData<List<HomeFeedItem>> = _homeFeedItems

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _hasNextPage = MutableLiveData<Boolean>()
    val hasNextPage: LiveData<Boolean> = _hasNextPage

    private val _nextCursor = MutableLiveData<Long?>()
    val nextCursor: LiveData<Long?> = _nextCursor
    // endregion


    // region 🔹 Delete post
    /** Removes a post from the current feed (called after delete succeeds). */
    fun removePost(postId: Int) {
        val updated = _homeFeedItems.value
            ?.filterNot { it is HomeFeedItem.PostItem && it.post.postId == postId }
            ?: emptyList()
        _homeFeedItems.value = updated
    }
    // endregion


    // region 🔹 Initial load
    fun loadHomeFeed(userId: Long, token: String) {
        _isLoading.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Step 1️⃣ - Load posts first
                val postResult = repository.loadMorePosts(userId, token, 0)
                val postsPage = postResult.getOrNull()

                postsPage?.let {
                    _homeFeedItems.postValue(it.items)
                    _nextCursor.postValue(it.nextCursor)
                    _hasNextPage.postValue(it.hasNextPage)
                }

                // Step 2️⃣ - Load full feed (ads, reels, announcements)
                val fullResult = repository.loadInitialHomeFeed(userId, token)
                val fullFeed = fullResult.getOrNull()

                fullFeed?.let {
                    _homeFeedItems.postValue(it.items)
                    _nextCursor.postValue(it.nextCursor)
                    _hasNextPage.postValue(it.hasNextPage)
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading home feed", e)
                _error.postValue("Failed to load home feed: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    // endregion


    // region 🔹 Pagination
    fun loadMorePosts(userId: Long, token: String, cursor: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.loadMorePosts(userId, token, cursor)
                val morePosts = result.getOrNull()

                morePosts?.let {
                    val current = _homeFeedItems.value?.toMutableList() ?: mutableListOf()
                    current.addAll(it.items)
                    _homeFeedItems.postValue(current)
                    _nextCursor.postValue(it.nextCursor)
                    _hasNextPage.postValue(it.hasNextPage)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading more posts", e)
                _error.postValue("Failed to load more posts: ${e.localizedMessage}")
            }
        }
    }
    // endregion
}
