package com.example.hmbuddy.viewmodel

import app.cash.turbine.test
import com.example.hmbuddy.data.RaceGoal
import com.example.hmbuddy.data.WeeklyTarget
import com.example.hmbuddy.data.repository.RaceGoalRepository
import com.example.hmbuddy.data.repository.WeeklyTargetRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TargetViewModelTest {

    private lateinit var viewModel: TargetViewModel
    private lateinit var weeklyTargetRepository: WeeklyTargetRepository
    private lateinit var raceGoalRepository: RaceGoalRepository
    private val testDispatcher = StandardTestDispatcher()

    private val sampleWeeklyTarget = WeeklyTarget(
        id = 1,
        zone2PaceSecondsPerKm = 390,
        tempoPaceSecondsPerKm = 300,
        weeklyDurationMinutes = 180
    )

    private val sampleRaceGoal = RaceGoal(
        id = 1,
        raceName = "Vancouver Half Marathon",
        raceDate = LocalDate.of(2026, 2, 20),
        targetTimeSeconds = 6300
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        weeklyTargetRepository = mockk(relaxed = true)
        raceGoalRepository = mockk(relaxed = true)

        every { weeklyTargetRepository.getWeeklyTarget() } returns flowOf(sampleWeeklyTarget)
        every { raceGoalRepository.getRaceGoal() } returns flowOf(sampleRaceGoal)

        viewModel = TargetViewModel(weeklyTargetRepository, raceGoalRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Weekly Target Tests

    @Test
    fun `weeklyTarget emits target from repository`() = runTest {
        viewModel.weeklyTarget.test {
            val initialItem = awaitItem()
            assertNull(initialItem)

            testDispatcher.scheduler.advanceUntilIdle()

            val item = awaitItem()
            assertEquals(sampleWeeklyTarget, item)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveWeeklyTarget calls repository with correct parameters`() = runTest {
        val zone2Pace = 400
        val tempoPace = 310
        val duration = 200

        viewModel.saveWeeklyTarget(zone2Pace, tempoPace, duration)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            weeklyTargetRepository.saveWeeklyTarget(match { target ->
                target.zone2PaceSecondsPerKm == zone2Pace &&
                target.tempoPaceSecondsPerKm == tempoPace &&
                target.weeklyDurationMinutes == duration
            })
        }
    }

    // Race Goal Tests

    @Test
    fun `raceGoal emits goal from repository`() = runTest {
        viewModel.raceGoal.test {
            val initialItem = awaitItem()
            assertNull(initialItem)

            testDispatcher.scheduler.advanceUntilIdle()

            val item = awaitItem()
            assertEquals(sampleRaceGoal, item)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveRaceGoal calls repository with correct parameters`() = runTest {
        val raceName = "Toronto Waterfront Marathon"
        val raceDate = LocalDate.of(2026, 10, 18)
        val targetTimeSeconds = 14400 // 4 hours

        viewModel.saveRaceGoal(raceName, raceDate, targetTimeSeconds)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            raceGoalRepository.saveRaceGoal(match { goal ->
                goal.raceName == raceName &&
                goal.raceDate == raceDate &&
                goal.targetTimeSeconds == targetTimeSeconds
            })
        }
    }

    @Test
    fun `saveRaceGoal with null target time calls repository correctly`() = runTest {
        val raceName = "Local 5K Fun Run"
        val raceDate = LocalDate.of(2026, 5, 1)

        viewModel.saveRaceGoal(raceName, raceDate, null)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            raceGoalRepository.saveRaceGoal(match { goal ->
                goal.raceName == raceName &&
                goal.raceDate == raceDate &&
                goal.targetTimeSeconds == null
            })
        }
    }

    @Test
    fun `clearRaceGoal calls repository deleteRaceGoal`() = runTest {
        viewModel.clearRaceGoal()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { raceGoalRepository.deleteRaceGoal() }
    }

    @Test
    fun `raceGoal initially emits null`() = runTest {
        val emptyRaceGoalRepository = mockk<RaceGoalRepository>(relaxed = true)
        every { emptyRaceGoalRepository.getRaceGoal() } returns flowOf(null)

        val emptyViewModel = TargetViewModel(weeklyTargetRepository, emptyRaceGoalRepository)

        emptyViewModel.raceGoal.test {
            val item = awaitItem()
            assertNull(item)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `weeklyTarget initially emits null`() = runTest {
        val emptyWeeklyTargetRepository = mockk<WeeklyTargetRepository>(relaxed = true)
        every { emptyWeeklyTargetRepository.getWeeklyTarget() } returns flowOf(null)

        val emptyViewModel = TargetViewModel(emptyWeeklyTargetRepository, raceGoalRepository)

        emptyViewModel.weeklyTarget.test {
            val item = awaitItem()
            assertNull(item)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveRaceGoal creates RaceGoal with id 1`() = runTest {
        viewModel.saveRaceGoal("Test Race", LocalDate.of(2026, 6, 1), 3600)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            raceGoalRepository.saveRaceGoal(match { goal ->
                goal.id == 1
            })
        }
    }
}
