package com.collage.empowermentstrishakti.ui.RegisterViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.Profile.UserProfileResponse
import com.collage.empowermentstrishakti.data.model.Reel.Reel
import com.collage.empowermentstrishakti.data.repository.UserProfileRepository
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
        if (uuid.isBlank()) {
            _error.value = "Profile UUID is missing"
            Log.w("UserProfileViewModel", "fetchUserProfile called with blank uuid")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getUserProfile(uuid)
                if (response.isSuccessful) _userProfile.value = response.body()
                else _error.value = "Failed to load profile (${response.code()})"
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