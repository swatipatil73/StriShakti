package com.collage.empowermentstrishakti.data.repository

import com.collage.empowermentstrishakti.data.model.Notification.NotificationItem
import com.collage.empowermentstrishakti.data.network.ApiService
import retrofit2.HttpException

class NotificationRepository(
    private val api: ApiService
) {

    /**
     * Fetch notifications from API.
     * Returns Kotlin Result containing a list of NotificationItem on success,
     * or a failure with an Exception on error.
     */
    suspend fun fetchNotifications(token: String, userId: Long): Result<List<NotificationItem>> {
        return try {
            val response = api.getAllNotifications(token, userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.notificationDetails)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // wrap retrofit error
                Result.failure(HttpException(response))
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun markAllAsRead(token: String, receiverId: Long): Result<Unit> {
        return try {
            val response = api.markAllNotificationsAsRead(token, receiverId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}