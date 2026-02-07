package com.cyclesync.ui.onboarding

import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.BmiCategory
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.prediction.PredictionEngine
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var cycleRepository: CycleRepository
    private lateinit var predictionRepository: PredictionRepository
    private lateinit var predictionEngine: PredictionEngine

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        cycleRepository = mockk(relaxed = true)
        predictionRepository = mockk(relaxed = true)
        predictionEngine = PredictionEngine()

        every { settingsRepository.getSettings() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = OnboardingViewModel(
        settingsRepository, cycleRepository, predictionRepository, predictionEngine
    )

    @Test
    fun `initial step is 0`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.currentStep.value)
    }

    @Test
    fun `nextStep increments current step`() = runTest {
        val viewModel = createViewModel()
        viewModel.nextStep()
        assertEquals(1, viewModel.currentStep.value)
    }

    @Test
    fun `nextStep does not exceed max`() = runTest {
        val viewModel = createViewModel()
        repeat(10) { viewModel.nextStep() }
        assertEquals(OnboardingViewModel.TOTAL_STEPS - 1, viewModel.currentStep.value)
    }

    @Test
    fun `previousStep decrements current step`() = runTest {
        val viewModel = createViewModel()
        viewModel.nextStep()
        viewModel.nextStep()
        viewModel.previousStep()
        assertEquals(1, viewModel.currentStep.value)
    }

    @Test
    fun `previousStep does not go below 0`() = runTest {
        val viewModel = createViewModel()
        viewModel.previousStep()
        assertEquals(0, viewModel.currentStep.value)
    }

    @Test
    fun `setBirthYear validates range`() = runTest {
        val viewModel = createViewModel()
        viewModel.setBirthYear(1990)
        assertEquals(1990, viewModel.birthYear.value)
    }

    @Test
    fun `setBirthYear clamps too old year`() = runTest {
        val viewModel = createViewModel()
        viewModel.setBirthYear(1800)
        assertEquals(OnboardingViewModel.MIN_BIRTH_YEAR, viewModel.birthYear.value)
    }

    @Test
    fun `setBirthYear clamps too young year`() = runTest {
        val viewModel = createViewModel()
        viewModel.setBirthYear(LocalDate.now().year)
        val maxYear = LocalDate.now().year - OnboardingViewModel.MAX_BIRTH_YEAR_OFFSET
        assertEquals(maxYear, viewModel.birthYear.value)
    }

    @Test
    fun `setBirthYear accepts null`() = runTest {
        val viewModel = createViewModel()
        viewModel.setBirthYear(null)
        assertNull(viewModel.birthYear.value)
    }

    @Test
    fun `setCycleLength clamps to valid range`() = runTest {
        val viewModel = createViewModel()
        viewModel.setCycleLength(10)
        assertEquals(15, viewModel.cycleLength.value)
        viewModel.setCycleLength(100)
        assertEquals(60, viewModel.cycleLength.value)
        viewModel.setCycleLength(28)
        assertEquals(28, viewModel.cycleLength.value)
    }

    @Test
    fun `setPeriodLength clamps to valid range`() = runTest {
        val viewModel = createViewModel()
        viewModel.setPeriodLength(0)
        assertEquals(1, viewModel.periodLength.value)
        viewModel.setPeriodLength(20)
        assertEquals(15, viewModel.periodLength.value)
        viewModel.setPeriodLength(5)
        assertEquals(5, viewModel.periodLength.value)
    }

    @Test
    fun `setLastPeriodDate rejects future dates`() = runTest {
        val viewModel = createViewModel()
        viewModel.setLastPeriodDate(LocalDate.now().plusDays(5))
        assertNull(viewModel.lastPeriodDate.value)
    }

    @Test
    fun `setLastPeriodDate accepts past dates`() = runTest {
        val viewModel = createViewModel()
        val pastDate = LocalDate.now().minusDays(10)
        viewModel.setLastPeriodDate(pastDate)
        assertEquals(pastDate, viewModel.lastPeriodDate.value)
    }

    @Test
    fun `setSelectedMode updates mode`() = runTest {
        val viewModel = createViewModel()
        viewModel.setSelectedMode(AppMode.CONCEIVE)
        assertEquals(AppMode.CONCEIVE, viewModel.selectedMode.value)
    }

    @Test
    fun `setBmiCategory updates category`() = runTest {
        val viewModel = createViewModel()
        viewModel.setBmiCategory(BmiCategory.NORMAL)
        assertEquals(BmiCategory.NORMAL, viewModel.bmiCategory.value)
    }

    @Test
    fun `completeOnboarding saves settings`() = runTest {
        val viewModel = createViewModel()
        viewModel.setBirthYear(1990)
        viewModel.setCycleLength(30)
        viewModel.setPeriodLength(6)

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify { settingsRepository.saveSettings(any()) }
    }

    @Test
    fun `completeOnboarding creates cycle when period date set`() = runTest {
        val viewModel = createViewModel()
        viewModel.setLastPeriodDate(LocalDate.now().minusDays(5))

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify { cycleRepository.insertCycle(any()) }
        coVerify { predictionRepository.savePredictions(any()) }
    }

    @Test
    fun `completeOnboarding does not create cycle without period date`() = runTest {
        val viewModel = createViewModel()

        viewModel.completeOnboarding()
        advanceUntilIdle()

        coVerify(exactly = 0) { cycleRepository.insertCycle(any()) }
    }

    @Test
    fun `isOnboardingCompleted reflects settings`() = runTest {
        val settings = Settings(onboardingCompleted = true)
        every { settingsRepository.getSettings() } returns flowOf(settings)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.isOnboardingCompleted.value)
    }

    @Test
    fun `isOnboardingCompleted false when no settings`() = runTest {
        every { settingsRepository.getSettings() } returns flowOf(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isOnboardingCompleted.value)
    }

    @Test
    fun `default values are sensible`() = runTest {
        val viewModel = createViewModel()
        assertEquals(28, viewModel.cycleLength.value)
        assertEquals(5, viewModel.periodLength.value)
        assertEquals(AppMode.PERIOD_TRACKING, viewModel.selectedMode.value)
        assertNull(viewModel.birthYear.value)
        assertNull(viewModel.bmiCategory.value)
        assertNull(viewModel.lastPeriodDate.value)
    }
}
