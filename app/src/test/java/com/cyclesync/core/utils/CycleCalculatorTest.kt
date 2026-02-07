package com.cyclesync.core.utils

import com.cyclesync.core.constants.PopulationPriors
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.CyclePhase
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleCalculatorTest {

    // --- getCurrentCycleDay ---

    @Test
    fun `getCurrentCycleDay returns 1 on start day`() {
        val startDate = LocalDate.of(2024, 3, 10)
        val today = LocalDate.of(2024, 3, 10)
        assertEquals(1, CycleCalculator.getCurrentCycleDay(startDate, today))
    }

    @Test
    fun `getCurrentCycleDay returns correct day mid-cycle`() {
        val startDate = LocalDate.of(2024, 3, 1)
        val today = LocalDate.of(2024, 3, 15)
        assertEquals(15, CycleCalculator.getCurrentCycleDay(startDate, today))
    }

    @Test
    fun `getCurrentCycleDay returns correct day at end of typical cycle`() {
        val startDate = LocalDate.of(2024, 3, 1)
        val today = LocalDate.of(2024, 3, 28)
        assertEquals(28, CycleCalculator.getCurrentCycleDay(startDate, today))
    }

    @Test
    fun `getCurrentCycleDay handles past dates`() {
        val startDate = LocalDate.of(2024, 3, 10)
        val today = LocalDate.of(2024, 3, 5) // before cycle start
        assertEquals(-4, CycleCalculator.getCurrentCycleDay(startDate, today))
    }

    // --- determineCyclePhase ---

    @Test
    fun `determineCyclePhase returns MENSTRUATION during period`() {
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 1,
            predictedCycleLength = 28,
            periodLength = 5
        )
        assertEquals(CyclePhase.MENSTRUATION, phase)
    }

    @Test
    fun `determineCyclePhase returns MENSTRUATION on last period day`() {
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 5,
            predictedCycleLength = 28,
            periodLength = 5
        )
        assertEquals(CyclePhase.MENSTRUATION, phase)
    }

    @Test
    fun `determineCyclePhase returns FOLLICULAR after period before fertile window`() {
        // For 28 day cycle: ovulation = 28-14 = 14, fertile start = 14-5 = 9
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 7,
            predictedCycleLength = 28,
            periodLength = 5
        )
        assertEquals(CyclePhase.FOLLICULAR, phase)
    }

    @Test
    fun `determineCyclePhase returns OVULATION during fertile window`() {
        // Ovulation day = 28-14 = 14, fertile window = day 9 to day 15
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 12,
            predictedCycleLength = 28,
            periodLength = 5
        )
        assertEquals(CyclePhase.OVULATION, phase)
    }

    @Test
    fun `determineCyclePhase returns LUTEAL after ovulation before PMS`() {
        // Ovulation day 14, PMS starts day 21 (28-7)
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 18,
            predictedCycleLength = 28,
            periodLength = 5
        )
        assertEquals(CyclePhase.LUTEAL, phase)
    }

    @Test
    fun `determineCyclePhase returns PMS near end of cycle`() {
        // PMS starts at day 28-7 = 21
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 25,
            predictedCycleLength = 28,
            periodLength = 5
        )
        assertEquals(CyclePhase.PMS, phase)
    }

    @Test
    fun `determineCyclePhase handles short cycle correctly`() {
        // 21-day cycle: ovulation = 21-14 = 7, fertile start = 7-5 = 2
        // But period is 5 days, so day 3 should still be menstruation
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 3,
            predictedCycleLength = 21,
            periodLength = 5
        )
        assertEquals(CyclePhase.MENSTRUATION, phase)
    }

    @Test
    fun `determineCyclePhase handles long cycle correctly`() {
        // 35-day cycle: ovulation = 35-14 = 21, fertile start = 21-5 = 16
        val phase = CycleCalculator.determineCyclePhase(
            cycleDay = 10,
            predictedCycleLength = 35,
            periodLength = 5
        )
        assertEquals(CyclePhase.FOLLICULAR, phase)
    }

    // --- calculateAverageCycleLength ---

    @Test
    fun `calculateAverageCycleLength returns population mean for empty list`() {
        assertEquals(PopulationPriors.POPULATION_MEAN_CYCLE, CycleCalculator.calculateAverageCycleLength(emptyList()), 0.01)
    }

    @Test
    fun `calculateAverageCycleLength ignores excluded cycles`() {
        val cycles = listOf(
            createCycle(cycleLength = 28, isExcluded = false),
            createCycle(cycleLength = 60, isExcluded = true),
            createCycle(cycleLength = 30, isExcluded = false)
        )
        assertEquals(29.0, CycleCalculator.calculateAverageCycleLength(cycles), 0.01)
    }

    @Test
    fun `calculateAverageCycleLength ignores null cycleLength`() {
        val cycles = listOf(
            createCycle(cycleLength = 28),
            createCycle(cycleLength = null),
            createCycle(cycleLength = 32)
        )
        assertEquals(30.0, CycleCalculator.calculateAverageCycleLength(cycles), 0.01)
    }

    @Test
    fun `calculateAverageCycleLength returns population mean when all excluded`() {
        val cycles = listOf(
            createCycle(cycleLength = 28, isExcluded = true),
            createCycle(cycleLength = 30, isExcluded = true)
        )
        assertEquals(PopulationPriors.POPULATION_MEAN_CYCLE, CycleCalculator.calculateAverageCycleLength(cycles), 0.01)
    }

    @Test
    fun `calculateAverageCycleLength handles single cycle`() {
        val cycles = listOf(createCycle(cycleLength = 35))
        assertEquals(35.0, CycleCalculator.calculateAverageCycleLength(cycles), 0.01)
    }

    // --- calculateAveragePeriodLength ---

    @Test
    fun `calculateAveragePeriodLength returns population mean for empty list`() {
        assertEquals(PopulationPriors.POPULATION_MEAN_PERIOD, CycleCalculator.calculateAveragePeriodLength(emptyList()), 0.01)
    }

    @Test
    fun `calculateAveragePeriodLength calculates correctly`() {
        val cycles = listOf(
            createCycle(periodLength = 4),
            createCycle(periodLength = 6),
            createCycle(periodLength = 5)
        )
        assertEquals(5.0, CycleCalculator.calculateAveragePeriodLength(cycles), 0.01)
    }

    @Test
    fun `calculateAveragePeriodLength ignores excluded cycles`() {
        val cycles = listOf(
            createCycle(periodLength = 4, isExcluded = false),
            createCycle(periodLength = 10, isExcluded = true),
            createCycle(periodLength = 6, isExcluded = false)
        )
        assertEquals(5.0, CycleCalculator.calculateAveragePeriodLength(cycles), 0.01)
    }

    @Test
    fun `calculateAveragePeriodLength ignores null periodLength`() {
        val cycles = listOf(
            createCycle(periodLength = 4),
            createCycle(periodLength = null),
            createCycle(periodLength = 6)
        )
        assertEquals(5.0, CycleCalculator.calculateAveragePeriodLength(cycles), 0.01)
    }

    // --- calculateStandardDeviation ---

    @Test
    fun `calculateStandardDeviation returns 0 for single value`() {
        assertEquals(0.0, CycleCalculator.calculateStandardDeviation(listOf(28)), 0.01)
    }

    @Test
    fun `calculateStandardDeviation returns 0 for identical values`() {
        assertEquals(0.0, CycleCalculator.calculateStandardDeviation(listOf(28, 28, 28)), 0.01)
    }

    @Test
    fun `calculateStandardDeviation calculates correctly`() {
        // Values: 26, 28, 30 → mean=28, variance = ((4+0+4)/3) = 2.667, sd ≈ 1.633
        val sd = CycleCalculator.calculateStandardDeviation(listOf(26, 28, 30))
        assertEquals(1.633, sd, 0.01)
    }

    @Test
    fun `calculateStandardDeviation returns 0 for empty list`() {
        assertEquals(0.0, CycleCalculator.calculateStandardDeviation(emptyList()), 0.01)
    }

    @Test
    fun `calculateStandardDeviation handles high variability`() {
        val sd = CycleCalculator.calculateStandardDeviation(listOf(20, 40, 25, 45))
        assertTrue("SD should be significant for highly variable data: $sd", sd > 5.0)
    }

    // --- calculateRegularityScore ---

    @Test
    fun `calculateRegularityScore returns 5 for single value`() {
        assertEquals(5, CycleCalculator.calculateRegularityScore(listOf(28)))
    }

    @Test
    fun `calculateRegularityScore returns 10 for identical cycles`() {
        assertEquals(10, CycleCalculator.calculateRegularityScore(listOf(28, 28, 28, 28)))
    }

    @Test
    fun `calculateRegularityScore returns low score for highly variable cycles`() {
        val score = CycleCalculator.calculateRegularityScore(listOf(20, 40, 22, 45, 18))
        assertTrue("Score should be low for variable cycles: $score", score <= 3)
    }

    @Test
    fun `calculateRegularityScore returns high score for regular cycles`() {
        val score = CycleCalculator.calculateRegularityScore(listOf(28, 29, 28, 27, 28))
        assertTrue("Score should be high for regular cycles: $score", score >= 8)
    }

    @Test
    fun `calculateRegularityScore returns 5 for empty list`() {
        assertEquals(5, CycleCalculator.calculateRegularityScore(emptyList()))
    }

    @Test
    fun `calculateRegularityScore spans full range`() {
        // Perfect regularity
        assertEquals(10, CycleCalculator.calculateRegularityScore(listOf(28, 28, 28)))

        // Moderate
        val moderate = CycleCalculator.calculateRegularityScore(listOf(26, 30, 28, 32))
        assertTrue("Moderate variability should score 5-8: $moderate", moderate in 4..8)

        // High variability
        val low = CycleCalculator.calculateRegularityScore(listOf(20, 45, 22, 50))
        assertTrue("High variability should score 1-3: $low", low in 1..3)
    }

    // --- daysUntilNextPeriod ---

    @Test
    fun `daysUntilNextPeriod calculates correctly for future date`() {
        val cycleStart = LocalDate.now().minusDays(10)
        val result = CycleCalculator.daysUntilNextPeriod(cycleStart, 28)
        assertEquals(18, result)
    }

    @Test
    fun `daysUntilNextPeriod returns negative for past date`() {
        val cycleStart = LocalDate.now().minusDays(35)
        val result = CycleCalculator.daysUntilNextPeriod(cycleStart, 28)
        assertTrue("Should be negative when period is overdue: $result", result < 0)
    }

    @Test
    fun `daysUntilNextPeriod returns 0 when period is today`() {
        val cycleStart = LocalDate.now().minusDays(28)
        val result = CycleCalculator.daysUntilNextPeriod(cycleStart, 28)
        assertEquals(0, result)
    }

    // --- Helper ---

    private var cycleCounter = 0

    private fun createCycle(
        cycleLength: Int? = 28,
        periodLength: Int? = 5,
        isExcluded: Boolean = false
    ): Cycle {
        cycleCounter++
        val startDate = LocalDate.of(2024, 1, 1).plusDays(cycleCounter * 28L)
        return Cycle(
            id = "cycle-$cycleCounter",
            cycleNumber = cycleCounter,
            startDate = startDate,
            periodStartDate = startDate,
            cycleLength = cycleLength,
            periodLength = periodLength,
            isExcluded = isExcluded
        )
    }
}
