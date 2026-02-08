package com.cyclesync.domain.entity

import com.cyclesync.core.utils.enumValueOfOrNull
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleTest {

    @Test
    fun `isComplete returns true when endDate and cycleLength are set`() {
        val cycle = createCycle(endDate = LocalDate.of(2024, 1, 28), cycleLength = 28)
        assertTrue(cycle.isComplete)
    }

    @Test
    fun `isComplete returns false when cycleLength is null`() {
        val cycle = createCycle(endDate = LocalDate.of(2024, 1, 28), cycleLength = null)
        assertFalse(cycle.isComplete)
    }

    @Test
    fun `durationDays calculates correctly`() {
        val cycle = createCycle(
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 28)
        )
        assertEquals(27L, cycle.durationDays)
    }

    @Test
    fun `durationDays returns null when endDate is null`() {
        val cycle = createCycle(endDate = null)
        assertNull(cycle.durationDays)
    }

    @Test
    fun `effectivePeriodEnd uses periodEndDate when available`() {
        val periodEnd = LocalDate.of(2024, 1, 5)
        val cycle = createCycle(periodEndDate = periodEnd)
        assertEquals(periodEnd, cycle.effectivePeriodEnd)
    }

    @Test
    fun `effectivePeriodEnd calculates from periodLength when periodEndDate is null`() {
        val cycle = createCycle(periodEndDate = null, periodLength = 5)
        val expected = cycle.periodStartDate.plusDays(4)
        assertEquals(expected, cycle.effectivePeriodEnd)
    }

    @Test
    fun `effectivePeriodEnd defaults to 5 days when both null`() {
        val cycle = createCycle(periodEndDate = null, periodLength = null)
        val expected = cycle.periodStartDate.plusDays(4)
        assertEquals(expected, cycle.effectivePeriodEnd)
    }

    @Test
    fun `effectiveCycleLength returns cycleLength when set`() {
        val cycle = createCycle(cycleLength = 30)
        assertEquals(30, cycle.effectiveCycleLength)
    }

    @Test
    fun `effectiveCycleLength returns 28 when cycleLength is null`() {
        val cycle = createCycle(cycleLength = null)
        assertEquals(28, cycle.effectiveCycleLength)
    }

    @Test
    fun `isDateInPeriod returns true for date in period`() {
        val start = LocalDate.of(2024, 1, 1)
        val cycle = createCycle(periodStartDate = start, periodEndDate = start.plusDays(4))
        assertTrue(cycle.isDateInPeriod(start.plusDays(2)))
    }

    @Test
    fun `isDateInPeriod returns true for first day`() {
        val start = LocalDate.of(2024, 1, 1)
        val cycle = createCycle(periodStartDate = start, periodEndDate = start.plusDays(4))
        assertTrue(cycle.isDateInPeriod(start))
    }

    @Test
    fun `isDateInPeriod returns true for last day`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = start.plusDays(4)
        val cycle = createCycle(periodStartDate = start, periodEndDate = end)
        assertTrue(cycle.isDateInPeriod(end))
    }

    @Test
    fun `isDateInPeriod returns false for date before period`() {
        val start = LocalDate.of(2024, 1, 5)
        val cycle = createCycle(periodStartDate = start, periodEndDate = start.plusDays(4))
        assertFalse(cycle.isDateInPeriod(start.minusDays(1)))
    }

    @Test
    fun `isDateInPeriod returns false for date after period`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = start.plusDays(4)
        val cycle = createCycle(periodStartDate = start, periodEndDate = end)
        assertFalse(cycle.isDateInPeriod(end.plusDays(1)))
    }

    @Test
    fun `isDateInCycle returns true for date in cycle`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 1, 28)
        val cycle = createCycle(startDate = start, endDate = end)
        assertTrue(cycle.isDateInCycle(start.plusDays(10)))
    }

    @Test
    fun `isDateInCycle returns false for date before cycle`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 1, 28)
        val cycle = createCycle(startDate = start, endDate = end)
        assertFalse(cycle.isDateInCycle(start.minusDays(1)))
    }

    @Test
    fun `SkipReason fromNameOrNull returns correct reason`() {
        assertEquals(SkipReason.STRESS, enumValueOfOrNull<SkipReason>("STRESS"))
        assertEquals(SkipReason.ILLNESS, enumValueOfOrNull<SkipReason>("illness"))
    }

    @Test
    fun `SkipReason fromNameOrNull returns null for invalid name`() {
        assertNull(enumValueOfOrNull<SkipReason>("invalid"))
        assertNull(enumValueOfOrNull<SkipReason>(null))
        assertNull(enumValueOfOrNull<SkipReason>(""))
    }

    @Test
    fun `all SkipReasons have non-empty display names`() {
        SkipReason.entries.forEach { reason ->
            assertTrue("${reason.name} should have non-empty displayName", reason.displayName.isNotBlank())
        }
    }

    private fun createCycle(
        startDate: LocalDate = LocalDate.of(2024, 1, 1),
        endDate: LocalDate? = null,
        periodStartDate: LocalDate = startDate,
        periodEndDate: LocalDate? = null,
        cycleLength: Int? = null,
        periodLength: Int? = null,
        notes: String? = null
    ) = Cycle(
        id = "test-id",
        cycleNumber = 1,
        startDate = startDate,
        endDate = endDate,
        periodStartDate = periodStartDate,
        periodEndDate = periodEndDate,
        cycleLength = cycleLength,
        periodLength = periodLength,
        notes = notes
    )
}
