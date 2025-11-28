package com.collage.empowermentstrishakti.data.model.regi

data class LoginResponse(
    val message: String,
    val status: String,
    val token: String,
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    val userId: Int,

    val userUUID: String,
    // Add other fields as needed
)

