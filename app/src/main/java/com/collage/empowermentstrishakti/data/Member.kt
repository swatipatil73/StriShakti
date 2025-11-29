package com.collage.empowermentstrishakti.data


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Member(
    val userId: Int = 0,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val profileImagePath: String? = null,
    val followingSince: String? = null
) : Parcelable