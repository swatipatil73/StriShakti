package com.collage.empowermentstrishakti.data.model.regi

data class RegisterRequest(
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    val userGender: String,
    val userDateOfBirth: String,
    val userAddress: String,
    val userPassword: String,
    val stateId: Int,
    val districtId: Int,
    val talukaId: Int,
    val userMobileNumber: String,
    val userRole: String,
    val subRole: String? = null,

    val isSwayamsiddha: Boolean,
    val isAdiShakti: Boolean,
    val termsAndConditionsAccepted: Boolean,

    // Optional IDs as strings
    val universityId: String = "",
    val collegeId: String = "",
    val schoolId: String = "",
    val studyCentreId: String = "",
    val departmentId: String = "",
    val streamId: String = "",

    val userProfileImagePath: String = "",
    val userCoverProfileImagePath: String = ""
)


