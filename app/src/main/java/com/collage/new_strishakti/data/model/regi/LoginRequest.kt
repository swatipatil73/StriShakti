package com.collage.new_strishakti.data.model.regi

data class LoginRequest(
    val userEmailOrMobileNumber: String,
    val userPassword: String
)
