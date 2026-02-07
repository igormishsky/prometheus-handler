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
    fun `cycleLengthRange zero when identical`() {
        val analysis = createAnalysis(longestCycle = 28, shortestCycle = 28)
        assertEquals(0, analysis.cycleLengthRange)
    }

    @Test
    fun `isRegular returns true for high score`() {
        assertTrue(createAnalysis(regularityScore = 8).isRegular)
        assertTrue(createAnalysis(regularityScore = 10).isRegular)
        assertTrue(createAnalysis(regularityScore = 7).isRegular)
    }

    @Test
    fun `isRegular returns false for low score`() {
        assertFalse(createAnalysis(regularityScore = 5).isRegular)
        assertFalse(createAnalysis(regularityScore = 2).isRegular)
        assertFalse(createAnalysis(regularityScore = 6).isRegular)
    }

    @Test
    fun `isIrregular returns true for very low score`() {
        assertTrue(createAnalysis(regularityScore = 1).isIrregular)
        assertTrue(createAnalysis(regularityScore = 2).isIrregular)
        assertTrue(createAnalysis(regularityScore = 3).isIrregular)
    }

    @Test
    fun `isIrregular returns false for high score`() {
        assertFalse(createAnalysis(regularityScore = 7).isIrregular)
        assertFalse(createAnalysis(regularityScore = 4).isIrregular)
    }

    @Test
    fun `hasEnoughDataForTrend requires 6 cycles`() {
        assertTrue(createAnalysis(cycleLengths = listOf(28, 28, 29, 28, 30, 28)).hasEnoughDataForTrend)
        assertTrue(createAnalysis(cycleLengths = listOf(1, 2, 3, 4, 5, 6, 7)).hasEnoughDataForTrend)
    }

    @Test
    fun `hasEnoughDataForTrend false for few cycles`() {
        assertFalse(createAnalysis(cycleLengths = listOf(28, 28, 29)).hasEnoughDataForTrend)
        assertFalse(createAnalysis(cycleLengths = emptyList()).hasEnoughDataForTrend)
        assertFalse(createAnalysis(cycleLengths = listOf(28, 28, 29, 28, 30)).hasEnoughDataForTrend)
    }

    @Test
    fun `regularityDescription returns Very Regular for score 9-10`() {
        assertEquals("Very Regular", createAnalysis(regularityScore = 10).regularityDescription)
        assertEquals("Very Regular", createAnalysis(regularityScore = 9).regularityDescription)
    }

    @Test
    fun `regularityDescription returns Regular for score 7-8`() {
        assertEquals("Regular", createAnalysis(regularityScore = 8).regularityDescription)
        assertEquals("Regular", createAnalysis(regularityScore = 7).regularityDescription)
    }

    @Test
    fun `regularityDescription returns Somewhat Regular for score 5-6`() {
        assertEquals("Somewhat Regular", createAnalysis(regularityScore = 6).regularityDescription)
        assertEquals("Somewhat Regular", createAnalysis(regularityScore = 5).regularityDescription)
    }

    @Test
    fun `regularityDescription returns Irregular for score 3-4`() {
        assertEquals("Irregular", createAnalysis(regularityScore = 4).regularityDescription)
        assertEquals("Irregular", createAnalysis(regularityScore = 3).regularityDescription)
    }

    @Test
    fun `regularityDescription returns Very Irregular for score 1-2`() {
        assertEquals("Very Irregular", createAnalysis(regularityScore = 2).regularityDescription)
        assertEquals("Very Irregular", createAnalysis(regularityScore = 1).regularityDescription)
    }

    @Test
    fun `trendDescription returns correct text for LENGTHENING`() {
        assertEquals("Cycles are getting longer", createAnalysis(trend = Trend.LENGTHENING).trendDescription)
    }

    @Test
    fun `trendDescription returns correct text for SHORTENING`() {
        assertEquals("Cycles are getting shorter", createAnalysis(trend = Trend.SHORTENING).trendDescription)
    }

    @Test
    fun `trendDescription returns correct text for STABLE`() {
        assertEquals("Cycles are stable", createAnalysis(trend = Trend.STABLE).trendDescription)
    }

    @Test
    fun `Trend has correct display names`() {
        assertEquals("Lengthening", Trend.LENGTHENING.displayName)
        assertEquals("Shortening", Trend.SHORTENING.displayName)
        assertEquals("Stable", Trend.STABLE.displayName)
    }

    @Test
    fun `all Trend values have non-empty display names`() {
        Trend.entries.forEach { trend ->
            assertTrue("${trend.name} should have non-empty displayName", trend.displayName.isNotBlank())
        }
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
