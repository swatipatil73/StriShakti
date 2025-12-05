package com.collage.empowermentstrishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetailsResponse
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupViewModel(private val repository: GroupRepository) : ViewModel() {

    private val _groupDetails = MutableLiveData<GroupDetailsResponse>()
    val groupDetails: LiveData<GroupDetailsResponse> get() = _groupDetails

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    fun loadGroupDetails(
        userId: Int,
        groupUUID: String,
        page: Int = 0,
        size: Int = 5,
        token: String
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true

                val response = repository.fetchGroupDetails(
                    userId, groupUUID, page, size, token
                )

                if (response.isSuccessful && response.body() != null) {
                    _groupDetails.value = response.body()
                } else {
                    _error.value = "Failed: ${response.message()}"
                }

            } catch (e: Exception) {
                _error.value = "Exception: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
