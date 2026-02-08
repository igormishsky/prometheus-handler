package com.cyclesync.domain.entity

import org.junit.Assert.*
import org.junit.Test

class CycleAnalysisTest {

    @Test
    fun `cycleLengthRange calculates correctly`() {
        val analysis = createAnalysis(longestCycle = 35, shortestCycle = 25)
        assertEquals(10, analysis.cycleLengthRange)
    }

    @Test
    fun `isRegular returns true for high score`() {
        assertTrue(createAnalysis(regularityScore = 8).isRegular)
        assertTrue(createAnalysis(regularityScore = 10).isRegular)
    }

    @Test
    fun `isRegular returns false for low score`() {
        assertFalse(createAnalysis(regularityScore = 5).isRegular)
        assertFalse(createAnalysis(regularityScore = 2).isRegular)
    }

    @Test
    fun `regularityDescription returns correct text`() {
        assertEquals("Very Regular", createAnalysis(regularityScore = 10).regularityDescription)
        assertEquals("Regular", createAnalysis(regularityScore = 8).regularityDescription)
        assertEquals("Somewhat Regular", createAnalysis(regularityScore = 5).regularityDescription)
        assertEquals("Irregular", createAnalysis(regularityScore = 3).regularityDescription)
        assertEquals("Very Irregular", createAnalysis(regularityScore = 1).regularityDescription)
    }

    @Test
    fun `trendDescription returns correct text`() {
        assertEquals("Cycles are getting longer", createAnalysis(trend = Trend.LENGTHENING).trendDescription)
        assertEquals("Cycles are getting shorter", createAnalysis(trend = Trend.SHORTENING).trendDescription)
        assertEquals("Cycles are stable", createAnalysis(trend = Trend.STABLE).trendDescription)
    }

    @Test
    fun `Trend has correct display names`() {
        assertEquals("Lengthening", Trend.LENGTHENING.displayName)
        assertEquals("Shortening", Trend.SHORTENING.displayName)
        assertEquals("Stable", Trend.STABLE.displayName)
    }

    private fun createAnalysis(
        longestCycle: Int = 30,
        shortestCycle: Int = 26,
        regularityScore: Int = 7,
        cycleLengths: List<Int> = emptyList(),
        trend: Trend = Trend.STABLE
    ) = CycleAnalysis(
        averageCycleLength = 28.0,
        averagePeriodLength = 5.0,
        cycleLengthVariability = 2.0,
        longestCycle = longestCycle,
        shortestCycle = shortestCycle,
        totalCyclesTracked = 10,
        currentStreak = 5,
        regularityScore = regularityScore,
        cycleLengthTrend = trend,
        cycleLengths = cycleLengths
    )
}
