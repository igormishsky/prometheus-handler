package com.cyclesync.core.constants

import org.junit.Assert.*
import org.junit.Test

class PopulationPriorsTest {

    // --- getPriorForAge ---

    @Test
    fun `getPriorForAge returns correct prior for teen`() {
        val prior = PopulationPriors.getPriorForAge(16)
        assertEquals(30.8, prior.cycleMean, 0.01)
        assertEquals(9.2, prior.cycleSD, 0.01)
    }

    @Test
    fun `getPriorForAge returns correct prior for age 22`() {
        val prior = PopulationPriors.getPriorForAge(22)
        assertEquals(30.1, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge returns correct prior for age 27`() {
        val prior = PopulationPriors.getPriorForAge(27)
        assertEquals(29.5, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge returns correct prior for age 32`() {
        val prior = PopulationPriors.getPriorForAge(32)
        assertEquals(29.1, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge returns correct prior for age 37`() {
        val prior = PopulationPriors.getPriorForAge(37)
        assertEquals(28.5, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge returns correct prior for age 42`() {
        val prior = PopulationPriors.getPriorForAge(42)
        assertEquals(27.8, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge returns correct prior for age 48`() {
        val prior = PopulationPriors.getPriorForAge(48)
        assertEquals(28.4, prior.cycleMean, 0.01)
        assertEquals(9.8, prior.cycleSD, 0.01)
    }

    @Test
    fun `getPriorForAge boundary at age 19 uses teen prior`() {
        val prior = PopulationPriors.getPriorForAge(19)
        assertEquals(30.8, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge boundary at age 20 uses 20-24 prior`() {
        val prior = PopulationPriors.getPriorForAge(20)
        assertEquals(30.1, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge very young age uses teen prior`() {
        val prior = PopulationPriors.getPriorForAge(12)
        assertEquals(30.8, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge very old age uses 45-50 prior`() {
        val prior = PopulationPriors.getPriorForAge(55)
        assertEquals(28.4, prior.cycleMean, 0.01)
    }

    @Test
    fun `getPriorForAge all priors have positive SD`() {
        val ages = listOf(16, 22, 27, 32, 37, 42, 48)
        for (age in ages) {
            val prior = PopulationPriors.getPriorForAge(age)
            assertTrue("SD should be positive for age $age", prior.cycleSD > 0)
        }
    }

    @Test
    fun `getPriorForAge all priors have reasonable luteal length`() {
        val ages = listOf(16, 22, 27, 32, 37, 42, 48)
        for (age in ages) {
            val prior = PopulationPriors.getPriorForAge(age)
            assertTrue("Luteal mean should be 10-16 for age $age: ${prior.lutealMean}",
                prior.lutealMean in 10.0..16.0)
        }
    }

    // --- getAdjustedPrior ---

    @Test
    fun `getAdjustedPrior with null age and BMI uses population defaults`() {
        val prior = PopulationPriors.getAdjustedPrior(null, null)
        assertEquals(PopulationPriors.POPULATION_MEAN_CYCLE, prior.cycleMean, 0.01)
        assertEquals(PopulationPriors.POPULATION_SD_CYCLE, prior.cycleSD, 0.01)
    }

    @Test
    fun `getAdjustedPrior with age only uses age prior`() {
        val prior = PopulationPriors.getAdjustedPrior(30, null)
        assertEquals(29.1, prior.cycleMean, 0.01) // 30-34 group
    }

    @Test
    fun `getAdjustedPrior with normal BMI makes no adjustment`() {
        val prior = PopulationPriors.getAdjustedPrior(30, "normal")
        assertEquals(29.1, prior.cycleMean, 0.01) // No BMI adjustment
        assertEquals(PopulationPriors.agePriors["30-34"]!!.cycleSD, prior.cycleSD, 0.01) // Multiplier 1.0
    }

    @Test
    fun `getAdjustedPrior with obese BMI increases cycle length and variance`() {
        val basePrior = PopulationPriors.getPriorForAge(30)
        val adjusted = PopulationPriors.getAdjustedPrior(30, "obese")

        assertEquals(basePrior.cycleMean + 0.9, adjusted.cycleMean, 0.01)
        assertEquals(basePrior.cycleSD * 1.3, adjusted.cycleSD, 0.01)
    }

    @Test
    fun `getAdjustedPrior with underweight BMI adjusts correctly`() {
        val basePrior = PopulationPriors.getPriorForAge(25)
        val adjusted = PopulationPriors.getAdjustedPrior(25, "underweight")

        assertEquals(basePrior.cycleMean + 0.5, adjusted.cycleMean, 0.01)
        assertEquals(basePrior.cycleSD * 1.1, adjusted.cycleSD, 0.01)
    }

    @Test
    fun `getAdjustedPrior with overweight BMI adjusts correctly`() {
        val basePrior = PopulationPriors.getPriorForAge(35)
        val adjusted = PopulationPriors.getAdjustedPrior(35, "overweight")

        assertEquals(basePrior.cycleMean + 0.3, adjusted.cycleMean, 0.01)
        assertEquals(basePrior.cycleSD * 1.15, adjusted.cycleSD, 0.01)
    }

    @Test
    fun `getAdjustedPrior with unknown BMI category uses no adjustment`() {
        val basePrior = PopulationPriors.getPriorForAge(30)
        val adjusted = PopulationPriors.getAdjustedPrior(30, "unknown")

        assertEquals(basePrior.cycleMean, adjusted.cycleMean, 0.01)
        assertEquals(basePrior.cycleSD, adjusted.cycleSD, 0.01)
    }

    @Test
    fun `getAdjustedPrior does not change luteal mean for BMI`() {
        val basePrior = PopulationPriors.getPriorForAge(30)
        val adjusted = PopulationPriors.getAdjustedPrior(30, "obese")

        assertEquals("Luteal mean should not change with BMI",
            basePrior.lutealMean, adjusted.lutealMean, 0.01)
    }

    @Test
    fun `getAdjustedPrior adjusts follicular mean with BMI`() {
        val basePrior = PopulationPriors.getPriorForAge(30)
        val adjusted = PopulationPriors.getAdjustedPrior(30, "obese")

        assertEquals("Follicular mean should increase by BMI adjustment",
            basePrior.follicularMean + 0.9, adjusted.follicularMean, 0.01)
    }

    // --- Constants validation ---

    @Test
    fun `population constants are reasonable`() {
        assertTrue(PopulationPriors.POPULATION_MEAN_CYCLE > 25)
        assertTrue(PopulationPriors.POPULATION_MEAN_CYCLE < 35)
        assertTrue(PopulationPriors.POPULATION_SD_CYCLE > 0)
        assertTrue(PopulationPriors.POPULATION_MEAN_PERIOD > 3)
        assertTrue(PopulationPriors.POPULATION_MEAN_PERIOD < 8)
    }

    @Test
    fun `fertile window constants are reasonable`() {
        assertEquals(5, PopulationPriors.FERTILE_WINDOW_START_BEFORE_OVULATION)
        assertEquals(1, PopulationPriors.FERTILE_WINDOW_END_AFTER_OVULATION)
    }

    @Test
    fun `PMS window constants are reasonable`() {
        assertEquals(7, PopulationPriors.PMS_WINDOW_START_BEFORE_PERIOD)
        assertEquals(1, PopulationPriors.PMS_WINDOW_END_BEFORE_PERIOD)
    }

    @Test
    fun `transition cycles constant is positive`() {
        assertTrue(PopulationPriors.TRANSITION_CYCLES > 0)
    }

    @Test
    fun `min population weight is between 0 and 1`() {
        assertTrue(PopulationPriors.MIN_POPULATION_WEIGHT > 0.0)
        assertTrue(PopulationPriors.MIN_POPULATION_WEIGHT < 1.0)
    }

    @Test
    fun `conception probability sums roughly to expected range`() {
        val total = PopulationPriors.conceptionProbabilityByDay.values.sum()
        assertTrue("Conception probability sum should be < 1.0: $total", total < 1.0)
        assertTrue("Conception probability sum should be > 0.5: $total", total > 0.5)
    }

    @Test
    fun `agePriors map has all expected keys`() {
        val expectedKeys = listOf("15-19", "20-24", "25-29", "30-34", "35-39", "40-44", "45-50")
        for (key in expectedKeys) {
            assertNotNull("agePriors should contain key $key", PopulationPriors.agePriors[key])
        }
    }

    @Test
    fun `bmiCycleAdjustment has all expected categories`() {
        val expectedKeys = listOf("underweight", "normal", "overweight", "obese")
        for (key in expectedKeys) {
            assertNotNull("bmiCycleAdjustment should contain $key", PopulationPriors.bmiCycleAdjustment[key])
        }
    }

    @Test
    fun `normal BMI has zero cycle adjustment`() {
        assertEquals(0.0, PopulationPriors.bmiCycleAdjustment["normal"]!!, 0.001)
    }

    @Test
    fun `normal BMI has unit variance multiplier`() {
        assertEquals(1.0, PopulationPriors.bmiVarianceMultiplier["normal"]!!, 0.001)
    }
}
