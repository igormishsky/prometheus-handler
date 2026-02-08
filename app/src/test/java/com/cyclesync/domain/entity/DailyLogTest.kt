package com.cyclesync.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DailyLogTest {

    @Test
    fun `hasEntries returns true when entries exist`() {
        val log = createLog(entries = listOf(
            TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "cramps")
        ))
        assertTrue(log.hasEntries)
    }

    @Test
    fun `hasEntries returns false when no entries`() {
        val log = createLog(entries = emptyList())
        assertFalse(log.hasEntries)
    }

    @Test
    fun `trackedCategories returns unique categories`() {
        val log = createLog(entries = listOf(
            TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "cramps"),
            TrackingEntry(id = "e2", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "headache"),
            TrackingEntry(id = "e3", dailyLogId = "l1", category = TrackingCategory.MOOD, subcategory = "happy")
        ))
        assertTrue(log.trackedCategories.contains(TrackingCategory.PAIN))
        assertTrue(log.trackedCategories.contains(TrackingCategory.MOOD))
    }

    @Test
    fun `hasCategory returns true when category exists`() {
        val log = createLog(entries = listOf(
            TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "cramps")
        ))
        assertTrue(log.hasCategory(TrackingCategory.PAIN))
        assertFalse(log.hasCategory(TrackingCategory.MOOD))
    }

    @Test
    fun `getEntriesForCategory filters correctly`() {
        val log = createLog(entries = listOf(
            TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "cramps"),
            TrackingEntry(id = "e2", dailyLogId = "l1", category = TrackingCategory.MOOD, subcategory = "happy"),
            TrackingEntry(id = "e3", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "headache")
        ))
        val painEntries = log.getEntriesForCategory(TrackingCategory.PAIN)
        assertEquals(2, painEntries.size)
    }

    @Test
    fun `TrackingEntry displaySubcategory formats correctly`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "lower_back")
        assertEquals("Lower back", entry.displaySubcategory)
    }

    @Test
    fun `TrackingEntry key formats correctly`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "cramps")
        assertEquals("PAIN_cramps", entry.key)
    }

    @Test
    fun `TrackingEntry hasIntensity checks correctly`() {
        assertTrue(TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", intensity = 5).hasIntensity)
        assertFalse(TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", intensity = null).hasIntensity)
    }

    private fun createLog(
        entries: List<TrackingEntry> = emptyList(),
        notes: String? = null
    ) = DailyLog(
        id = "log1",
        date = LocalDate.of(2024, 1, 15),
        entries = entries,
        notes = notes
    )
}
