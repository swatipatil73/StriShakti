package com.collage.empowermentstrishakti.data.model.regi

data class RegisterRequest(
    val userFirstName: String,
    val userLastName: String,
    val userEmail: String,
    val userGender: String,
    val userDateOfBirth: String,
    val userAddress: String,
    val userPassword: String,
    val userMobileNumber: String,

    val stateId: Int,
    val districtId: Int,
    val talukaId: Int,

    val userRole: String,

    // ===== SWAYAMSIDHA =====
    val subRole: String? = null,
    val universityId: String? = null,
    val collegeId: String? = null,
    val schoolId: String? = null,
    val studyCentreId: String? = null,
    val departmentId: String? = null,
    val streamId: String? = null,

    // ===== ADISHAKTI (ADD THESE) =====
    val localBodyType: String? = null,
    val localBodyName: String? = null,
    val wardNo: String? = null,
    val designation: String? = null,

    val isSwayamsiddha: Boolean,
    val isAdiShakti: Boolean,
    val termsAndConditionsAccepted: Boolean,

    val userProfileImagePath: String = "",
    val userCoverProfileImagePath: String = ""
)

