package com.collage.new_strishakti.ui.RegisterViewModel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.new_strishakti.data.model.friend.FriendRequestItem
import com.collage.new_strishakti.data.repository.FriendRepository
import kotlinx.coroutines.launch

class FriendRequestsViewModel(private val repo: FriendRepository) : ViewModel() {

    sealed class UI {
        object Loading : UI()
        data class Success(val list: List<FriendRequestItem>) : UI()
        object Empty : UI()
        data class Error(val message: String) : UI()
    }

    private val _state = MutableLiveData<UI>()
    val state: LiveData<UI> = _state

    fun fetchFriendRequests(receiverId: Int) {
        _state.value = UI.Loading
        viewModelScope.launch {
            try {
                val res = repo.getFriendRequests(receiverId)
                if (res.isSuccessful) {
                    val list = res.body()?.friendRequestData.orEmpty()
                    _state.value = if (list.isEmpty()) UI.Empty else UI.Success(list)
                } else {
                    _state.value = UI.Error("Server ${res.code()}")
                }
            } catch (e: Exception) {
                _state.value = UI.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }


}


