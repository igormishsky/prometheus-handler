package com.cyclesync.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarDay(
    val date: LocalDate,
    val isPeriod: Boolean = false,
    val isPredictedPeriod: Boolean = false,
    val isFertile: Boolean = false,
    val isOvulation: Boolean = false,
    val isPMS: Boolean = false,
    val hasLog: Boolean = false,
    val isToday: Boolean = false
)

data class CalendarState(
    val currentMonth: YearMonth = YearMonth.now(),
    val days: List<CalendarDay> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val predictionRepository: PredictionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                cycleRepository.getAllCycles(),
                predictionRepository.getActivePredictions()
            ) { cycles, predictions ->
                buildCalendarState(cycles, predictions)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        refreshDays()
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        refreshDays()
    }

    fun goToMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        refreshDays()
    }

    fun goToToday() {
        _currentMonth.value = YearMonth.now()
        refreshDays()
    }

    private fun refreshDays() {
        viewModelScope.launch {
            try {
                val cycles = cycleRepository.getAllCyclesOnce()
                val predictions = predictionRepository.getActivePredictionsOnce()
                _state.value = buildCalendarState(cycles, predictions)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to load calendar data")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun buildCalendarState(
        cycles: List<Cycle>,
        predictions: List<Prediction>
    ): CalendarState {
        val month = _currentMonth.value
        val today = LocalDate.now()
        val firstDay = month.atDay(1)

        val periodDates = buildPeriodDates(cycles)
        val predictedPeriodDates = buildPredictedPeriodDates(predictions)
        val fertileDates = buildDateRangeFromPredictions(predictions, PredictionType.FERTILE_START, PredictionType.FERTILE_END)
        val ovulationDates = predictions.filter { it.type == PredictionType.OVULATION }.map { it.predictedDate }.toSet()
        val pmsDates = buildDateRangeFromPredictions(predictions, PredictionType.PMS_START, PredictionType.PMS_END)

        val days = (0 until month.lengthOfMonth()).map { dayOffset ->
            val date = firstDay.plusDays(dayOffset.toLong())
            CalendarDay(
                date = date,
                isPeriod = date in periodDates,
                isPredictedPeriod = date in predictedPeriodDates && date !in periodDates,
                isFertile = date in fertileDates,
                isOvulation = date in ovulationDates,
                isPMS = date in pmsDates,
                isToday = date == today
            )
        }

        return CalendarState(
            currentMonth = month,
            days = days,
            isLoading = false
        )
    }

    private fun buildPeriodDates(cycles: List<Cycle>): Set<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        cycles.forEach { cycle ->
            val start = cycle.periodStartDate
            val end = cycle.effectivePeriodEnd
            DateUtils.generateDateRange(start, end).forEach { dates.add(it) }
        }
        return dates
    }

    private fun buildPredictedPeriodDates(predictions: List<Prediction>): Set<LocalDate> {
        return predictions
            .filter { it.type == PredictionType.PERIOD_START }
            .flatMap { pred ->
                val endPred = predictions.find {
                    it.type == PredictionType.PERIOD_END &&
                    !it.predictedDate.isBefore(pred.predictedDate) &&
                    it.predictedDate.isBefore(pred.predictedDate.plusDays(10))
                }
                val end = endPred?.predictedDate ?: pred.predictedDate.plusDays(4)
                DateUtils.generateDateRange(pred.predictedDate, end)
            }.toSet()
    }

    private fun buildDateRangeFromPredictions(
        predictions: List<Prediction>,
        startType: PredictionType,
        endType: PredictionType
    ): Set<LocalDate> {
        return predictions
            .filter { it.type == startType }
            .flatMap { start ->
                val end = predictions.find {
                    it.type == endType &&
                    !it.predictedDate.isBefore(start.predictedDate)
                }
                if (end != null) DateUtils.generateDateRange(start.predictedDate, end.predictedDate) else emptyList()
            }.toSet()
    }
}
