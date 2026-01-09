package com.example.hmbuddy.viewmodel

import app.cash.turbine.test
import com.example.hmbuddy.data.RunLog
import com.example.hmbuddy.data.RunType
import com.example.hmbuddy.data.repository.RunLogRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunViewModelTest {

    private lateinit var viewModel: RunViewModel
    private lateinit var repository: RunLogRepository
    private val testDispatcher = StandardTestDispatcher()

    private val sampleRuns = listOf(
        RunLog(id = 1, date = System.currentTimeMillis(), durationMinutes = 30, runType = RunType.ZONE2, paceSecondsPerKm = 360),
        RunLog(id = 2, date = System.currentTimeMillis(), durationMinutes = 45, runType = RunType.TEMPO, paceSecondsPerKm = 300)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)

        // Setup default mock behaviors
        every { repository.getAllRuns() } returns flowOf(sampleRuns)
        every { repository.getRunsThisWeek(any()) } returns flowOf(sampleRuns)

        viewModel = RunViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allRuns emits runs from repository`() = runTest {
        viewModel.allRuns.test {
            // Skip initial empty list
            val initialItem = awaitItem()
            assertTrue(initialItem.isEmpty())

            // Advance to get the actual items
            testDispatcher.scheduler.advanceUntilIdle()

            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals(RunType.ZONE2, items[0].runType)
            assertEquals(RunType.TEMPO, items[1].runType)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logRun calls repository insertRun with correct parameters`() = runTest {
        val durationMinutes = 45
        val runType = RunType.ZONE2
        val paceSecondsPerKm = 360
        val date = System.currentTimeMillis()

        viewModel.logRun(durationMinutes, runType, paceSecondsPerKm, date)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertRun(match { run ->
                run.durationMinutes == durationMinutes &&
                run.runType == runType &&
                run.paceSecondsPerKm == paceSecondsPerKm &&
                run.date == date
            })
        }
    }

    @Test
    fun `deleteRun calls repository deleteRun`() = runTest {
        val runToDelete = sampleRuns[0]

        viewModel.deleteRun(runToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.deleteRun(runToDelete) }
    }

    @Test
    fun `updateRun calls repository updateRun`() = runTest {
        val updatedRun = sampleRuns[0].copy(durationMinutes = 60)

        viewModel.updateRun(updatedRun)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateRun(updatedRun) }
    }

    @Test
    fun `runsThisWeek emits runs from repository for current week`() = runTest {
        // Advance scheduler to allow init block coroutine to run
        testDispatcher.scheduler.advanceUntilIdle()

        // After advancing, the runs should be loaded
        val items = viewModel.runsThisWeek.value
        assertEquals(2, items.size)
    }

    @Test
    fun `allRuns initially emits empty list`() = runTest {
        val emptyRepository = mockk<RunLogRepository>(relaxed = true)
        every { emptyRepository.getAllRuns() } returns flowOf(emptyList())
        every { emptyRepository.getRunsThisWeek(any()) } returns flowOf(emptyList())

        val emptyViewModel = RunViewModel(emptyRepository)

        emptyViewModel.allRuns.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logRun creates RunLog with auto-generated id of 0`() = runTest {
        viewModel.logRun(30, RunType.ZONE2, 360, System.currentTimeMillis())
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.insertRun(match { run ->
                run.id == 0L // Auto-generated IDs start at 0 before Room assigns them
            })
        }
    }
}
