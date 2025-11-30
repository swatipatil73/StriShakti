package com.collage.empowermentstrishakti.data.model.Notification


import com.google.gson.annotations.SerializedName

data class NotificationItem(
    @SerializedName("notificationId")
    val notificationId: Long,

    @SerializedName("notificationMessage")
    val notificationMessage: String?,

    @SerializedName("notificationType")
    val notificationType: String?,

    @SerializedName("notificationStatus")
    val notificationStatus: String?,

    @SerializedName("notificationCreatedAt")
    val notificationCreatedAt: String?,

    @SerializedName("notificationUpdatedAt")
    val notificationUpdatedAt: String?
)

