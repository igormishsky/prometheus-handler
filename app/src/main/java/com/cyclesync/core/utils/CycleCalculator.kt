package com.cyclesync.core.utils

import com.cyclesync.core.constants.PopulationPriors
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CycleCalculator {

    fun getCurrentCycleDay(cycleStartDate: LocalDate, today: LocalDate = LocalDate.now()): Int {
        return ChronoUnit.DAYS.between(cycleStartDate, today).toInt() + 1
    }

    fun determineCyclePhase(
        cycleDay: Int,
        predictedCycleLength: Int,
        periodLength: Int
    ): CyclePhase {
        val ovulationDay = predictedCycleLength - 14 // approximate
        val fertileWindowStart = ovulationDay - PopulationPriors.FERTILE_WINDOW_START_BEFORE_OVULATION
        val fertileWindowEnd = ovulationDay + PopulationPriors.FERTILE_WINDOW_END_AFTER_OVULATION
        val pmsStart = predictedCycleLength - PopulationPriors.PMS_WINDOW_START_BEFORE_PERIOD

        return when {
            cycleDay <= periodLength -> CyclePhase.MENSTRUATION
            cycleDay in fertileWindowStart..fertileWindowEnd -> CyclePhase.OVULATION
            cycleDay < ovulationDay -> CyclePhase.FOLLICULAR
            cycleDay >= pmsStart -> CyclePhase.PMS
            else -> CyclePhase.LUTEAL
        }
    }

    fun calculateAverageCycleLength(cycles: List<Cycle>): Double {
        val validCycles = cycles.filter { !it.isExcluded && it.cycleLength != null }
        if (validCycles.isEmpty()) return PopulationPriors.POPULATION_MEAN_CYCLE
        return validCycles.mapNotNull { it.cycleLength }.average()
    }

    fun calculateAveragePeriodLength(cycles: List<Cycle>): Double {
        val validCycles = cycles.filter { !it.isExcluded && it.periodLength != null }
        if (validCycles.isEmpty()) return PopulationPriors.POPULATION_MEAN_PERIOD
        return validCycles.mapNotNull { it.periodLength }.average()
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
}
