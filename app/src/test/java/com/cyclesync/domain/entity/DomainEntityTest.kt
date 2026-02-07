package com.cyclesync.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DomainEntityTest {

    // --- Cycle data class ---

    @Test
    fun `Cycle has correct default values`() {
        val cycle = Cycle(
            id = "test",
            cycleNumber = 1,
            startDate = LocalDate.of(2024, 1, 1),
            periodStartDate = LocalDate.of(2024, 1, 1)
        )

        assertNull(cycle.endDate)
        assertNull(cycle.periodEndDate)
        assertNull(cycle.cycleLength)
        assertNull(cycle.periodLength)
        assertFalse(cycle.isExcluded)
        assertNull(cycle.notes)
    }

    @Test
    fun `Cycle copy works correctly`() {
        val original = Cycle(
            id = "test",
            cycleNumber = 1,
            startDate = LocalDate.of(2024, 1, 1),
            periodStartDate = LocalDate.of(2024, 1, 1),
            cycleLength = 28
        )

        val modified = original.copy(cycleLength = 30, isExcluded = true)

        assertEquals("test", modified.id)
        assertEquals(30, modified.cycleLength)
        assertTrue(modified.isExcluded)
        assertEquals(28, original.cycleLength) // Original unchanged
    }

    // --- Prediction data class ---

    @Test
    fun `Prediction has correct default values`() {
        val prediction = Prediction(
            id = "pred",
            type = PredictionType.PERIOD_START,
            predictedDate = LocalDate.of(2024, 2, 1),
            confidence = 0.8,
            algorithmVersion = "1.0"
        )

        assertNull(prediction.cycleId)
        assertNull(prediction.lowerBound)
        assertNull(prediction.upperBound)
        assertFalse(prediction.isStale)
    }

    // --- CyclePhase enum ---

    @Test
    fun `CyclePhase has all expected phases`() {
        val phases = CyclePhase.entries
        assertEquals(5, phases.size)
        assertTrue(phases.contains(CyclePhase.MENSTRUATION))
        assertTrue(phases.contains(CyclePhase.FOLLICULAR))
        assertTrue(phases.contains(CyclePhase.OVULATION))
        assertTrue(phases.contains(CyclePhase.LUTEAL))
        assertTrue(phases.contains(CyclePhase.PMS))
    }

    @Test
    fun `CyclePhase display names are non-empty`() {
        for (phase in CyclePhase.entries) {
            assertTrue("${phase.name} should have display name", phase.displayName.isNotEmpty())
        }
    }

    // --- PredictionType enum ---

    @Test
    fun `PredictionType has all expected types`() {
        val types = PredictionType.entries
        assertEquals(7, types.size)
    }

    // --- AppMode enum ---

    @Test
    fun `AppMode has all expected modes`() {
        val modes = AppMode.entries
        assertEquals(4, modes.size)
        assertTrue(modes.contains(AppMode.PERIOD_TRACKING))
        assertTrue(modes.contains(AppMode.CONCEIVE))
        assertTrue(modes.contains(AppMode.PREGNANCY))
        assertTrue(modes.contains(AppMode.PERIMENOPAUSE))
    }

    @Test
    fun `AppMode display names are non-empty`() {
        for (mode in AppMode.entries) {
            assertTrue("${mode.name} should have display name", mode.displayName.isNotEmpty())
        }
    }

    // --- TrackingCategory enum ---

    @Test
    fun `TrackingCategory has all expected categories`() {
        val categories = TrackingCategory.entries
        assertEquals(19, categories.size)
    }

    @Test
    fun `TrackingCategory display names are non-empty`() {
        for (cat in TrackingCategory.entries) {
            assertTrue("${cat.name} should have display name", cat.displayName.isNotEmpty())
        }
    }

    // --- Settings data class ---

    @Test
    fun `Settings has correct defaults`() {
        val settings = Settings()
        assertEquals(1, settings.id)
        assertEquals(AppMode.PERIOD_TRACKING, settings.activeMode)
        assertNull(settings.birthYear)
        assertNull(settings.bmiCategory)
        assertEquals(28, settings.typicalCycleLength)
        assertEquals(5, settings.typicalPeriodLength)
        assertNull(settings.pinHash)
        assertEquals("default", settings.appIcon)
        assertEquals(AppTheme.SYSTEM, settings.theme)
        assertEquals("kg", settings.unitsWeight)
        assertEquals("celsius", settings.unitsTemp)
        assertNull(settings.lastBackupDate)
        assertFalse(settings.onboardingCompleted)
        assertEquals(NotificationPrivacy.HIGH, settings.notificationPrivacy)
    }

    // --- BmiCategory enum ---

    @Test
    fun `BmiCategory has all expected categories`() {
        val categories = BmiCategory.entries
        assertEquals(4, categories.size)
        assertTrue(categories.contains(BmiCategory.UNDERWEIGHT))
        assertTrue(categories.contains(BmiCategory.NORMAL))
        assertTrue(categories.contains(BmiCategory.OVERWEIGHT))
        assertTrue(categories.contains(BmiCategory.OBESE))
    }

    // --- CycleAnalysis data class ---

    @Test
    fun `CycleAnalysis has correct defaults`() {
        val analysis = CycleAnalysis(
            averageCycleLength = 28.0,
            averagePeriodLength = 5.0,
            cycleLengthVariability = 2.0,
            longestCycle = 30,
            shortestCycle = 26,
            totalCyclesTracked = 5,
            currentStreak = 3,
            regularityScore = 8
        )

        assertEquals(Trend.STABLE, analysis.cycleLengthTrend)
        assertTrue(analysis.cycleLengths.isEmpty())
    }

    // --- Trend enum ---

    @Test
    fun `Trend has all expected values`() {
        val trends = Trend.entries
        assertEquals(3, trends.size)
        assertTrue(trends.contains(Trend.LENGTHENING))
        assertTrue(trends.contains(Trend.SHORTENING))
        assertTrue(trends.contains(Trend.STABLE))
    }

    // --- DailyLog data class ---

    @Test
    fun `DailyLog has correct defaults`() {
        val log = DailyLog(
            id = "log-1",
            date = LocalDate.of(2024, 3, 15)
        )

        assertNull(log.cycleId)
        assertNull(log.cycleDay)
        assertNull(log.notes)
        assertTrue(log.entries.isEmpty())
    }

    // --- TrackingEntry data class ---

    @Test
    fun `TrackingEntry has correct defaults`() {
        val entry = TrackingEntry(
            id = "entry-1",
            dailyLogId = "log-1",
            category = TrackingCategory.MOOD,
            subcategory = "happy"
        )

        assertNull(entry.intensity)
        assertNull(entry.customValue)
    }
}
