package com.collage.new_strishakti.data.model.regi

data class State(
    val stateId: Int,
    val stateName: String
) {
    val customStateId: Int
        get() = stateId  // Custom getter for stateId (you can add any logic here)

    val customStateName: String
        get() = stateName.uppercase()  // Example: return stateName in uppercase
}

data class District(
    val districtId: Int,
    val districtName: String
) {
    val customDistrictId: Int
        get() = districtId  // Custom getter

    val customDistrictName: String
        get() = districtName.lowercase()  // Example: return districtName in lowercase
}

data class Taluka(
    val talukaId: Int,
    val talukaName: String
) {
    val customTalukaId: Int
        get() = talukaId  // Custom getter

    val customTalukaName: String
        get() = "Taluka: $talukaName"  // Example: prefix talukaName with "Taluka: "
}
