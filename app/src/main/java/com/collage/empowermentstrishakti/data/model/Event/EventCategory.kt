package com.collage.empowermentstrishakti.data.model.Event

// EventCategory.kt
data class EventCategory(
    val catId: Int,
    val catName: String,
    val catCreatedAt: String?,
    val catUpdatedAt: String?
) {
    override fun toString(): String {
        // Spinner will show this by default when using ArrayAdapter
        return catName
    }
}

// EventCategoryResponse.kt
data class EventCategoryResponse(
    val eventCatgDetails: List<EventCategory>?
)
