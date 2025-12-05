package com.collage.empowermentstrishakti.data.model.Groups

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class GroupMember(
    val userProfileImagePath: String?,
    val userFirstName: String?,
    val userlastName: String?,
    val userUUID: String?,
    val userId: Int
): Parcelable


