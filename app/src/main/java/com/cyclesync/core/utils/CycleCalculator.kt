package com.cyclesync.core.utils

import com.cyclesync.core.constants.PopulationPriors
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CycleCalculator {

    const val MIN_CYCLE_LENGTH = 15
    const val MAX_CYCLE_LENGTH = 60
    const val MIN_PERIOD_LENGTH = 1
    const val MAX_PERIOD_LENGTH = 15

    fun getCurrentCycleDay(cycleStartDate: LocalDate, today: LocalDate = LocalDate.now()): Int {
        val days = ChronoUnit.DAYS.between(cycleStartDate, today).toInt() + 1
        return days.coerceAtLeast(1)
    }

    fun determineCyclePhase(
        cycleDay: Int,
        predictedCycleLength: Int,
        periodLength: Int
    ): CyclePhase {
        val safeCycleLength = predictedCycleLength.coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
        val safePeriodLength = periodLength.coerceIn(MIN_PERIOD_LENGTH, MAX_PERIOD_LENGTH)

        val ovulationDay = safeCycleLength - 14
        val fertileWindowStart = ovulationDay - PopulationPriors.FERTILE_WINDOW_START_BEFORE_OVULATION
        val fertileWindowEnd = ovulationDay + PopulationPriors.FERTILE_WINDOW_END_AFTER_OVULATION
        val pmsStart = safeCycleLength - PopulationPriors.PMS_WINDOW_START_BEFORE_PERIOD

        return when {
            cycleDay <= safePeriodLength -> CyclePhase.MENSTRUATION
            cycleDay in fertileWindowStart..fertileWindowEnd -> CyclePhase.OVULATION
            cycleDay < ovulationDay -> CyclePhase.FOLLICULAR
            cycleDay >= pmsStart -> CyclePhase.PMS
            else -> CyclePhase.LUTEAL
        }
    }

    fun calculateAverageCycleLength(cycles: List<Cycle>): Double {
        val validLengths = cycles
            .filter { !it.isExcluded }
            .mapNotNull { it.cycleLength }
            .filter { it in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH }
        if (validLengths.isEmpty()) return PopulationPriors.POPULATION_MEAN_CYCLE
        return validLengths.average()
    }

    fun calculateAveragePeriodLength(cycles: List<Cycle>): Double {
        val validLengths = cycles
            .filter { !it.isExcluded }
            .mapNotNull { it.periodLength }
            .filter { it in MIN_PERIOD_LENGTH..MAX_PERIOD_LENGTH }
        if (validLengths.isEmpty()) return PopulationPriors.POPULATION_MEAN_PERIOD
        return validLengths.average()
    }

    fun calculateMedianCycleLength(cycles: List<Cycle>): Double {
        val validLengths = cycles
            .filter { !it.isExcluded }
            .mapNotNull { it.cycleLength }
            .filter { it in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH }
            .sorted()
        if (validLengths.isEmpty()) return PopulationPriors.POPULATION_MEAN_CYCLE
        val mid = validLengths.size / 2
        return if (validLengths.size % 2 == 0) {
            (validLengths[mid - 1] + validLengths[mid]) / 2.0
        } else {
            validLengths[mid].toDouble()
        }
    }

    fun calculateStandardDeviation(values: List<Int>): Double {
        if (values.size < 2) return 0.0
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }

    fun calculateRegularityScore(cycleLengths: List<Int>): Int {
        if (cycleLengths.size < 2) return 5
        val sd = calculateStandardDeviation(cycleLengths)
        return when {
            sd < 1.0 -> 10
            sd < 2.0 -> 9
            sd < 3.0 -> 8
            sd < 4.0 -> 7
            sd < 5.0 -> 6
            sd < 6.0 -> 5
            sd < 7.0 -> 4
            sd < 8.0 -> 3
            sd < 9.0 -> 2
            else -> 1
        }
    }

    fun daysUntilNextPeriod(cycleStartDate: LocalDate, predictedCycleLength: Int): Int {
        val nextPeriodDate = cycleStartDate.plusDays(predictedCycleLength.toLong())
        return ChronoUnit.DAYS.between(LocalDate.now(), nextPeriodDate).toInt()
    }

    fun isValidCycleLength(length: Int): Boolean =
        length in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH

    fun isValidPeriodLength(length: Int): Boolean =
        length in MIN_PERIOD_LENGTH..MAX_PERIOD_LENGTH

    fun calculateCycleLengthRange(cycles: List<Cycle>): Pair<Int, Int>? {
        val validLengths = cycles
            .filter { !it.isExcluded }
            .mapNotNull { it.cycleLength }
            .filter { it in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH }
        if (validLengths.isEmpty()) return null
        return validLengths.min() to validLengths.max()
    }

    fun calculateConsecutiveCycleStreak(cycles: List<Cycle>): Int {
        if (cycles.isEmpty()) return 0
        val sorted = cycles.sortedByDescending { it.startDate }
        var streak = 1
        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]
            val daysBetween = ChronoUnit.DAYS.between(next.startDate, current.startDate)
            if (daysBetween in 1..MAX_CYCLE_LENGTH.toLong()) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
