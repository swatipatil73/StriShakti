package com.collage.empowermentstrishakti.data.model

data class RefreshResponse(
    val status: String,
    val token: String,
    val refreshToken: String
)