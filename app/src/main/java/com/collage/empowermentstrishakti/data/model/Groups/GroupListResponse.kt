package com.collage.empowermentstrishakti.data.model.Groups



import com.google.gson.annotations.SerializedName

data class GroupListResponse(
    @SerializedName("groupDetails")
    val groupDetails: List<GroupDetail>? = emptyList()
)
