package com.cyclesync.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.core.utils.UuidGenerator
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.BmiCategory
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.prediction.PredictionEngine
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cycleRepository: CycleRepository,
    private val predictionRepository: PredictionRepository,
    private val predictionEngine: PredictionEngine
) : ViewModel() {

    val isOnboardingCompleted: StateFlow<Boolean> = settingsRepository.getSettings()
        .map { it?.onboardingCompleted ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _birthYear = MutableStateFlow<Int?>(null)
    val birthYear: StateFlow<Int?> = _birthYear.asStateFlow()

    private val _cycleLength = MutableStateFlow(28)
    val cycleLength: StateFlow<Int> = _cycleLength.asStateFlow()

    private val _periodLength = MutableStateFlow(5)
    val periodLength: StateFlow<Int> = _periodLength.asStateFlow()

    private val _bmiCategory = MutableStateFlow<BmiCategory?>(null)
    val bmiCategory: StateFlow<BmiCategory?> = _bmiCategory.asStateFlow()

    private val _lastPeriodDate = MutableStateFlow<LocalDate?>(null)
    val lastPeriodDate: StateFlow<LocalDate?> = _lastPeriodDate.asStateFlow()

    private val _selectedMode = MutableStateFlow(AppMode.PERIOD_TRACKING)
    val selectedMode: StateFlow<AppMode> = _selectedMode.asStateFlow()

    fun nextStep() {
        _currentStep.value = (_currentStep.value + 1).coerceAtMost(5)
    }

    fun previousStep() {
        _currentStep.value = (_currentStep.value - 1).coerceAtLeast(0)
    }

    fun setBirthYear(year: Int?) { _birthYear.value = year }
    fun setCycleLength(length: Int) { _cycleLength.value = length }
    fun setPeriodLength(length: Int) { _periodLength.value = length }
    fun setBmiCategory(category: BmiCategory?) { _bmiCategory.value = category }
    fun setLastPeriodDate(date: LocalDate?) { _lastPeriodDate.value = date }
    fun setSelectedMode(mode: AppMode) { _selectedMode.value = mode }

    fun completeOnboarding() {
        viewModelScope.launch {
            val settings = Settings(
                activeMode = _selectedMode.value,
                birthYear = _birthYear.value,
                bmiCategory = _bmiCategory.value,
                typicalCycleLength = _cycleLength.value,
                typicalPeriodLength = _periodLength.value,
                firstLaunchDate = LocalDate.now(),
                onboardingCompleted = true
            )
            settingsRepository.saveSettings(settings)

            // Create first cycle if last period date provided
            _lastPeriodDate.value?.let { periodDate ->
                val cycle = Cycle(
                    id = UuidGenerator.generate(),
                    cycleNumber = 1,
                    startDate = periodDate,
                    periodStartDate = periodDate
                )
                cycleRepository.insertCycle(cycle)

                // Generate initial predictions
                val predictions = predictionEngine.generatePredictions(
                    cycles = listOf(cycle),
                    userAge = _birthYear.value?.let { LocalDate.now().year - it },
                    userBmi = _bmiCategory.value?.name?.lowercase()
                )
                predictionRepository.savePredictions(predictions)
            }
        }
    }
}
