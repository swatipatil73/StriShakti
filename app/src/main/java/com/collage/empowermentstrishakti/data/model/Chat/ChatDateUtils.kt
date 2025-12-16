package com.collage.empowermentstrishakti.data.model.Chat


import java.text.SimpleDateFormat
import java.util.*


object ChatDateUtils {

    private val apiFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private val timeFormat =
        SimpleDateFormat("hh:mm a", Locale.getDefault())

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun getTime(ts: String): String {
        return try {
            timeFormat.format(apiFormat.parse(ts)!!)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDateLabel(ts: String): String {
        return try {
            val msgDate = apiFormat.parse(ts)!!
            val calMsg = Calendar.getInstance().apply { time = msgDate }
            val calNow = Calendar.getInstance()

            when {
                isSameDay(calMsg, calNow) -> "Today"
                isYesterday(calMsg, calNow) -> "Yesterday"
                else -> dateFormat.format(msgDate)
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(msg: Calendar, now: Calendar): Boolean {
        now.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(msg, now)
    }
}

