package com.collage.new_strishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.FriendData
import com.collage.new_strishakti.data.model.chat.GroupData
import com.collage.new_strishakti.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _friends = MutableLiveData<List<FriendData>>()
    val friends: LiveData<List<FriendData>> get() = _friends

    private val _groups = MutableLiveData<List<GroupData>>()
    val groups: LiveData<List<GroupData>> get() = _groups

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    /** -------------------- LOAD FRIEND LIST -------------------- **/
    fun loadFriends(userId: Int, token: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.getFriends(userId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _friends.value = response.body()!!.friendListData
                } else {
                    _errorMessage.value = "Failed to load friend list"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Something went wrong"
            } finally {
                _loading.value = false
            }
        }
    }


    /** -------------------- LOAD GROUP LIST -------------------- **/
    fun loadGroups(userId: Int, token: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.getGroups(userId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _groups.value = response.body()!!.groupDetails
                } else {
                    _errorMessage.value = "Failed to load group list"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Something went wrong"
            } finally {
                _loading.value = false
            }
        }
    }
}
