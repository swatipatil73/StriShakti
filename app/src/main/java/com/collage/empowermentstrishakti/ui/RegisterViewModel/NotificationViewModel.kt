package com.collage.empowermentstrishakti.ui.RegisterViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collage.empowermentstrishakti.data.model.Notification.NotificationItem
import com.collage.empowermentstrishakti.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Load notifications — token must include "Bearer " prefix.
     */
    fun loadNotifications(token: String, userId: Long) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = repository.fetchNotifications(token, userId)
            if (result.isSuccess) {
                _notifications.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            _isLoading.value = false
        }
    }

    fun markAllRead(token: String, receiverId: Long): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()

        viewModelScope.launch {
            val res = repository.markAllAsRead(token, receiverId)
            result.value = res

            if (res.isSuccess) {
                // update UI locally
                val updated = _notifications.value?.map { item ->
                    item.copy(notificationStatus = "READ")
                }
                _notifications.value = updated
            }
        }

        return result
    }


    /** Convenience to refresh current list if needed. */
    fun refresh(token: String, userId: Long) = loadNotifications(token, userId)
}