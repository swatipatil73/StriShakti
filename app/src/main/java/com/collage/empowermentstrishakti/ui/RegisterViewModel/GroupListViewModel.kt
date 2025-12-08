package com.collage.empowermentstrishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import kotlinx.coroutines.launch
class GroupListViewModel(
    private val repository: GroupRepository
) : ViewModel() {

    private val _createGroupResult = MutableLiveData<NetworkResult<CommonResponse>>()
    val createGroupResult: LiveData<NetworkResult<CommonResponse>> = _createGroupResult

    private val _selectedMemberIds = MutableLiveData<MutableList<Int>>(mutableListOf())
    val selectedMemberIds: LiveData<MutableList<Int>> = _selectedMemberIds

    private val _groups = MutableLiveData<NetworkResult<List<GroupDetail>>>()
    val groups: LiveData<NetworkResult<List<GroupDetail>>> = _groups   // <-- expose _groups

    val deleteResult = MutableLiveData<NetworkResult<CommonResponse>>()

    fun deleteGroup(adminId: Int, groupId: Int) {
        viewModelScope.launch {
            deleteResult.value = NetworkResult. Loading
            try {
                val response = repository.deleteGroup(adminId, groupId)
                deleteResult.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                deleteResult.value = NetworkResult.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun fetchGroups(userId: Int) {
        _groups.value = NetworkResult.Loading
        viewModelScope.launch {
            val result = repository.getGroupsForUser(userId)
            _groups.postValue(result)
        }
    }
}
