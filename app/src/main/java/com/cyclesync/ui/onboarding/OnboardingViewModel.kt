package com.cyclesync.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.CycleCalculator
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

    companion object {
        const val TOTAL_STEPS = 6
        const val MIN_BIRTH_YEAR = 1940
        const val MAX_BIRTH_YEAR_OFFSET = 10
    }

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

    private val _isCompleting = MutableStateFlow(false)
    val isCompleting: StateFlow<Boolean> = _isCompleting.asStateFlow()

    fun nextStep() {
        _currentStep.value = (_currentStep.value + 1).coerceAtMost(TOTAL_STEPS - 1)
    }

    fun previousStep() {
        _currentStep.value = (_currentStep.value - 1).coerceAtLeast(0)
    }

    fun setBirthYear(year: Int?) {
        val maxYear = LocalDate.now().year - MAX_BIRTH_YEAR_OFFSET
        _birthYear.value = year?.coerceIn(MIN_BIRTH_YEAR, maxYear)
    }

    fun setCycleLength(length: Int) {
        _cycleLength.value = length.coerceIn(CycleCalculator.MIN_CYCLE_LENGTH, CycleCalculator.MAX_CYCLE_LENGTH)
    }

    fun setPeriodLength(length: Int) {
        _periodLength.value = length.coerceIn(CycleCalculator.MIN_PERIOD_LENGTH, CycleCalculator.MAX_PERIOD_LENGTH)
    }

    fun setBmiCategory(category: BmiCategory?) { _bmiCategory.value = category }

    fun setLastPeriodDate(date: LocalDate?) {
        if (date != null && date.isAfter(LocalDate.now())) return
        _lastPeriodDate.value = date
    }

    fun setSelectedMode(mode: AppMode) { _selectedMode.value = mode }

    fun completeOnboarding() {
        if (_isCompleting.value) return
        _isCompleting.value = true

        viewModelScope.launch {
            try {
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

                _lastPeriodDate.value?.let { periodDate ->
                    val cycle = Cycle(
                        id = UuidGenerator.generate(),
                        cycleNumber = 1,
                        startDate = periodDate,
                        periodStartDate = periodDate
                    )
                    cycleRepository.insertCycle(cycle)

                    val predictions = predictionEngine.generatePredictions(
                        cycles = listOf(cycle),
                        userAge = _birthYear.value?.let { LocalDate.now().year - it },
                        userBmi = _bmiCategory.value?.name?.lowercase()
                    )
                    predictionRepository.savePredictions(predictions)
                }
            } finally {
                _isCompleting.value = false
            }
        }
    }
}
