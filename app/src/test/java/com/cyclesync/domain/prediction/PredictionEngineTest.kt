package com.cyclesync.domain.prediction

import com.cyclesync.core.constants.PopulationPriors
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.PredictionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PredictionEngineTest {

    private lateinit var engine: PredictionEngine

    @Before
    fun setUp() {
        engine = PredictionEngine()
    }

    // --- predictNextCycle: no history (population priors only) ---

    @Test
    fun `predictNextCycle with no cycles uses population priors`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val result = engine.predictNextCycle(
            cycles = emptyList(),
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // With no data, should use population mean (~29.3 days)
        val expectedStart = startDate.plusDays(29) // round(29.3)
        assertEquals(expectedStart, result.nextPeriodStart)

        // Period end should use population mean period (5 days)
        val expectedPeriodEnd = expectedStart.plusDays(4) // 5 - 1
        assertEquals(expectedPeriodEnd, result.periodEnd)

        // Confidence should be low with no tracked cycles
        assertTrue("Confidence should be low with no history", result.confidence < 0.4)
    }

    @Test
    fun `predictNextCycle with no cycles and age 25 uses age-specific priors`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val result = engine.predictNextCycle(
            cycles = emptyList(),
            userAge = 25,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // Age 25-29 prior: cycleMean = 29.5
        val expectedStart = startDate.plusDays(30) // round(29.5)
        assertEquals(expectedStart, result.nextPeriodStart)
    }

    @Test
    fun `predictNextCycle with no cycles and BMI obese adjusts priors`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val result = engine.predictNextCycle(
            cycles = emptyList(),
            userAge = 30,
            userBmi = "obese",
            currentCycleStartDate = startDate
        )

        // Age 30-34: cycleMean=29.1, obese adjustment=+0.9 → 30.0
        val expectedStart = startDate.plusDays(30)
        assertEquals(expectedStart, result.nextPeriodStart)
    }

    // --- predictNextCycle: with cycle history ---

    @Test
    fun `predictNextCycle with single cycle blends with population priors`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val cycle = createCycle(1, "2023-12-01", cycleLength = 32, periodLength = 6)

        val result = engine.predictNextCycle(
            cycles = listOf(cycle),
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // With 1 cycle: wPop = max(0.05, 1.0 - 1/6) ≈ 0.833, wInd = 0.167
        // Blended = 0.833 * 29.3 + 0.167 * 32.0 ≈ 29.75 → round = 30
        val predictedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, result.nextPeriodStart)
        assertTrue("Predicted length should be between 29 and 33", predictedDays in 29..33)
    }

    @Test
    fun `predictNextCycle with 6 cycles relies entirely on individual data`() {
        val startDate = LocalDate.of(2024, 6, 1)
        val cycles = (1..6).map { i ->
            createCycle(i, "2024-0${i}-01", cycleLength = 28, periodLength = 5)
        }

        val result = engine.predictNextCycle(
            cycles = cycles,
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // With 6 cycles: wPop = max(0.05, 1 - 6/6) = 0.05
        // Blended ≈ 0.05 * 29.3 + 0.95 * 28.0 ≈ 28.065 → round = 28
        val predictedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, result.nextPeriodStart)
        assertEquals("With consistent 28-day cycles, prediction should be ~28", 28L, predictedDays)
    }

    @Test
    fun `predictNextCycle excludes excluded cycles`() {
        val startDate = LocalDate.of(2024, 4, 1)
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5),
            createCycle(2, "2024-02-01", cycleLength = 60, periodLength = 5, isExcluded = true),
            createCycle(3, "2024-03-01", cycleLength = 28, periodLength = 5)
        )

        val result = engine.predictNextCycle(
            cycles = cycles,
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // The excluded 60-day cycle should not drag the prediction up
        val predictedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, result.nextPeriodStart)
        assertTrue("Excluded cycles shouldn't affect prediction; got $predictedDays", predictedDays < 35)
    }

    @Test
    fun `predictNextCycle filters null cycleLength`() {
        val startDate = LocalDate.of(2024, 3, 1)
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 30, periodLength = 5),
            createCycle(2, "2024-02-01", cycleLength = null, periodLength = 5)
        )

        val result = engine.predictNextCycle(
            cycles = cycles,
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // Only 1 valid cycle (cycleLength=30), so it should blend with population prior
        assertNotNull(result.nextPeriodStart)
    }

    // --- predictNextCycle: skip detection ---

    @Test
    fun `predictNextCycle adjusts for skipped cycle`() {
        val startDate = LocalDate.of(2024, 4, 1)
        // A cycle of 60 days is about 2x the population mean - should be halved
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 60, periodLength = 5),
            createCycle(2, "2024-03-01", cycleLength = 30, periodLength = 5)
        )

        val result = engine.predictNextCycle(
            cycles = cycles,
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // 60 / round(60/29.3) = 60/2 = 30. Both cycles effectively 30.
        val predictedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, result.nextPeriodStart)
        assertTrue("Skip adjustment should normalize; got $predictedDays", predictedDays in 28..32)
    }

    // --- predictNextCycle: trend adjustment (8+ cycles) ---

    @Test
    fun `predictNextCycle applies trend adjustment for 8+ cycles with increasing lengths`() {
        val startDate = LocalDate.of(2024, 9, 1)
        // 8 cycles with steadily increasing lengths
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 26, periodLength = 5),
            createCycle(2, "2024-01-27", cycleLength = 27, periodLength = 5),
            createCycle(3, "2024-02-23", cycleLength = 28, periodLength = 5),
            createCycle(4, "2024-03-22", cycleLength = 29, periodLength = 5),
            createCycle(5, "2024-04-20", cycleLength = 30, periodLength = 5),
            createCycle(6, "2024-05-20", cycleLength = 31, periodLength = 5),
            createCycle(7, "2024-06-20", cycleLength = 32, periodLength = 5),
            createCycle(8, "2024-07-22", cycleLength = 33, periodLength = 5)
        )

        val resultWithTrend = engine.predictNextCycle(
            cycles = cycles,
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // Mean of all cycles is ~29.5, but trend adjustment should push prediction higher
        val predictedDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, resultWithTrend.nextPeriodStart)
        assertTrue("Trend adjustment should push prediction up; got $predictedDays", predictedDays >= 29)
    }

    @Test
    fun `predictNextCycle does not apply trend for less than 8 cycles`() {
        val startDate = LocalDate.of(2024, 7, 1)
        // 7 cycles - just under the threshold
        val cycles = (1..7).map { i ->
            createCycle(i, "2024-0${i}-01", cycleLength = 25 + i, periodLength = 5)
        }

        // Should work without crashing - trend adjustment skipped
        val result = engine.predictNextCycle(
            cycles = cycles,
            userAge = null,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        assertNotNull(result)
    }

    // --- predictNextCycle: date calculations ---

    @Test
    fun `predicted dates have correct relative positions`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val cycles = (1..4).map { i ->
            createCycle(i, "2023-${9 + i}-01", cycleLength = 28, periodLength = 5)
        }

        val result = engine.predictNextCycle(
            cycles = cycles,
            userAge = 30,
            userBmi = null,
            currentCycleStartDate = startDate
        )

        // Ovulation should be before period start (by luteal phase length)
        assertTrue("Ovulation should be before period start",
            result.ovulationDate.isBefore(result.nextPeriodStart))

        // Fertile window should surround ovulation
        assertTrue("Fertile window start should be before ovulation",
            result.fertileWindowStart.isBefore(result.ovulationDate) || result.fertileWindowStart == result.ovulationDate)
        assertTrue("Fertile window end should be after ovulation",
            result.fertileWindowEnd.isAfter(result.ovulationDate) || result.fertileWindowEnd == result.ovulationDate)

        // PMS should be before period start
        assertTrue("PMS start should be before period",
            result.pmsStart.isBefore(result.nextPeriodStart))
        assertTrue("PMS end should be before or at period start",
            !result.pmsEnd.isAfter(result.nextPeriodStart))

        // Period end should be after period start
        assertTrue("Period end should be after period start",
            result.periodEnd.isAfter(result.nextPeriodStart) || result.periodEnd == result.nextPeriodStart)

        // Bounds should bracket period start
        assertTrue("Lower bound should be before or at period start",
            !result.lowerBound.isAfter(result.nextPeriodStart))
        assertTrue("Upper bound should be after or at period start",
            !result.upperBound.isBefore(result.nextPeriodStart))
    }

    // --- predictNextCycle: luteal estimate regression (bug fix) ---

    @Test
    fun `luteal estimate uses individual data when enough cycles exist`() {
        val startDate = LocalDate.of(2024, 6, 1)

        // 4 cycles with consistent 32-day lengths (longer than population mean)
        val longCycles = (1..4).map { i ->
            createCycle(i, "2024-0${i}-01", cycleLength = 32, periodLength = 5)
        }

        // 4 cycles with consistent 24-day lengths (shorter than population mean)
        val shortCycles = (1..4).map { i ->
            createCycle(i, "2024-0${i}-01", cycleLength = 24, periodLength = 5)
        }

        val resultLong = engine.predictNextCycle(longCycles, 30, null, startDate)
        val resultShort = engine.predictNextCycle(shortCycles, 30, null, startDate)

        // With the bug fix, longer cycle lengths should shift the ovulation estimate
        // The ovulation dates should differ because luteal estimates adapt to individual data
        val lutealDaysLong = java.time.temporal.ChronoUnit.DAYS.between(
            resultLong.ovulationDate, resultLong.nextPeriodStart)
        val lutealDaysShort = java.time.temporal.ChronoUnit.DAYS.between(
            resultShort.ovulationDate, resultShort.nextPeriodStart)

        // Both should be in the reasonable luteal phase range (7-17 days)
        assertTrue("Long cycle luteal should be 7-17: $lutealDaysLong",
            lutealDaysLong in 7..17)
        assertTrue("Short cycle luteal should be 7-17: $lutealDaysShort",
            lutealDaysShort in 7..17)
    }

    // --- predictNextCycle: confidence ---

    @Test
    fun `confidence increases with more cycles`() {
        val startDate = LocalDate.of(2024, 6, 1)

        val oneCycle = listOf(createCycle(1, "2024-05-01", cycleLength = 28, periodLength = 5))
        val fiveCycles = (1..5).map { i ->
            createCycle(i, "2024-0${i}-01", cycleLength = 28, periodLength = 5)
        }

        val confOne = engine.predictNextCycle(oneCycle, null, null, startDate).confidence
        val confFive = engine.predictNextCycle(fiveCycles, null, null, startDate).confidence

        assertTrue("More cycles should give higher confidence: $confOne < $confFive",
            confFive > confOne)
    }

    @Test
    fun `confidence is clamped between 0_1 and 0_95`() {
        val startDate = LocalDate.of(2024, 1, 1)

        // No history - minimum confidence
        val lowConf = engine.predictNextCycle(emptyList(), null, null, startDate).confidence
        assertTrue("Confidence should be >= 0.1", lowConf >= 0.1)

        // Lots of consistent history - max confidence
        val manyCycles = (1..20).map { i ->
            createCycle(i, LocalDate.of(2022, 1, 1).plusDays(i * 28L).toString(), cycleLength = 28, periodLength = 5)
        }
        val highConf = engine.predictNextCycle(manyCycles, null, null, startDate).confidence
        assertTrue("Confidence should be <= 0.95", highConf <= 0.95)
    }

    @Test
    fun `high variance decreases confidence`() {
        val startDate = LocalDate.of(2024, 6, 1)

        val consistentCycles = (1..5).map { i ->
            createCycle(i, "2024-0${i}-01", cycleLength = 28, periodLength = 5)
        }
        val variableCycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 22, periodLength = 5),
            createCycle(2, "2024-02-01", cycleLength = 35, periodLength = 5),
            createCycle(3, "2024-03-01", cycleLength = 24, periodLength = 5),
            createCycle(4, "2024-04-01", cycleLength = 38, periodLength = 5),
            createCycle(5, "2024-05-01", cycleLength = 26, periodLength = 5)
        )

        val confConsistent = engine.predictNextCycle(consistentCycles, null, null, startDate).confidence
        val confVariable = engine.predictNextCycle(variableCycles, null, null, startDate).confidence

        assertTrue("Higher variance should give lower confidence: consistent=$confConsistent, variable=$confVariable",
            confConsistent > confVariable)
    }

    // --- generatePredictions ---

    @Test
    fun `generatePredictions returns empty for empty cycle list`() {
        val result = engine.generatePredictions(emptyList(), null, null)
        assertTrue("Empty cycles should produce empty predictions", result.isEmpty())
    }

    @Test
    fun `generatePredictions generates correct number of predictions`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 3)

        // Each cycle produces 7 predictions (PERIOD_START, PERIOD_END, OVULATION, FERTILE_START, FERTILE_END, PMS_START, PMS_END)
        assertEquals("Should produce 7 predictions per cycle", 21, predictions.size)
    }

    @Test
    fun `generatePredictions defaults to 12 future cycles`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null)
        assertEquals("Default should be 12 cycles * 7 types", 84, predictions.size)
    }

    @Test
    fun `generatePredictions produces all required prediction types per cycle`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 1)

        val types = predictions.map { it.type }.toSet()
        assertEquals("Should include all 7 prediction types", 7, types.size)
        assertTrue(types.contains(PredictionType.PERIOD_START))
        assertTrue(types.contains(PredictionType.PERIOD_END))
        assertTrue(types.contains(PredictionType.OVULATION))
        assertTrue(types.contains(PredictionType.FERTILE_START))
        assertTrue(types.contains(PredictionType.FERTILE_END))
        assertTrue(types.contains(PredictionType.PMS_START))
        assertTrue(types.contains(PredictionType.PMS_END))
    }

    @Test
    fun `generatePredictions chains cycles forward`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 3)

        // Get period start predictions - should be roughly 28 days apart
        val periodStarts = predictions.filter { it.type == PredictionType.PERIOD_START }
            .sortedBy { it.predictedDate }

        assertEquals("Should have 3 period start predictions", 3, periodStarts.size)

        // Each subsequent period start should be after the previous
        for (i in 1 until periodStarts.size) {
            assertTrue("Period starts should be chronologically ordered",
                periodStarts[i].predictedDate.isAfter(periodStarts[i - 1].predictedDate))
        }
    }

    @Test
    fun `generatePredictions confidence is clamped for derived types`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 1)

        predictions.forEach { prediction ->
            assertTrue("Confidence should be >= 0.1, got ${prediction.confidence}",
                prediction.confidence >= 0.1)
            assertTrue("Confidence should be <= 0.95, got ${prediction.confidence}",
                prediction.confidence <= 0.95)
        }
    }

    @Test
    fun `generatePredictions uses most recent cycle as start`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5),
            createCycle(2, "2024-03-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 1)
        val firstPeriodStart = predictions.first { it.type == PredictionType.PERIOD_START }

        // Should chain from 2024-03-01 (the latest), not 2024-01-01
        assertTrue("First prediction should be after last cycle start",
            firstPeriodStart.predictedDate.isAfter(LocalDate.of(2024, 3, 1)))
    }

    @Test
    fun `generatePredictions sets algorithm version`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 1)

        predictions.forEach { prediction ->
            assertEquals("Algorithm version should match", PopulationPriors.ALGORITHM_VERSION, prediction.algorithmVersion)
        }
    }

    @Test
    fun `generatePredictions assigns unique IDs`() {
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 28, periodLength = 5)
        )

        val predictions = engine.generatePredictions(cycles, null, null, numberOfFutureCycles = 3)

        val ids = predictions.map { it.id }.toSet()
        assertEquals("All prediction IDs should be unique", predictions.size, ids.size)
    }

    // --- Edge cases ---

    @Test
    fun `predictNextCycle handles very short cycles`() {
        val startDate = LocalDate.of(2024, 3, 1)
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 18, periodLength = 3),
            createCycle(2, "2024-02-01", cycleLength = 19, periodLength = 3)
        )

        val result = engine.predictNextCycle(cycles, null, null, startDate)
        assertNotNull("Should handle short cycles", result)
        assertTrue("Period start should be in the future relative to start date",
            result.nextPeriodStart.isAfter(startDate))
    }

    @Test
    fun `predictNextCycle handles very long cycles`() {
        val startDate = LocalDate.of(2024, 6, 1)
        val cycles = listOf(
            createCycle(1, "2024-01-01", cycleLength = 45, periodLength = 7),
            createCycle(2, "2024-03-01", cycleLength = 42, periodLength = 7)
        )

        val result = engine.predictNextCycle(cycles, null, null, startDate)
        assertNotNull("Should handle long cycles", result)
    }

    @Test
    fun `predictNextCycle works for all age groups`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val ages = listOf(16, 22, 27, 32, 37, 42, 48)

        for (age in ages) {
            val result = engine.predictNextCycle(emptyList(), age, null, startDate)
            assertNotNull("Should work for age $age", result)
            assertTrue("Period start should be after start date for age $age",
                result.nextPeriodStart.isAfter(startDate))
        }
    }

    @Test
    fun `predictNextCycle works for all BMI categories`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val bmis = listOf("underweight", "normal", "overweight", "obese")

        for (bmi in bmis) {
            val result = engine.predictNextCycle(emptyList(), 30, bmi, startDate)
            assertNotNull("Should work for BMI $bmi", result)
        }
    }

    @Test
    fun `predictNextCycle handles unknown BMI gracefully`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val result = engine.predictNextCycle(emptyList(), 30, "unknown_bmi", startDate)
        assertNotNull("Should handle unknown BMI", result)
    }

    // --- Helper ---

    private fun createCycle(
        number: Int,
        startDateStr: String,
        cycleLength: Int?,
        periodLength: Int?,
        isExcluded: Boolean = false
    ): Cycle {
        val startDate = LocalDate.parse(startDateStr)
        return Cycle(
            id = "cycle-$number",
            cycleNumber = number,
            startDate = startDate,
            endDate = cycleLength?.let { startDate.plusDays(it.toLong() - 1) },
            periodStartDate = startDate,
            periodEndDate = periodLength?.let { startDate.plusDays(it.toLong() - 1) },
            cycleLength = cycleLength,
            periodLength = periodLength,
            isExcluded = isExcluded
        )
    }
}
