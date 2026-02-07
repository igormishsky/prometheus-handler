package com.cyclesync.domain.entity

import org.junit.Assert.*
import org.junit.Test

class TrackingCategoryTest {

    @Test
    fun `all categories have non-empty display names`() {
        TrackingCategory.entries.forEach { category ->
            assertTrue("${category.name} should have non-empty displayName", category.displayName.isNotBlank())
        }
    }

    @Test
    fun `all categories have subcategories`() {
        TrackingCategory.entries.forEach { category ->
            assertTrue("${category.name} should have subcategories", category.subcategories.isNotEmpty())
        }
    }

    @Test
    fun `BLEEDING category exists`() {
        val bleeding = TrackingCategory.entries.find { it.name == "BLEEDING" }
        assertNotNull("BLEEDING category should exist", bleeding)
    }

    @Test
    fun `PAIN category has common subcategories`() {
        val pain = TrackingCategory.PAIN
        assertTrue("PAIN should have cramps", pain.subcategories.any { it.contains("cramp", ignoreCase = true) })
    }

    @Test
    fun `MOOD category has subcategories`() {
        val mood = TrackingCategory.MOOD
        assertTrue("MOOD should have subcategories", mood.subcategories.isNotEmpty())
    }

    @Test
    fun `display names are human readable`() {
        TrackingCategory.entries.forEach { category ->
            assertFalse("${category.name} displayName should not be all uppercase",
                category.displayName == category.displayName.uppercase() && category.displayName.length > 3)
        }
    }

    @Test
    fun `category count is at least 19`() {
        assertTrue("Should have at least 19 categories", TrackingCategory.entries.size >= 19)
    }

    @Test
    fun `no duplicate display names`() {
        val displayNames = TrackingCategory.entries.map { it.displayName }
        assertEquals("Display names should be unique", displayNames.size, displayNames.distinct().size)
    }

    @Test
    fun `ENERGY category exists`() {
        assertNotNull(TrackingCategory.entries.find { it.name == "ENERGY" })
    }

    @Test
    fun `SLEEP category exists`() {
        assertNotNull(TrackingCategory.entries.find { it.name == "SLEEP" })
    }

    @Test
    fun `EXERCISE category exists`() {
        assertNotNull(TrackingCategory.entries.find { it.name == "EXERCISE" })
    }

    @Test
    fun `DISCHARGE category exists`() {
        assertNotNull(TrackingCategory.entries.find { it.name == "DISCHARGE" })
    }

    @Test
    fun `SKIN category exists`() {
        assertNotNull(TrackingCategory.entries.find { it.name == "SKIN" })
    }

    @Test
    fun `DIGESTION category exists`() {
        assertNotNull(TrackingCategory.entries.find { it.name == "DIGESTION" })
    }

    @Test
    fun `subcategories do not contain empty strings`() {
        TrackingCategory.entries.forEach { category ->
            category.subcategories.forEach { sub ->
                assertTrue("Subcategory in ${category.name} should not be empty", sub.isNotBlank())
            }
        }
    }
}
