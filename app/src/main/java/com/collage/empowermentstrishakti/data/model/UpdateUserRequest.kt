package com.collage.empowermentstrishakti.data.model



data class UpdateUserRequest(
    val isSwayamsiddha: Boolean?,
    val isAdiShakti: Boolean?,

    // ---------- Swayamsiddha ----------
    val subRole: String?,
    val universityId: String?,
    val collegeId: String?,
    val schoolId: String?,
    val studyCentreId: String?,
    val departmentId: String?,
    val streamId: String?,

    // ---------- AdiShakti ----------
    val localBodyType: String?,
    val localBodyName: String?,
    val wardNo: String?
)
