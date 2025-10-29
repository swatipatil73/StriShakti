package com.collage.new_strishakti.data.model.regi

data class LoginResponse(
    val message: String,
    val status: String,
    val token: String,
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    val userId: Int
    // Add other fields as needed
)

