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

    // Initialize with an empty (non-null) list so LiveData never holds null
    private val _notifications = MutableLiveData<List<NotificationItem>>(emptyList())
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
                // getOrNull() may return null, so fall back to emptyList()
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
                // if _notifications.value is nullable for any reason, map result can be null —
                // guard with ?: emptyList() so _notifications.value remains non-null
                val updated = _notifications.value?.map { item ->
                    item.copy(notificationStatus = "READ")
                }
                _notifications.value = updated ?: emptyList()
            }
        }

        return result
    }




    /** Convenience to refresh current list if needed. */
    fun refresh(token: String, userId: Long) = loadNotifications(token, userId)
}