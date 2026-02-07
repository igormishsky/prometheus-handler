package com.cyclesync.ui.tracking

import androidx.lifecycle.SavedStateHandle
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.DailyLog
import com.cyclesync.domain.entity.SkipReason
import com.cyclesync.domain.entity.TrackingCategory
import com.cyclesync.domain.entity.TrackingEntry
import com.cyclesync.domain.prediction.PredictionEngine
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.DailyLogRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class TrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dailyLogRepository: DailyLogRepository
    private lateinit var cycleRepository: CycleRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var predictionRepository: PredictionRepository
    private lateinit var predictionEngine: PredictionEngine
    private val today = LocalDate.now()
    private val todayStr = DateUtils.toIsoString(today)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dailyLogRepository = mockk(relaxed = true)
        cycleRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        predictionRepository = mockk(relaxed = true)
        predictionEngine = PredictionEngine()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(date: String = todayStr): TrackingViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("date" to date))
        coEvery { dailyLogRepository.getByDate(any()) } returns null
        coEvery { cycleRepository.getCurrentCycle() } returns null
        return TrackingViewModel(
            savedStateHandle, dailyLogRepository, cycleRepository,
            settingsRepository, predictionRepository, predictionEngine
        )
    }

    @Test
    fun `initial state is loading`() = runTest {
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `loads with correct date`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(today, viewModel.state.value.date)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loads existing log entries`() = runTest {
        val existingLog = DailyLog(
            id = "log1", date = today,
            entries = listOf(
                TrackingEntry(id = "e1", dailyLogId = "log1", category = TrackingCategory.PAIN, subcategory = "cramps"),
                TrackingEntry(id = "e2", dailyLogId = "log1", category = TrackingCategory.MOOD, subcategory = "happy")
            ),
            notes = "Test notes"
        )
        coEvery { dailyLogRepository.getByDate(today) } returns existingLog
        coEvery { cycleRepository.getCurrentCycle() } returns null

        val savedStateHandle = SavedStateHandle(mapOf("date" to todayStr))
        val viewModel = TrackingViewModel(
            savedStateHandle, dailyLogRepository, cycleRepository,
            settingsRepository, predictionRepository, predictionEngine
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.entries.size)
        assertEquals("Test notes", viewModel.state.value.notes)
    }

    @Test
    fun `togglePeriod sets period day and default flow`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.togglePeriod(true)
        assertTrue(viewModel.state.value.isPeriodDay)
        assertEquals("medium", viewModel.state.value.flowIntensity)
    }

    @Test
    fun `togglePeriod off clears period day`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.togglePeriod(true)
        viewModel.togglePeriod(false)
        assertFalse(viewModel.state.value.isPeriodDay)
    }

    @Test
    fun `setFlowIntensity updates flow`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFlowIntensity("heavy")
        assertEquals("heavy", viewModel.state.value.flowIntensity)
    }

    @Test
    fun `toggleEntry adds and removes entries`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleEntry(TrackingCategory.PAIN, "cramps")
        assertTrue(viewModel.state.value.entries.containsKey("PAIN_cramps"))

        viewModel.toggleEntry(TrackingCategory.PAIN, "cramps")
        assertFalse(viewModel.state.value.entries.containsKey("PAIN_cramps"))
    }

    @Test
    fun `setNotes updates notes`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setNotes("My notes")
        assertEquals("My notes", viewModel.state.value.notes)
    }

    @Test
    fun `toggleIrregular sets and clears skip reason`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleIrregular(true)
        viewModel.setSkipReason(SkipReason.STRESS)
        assertTrue(viewModel.state.value.isIrregular)
        assertEquals(SkipReason.STRESS, viewModel.state.value.skipReason)

        viewModel.toggleIrregular(false)
        assertFalse(viewModel.state.value.isIrregular)
        assertNull(viewModel.state.value.skipReason)
    }

    @Test
    fun `save creates daily log`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleEntry(TrackingCategory.MOOD, "happy")
        viewModel.save()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSaved)
        coVerify { dailyLogRepository.insertOrUpdate(any()) }
        coVerify { dailyLogRepository.saveTrackingEntries(any(), any()) }
    }

    @Test
    fun `save with period creates new cycle when no current cycle`() = runTest {
        coEvery { dailyLogRepository.getByDate(any()) } returns null
        coEvery { cycleRepository.getCurrentCycle() } returns null
        coEvery { cycleRepository.getCycleCount() } returns 0
        coEvery { cycleRepository.getLastCycle() } returns null
        coEvery { cycleRepository.getAllCyclesOnce() } returns emptyList()
        coEvery { settingsRepository.getSettingsOnce() } returns null

        val savedStateHandle = SavedStateHandle(mapOf("date" to todayStr))
        val viewModel = TrackingViewModel(
            savedStateHandle, dailyLogRepository, cycleRepository,
            settingsRepository, predictionRepository, predictionEngine
        )
        advanceUntilIdle()

        viewModel.togglePeriod(true)
        viewModel.save()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isSaved)
        coVerify { cycleRepository.insertCycle(any()) }
    }

    @Test
    fun `clearError clears error state`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `multiple entries for different categories`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleEntry(TrackingCategory.PAIN, "cramps")
        viewModel.toggleEntry(TrackingCategory.MOOD, "happy")
        viewModel.toggleEntry(TrackingCategory.ENERGY, "high")

        assertEquals(3, viewModel.state.value.entries.size)
    }

    @Test
    fun `isPeriodDay detected for cycle in period range`() = runTest {
        val periodStart = today.minusDays(2)
        val cycle = Cycle(
            id = "c1", cycleNumber = 1,
            startDate = periodStart,
            periodStartDate = periodStart,
            periodEndDate = periodStart.plusDays(4),
            periodLength = 5
        )
        coEvery { dailyLogRepository.getByDate(today) } returns null
        coEvery { cycleRepository.getCurrentCycle() } returns cycle

        val savedStateHandle = SavedStateHandle(mapOf("date" to todayStr))
        val viewModel = TrackingViewModel(
            savedStateHandle, dailyLogRepository, cycleRepository,
            settingsRepository, predictionRepository, predictionEngine
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isPeriodDay)
    }
}
