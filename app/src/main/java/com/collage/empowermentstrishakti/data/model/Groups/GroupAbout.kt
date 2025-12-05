package com.collage.empowermentstrishakti.data.model.Groups

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupAbout(
    val groupId: Int,
    val groupName: String,
    val groupDescription: String?,
    val groupCoverProfileImagePath: String?,
    val adminId: Int,
    val adminUserFirstName: String?,
    val adminUserLastName: String?,
    val adminUserProfileImagePath: String?,
    val groupCreatedAt: String?,
    val groupUUID: String?
) : Parcelable
