package com.example.hmbuddy.util

import java.util.Calendar

object DateUtils {
    /**
     * Returns the timestamp for the start of the current week (Monday at 00:00:00.000).
     * Week always starts on Monday regardless of locale.
     */
    fun getStartOfWeekTimestamp(referenceTime: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = referenceTime

        // Get current day of week (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate days to subtract to get to Monday
        // Sunday (1) -> subtract 6 days
        // Monday (2) -> subtract 0 days
        // Tuesday (3) -> subtract 1 day, etc.
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) {
            6
        } else {
            currentDayOfWeek - Calendar.MONDAY
        }

        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    /**
     * Returns the timestamp for the start of the previous week (Monday at 00:00:00.000).
     */
    fun getStartOfPreviousWeekTimestamp(referenceTime: Long = System.currentTimeMillis()): Long {
        val currentWeekStart = getStartOfWeekTimestamp(referenceTime)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentWeekStart
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
        return calendar.timeInMillis
    }
}
