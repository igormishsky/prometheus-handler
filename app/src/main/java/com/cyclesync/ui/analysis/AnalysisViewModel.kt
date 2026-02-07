package com.cyclesync.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.CycleCalculator
import com.cyclesync.domain.entity.CycleAnalysis
import com.cyclesync.domain.entity.Trend
import com.cyclesync.domain.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalysisState(
    val analysis: CycleAnalysis? = null,
    val isLoading: Boolean = true,
    val hasCycles: Boolean = false
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    init {
        loadAnalysis()
    }

    private fun loadAnalysis() {
        viewModelScope.launch {
            cycleRepository.getAllCycles().collect { cycles ->
                if (cycles.isEmpty()) {
                    _state.value = AnalysisState(isLoading = false, hasCycles = false)
                    return@collect
                }

                val validCycles = cycles.filter { !it.isExcluded && it.cycleLength != null }
                val cycleLengths = validCycles.mapNotNull { it.cycleLength }

                if (cycleLengths.isEmpty()) {
                    _state.value = AnalysisState(isLoading = false, hasCycles = true)
                    return@collect
                }

                val avgCycle = CycleCalculator.calculateAverageCycleLength(cycles)
                val avgPeriod = CycleCalculator.calculateAveragePeriodLength(cycles)
                val sd = CycleCalculator.calculateStandardDeviation(cycleLengths)
                val regularity = CycleCalculator.calculateRegularityScore(cycleLengths)

                val trend = if (cycleLengths.size >= 6) {
                    val recent = cycleLengths.takeLast(6)
                    val slope = calculateSlope(recent)
                    when {
                        slope > 0.3 -> Trend.LENGTHENING
                        slope < -0.3 -> Trend.SHORTENING
                        else -> Trend.STABLE
                    }
                } else Trend.STABLE

                _state.value = AnalysisState(
                    analysis = CycleAnalysis(
                        averageCycleLength = avgCycle,
                        averagePeriodLength = avgPeriod,
                        cycleLengthVariability = sd,
                        longestCycle = cycleLengths.max(),
                        shortestCycle = cycleLengths.min(),
                        totalCyclesTracked = cycles.size,
                        currentStreak = calculateStreak(cycles),
                        regularityScore = regularity,
                        cycleLengthTrend = trend,
                        cycleLengths = cycleLengths
                    ),
                    isLoading = false,
                    hasCycles = true
                )
            }
        }
    }

    private fun calculateSlope(values: List<Int>): Double {
        val n = values.size
        if (n < 2) return 0.0
        val xMean = (n - 1) / 2.0
        val yMean = values.average()
        var num = 0.0
        var den = 0.0
        for (i in values.indices) {
            num += (i - xMean) * (values[i] - yMean)
            den += (i - xMean) * (i - xMean)
        }
        return if (den != 0.0) num / den else 0.0
    }

    private fun calculateStreak(cycles: List<com.cyclesync.domain.entity.Cycle>): Int {
        if (cycles.isEmpty()) return 0
        val sorted = cycles.sortedByDescending { it.startDate }
        var streak = 1
        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]
            // Consider cycles consecutive if they are within 45 days of each other
            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(next.startDate, current.startDate)
            if (daysBetween <= 45) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
