package com.example.hmbuddy.viewmodel

import app.cash.turbine.test
import com.example.hmbuddy.data.WeeklyAchievement
import com.example.hmbuddy.data.WeeklyTarget
import com.example.hmbuddy.data.repository.WeeklyAchievementRepository
import com.example.hmbuddy.util.DateUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyAchievementViewModelTest {

    private lateinit var repository: WeeklyAchievementRepository
    private val testDispatcher = StandardTestDispatcher()

    private val weeklyTarget = WeeklyTarget(
        id = 1,
        zone2PaceSecondsPerKm = 390,
        tempoPaceSecondsPerKm = 300,
        weeklyDurationMinutes = 180
    )

    // Fixed timestamps for testing (Monday 00:00:00)
    private val currentWeekStart = getTestWeekStart(0)
    private val previousWeekStart = getTestWeekStart(-1)
    private val twoWeeksAgoStart = getTestWeekStart(-2)

    private fun getTestWeekStart(weeksOffset: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 13, 0, 0, 0) // A Monday
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.WEEK_OF_YEAR, weeksOffset)
        return calendar.timeInMillis
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        // Mock DateUtils to return consistent timestamps
        mockkObject(DateUtils)
        every { DateUtils.getStartOfWeekTimestamp(any()) } returns currentWeekStart
        every { DateUtils.getStartOfWeekTimestamp() } returns currentWeekStart
        every { DateUtils.getStartOfPreviousWeekTimestamp(any()) } returns previousWeekStart
        every { DateUtils.getStartOfPreviousWeekTimestamp() } returns previousWeekStart

        // Default: no weekly target set
        every { repository.getWeeklyTarget() } returns flowOf(weeklyTarget)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(DateUtils)
    }

    @Test
    fun `streak is 1 when current week achieved and no past weeks exist`() = runTest {
        // Setup: No past achievements, current week has 200 minutes (above 180 target)
        every { repository.getAllAchievements() } returns flowOf(emptyList())
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns null
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(200)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(1, awaitItem()) // After current week check
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 0 when current week not achieved and no past weeks exist`() = runTest {
        // Setup: No past achievements, current week has 100 minutes (below 180 target)
        every { repository.getAllAchievements() } returns flowOf(emptyList())
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns null
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(100)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // Should remain 0 since target not met
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 2 when current week achieved and previous week was achieved`() = runTest {
        // Setup: Previous week achieved, current week has 200 minutes
        val previousWeekAchievement = WeeklyAchievement(
            weekStartTimestamp = previousWeekStart,
            targetMinutes = 180,
            actualMinutes = 190,
            goalAchieved = true
        )

        every { repository.getAllAchievements() } returns flowOf(listOf(previousWeekAchievement))
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns previousWeekAchievement
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(200)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // First emission: past streak only = 1 (before current week check completes)
            assertEquals(1, awaitItem())
            // Second emission: past streak + current week = 2
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 0 when current week achieved but previous week was not achieved with activity`() = runTest {
        // Setup: Previous week NOT achieved but had activity (100 min), current week has 200 minutes
        // This should break the streak because user was active but didn't meet goal
        val previousWeekAchievement = WeeklyAchievement(
            weekStartTimestamp = previousWeekStart,
            targetMinutes = 180,
            actualMinutes = 100, // Had activity but didn't meet goal
            goalAchieved = false
        )

        every { repository.getAllAchievements() } returns flowOf(listOf(previousWeekAchievement))
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns previousWeekAchievement
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(200)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // Streak should remain 0 because previous week broke the streak
            // (had activity but didn't meet goal)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when current week achieved and previous week had zero activity`() = runTest {
        // Setup: Previous week had 0 minutes (user wasn't active), current week has 200 minutes
        // A week with 0 minutes shouldn't break the streak - user wasn't using app
        val previousWeekAchievement = WeeklyAchievement(
            weekStartTimestamp = previousWeekStart,
            targetMinutes = 180,
            actualMinutes = 0, // No activity - shouldn't break streak
            goalAchieved = false
        )

        every { repository.getAllAchievements() } returns flowOf(listOf(previousWeekAchievement))
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns previousWeekAchievement
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(200)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // Streak should be 1 because previous week with 0 minutes doesn't break streak
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when current week not achieved but previous week was achieved`() = runTest {
        // Setup: Previous week achieved, current week has 100 minutes (not achieved)
        val previousWeekAchievement = WeeklyAchievement(
            weekStartTimestamp = previousWeekStart,
            targetMinutes = 180,
            actualMinutes = 190,
            goalAchieved = true
        )

        every { repository.getAllAchievements() } returns flowOf(listOf(previousWeekAchievement))
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns previousWeekAchievement
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(100)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // Past streak = 1, current week not achieved, total = 1
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 3 when current week achieved and two previous weeks were achieved`() = runTest {
        // Setup: Two previous weeks achieved, current week has 200 minutes
        val twoWeeksAgoAchievement = WeeklyAchievement(
            weekStartTimestamp = twoWeeksAgoStart,
            targetMinutes = 180,
            actualMinutes = 185,
            goalAchieved = true
        )
        val previousWeekAchievement = WeeklyAchievement(
            weekStartTimestamp = previousWeekStart,
            targetMinutes = 180,
            actualMinutes = 190,
            goalAchieved = true
        )

        every { repository.getAllAchievements() } returns flowOf(
            listOf(previousWeekAchievement, twoWeeksAgoAchievement)
        )
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns previousWeekAchievement
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(200)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // First emission: past streak only = 2 (before current week check completes)
            assertEquals(2, awaitItem())
            // Second emission: past streak + current week = 3
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak updates when current week reaches goal mid-week`() = runTest {
        // Setup: No past achievements, current week starts at 100 then goes to 200
        every { repository.getAllAchievements() } returns flowOf(emptyList())
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns null
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0

        // Simulate minutes flow that updates
        val minutesFlow = kotlinx.coroutines.flow.MutableStateFlow(100)
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns minutesFlow

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()

            // Still 0 because 100 < 180 target
            expectNoEvents()

            // User logs more runs, now at 200 minutes
            minutesFlow.value = 200
            testDispatcher.scheduler.advanceUntilIdle()

            // Now streak should be 1
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 0 when no weekly target is set`() = runTest {
        // Setup: No weekly target
        every { repository.getWeeklyTarget() } returns flowOf(null)
        every { repository.getAllAchievements() } returns flowOf(emptyList())
        coEvery { repository.getAchievementForWeek(any()) } returns null
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // Should remain 0 since no target is set
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `streak is 1 when current week exactly meets target`() = runTest {
        // Setup: No past achievements, current week has exactly 180 minutes (equals target)
        every { repository.getAllAchievements() } returns flowOf(emptyList())
        coEvery { repository.getAchievementForWeek(previousWeekStart) } returns null
        coEvery { repository.getTotalMinutesForWeek(any(), any()) } returns 0
        every { repository.getTotalMinutesFlowForWeek(currentWeekStart) } returns flowOf(180)

        val viewModel = WeeklyAchievementViewModel(repository)

        viewModel.currentStreak.test {
            assertEquals(0, awaitItem()) // Initial value
            testDispatcher.scheduler.advanceUntilIdle()
            // Exactly 180 should count as achieved (>= target)
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
