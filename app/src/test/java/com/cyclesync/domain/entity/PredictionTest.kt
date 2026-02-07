package com.cyclesync.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PredictionTest {

    @Test
    fun `isHighConfidence returns true when confidence above 0_7`() {
        val pred = createPrediction(confidence = 0.8)
        assertTrue(pred.isHighConfidence)
    }

    @Test
    fun `isHighConfidence returns false when confidence below 0_7`() {
        val pred = createPrediction(confidence = 0.5)
        assertFalse(pred.isHighConfidence)
    }

    @Test
    fun `isMediumConfidence returns true for mid-range`() {
        val pred = createPrediction(confidence = 0.5)
        assertTrue(pred.isMediumConfidence)
    }

    @Test
    fun `isLowConfidence returns true when confidence below 0_4`() {
        val pred = createPrediction(confidence = 0.2)
        assertTrue(pred.isLowConfidence)
    }

    @Test
    fun `confidencePercent converts correctly`() {
        assertEquals(80, createPrediction(confidence = 0.8).confidencePercent)
        assertEquals(50, createPrediction(confidence = 0.5).confidencePercent)
        assertEquals(10, createPrediction(confidence = 0.1).confidencePercent)
    }

    @Test
    fun `confidencePercent clamped to 0-100`() {
        assertTrue(createPrediction(confidence = 1.5).confidencePercent <= 100)
        assertTrue(createPrediction(confidence = -0.5).confidencePercent >= 0)
    }

    @Test
    fun `isPastPrediction returns true for past dates`() {
        val pred = createPrediction(predictedDate = LocalDate.now().minusDays(1))
        assertTrue(pred.isPastPrediction)
    }

    @Test
    fun `isFuturePrediction returns true for future dates`() {
        val pred = createPrediction(predictedDate = LocalDate.now().plusDays(1))
        assertTrue(pred.isFuturePrediction)
    }

    @Test
    fun `isFuturePrediction returns true for today`() {
        val pred = createPrediction(predictedDate = LocalDate.now())
        assertTrue(pred.isFuturePrediction)
    }

    @Test
    fun `uncertaintyDays calculates correctly`() {
        val pred = createPrediction(
            lowerBound = LocalDate.of(2024, 1, 25),
            upperBound = LocalDate.of(2024, 2, 2)
        )
        assertEquals(8L, pred.uncertaintyDays)
    }

    @Test
    fun `uncertaintyDays returns null when bounds missing`() {
        val pred = createPrediction(lowerBound = null, upperBound = null)
        assertNull(pred.uncertaintyDays)
    }

    @Test
    fun `PredictionType fromNameOrNull returns correct type`() {
        assertEquals(PredictionType.PERIOD_START, PredictionType.fromNameOrNull("PERIOD_START"))
        assertEquals(PredictionType.OVULATION, PredictionType.fromNameOrNull("ovulation"))
    }

    @Test
    fun `PredictionType fromNameOrNull returns null for invalid`() {
        assertNull(PredictionType.fromNameOrNull("invalid"))
        assertNull(PredictionType.fromNameOrNull(null))
        assertNull(PredictionType.fromNameOrNull(""))
    }

    @Test
    fun `all PredictionTypes have non-empty display names`() {
        PredictionType.entries.forEach { type ->
            assertTrue("${type.name} should have non-empty displayName", type.displayName.isNotBlank())
        }
    }

    private fun createPrediction(
        confidence: Double = 0.5,
        predictedDate: LocalDate = LocalDate.of(2024, 2, 1),
        lowerBound: LocalDate? = null,
        upperBound: LocalDate? = null
    ) = Prediction(
        id = "test-id",
        type = PredictionType.PERIOD_START,
        predictedDate = predictedDate,
        confidence = confidence,
        lowerBound = lowerBound,
        upperBound = upperBound,
        algorithmVersion = "1.0"
    )
}
