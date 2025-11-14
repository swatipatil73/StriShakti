package com.collage.new_strishakti.data



data class ChatMessageResponse(
    val id: Long?,
    val content: String?,
    val timestamp: String?,
    val sender: UserResponse?,
    val receiver: UserResponse?,
    val group: Any? = null, // keep generic for now; backend returns null when not group
    val files: List<ChatFile>? = null // some endpoints may include files
)

data class UserResponse(
    val userId: Int = -1,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val userEmail: String? = null,
    val userGender: String? = null,
    val userDateOfBirth: String? = null,
    val userAddress: String? = null,
    val userStatus: String? = null,
    val userpassword: String? = null,
    val userProfileImagePath: String? = null,
    val userCoverProfileImagePath: String? = null,
    val userMobileNumber: String? = null,
    val fullName: String? = null,
    val userCreatedAt: String? = null,
    val userUpdateAt: String? = null,
    val uuid: String? = null,
    val userRole: String? = null,
    val institutionName: String? = null,
    val institutionCode: String? = null,
    val institutionType: String? = null,
    val affiliation: String? = null,
    val establishedYear: String? = null,
    val description: String? = null,
    val website: String? = null,
    val deanName: String? = null,
    val alternateContactNumber: String? = null,
    val location: String? = null,
    val stream: String? = null,
    val termsAndConditionsAccepted: Boolean = false,
    val swayamsiddha: Boolean = false
)


data class ChatFile(
    val fileUrl: String,
    val fileName: String?,
    val fileType: String?
)

