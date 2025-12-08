package com.collage.empowermentstrishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.FriendData
import com.collage.empowermentstrishakti.data.model.Groups.NetworkResult
import com.collage.empowermentstrishakti.data.model.post.CommonResponse
import com.collage.empowermentstrishakti.data.repository.GroupRepository
import kotlinx.coroutines.launch

class CreateGroupViewModel(
    private val repository: GroupRepository
) : ViewModel() {

    private val _createGroupResult = MutableLiveData<NetworkResult<CommonResponse>>()
    val createGroupResult: LiveData<NetworkResult<CommonResponse>> = _createGroupResult

    private val _selectedMemberIds = MutableLiveData<MutableList<Int>>(mutableListOf())
    val selectedMemberIds: LiveData<MutableList<Int>> = _selectedMemberIds

    // For loading friends list
    private val _friendList = MutableLiveData<NetworkResult<List<FriendData>>>()
    val friendList: LiveData<NetworkResult<List<FriendData>>> = _friendList


    fun toggleMemberSelection(userId: Int, isChecked: Boolean) {
        val list = _selectedMemberIds.value ?: mutableListOf()

        if (isChecked) {
            if (!list.contains(userId)) list.add(userId)
        } else {
            list.remove(userId)
        }

        _selectedMemberIds.value = list
    }


    fun fetchFriends(userId: Int) {
        _friendList.value = NetworkResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getFriendsList(userId)
                if (response.isSuccessful) {
                    val data = response.body()?.friendListData ?: emptyList()
                    _friendList.postValue(NetworkResult.Success(data))
                } else {
                    _friendList.postValue(
                        NetworkResult.Error("Failed: ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                _friendList.postValue(NetworkResult.Error(e.localizedMessage ?: "Error"))
            }
        }
    }


    fun createGroup(
        adminUserId: Int,
        groupName: String,
        groupDescription: String
    ) {
        _createGroupResult.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = repository.createGroup(
                adminUserId = adminUserId,
                membersIds = _selectedMemberIds.value ?: emptyList(),
                groupName = groupName,
                groupDescription = groupDescription
            )
            _createGroupResult.postValue(result)
        }
    }
}
