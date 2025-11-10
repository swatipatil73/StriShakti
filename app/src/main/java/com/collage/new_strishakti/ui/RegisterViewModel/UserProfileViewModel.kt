package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.collage.new_strishakti.Common.SessionManager
import com.collage.new_strishakti.data.model.Profile.UserProfileResponse
import com.collage.new_strishakti.data.model.Reel.Reel
import com.collage.new_strishakti.data.model.post.ReelData
import com.collage.new_strishakti.data.model.post.ReelsResponse
import com.collage.new_strishakti.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
class UserProfileViewModel(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _userProfile = MutableLiveData<UserProfileResponse?>()
    val userProfile: LiveData<UserProfileResponse?> get() = _userProfile

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _userPosts = MutableLiveData<List<Reel>>()
    val userPosts: LiveData<List<Reel>> get() = _userPosts

    fun fetchUserProfile(uuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getUserProfile(uuid)
                if (response.isSuccessful) {
                    _userProfile.value = response.body()
                } else {
                    _error.value = "Failed to load profile (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val posts = repository.getAllUserPosts()
                _userPosts.value = posts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}