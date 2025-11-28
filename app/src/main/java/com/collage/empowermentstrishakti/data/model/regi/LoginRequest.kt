package com.collage.empowermentstrishakti.data.model.regi

data class LoginRequest(
    val userEmailOrMobileNumber: String,
    val userPassword: String
)
