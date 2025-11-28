package com.collage.empowermentstrishakti.data.model.regi

data class College(
    val userId: Int,
    val institutionName: String,
    val institutionCode: String,
    // other fields if present
)

data class School(
    val schoolId: Int,
    val schoolName: String,
    val schoolCode: String,
    val email: String?
    // other fields if present
)

data class StudyCentre(
    val studyCentreId: Int,
    val studyCentreName: String,
    val studyCentreCode: String,
    val email: String?
    // other fields if present
)

