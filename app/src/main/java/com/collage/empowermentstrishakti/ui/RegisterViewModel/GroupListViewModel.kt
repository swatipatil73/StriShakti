package com.collage.empowermentstrishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.Groups.GroupDetail
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import kotlinx.coroutines.launch

class GroupListViewModel(
    private val repository: GroupRepository
) : ViewModel() {

    private val _groups = MutableLiveData<NetworkResult<List<GroupDetail>>>()
    val groups: LiveData<NetworkResult<List<GroupDetail>>> = _groups

    fun fetchGroups(userId: Int) {
        _groups.value = NetworkResult.Loading
        viewModelScope.launch {
            val result = repository.getGroupsForUser(userId)
            _groups.postValue(result)
        }
    }
}