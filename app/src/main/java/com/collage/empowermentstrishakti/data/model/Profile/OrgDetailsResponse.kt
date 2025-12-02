package com.collage.empowermentstrishakti.data.model.Profile

import com.google.gson.annotations.SerializedName


data class OrgDetailsResponse(
    @SerializedName("details")
    val details: List<OrgDetail> = emptyList()
)

data class OrgDetail(
    @SerializedName("orgId")
    val orgId: Int = 0,
    @SerializedName("orgName")
    val orgName: String = ""
)