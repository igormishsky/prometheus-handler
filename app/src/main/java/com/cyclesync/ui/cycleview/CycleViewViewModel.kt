package com.cyclesync.ui.cycleview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.constants.PopulationPriors
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    val pmsStart: LocalDate? = null
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
            combine(
                cycleRepository.getCurrentCycleFlow(),
                predictionRepository.getActivePredictions(),
                settingsRepository.getSettings()
            ) { currentCycle, predictions, settings ->
                buildState(currentCycle, predictions, settings)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun buildState(
        currentCycle: Cycle?,
        predictions: List<Prediction>,
        settings: Settings?
    ): CycleViewState {
        val today = LocalDate.now()
        val typicalCycleLength = settings?.typicalCycleLength ?: 28
        val typicalPeriodLength = settings?.typicalPeriodLength ?: 5

        if (currentCycle == null) {
            return CycleViewState(
                isLoading = false,
                hasCycles = false,
                predictedCycleLength = typicalCycleLength,
                periodLength = typicalPeriodLength,
                mode = settings?.activeMode?.displayName ?: "Period Tracking"
            )
        }

        val cycleDay = CycleCalculator.getCurrentCycleDay(currentCycle.startDate, today)
        val nextPeriodPrediction = predictions
            .filter { it.type == PredictionType.PERIOD_START && it.predictedDate.isAfter(today) }
            .minByOrNull { it.predictedDate }

        val ovulationPrediction = predictions
            .filter { it.type == PredictionType.OVULATION && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val fertileStart = predictions
            .filter { it.type == PredictionType.FERTILE_START && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val fertileEnd = predictions
            .filter { it.type == PredictionType.FERTILE_END && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val pmsStart = predictions
            .filter { it.type == PredictionType.PMS_START && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val predictedLength = nextPeriodPrediction?.let {
            ChronoUnit.DAYS.between(currentCycle.startDate, it.predictedDate).toInt()
        } ?: typicalCycleLength

        val periodLen = currentCycle.periodLength ?: typicalPeriodLength

        val phase = CycleCalculator.determineCyclePhase(cycleDay, predictedLength, periodLen)

        val daysUntil = nextPeriodPrediction?.let {
            ChronoUnit.DAYS.between(today, it.predictedDate).toInt()
        }

        return CycleViewState(
            currentCycleDay = cycleDay,
            predictedCycleLength = predictedLength,
            periodLength = periodLen,
            currentPhase = phase,
            daysUntilNextPeriod = daysUntil,
            confidence = nextPeriodPrediction?.confidence ?: 0.3,
            phaseName = phase.displayName,
            cycleStartDate = currentCycle.startDate,
            mode = settings?.activeMode?.displayName ?: "Period Tracking",
            isLoading = false,
            hasCycles = true,
            nextPeriodDate = nextPeriodPrediction?.predictedDate,
            ovulationDate = ovulationPrediction?.predictedDate,
            fertileWindowStart = fertileStart?.predictedDate,
            fertileWindowEnd = fertileEnd?.predictedDate,
            pmsStart = pmsStart?.predictedDate
        )
    }
}
