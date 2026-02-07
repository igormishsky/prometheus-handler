package com.cyclesync.core.utils

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeParseException

class DateUtilsTest {

    // --- toIsoString ---

    @Test
    fun `toIsoString formats date correctly`() {
        val date = LocalDate.of(2024, 3, 15)
        assertEquals("2024-03-15", DateUtils.toIsoString(date))
    }

    @Test
    fun `toIsoString pads single-digit month and day`() {
        val date = LocalDate.of(2024, 1, 5)
        assertEquals("2024-01-05", DateUtils.toIsoString(date))
    }

    @Test
    fun `toIsoString handles leap year date`() {
        val date = LocalDate.of(2024, 2, 29)
        assertEquals("2024-02-29", DateUtils.toIsoString(date))
    }

    @Test
    fun `toIsoString handles end of year`() {
        val date = LocalDate.of(2024, 12, 31)
        assertEquals("2024-12-31", DateUtils.toIsoString(date))
    }

    // --- fromIsoString ---

    @Test
    fun `fromIsoString parses ISO date correctly`() {
        val date = DateUtils.fromIsoString("2024-03-15")
        assertEquals(LocalDate.of(2024, 3, 15), date)
    }

    @Test
    fun `fromIsoString parses leap year date`() {
        val date = DateUtils.fromIsoString("2024-02-29")
        assertEquals(LocalDate.of(2024, 2, 29), date)
    }

    @Test(expected = DateTimeParseException::class)
    fun `fromIsoString throws on invalid format`() {
        DateUtils.fromIsoString("03-15-2024")
    }

    @Test(expected = DateTimeParseException::class)
    fun `fromIsoString throws on empty string`() {
        DateUtils.fromIsoString("")
    }

    @Test(expected = DateTimeParseException::class)
    fun `fromIsoString throws on garbage input`() {
        DateUtils.fromIsoString("not-a-date")
    }

    // --- roundtrip ---

    @Test
    fun `toIsoString and fromIsoString are inverses`() {
        val original = LocalDate.of(2024, 6, 15)
        val roundtripped = DateUtils.fromIsoString(DateUtils.toIsoString(original))
        assertEquals(original, roundtripped)
    }

    @Test
    fun `roundtrip works for boundary dates`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            LocalDate.of(2024, 2, 29),
            LocalDate.of(2000, 1, 1)
        )

        for (date in dates) {
            assertEquals(date, DateUtils.fromIsoString(DateUtils.toIsoString(date)))
        }
    }

    // --- daysBetween ---

    @Test
    fun `daysBetween returns positive for forward range`() {
        val start = LocalDate.of(2024, 1, 1)
        val end = LocalDate.of(2024, 1, 31)
        assertEquals(30L, DateUtils.daysBetween(start, end))
    }

    @Test
    fun `daysBetween returns negative for reversed range`() {
        val start = LocalDate.of(2024, 1, 31)
        val end = LocalDate.of(2024, 1, 1)
        assertEquals(-30L, DateUtils.daysBetween(start, end))
    }

    @Test
    fun `daysBetween returns 0 for same date`() {
        val date = LocalDate.of(2024, 3, 15)
        assertEquals(0L, DateUtils.daysBetween(date, date))
    }

    @Test
    fun `daysBetween handles month boundaries`() {
        val start = LocalDate.of(2024, 1, 31)
        val end = LocalDate.of(2024, 2, 1)
        assertEquals(1L, DateUtils.daysBetween(start, end))
    }

    @Test
    fun `daysBetween handles year boundaries`() {
        val start = LocalDate.of(2023, 12, 31)
        val end = LocalDate.of(2024, 1, 1)
        assertEquals(1L, DateUtils.daysBetween(start, end))
    }

    @Test
    fun `daysBetween handles leap year correctly`() {
        val start = LocalDate.of(2024, 2, 28)
        val end = LocalDate.of(2024, 3, 1)
        assertEquals(2L, DateUtils.daysBetween(start, end)) // Feb 29 exists in 2024
    }

    @Test
    fun `daysBetween handles non-leap year correctly`() {
        val start = LocalDate.of(2023, 2, 28)
        val end = LocalDate.of(2023, 3, 1)
        assertEquals(1L, DateUtils.daysBetween(start, end)) // No Feb 29 in 2023
    }

    // --- isToday ---

    @Test
    fun `isToday returns true for today`() {
        assertTrue(DateUtils.isToday(LocalDate.now()))
    }

    @Test
    fun `isToday returns false for yesterday`() {
        assertFalse(DateUtils.isToday(LocalDate.now().minusDays(1)))
    }

    @Test
    fun `isToday returns false for tomorrow`() {
        assertFalse(DateUtils.isToday(LocalDate.now().plusDays(1)))
    }

    // --- isFuture ---

    @Test
    fun `isFuture returns true for tomorrow`() {
        assertTrue(DateUtils.isFuture(LocalDate.now().plusDays(1)))
    }

    @Test
    fun `isFuture returns false for today`() {
        assertFalse(DateUtils.isFuture(LocalDate.now()))
    }

    @Test
    fun `isFuture returns false for yesterday`() {
        assertFalse(DateUtils.isFuture(LocalDate.now().minusDays(1)))
    }

    @Test
    fun `isFuture returns true for far future`() {
        assertTrue(DateUtils.isFuture(LocalDate.now().plusYears(10)))
    }

    // --- isPast ---

    @Test
    fun `isPast returns true for yesterday`() {
        assertTrue(DateUtils.isPast(LocalDate.now().minusDays(1)))
    }

    @Test
    fun `isPast returns false for today`() {
        assertFalse(DateUtils.isPast(LocalDate.now()))
    }

    @Test
    fun `isPast returns false for tomorrow`() {
        assertFalse(DateUtils.isPast(LocalDate.now().plusDays(1)))
    }

    @Test
    fun `isPast returns true for far past`() {
        assertTrue(DateUtils.isPast(LocalDate.now().minusYears(10)))
    }
}
