package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DaySlot(
    val dateKey: String,
    val shortDayLabel: String,
    val fullDayLabel: String,
    val isToday: Boolean
)

object DateUtils {
    private const val KEY_PATTERN = "yyyy-MM-dd"

    fun todayDateKey(): String = dateKeyOffset(0)

    fun tomorrowDateKey(): String = dateKeyOffset(-1) // -1 daysAgo = +1 day

    fun dateKeyOffset(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat(KEY_PATTERN, Locale.getDefault()).format(cal.time)
    }

    fun formatFullTodayDate(): String {
        val cal = Calendar.getInstance()
        return SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(cal.time)
    }

    fun formatShortDate(dateKey: String): String {
        return try {
            val parser = SimpleDateFormat(KEY_PATTERN, Locale.getDefault())
            val date = parser.parse(dateKey) ?: return dateKey
            SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            dateKey
        }
    }

    fun formatTimestamp(millis: Long): String {
        val date = Date(millis)
        return SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }

    fun last7DaySlots(): List<DaySlot> {
        val keyFmt = SimpleDateFormat(KEY_PATTERN, Locale.getDefault())
        val shortFmt = SimpleDateFormat("EEE", Locale.getDefault())
        val fullFmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

        return (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val time = cal.time
            DaySlot(
                dateKey = keyFmt.format(time),
                shortDayLabel = shortFmt.format(time),
                fullDayLabel = fullFmt.format(time),
                isToday = daysAgo == 0
            )
        }
    }

    fun timeGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning, Scholar!"
            hour < 17 -> "Good afternoon, Scholar!"
            else -> "Good evening, Scholar!"
        }
    }
}
