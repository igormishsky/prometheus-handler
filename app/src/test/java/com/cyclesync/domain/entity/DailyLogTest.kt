package com.cyclesync.domain.entity

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DailyLogTest {

    @Test
    fun `hasEntries returns true when entries exist`() {
        val log = createLog(entries = listOf(
            createEntry(TrackingCategory.PAIN, "cramps")
        ))
        assertTrue(log.hasEntries)
    }

    @Test
    fun `hasEntries returns false when no entries`() {
        val log = createLog(entries = emptyList())
        assertFalse(log.hasEntries)
    }

    @Test
    fun `hasNotes returns true for non-blank notes`() {
        assertTrue(createLog(notes = "Some notes").hasNotes)
    }

    @Test
    fun `hasNotes returns false for null notes`() {
        assertFalse(createLog(notes = null).hasNotes)
    }

    @Test
    fun `hasNotes returns false for blank notes`() {
        assertFalse(createLog(notes = "   ").hasNotes)
    }

    @Test
    fun `entryCount returns correct count`() {
        val log = createLog(entries = listOf(
            createEntry(TrackingCategory.PAIN, "cramps"),
            createEntry(TrackingCategory.MOOD, "happy")
        ))
        assertEquals(2, log.entryCount)
    }

    @Test
    fun `entryCount returns 0 for empty`() {
        assertEquals(0, createLog(entries = emptyList()).entryCount)
    }

    @Test
    fun `trackedCategories returns unique categories`() {
        val log = createLog(entries = listOf(
            createEntry(TrackingCategory.PAIN, "cramps"),
            createEntry(TrackingCategory.PAIN, "headache"),
            createEntry(TrackingCategory.MOOD, "happy")
        ))
        assertEquals(2, log.categoryCount)
        assertTrue(log.trackedCategories.contains(TrackingCategory.PAIN))
        assertTrue(log.trackedCategories.contains(TrackingCategory.MOOD))
    }

    @Test
    fun `hasCategory returns true when category exists`() {
        val log = createLog(entries = listOf(
            createEntry(TrackingCategory.PAIN, "cramps")
        ))
        assertTrue(log.hasCategory(TrackingCategory.PAIN))
        assertFalse(log.hasCategory(TrackingCategory.MOOD))
    }

    @Test
    fun `getEntriesForCategory filters correctly`() {
        val log = createLog(entries = listOf(
            createEntry(TrackingCategory.PAIN, "cramps"),
            createEntry(TrackingCategory.MOOD, "happy"),
            createEntry(TrackingCategory.PAIN, "headache")
        ))
        val painEntries = log.getEntriesForCategory(TrackingCategory.PAIN)
        assertEquals(2, painEntries.size)
    }

    @Test
    fun `getEntriesForCategory returns empty for missing category`() {
        val log = createLog(entries = listOf(
            createEntry(TrackingCategory.PAIN, "cramps")
        ))
        val moodEntries = log.getEntriesForCategory(TrackingCategory.MOOD)
        assertTrue(moodEntries.isEmpty())
    }

    @Test
    fun `TrackingEntry displaySubcategory formats correctly`() {
        val entry = createEntry(TrackingCategory.PAIN, "lower_back")
        assertEquals("Lower back", entry.displaySubcategory)
    }

    @Test
    fun `TrackingEntry displaySubcategory handles single word`() {
        val entry = createEntry(TrackingCategory.PAIN, "cramps")
        assertEquals("Cramps", entry.displaySubcategory)
    }

    @Test
    fun `TrackingEntry key formats correctly`() {
        val entry = createEntry(TrackingCategory.PAIN, "cramps")
        assertEquals("PAIN_cramps", entry.key)
    }

    @Test
    fun `TrackingEntry hasIntensity returns true when set`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", intensity = 5)
        assertTrue(entry.hasIntensity)
    }

    @Test
    fun `TrackingEntry hasIntensity returns false when null`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", intensity = null)
        assertFalse(entry.hasIntensity)
    }

    @Test
    fun `TrackingEntry hasCustomValue returns true when set`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", customValue = "test")
        assertTrue(entry.hasCustomValue)
    }

    @Test
    fun `TrackingEntry hasCustomValue returns false when null`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", customValue = null)
        assertFalse(entry.hasCustomValue)
    }

    @Test
    fun `TrackingEntry hasCustomValue returns false when blank`() {
        val entry = TrackingEntry(id = "e1", dailyLogId = "l1", category = TrackingCategory.PAIN, subcategory = "c", customValue = "  ")
        assertFalse(entry.hasCustomValue)
    }

    private fun createEntry(
        category: TrackingCategory,
        subcategory: String
    ) = TrackingEntry(
        id = "e-${category.name}-$subcategory",
        dailyLogId = "l1",
        category = category,
        subcategory = subcategory
    )

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
