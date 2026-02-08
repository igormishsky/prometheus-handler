package com.cyclesync.ui.cycleview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.CycleCalculator
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.CyclePhase
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CycleViewState(
    val currentCycleDay: Int = 0,
    val predictedCycleLength: Int = 28,
    val periodLength: Int = 5,
    val currentPhase: CyclePhase = CyclePhase.FOLLICULAR,
    val daysUntilNextPeriod: Int? = null,
    val confidence: Double = 0.0,
    val phaseName: String = "Follicular",
    val cycleStartDate: LocalDate? = null,
    val mode: String = "Period Tracking",
    val isLoading: Boolean = true,
    val hasCycles: Boolean = false,
    val nextPeriodDate: LocalDate? = null,
    val ovulationDate: LocalDate? = null,
    val fertileWindowStart: LocalDate? = null,
    val fertileWindowEnd: LocalDate? = null,
    val pmsStart: LocalDate? = null,
    val dailyAffirmation: String = "",
    val wellnessTip: String = "",
    val phaseSuperpower: String = "",
    val bodyInsight: String = "",
    val streakDays: Int = 0,
    val streakMessage: String = "",
    val error: String? = null
)

@HiltViewModel
class CycleViewViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val predictionRepository: PredictionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CycleViewState())
    val state: StateFlow<CycleViewState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                combine(
                    cycleRepository.getCurrentCycleFlow(),
                    predictionRepository.getActivePredictions(),
                    settingsRepository.getSettings()
                ) { currentCycle, predictions, settings ->
                    buildState(currentCycle, predictions, settings)
                }.collect { newState ->
                    _state.value = newState
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load cycle data"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun findNextPrediction(
        predictions: List<Prediction>,
        type: PredictionType,
        after: LocalDate
    ): Prediction? = predictions
        .filter { it.type == type && it.predictedDate.isAfter(after) }
        .minByOrNull { it.predictedDate }

    private fun buildState(
        currentCycle: Cycle?,
        predictions: List<Prediction>,
        settings: Settings?
    ): CycleViewState {
        val today = LocalDate.now()
        val typicalCycleLength = settings?.typicalCycleLength ?: 28
        val typicalPeriodLength = settings?.typicalPeriodLength ?: 5

        if (currentCycle == null) {
            val phase = CyclePhase.FOLLICULAR
            return CycleViewState(
                isLoading = false,
                hasCycles = false,
                predictedCycleLength = typicalCycleLength,
                periodLength = typicalPeriodLength,
                mode = settings?.activeMode?.displayName ?: "Period Tracking",
                dailyAffirmation = CycleContent.getAffirmationForToday(),
                wellnessTip = CycleContent.getWellnessTipForPhase(phase),
                phaseSuperpower = CycleContent.getPhaseSuperpowerForToday(phase),
                bodyInsight = CycleContent.getBodyInsightForToday(phase),
                streakDays = 0,
                streakMessage = CycleContent.getStreakMessage(0)
            )
        }

        val cycleDay = CycleCalculator.getCurrentCycleDay(currentCycle.startDate, today)
        val nextPeriod = findNextPrediction(predictions, PredictionType.PERIOD_START, today)
        val ovulation = findNextPrediction(predictions, PredictionType.OVULATION, currentCycle.startDate)
        val fertileStart = findNextPrediction(predictions, PredictionType.FERTILE_START, currentCycle.startDate)
        val fertileEnd = findNextPrediction(predictions, PredictionType.FERTILE_END, currentCycle.startDate)
        val pmsStart = findNextPrediction(predictions, PredictionType.PMS_START, currentCycle.startDate)

        val predictedLength = nextPeriod?.let {
            ChronoUnit.DAYS.between(currentCycle.startDate, it.predictedDate).toInt()
        } ?: typicalCycleLength

        val periodLen = currentCycle.periodLength ?: typicalPeriodLength
        val phase = CycleCalculator.determineCyclePhase(cycleDay, predictedLength, periodLen)

        val daysUntil = nextPeriod?.let {
            ChronoUnit.DAYS.between(today, it.predictedDate).toInt()
        }

        val streakDays = currentCycle.startDate?.let {
            ChronoUnit.DAYS.between(it, today).toInt().coerceAtLeast(0)
        } ?: 0

        return CycleViewState(
            currentCycleDay = cycleDay,
            predictedCycleLength = predictedLength,
            periodLength = periodLen,
            currentPhase = phase,
            daysUntilNextPeriod = daysUntil,
            confidence = nextPeriod?.confidence ?: 0.3,
            phaseName = phase.displayName,
            cycleStartDate = currentCycle.startDate,
            mode = settings?.activeMode?.displayName ?: "Period Tracking",
            isLoading = false,
            hasCycles = true,
            nextPeriodDate = nextPeriod?.predictedDate,
            ovulationDate = ovulation?.predictedDate,
            fertileWindowStart = fertileStart?.predictedDate,
            fertileWindowEnd = fertileEnd?.predictedDate,
            pmsStart = pmsStart?.predictedDate,
            dailyAffirmation = CycleContent.getAffirmationForToday(),
            wellnessTip = CycleContent.getWellnessTipForPhase(phase),
            phaseSuperpower = CycleContent.getPhaseSuperpowerForToday(phase),
            bodyInsight = CycleContent.getBodyInsightForToday(phase),
            streakDays = streakDays,
            streakMessage = CycleContent.getStreakMessage(streakDays)
        )
    }
}
