package com.collage.empowermentstrishakti.ui.RegisterViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.Profile.OrgDetail
import com.collage.empowermentstrishakti.data.model.Profile.OrgDetailsResponse
import com.collage.empowermentstrishakti.data.model.Profile.UpdateUserResponse
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


    private val _orgDetails = MutableLiveData<List<OrgDetail>>()
    val orgDetails: LiveData<List<OrgDetail>> = _orgDetails

    private val _updateResponse = MutableLiveData<UpdateUserResponse?>()
    val updateResponse: LiveData<UpdateUserResponse?> = _updateResponse


    fun fetchOrgDetails() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val resp = repository.getOrgDetails()
                if (resp.isSuccessful) {
                    val body: OrgDetailsResponse? = resp.body()
                    _orgDetails.value = body?.details ?: emptyList()
                } else {
                    _error.value = "Failed to fetch orgs: ${resp.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Call multipart update API
     *
     * Provide nullable fields for those user may not edit.
     */
    fun updateUserMultipart(
        userId: Int,
        userDateOfBirth: String? = null,
        userAddress: String? = null,
        userFirstName: String? = null,
        userLastName: String? = null,
        orgId: Int? = null,
        subRole: String? = null,
        profileImageUri: Uri? = null,
        coverImageUri: Uri? = null
    ) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val resp = repository.updateUserMultipart(
                    userId = userId,
                    userDateOfBirth = userDateOfBirth,
                    userAddress = userAddress,
                    userFirstName = userFirstName,
                    userLastName = userLastName,
                    orgId = orgId,
                    subRole = subRole,
                    profileImageUri = profileImageUri,
                    coverImageUri = coverImageUri
                )

                if (resp.isSuccessful) {
                    _updateResponse.value = resp.body()
                } else {
                    // try to parse error body or show code
                    _error.value = "Update failed: ${resp.code()}"
                    _updateResponse.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _updateResponse.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear one-time fields after consumption
     */
    fun clearUpdateResponse() {
        _updateResponse.value = null
    }


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



//    fun fetchUserPosts() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                val posts = repository.getAllUserPosts()
//                _userPosts.value = posts
//            } catch (e: Exception) {
//                _error.value = e.message
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
    //
    fun fetchUserPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val posts = repository.getAllUserPosts()
                _userPosts.value = posts
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("UserProfileViewModel", "Error fetching posts: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

}