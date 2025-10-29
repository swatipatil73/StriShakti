package com.collage.new_strishakti.ui.RegisterViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.post.Announcement
import com.collage.new_strishakti.data.model.post.HomeFeedItem
import com.collage.new_strishakti.data.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    val homeFeedItems = MutableLiveData<List<HomeFeedItem>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    val hasNextPage = MutableLiveData<Boolean>()
    val nextCursor = MutableLiveData<Long?>()

    fun loadHomeFeed(userId: Long, token: String) {
        isLoading.postValue(true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Step 1️⃣ - Load only posts first
                val postResult = repository.loadMorePosts(userId, token, 0)
                val postsPage = postResult.getOrNull()

                if (postsPage != null) {
                    val items = postsPage.items.toMutableList()
                    homeFeedItems.postValue(items)
                    nextCursor.postValue(postsPage.nextCursor)
                    hasNextPage.postValue(postsPage.hasNextPage)
                }

                // Step 2️⃣ - Now load remaining data (ads, reels, announcement)
                val fullResult = repository.loadInitialHomeFeed(userId, token)
                val fullFeed = fullResult.getOrNull()

                if (fullFeed != null) {
                    homeFeedItems.postValue(fullFeed.items)
                    nextCursor.postValue(fullFeed.nextCursor)
                    hasNextPage.postValue(fullFeed.hasNextPage)
                }

            } catch (e: Exception) {
                error.postValue("Failed to load home feed: ${e.localizedMessage}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun loadMorePosts(userId: Long, token: String, cursor: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.loadMorePosts(userId, token, cursor)
                val morePosts = result.getOrNull()

                if (morePosts != null) {
                    val current = homeFeedItems.value?.toMutableList() ?: mutableListOf()
                    current.addAll(morePosts.items)
                    homeFeedItems.postValue(current)
                    nextCursor.postValue(morePosts.nextCursor)
                    hasNextPage.postValue(morePosts.hasNextPage)
                }
            } catch (e: Exception) {
                error.postValue("Failed to load more posts: ${e.localizedMessage}")
            }
        }
    }
}
