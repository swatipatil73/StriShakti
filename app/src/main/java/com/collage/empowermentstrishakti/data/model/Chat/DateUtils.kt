package com.collage.empowermentstrishakti.data.model.Chat

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtils {

    private val inputFormat =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

    private val timeFormat =
        DateTimeFormatter.ofPattern("hh:mm a")

    private val dateHeaderFormat =
        DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun parseTime(timestamp: String): String {
        val dateTime = LocalDateTime.parse(timestamp, inputFormat)
        return dateTime.format(timeFormat)
    }

    fun getDateHeader(timestamp: String): String {
        val date = LocalDateTime.parse(timestamp, inputFormat).toLocalDate()
        val today = LocalDate.now()

        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            else -> date.format(dateHeaderFormat)
        }
    }
}
