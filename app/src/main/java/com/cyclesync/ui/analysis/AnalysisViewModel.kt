package com.cyclesync.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.CycleCalculator
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.CycleAnalysis
import com.cyclesync.domain.entity.TrackingCategory
import com.cyclesync.domain.entity.Trend
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SymptomFrequency(
    val category: String,
    val subcategory: String,
    val count: Int,
    val percentage: Double
)

data class CycleSummary(
    val cycleNumber: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val cycleLength: Int?,
    val periodLength: Int?,
    val topSymptoms: List<String>,
    val notes: String?
)

data class AnalysisState(
    val analysis: CycleAnalysis? = null,
    val isLoading: Boolean = true,
    val hasCycles: Boolean = false,
    val symptomPatterns: List<SymptomFrequency> = emptyList(),
    val cycleSummaries: List<CycleSummary> = emptyList(),
    val totalTrackedDays: Int = 0,
    val error: String? = null
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    init {
        loadAnalysis()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
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

                val trend = calculateTrend(cycleLengths)

                val firstDate = cycles.minOf { it.startDate }
                val allLogs = try {
                    dailyLogRepository.getByDateRange(firstDate, LocalDate.now())
                } catch (_: Exception) {
                    emptyList()
                }
                val totalDays = allLogs.size

                val symptomPatterns = buildSymptomPatterns(allLogs, totalDays)
                val cycleSummaries = buildCycleSummaries(cycles)

                _state.value = AnalysisState(
                    analysis = CycleAnalysis(
                        averageCycleLength = avgCycle,
                        averagePeriodLength = avgPeriod,
                        cycleLengthVariability = sd,
                        longestCycle = cycleLengths.max(),
                        shortestCycle = cycleLengths.min(),
                        totalCyclesTracked = cycles.size,
                        currentStreak = CycleCalculator.calculateConsecutiveCycleStreak(cycles),
                        regularityScore = regularity,
                        cycleLengthTrend = trend,
                        cycleLengths = cycleLengths
                    ),
                    isLoading = false,
                    hasCycles = true,
                    symptomPatterns = symptomPatterns,
                    cycleSummaries = cycleSummaries,
                    totalTrackedDays = totalDays
                )
            }
        }
    }

    private fun calculateTrend(cycleLengths: List<Int>): Trend {
        if (cycleLengths.size < 6) return Trend.STABLE
        val recent = cycleLengths.takeLast(6)
        val slope = calculateSlope(recent)
        return when {
            slope > 0.3 -> Trend.LENGTHENING
            slope < -0.3 -> Trend.SHORTENING
            else -> Trend.STABLE
        }
    }

    private fun buildSymptomPatterns(
        allLogs: List<com.cyclesync.domain.entity.DailyLog>,
        totalDays: Int
    ): List<SymptomFrequency> {
        val symptomCounts = mutableMapOf<String, Int>()
        allLogs.forEach { log ->
            log.entries.forEach { entry ->
                if (entry.category != TrackingCategory.BLEEDING) {
                    val key = "${entry.category.displayName}|${entry.subcategory}"
                    symptomCounts[key] = (symptomCounts[key] ?: 0) + 1
                }
            }
        }

        return symptomCounts.entries
            .sortedByDescending { it.value }
            .take(15)
            .map { (key, count) ->
                val parts = key.split("|")
                SymptomFrequency(
                    category = parts[0],
                    subcategory = parts.getOrElse(1) { "" }.replace("_", " "),
                    count = count,
                    percentage = if (totalDays > 0) (count.toDouble() / totalDays) * 100 else 0.0
                )
            }
    }

    private fun buildCycleSummaries(cycles: List<Cycle>): List<CycleSummary> {
        return cycles
            .sortedByDescending { it.startDate }
            .take(6)
            .map { cycle ->
                val cycleLogs = try {
                    dailyLogRepository.getByCycleId(cycle.id)
                } catch (_: Exception) {
                    emptyList()
                }
                val topSymptoms = cycleLogs
                    .flatMap { it.entries }
                    .filter { it.category != TrackingCategory.BLEEDING }
                    .groupBy { it.subcategory.replace("_", " ") }
                    .entries
                    .sortedByDescending { it.value.size }
                    .take(3)
                    .map { it.key }

                CycleSummary(
                    cycleNumber = cycle.cycleNumber,
                    startDate = cycle.startDate,
                    endDate = cycle.endDate,
                    cycleLength = cycle.cycleLength,
                    periodLength = cycle.periodLength,
                    topSymptoms = topSymptoms,
                    notes = cycle.notes
                )
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
}
