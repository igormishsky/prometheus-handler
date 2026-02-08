package com.cyclesync.domain.entity

import com.cyclesync.core.utils.enumValueOfOrNull
import org.junit.Assert.*
import org.junit.Test

class AppModeTest {

    @Test
    fun `all app modes have non-empty display names`() {
        AppMode.entries.forEach { mode ->
            assertTrue("${mode.name} should have non-empty displayName", mode.displayName.isNotBlank())
        }
    }

    @Test
    fun `all app modes have non-empty descriptions`() {
        AppMode.entries.forEach { mode ->
            assertTrue("${mode.name} should have non-empty description", mode.description.isNotBlank())
        }
    }

    @Test
    fun `showsFertilityData true for relevant modes`() {
        assertTrue(AppMode.CONCEIVE.showsFertilityData)
        assertTrue(AppMode.PERIOD_TRACKING.showsFertilityData)
        assertFalse(AppMode.PREGNANCY.showsFertilityData)
        assertFalse(AppMode.PERIMENOPAUSE.showsFertilityData)
    }

    @Test
    fun `showsPregnancyData true only for PREGNANCY`() {
        assertTrue(AppMode.PREGNANCY.showsPregnancyData)
        assertFalse(AppMode.PERIOD_TRACKING.showsPregnancyData)
        assertFalse(AppMode.CONCEIVE.showsPregnancyData)
        assertFalse(AppMode.PERIMENOPAUSE.showsPregnancyData)
    }

    @Test
    fun `fromNameOrNull returns correct mode`() {
        assertEquals(AppMode.PERIOD_TRACKING, enumValueOfOrNull<AppMode>("PERIOD_TRACKING"))
        assertEquals(AppMode.CONCEIVE, enumValueOfOrNull<AppMode>("conceive"))
        assertEquals(AppMode.PREGNANCY, enumValueOfOrNull<AppMode>("PREGNANCY"))
        assertEquals(AppMode.PERIMENOPAUSE, enumValueOfOrNull<AppMode>("perimenopause"))
    }

    @Test
    fun `fromNameOrNull returns null for invalid`() {
        assertNull(enumValueOfOrNull<AppMode>("invalid"))
        assertNull(enumValueOfOrNull<AppMode>(null))
        assertNull(enumValueOfOrNull<AppMode>(""))
        assertNull(enumValueOfOrNull<AppMode>("   "))
    }

    @Test
    fun `next cycles through all modes`() {
        assertEquals(AppMode.CONCEIVE, AppMode.PERIOD_TRACKING.next())
        assertEquals(AppMode.PREGNANCY, AppMode.CONCEIVE.next())
        assertEquals(AppMode.PERIMENOPAUSE, AppMode.PREGNANCY.next())
        assertEquals(AppMode.PERIOD_TRACKING, AppMode.PERIMENOPAUSE.next())
    }

    @Test
    fun `next wraps around from last to first`() {
        val last = AppMode.entries.last()
        val expected = AppMode.entries.first()
        assertEquals(expected, last.next())
    }

    @Test
    fun `CyclePhase has all 5 phases`() {
        assertEquals(5, CyclePhase.entries.size)
    }

    @Test
    fun `CyclePhase all have display names`() {
        CyclePhase.entries.forEach { phase ->
            assertTrue("${phase.name} should have non-empty displayName", phase.displayName.isNotBlank())
        }
    }

    @Test
    fun `CyclePhase all have descriptions`() {
        CyclePhase.entries.forEach { phase ->
            assertTrue("${phase.name} should have non-empty description", phase.description.isNotBlank())
        }
    }

    @Test
    fun `CyclePhase isBleedingPhase only for MENSTRUATION`() {
        assertTrue(CyclePhase.MENSTRUATION.isBleedingPhase)
        assertFalse(CyclePhase.FOLLICULAR.isBleedingPhase)
        assertFalse(CyclePhase.OVULATION.isBleedingPhase)
        assertFalse(CyclePhase.LUTEAL.isBleedingPhase)
        assertFalse(CyclePhase.PMS.isBleedingPhase)
    }

    @Test
    fun `CyclePhase isFertilePhase only for OVULATION`() {
        assertTrue(CyclePhase.OVULATION.isFertilePhase)
        assertFalse(CyclePhase.MENSTRUATION.isFertilePhase)
        assertFalse(CyclePhase.FOLLICULAR.isFertilePhase)
        assertFalse(CyclePhase.LUTEAL.isFertilePhase)
        assertFalse(CyclePhase.PMS.isFertilePhase)
    }

    @Test
    fun `CyclePhase fromNameOrNull returns correct phase`() {
        assertEquals(CyclePhase.MENSTRUATION, enumValueOfOrNull<CyclePhase>("MENSTRUATION"))
        assertEquals(CyclePhase.OVULATION, enumValueOfOrNull<CyclePhase>("ovulation"))
    }

    @Test
    fun `CyclePhase fromNameOrNull returns null for invalid`() {
        assertNull(enumValueOfOrNull<CyclePhase>("invalid"))
        assertNull(enumValueOfOrNull<CyclePhase>(null))
    }
}
