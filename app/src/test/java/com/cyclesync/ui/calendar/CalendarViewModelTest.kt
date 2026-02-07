package com.cyclesync.ui.calendar

import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var cycleRepository: CycleRepository
    private lateinit var predictionRepository: PredictionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        cycleRepository = mockk()
        predictionRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `empty data produces empty days after loading`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        val daysInMonth = YearMonth.now().lengthOfMonth()
        assertEquals(daysInMonth, viewModel.state.value.days.size)
    }

    @Test
    fun `cycles mark period days correctly`() = runTest {
        val periodStart = LocalDate.now().withDayOfMonth(5)
        val cycle = Cycle(
            id = "c1",
            cycleNumber = 1,
            startDate = periodStart,
            periodStartDate = periodStart,
            periodEndDate = periodStart.plusDays(4),
            periodLength = 5
        )

        every { cycleRepository.getAllCycles() } returns flowOf(listOf(cycle))
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        val periodDays = viewModel.state.value.days.filter { it.isPeriod }
        assertTrue("Should have period days", periodDays.isNotEmpty())
    }

    @Test
    fun `predictions mark fertile and ovulation days`() = runTest {
        val today = LocalDate.now()
        val predictions = listOf(
            Prediction(
                id = "p1", type = PredictionType.FERTILE_START,
                predictedDate = today.plusDays(10), confidence = 0.8,
                algorithmVersion = "1.0"
            ),
            Prediction(
                id = "p2", type = PredictionType.FERTILE_END,
                predictedDate = today.plusDays(15), confidence = 0.8,
                algorithmVersion = "1.0"
            ),
            Prediction(
                id = "p3", type = PredictionType.OVULATION,
                predictedDate = today.plusDays(12), confidence = 0.7,
                algorithmVersion = "1.0"
            )
        )

        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(predictions)

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        val fertileDays = viewModel.state.value.days.filter { it.isFertile }
        val ovulationDays = viewModel.state.value.days.filter { it.isOvulation }
        assertTrue("Should have fertile days in current month", fertileDays.isNotEmpty() || today.plusDays(10).month != today.month)
        assertTrue("Should have ovulation days in current month", ovulationDays.isNotEmpty() || today.plusDays(12).month != today.month)
    }

    @Test
    fun `today is correctly marked`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        val todayDay = viewModel.state.value.days.find { it.isToday }
        assertNotNull("Today should be marked", todayDay)
        assertEquals(LocalDate.now(), todayDay?.date)
    }

    @Test
    fun `current month is set correctly`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        assertEquals(YearMonth.now(), viewModel.state.value.currentMonth)
    }

    @Test
    fun `previousMonth decrements month`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())
        coEvery { cycleRepository.getAllCyclesOnce() } returns emptyList()
        coEvery { predictionRepository.getActivePredictionsOnce() } returns emptyList()

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        viewModel.previousMonth()
        advanceUntilIdle()

        assertEquals(YearMonth.now().minusMonths(1), viewModel.state.value.currentMonth)
    }

    @Test
    fun `nextMonth increments month`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())
        coEvery { cycleRepository.getAllCyclesOnce() } returns emptyList()
        coEvery { predictionRepository.getActivePredictionsOnce() } returns emptyList()

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        viewModel.nextMonth()
        advanceUntilIdle()

        assertEquals(YearMonth.now().plusMonths(1), viewModel.state.value.currentMonth)
    }

    @Test
    fun `predicted period days are separate from actual period days`() = runTest {
        val periodStart = LocalDate.now().withDayOfMonth(1)
        val cycle = Cycle(
            id = "c1", cycleNumber = 1,
            startDate = periodStart,
            periodStartDate = periodStart,
            periodEndDate = periodStart.plusDays(4),
            periodLength = 5
        )
        val predictions = listOf(
            Prediction(
                id = "p1", type = PredictionType.PERIOD_START,
                predictedDate = periodStart, confidence = 0.8,
                algorithmVersion = "1.0"
            ),
            Prediction(
                id = "p2", type = PredictionType.PERIOD_END,
                predictedDate = periodStart.plusDays(4), confidence = 0.8,
                algorithmVersion = "1.0"
            )
        )

        every { cycleRepository.getAllCycles() } returns flowOf(listOf(cycle))
        every { predictionRepository.getActivePredictions() } returns flowOf(predictions)

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        val predictedOnlyDays = viewModel.state.value.days.filter { it.isPredictedPeriod }
        val actualPeriodDays = viewModel.state.value.days.filter { it.isPeriod }

        assertTrue("Actual period days should exist", actualPeriodDays.isNotEmpty())
        predictedOnlyDays.forEach { day ->
            assertFalse("Predicted-only days should not overlap actual period", day.isPeriod)
        }
    }

    @Test
    fun `days count matches month length`() = runTest {
        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        val expected = YearMonth.now().lengthOfMonth()
        assertEquals(expected, viewModel.state.value.days.size)
    }

    @Test
    fun `PMS dates are populated from predictions`() = runTest {
        val today = LocalDate.now()
        val predictions = listOf(
            Prediction(
                id = "p1", type = PredictionType.PMS_START,
                predictedDate = today.plusDays(3), confidence = 0.7,
                algorithmVersion = "1.0"
            ),
            Prediction(
                id = "p2", type = PredictionType.PMS_END,
                predictedDate = today.plusDays(7), confidence = 0.7,
                algorithmVersion = "1.0"
            )
        )

        every { cycleRepository.getAllCycles() } returns flowOf(emptyList())
        every { predictionRepository.getActivePredictions() } returns flowOf(predictions)

        val viewModel = CalendarViewModel(cycleRepository, predictionRepository)
        advanceUntilIdle()

        val pmsDays = viewModel.state.value.days.filter { it.isPMS }
        val pmsInCurrentMonth = today.plusDays(3).month == today.month
        if (pmsInCurrentMonth) {
            assertTrue("Should have PMS days", pmsDays.isNotEmpty())
        }
    }
}
