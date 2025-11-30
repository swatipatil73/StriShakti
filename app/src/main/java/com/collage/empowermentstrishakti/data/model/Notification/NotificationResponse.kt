package com.collage.empowermentstrishakti.data.model.Notification



import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("notificationDetails")
    val notificationDetails: List<NotificationItem>
)
