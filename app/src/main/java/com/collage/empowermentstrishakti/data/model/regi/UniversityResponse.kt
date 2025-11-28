package com.collage.empowermentstrishakti.data.model.regi

data class UniversityResponse(
    val universities: List<University>,
    val totalCount: Int
)

data class University(
    val universityId: Int,
    val institutionName: String
)

