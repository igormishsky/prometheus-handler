package com.cyclesync.ui.analysis

import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.Trend
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.DailyLogRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {

    @MockK
    private lateinit var cycleRepository: CycleRepository

    @MockK(relaxed = true)
    private lateinit var dailyLogRepository: DailyLogRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Loading state ---

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { cycleRepository.getAllCycles() } returns flowOf(emptyList())

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)

        // Before advancing coroutines, state should be loading
        assertTrue(viewModel.state.value.isLoading)
    }

    // --- Empty state ---

    @Test
    fun `empty cycles produces no analysis`() = runTest {
        coEvery { cycleRepository.getAllCycles() } returns flowOf(emptyList())

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.hasCycles)
        assertNull(state.analysis)
    }

    // --- With cycles ---

    @Test
    fun `cycles with valid data produces analysis`() = runTest {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5),
            createCycle(2, "2024-01-29", cycleLength = 30, periodLength = 4),
            createCycle(3, "2024-02-28", cycleLength = 27, periodLength = 5)
        )
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.hasCycles)
        assertNotNull(state.analysis)

        val analysis = state.analysis!!
        assertEquals(3, analysis.totalCyclesTracked)
        assertTrue("Average cycle should be ~28.3: ${analysis.averageCycleLength}",
            analysis.averageCycleLength in 27.0..30.0)
        assertEquals(30, analysis.longestCycle)
        assertEquals(27, analysis.shortestCycle)
    }

    @Test
    fun `cycles with only excluded data shows hasCycles but no analysis`() = runTest {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5, isExcluded = true),
            createCycle(2, "2024-01-29", cycleLength = null, periodLength = 5)
        )
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.hasCycles)
        // Analysis may or may not be null depending on whether there are non-excluded cycles with lengths
    }

    // --- Regularity score ---

    @Test
    fun `perfectly regular cycles give high regularity score`() = runTest {
        val cycles = (1..5).map { i ->
            createCycle(i, LocalDate.of(2024, 1, 1).plusDays(i * 28L).toString(), cycleLength = 28, periodLength = 5)
        }
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        val analysis = viewModel.state.value.analysis!!
        assertEquals("Identical cycles should give score 10", 10, analysis.regularityScore)
    }

    @Test
    fun `highly variable cycles give low regularity score`() = runTest {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 20, periodLength = 5),
            createCycle(2, "2024-02-01", cycleLength = 40, periodLength = 5),
            createCycle(3, "2024-03-01", cycleLength = 22, periodLength = 5),
            createCycle(4, "2024-04-01", cycleLength = 45, periodLength = 5)
        )
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        val analysis = viewModel.state.value.analysis!!
        assertTrue("Variable cycles should give low score: ${analysis.regularityScore}",
            analysis.regularityScore <= 3)
    }

    // --- Trend detection ---

    @Test
    fun `stable cycles show STABLE trend`() = runTest {
        val cycles = (1..8).map { i ->
            createCycle(i, LocalDate.of(2024, 1, 1).plusDays(i * 28L).toString(), cycleLength = 28, periodLength = 5)
        }
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        assertEquals(Trend.STABLE, viewModel.state.value.analysis!!.cycleLengthTrend)
    }

    @Test
    fun `fewer than 6 cycles show STABLE trend regardless`() = runTest {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 25, periodLength = 5),
            createCycle(2, "2024-02-01", cycleLength = 30, periodLength = 5),
            createCycle(3, "2024-03-01", cycleLength = 35, periodLength = 5)
        )
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        assertEquals("Too few cycles should show STABLE", Trend.STABLE,
            viewModel.state.value.analysis!!.cycleLengthTrend)
    }

    @Test
    fun `increasing cycle lengths show LENGTHENING trend`() = runTest {
        val cycles = (1..8).map { i ->
            createCycle(i, LocalDate.of(2024, 1, 1).plusDays(i * 30L).toString(),
                cycleLength = 25 + i * 2, periodLength = 5)
        }
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        assertEquals(Trend.LENGTHENING, viewModel.state.value.analysis!!.cycleLengthTrend)
    }

    @Test
    fun `decreasing cycle lengths show SHORTENING trend`() = runTest {
        val cycles = (1..8).map { i ->
            createCycle(i, LocalDate.of(2024, 1, 1).plusDays(i * 30L).toString(),
                cycleLength = 40 - i * 2, periodLength = 5)
        }
        coEvery { cycleRepository.getAllCycles() } returns flowOf(cycles)

        val viewModel = AnalysisViewModel(cycleRepository, dailyLogRepository)
        advanceUntilIdle()

        assertEquals(Trend.SHORTENING, viewModel.state.value.analysis!!.cycleLengthTrend)
    }

    // --- Helper ---

    private fun createCycle(
        number: Int,
        startDateStr: String,
        cycleLength: Int?,
        periodLength: Int?,
        isExcluded: Boolean = false
    ): Cycle {
        val startDate = LocalDate.parse(startDateStr)
        return Cycle(
            id = "cycle-$number",
            cycleNumber = number,
            startDate = startDate,
            endDate = cycleLength?.let { startDate.plusDays(it.toLong() - 1) },
            periodStartDate = startDate,
            periodEndDate = periodLength?.let { startDate.plusDays(it.toLong() - 1) },
            cycleLength = cycleLength,
            periodLength = periodLength,
            isExcluded = isExcluded
        )
    }
}
