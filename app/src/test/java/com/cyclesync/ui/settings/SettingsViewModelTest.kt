package com.cyclesync.ui.settings

import com.cyclesync.core.notifications.ReminderManager
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.NotificationPrivacy
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.DailyLogRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import io.mockk.*
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    @MockK
    private lateinit var cycleRepository: CycleRepository

    @MockK
    private lateinit var predictionRepository: PredictionRepository

    @MockK(relaxed = true)
    private lateinit var dailyLogRepository: DailyLogRepository

    @MockK(relaxed = true)
    private lateinit var reminderManager: ReminderManager

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
        coEvery { settingsRepository.getSettings() } returns flowOf(null)

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        assertTrue(viewModel.state.value.isLoading)
    }

    // --- Settings loaded ---

    @Test
    fun `settings are loaded and mapped to state`() = runTest {
        val settings = Settings(
            activeMode = AppMode.CONCEIVE,
            typicalCycleLength = 30,
            typicalPeriodLength = 6,
            appIcon = "lotus",
            notificationPrivacy = NotificationPrivacy.MEDIUM
        )
        coEvery { settingsRepository.getSettings() } returns flowOf(settings)

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Conceive", state.mode)
        assertEquals(30, state.typicalCycleLength)
        assertEquals(6, state.typicalPeriodLength)
        assertEquals("Lotus", state.appIcon) // first char uppercased
        assertEquals("Medium — Vague", state.notificationPrivacy)
    }

    @Test
    fun `default settings map correctly`() = runTest {
        val settings = Settings() // all defaults
        coEvery { settingsRepository.getSettings() } returns flowOf(settings)

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Period Tracking", state.mode)
        assertEquals(28, state.typicalCycleLength)
        assertEquals(5, state.typicalPeriodLength)
        assertEquals("Default", state.appIcon)
        assertEquals("High — No details", state.notificationPrivacy)
    }

    @Test
    fun `null settings keeps loading state properties`() = runTest {
        coEvery { settingsRepository.getSettings() } returns flowOf(null)

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        // When settings is null, the collect block doesn't update state, so it stays at initial values
        val state = viewModel.state.value
        assertTrue(state.isLoading) // stays loading since settings was null
    }

    // --- cycleMode ---

    @Test
    fun `cycleMode advances to next mode`() = runTest {
        val settings = Settings(activeMode = AppMode.PERIOD_TRACKING)
        coEvery { settingsRepository.getSettings() } returns flowOf(settings)
        coEvery { settingsRepository.saveSettings(any()) } just Runs

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        viewModel.cycleMode()
        advanceUntilIdle()

        coVerify {
            settingsRepository.saveSettings(match {
                it.activeMode == AppMode.CONCEIVE
            })
        }
    }

    @Test
    fun `cycleMode wraps around from last mode to first`() = runTest {
        val settings = Settings(activeMode = AppMode.PERIMENOPAUSE)
        coEvery { settingsRepository.getSettings() } returns flowOf(settings)
        coEvery { settingsRepository.saveSettings(any()) } just Runs

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        viewModel.cycleMode()
        advanceUntilIdle()

        coVerify {
            settingsRepository.saveSettings(match {
                it.activeMode == AppMode.PERIOD_TRACKING
            })
        }
    }

    @Test
    fun `cycleMode does nothing when settings not loaded`() = runTest {
        coEvery { settingsRepository.getSettings() } returns flowOf(null)

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        viewModel.cycleMode()
        advanceUntilIdle()

        coVerify(exactly = 0) { settingsRepository.saveSettings(any()) }
    }

    // --- deleteAllData ---

    @Test
    fun `deleteAllData clears cycles and predictions`() = runTest {
        coEvery { settingsRepository.getSettings() } returns flowOf(null)
        coEvery { cycleRepository.deleteAllCycles() } just Runs
        coEvery { predictionRepository.deleteAll() } just Runs

        val viewModel = SettingsViewModel(settingsRepository, cycleRepository, dailyLogRepository, predictionRepository, reminderManager)
        advanceUntilIdle()

        viewModel.deleteAllData()
        advanceUntilIdle()

        coVerify { cycleRepository.deleteAllCycles() }
        coVerify { predictionRepository.deleteAll() }
    }

    // --- Mode display names ---

    @Test
    fun `all app modes have non-empty display names`() {
        for (mode in AppMode.entries) {
            assertTrue("Mode ${mode.name} should have display name",
                mode.displayName.isNotEmpty())
        }
    }

    @Test
    fun `all notification privacy levels have non-empty display names`() {
        for (privacy in NotificationPrivacy.entries) {
            assertTrue("Privacy ${privacy.name} should have display name",
                privacy.displayName.isNotEmpty())
        }
    }
}
