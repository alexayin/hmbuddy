package com.example.hmbuddy.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RaceGoalTest {

    @Test
    fun `RaceGoal creation with all fields`() {
        val raceGoal = RaceGoal(
            id = 1,
            raceName = "Vancouver Half Marathon",
            raceDate = LocalDate.of(2026, 2, 20),
            targetTimeSeconds = 6300 // 1:45:00
        )

        assertEquals(1, raceGoal.id)
        assertEquals("Vancouver Half Marathon", raceGoal.raceName)
        assertEquals(LocalDate.of(2026, 2, 20), raceGoal.raceDate)
        assertEquals(6300, raceGoal.targetTimeSeconds)
    }

    @Test
    fun `RaceGoal creation with optional target time as null`() {
        val raceGoal = RaceGoal(
            id = 1,
            raceName = "Local 10K",
            raceDate = LocalDate.of(2026, 3, 15),
            targetTimeSeconds = null
        )

        assertEquals("Local 10K", raceGoal.raceName)
        assertNull(raceGoal.targetTimeSeconds)
    }

    @Test
    fun `RaceGoal default id is 1`() {
        val raceGoal = RaceGoal(
            raceName = "Test Race",
            raceDate = LocalDate.of(2026, 5, 1)
        )

        assertEquals(1, raceGoal.id)
    }

    @Test
    fun `RaceGoal default targetTimeSeconds is null`() {
        val raceGoal = RaceGoal(
            raceName = "Test Race",
            raceDate = LocalDate.of(2026, 5, 1)
        )

        assertNull(raceGoal.targetTimeSeconds)
    }

    @Test
    fun `RaceGoal copy updates specified fields`() {
        val original = RaceGoal(
            id = 1,
            raceName = "Original Race",
            raceDate = LocalDate.of(2026, 2, 20),
            targetTimeSeconds = 6300
        )

        val copied = original.copy(raceName = "Updated Race")

        assertEquals("Updated Race", copied.raceName)
        assertEquals(original.raceDate, copied.raceDate)
        assertEquals(original.targetTimeSeconds, copied.targetTimeSeconds)
    }

    @Test
    fun `RaceGoal equality based on all fields`() {
        val goal1 = RaceGoal(
            id = 1,
            raceName = "Vancouver Half Marathon",
            raceDate = LocalDate.of(2026, 2, 20),
            targetTimeSeconds = 6300
        )

        val goal2 = RaceGoal(
            id = 1,
            raceName = "Vancouver Half Marathon",
            raceDate = LocalDate.of(2026, 2, 20),
            targetTimeSeconds = 6300
        )

        assertEquals(goal1, goal2)
    }

    @Test
    fun `RaceGoal inequality when fields differ`() {
        val goal1 = RaceGoal(
            id = 1,
            raceName = "Vancouver Half Marathon",
            raceDate = LocalDate.of(2026, 2, 20),
            targetTimeSeconds = 6300
        )

        val goal2 = RaceGoal(
            id = 1,
            raceName = "Toronto Half Marathon",
            raceDate = LocalDate.of(2026, 2, 20),
            targetTimeSeconds = 6300
        )

        assertNotEquals(goal1, goal2)
    }

    // Target time conversion tests

    @Test
    fun `targetTimeSeconds 6300 represents 1 hour 45 minutes`() {
        val targetSeconds = 6300
        val hours = targetSeconds / 3600
        val minutes = (targetSeconds % 3600) / 60
        val seconds = targetSeconds % 60

        assertEquals(1, hours)
        assertEquals(45, minutes)
        assertEquals(0, seconds)
    }

    @Test
    fun `targetTimeSeconds 5400 represents 1 hour 30 minutes`() {
        val targetSeconds = 5400
        val hours = targetSeconds / 3600
        val minutes = (targetSeconds % 3600) / 60

        assertEquals(1, hours)
        assertEquals(30, minutes)
    }

    @Test
    fun `targetTimeSeconds 7200 represents 2 hours`() {
        val targetSeconds = 7200
        val hours = targetSeconds / 3600
        val minutes = (targetSeconds % 3600) / 60

        assertEquals(2, hours)
        assertEquals(0, minutes)
    }

    @Test
    fun `targetTimeSeconds calculation from hours minutes seconds`() {
        val hours = 1
        val minutes = 45
        val seconds = 30
        val expectedTotal = hours * 3600 + minutes * 60 + seconds

        assertEquals(6330, expectedTotal)
    }
}