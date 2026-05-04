package com.collage.empowermentstrishakti.data.model.regi

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String,
    val status: String,
    val token: String,
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    val tokenExpirationDate: String,
    val userId: Int,

    val userUUID: String,
    val refreshToken: String,

    @SerializedName("isSwyamsiddha") val isSwyamsiddha: Boolean = false,
    @SerializedName("isAdiShakti") val isAdiShakti: Boolean = false


    // Add other fields as needed
)

