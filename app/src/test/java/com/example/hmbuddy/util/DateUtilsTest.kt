package com.example.hmbuddy.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateUtilsTest {

    private fun createCalendar(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12,
        minute: Int = 0
    ): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(year, month, day, hour, minute, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getCalendarFromTimestamp(timestamp: Long): Calendar {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = timestamp
        return calendar
    }

    @Test
    fun `getStartOfWeekTimestamp returns Monday for a Wednesday`() {
        // Wednesday, January 8, 2025
        val wednesday = createCalendar(2025, Calendar.JANUARY, 8)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(wednesday)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getStartOfWeekTimestamp returns same Monday for a Monday`() {
        // Monday, January 6, 2025
        val monday = createCalendar(2025, Calendar.JANUARY, 6)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(monday)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfWeekTimestamp returns previous Monday for a Sunday`() {
        // Sunday, January 12, 2025
        val sunday = createCalendar(2025, Calendar.JANUARY, 12)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(sunday)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        // Should be Monday January 6, not January 13
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfWeekTimestamp returns Monday for a Saturday`() {
        // Saturday, January 11, 2025
        val saturday = createCalendar(2025, Calendar.JANUARY, 11)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(saturday)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfWeekTimestamp handles month boundary correctly`() {
        // Tuesday, February 4, 2025 - week starts in January
        val tuesday = createCalendar(2025, Calendar.FEBRUARY, 4)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(tuesday)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH))
        assertEquals(3, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfWeekTimestamp handles year boundary correctly`() {
        // Wednesday, January 1, 2025 - week starts in December 2024
        val newYearDay = createCalendar(2025, Calendar.JANUARY, 1)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(newYearDay)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(30, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfPreviousWeekTimestamp returns correct previous Monday`() {
        // Wednesday, January 15, 2025
        val wednesday = createCalendar(2025, Calendar.JANUARY, 15)
        val previousWeekStart = DateUtils.getStartOfPreviousWeekTimestamp(wednesday)

        val calendar = getCalendarFromTimestamp(previousWeekStart)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        // Current week starts Jan 13, previous week starts Jan 6
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfPreviousWeekTimestamp handles month boundary`() {
        // Monday, February 3, 2025
        val monday = createCalendar(2025, Calendar.FEBRUARY, 3)
        val previousWeekStart = DateUtils.getStartOfPreviousWeekTimestamp(monday)

        val calendar = getCalendarFromTimestamp(previousWeekStart)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(27, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfPreviousWeekTimestamp handles year boundary`() {
        // Monday, January 6, 2025
        val monday = createCalendar(2025, Calendar.JANUARY, 6)
        val previousWeekStart = DateUtils.getStartOfPreviousWeekTimestamp(monday)

        val calendar = getCalendarFromTimestamp(previousWeekStart)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(2024, calendar.get(Calendar.YEAR))
        assertEquals(30, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `getStartOfWeekTimestamp resets time to midnight`() {
        // Wednesday at 3:45 PM
        val afternoon = createCalendar(2025, Calendar.JANUARY, 8, 15, 45)
        val startOfWeek = DateUtils.getStartOfWeekTimestamp(afternoon)

        val calendar = getCalendarFromTimestamp(startOfWeek)
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }
}
