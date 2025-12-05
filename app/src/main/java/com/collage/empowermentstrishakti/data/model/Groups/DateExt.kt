package com.collage.empowermentstrishakti.data.model.Groups



import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun String?.toPrettyDate(): String {
    if (this.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(this)
        odt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (e: Exception) {
        // fallback if parsing fails
        this.substringBefore("T")
    }
}
