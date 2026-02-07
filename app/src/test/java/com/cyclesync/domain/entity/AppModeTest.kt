package com.cyclesync.domain.entity

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
        assertEquals(AppMode.PERIOD_TRACKING, AppMode.fromNameOrNull("PERIOD_TRACKING"))
        assertEquals(AppMode.CONCEIVE, AppMode.fromNameOrNull("conceive"))
        assertEquals(AppMode.PREGNANCY, AppMode.fromNameOrNull("PREGNANCY"))
        assertEquals(AppMode.PERIMENOPAUSE, AppMode.fromNameOrNull("perimenopause"))
    }

    @Test
    fun `fromNameOrNull returns null for invalid`() {
        assertNull(AppMode.fromNameOrNull("invalid"))
        assertNull(AppMode.fromNameOrNull(null))
        assertNull(AppMode.fromNameOrNull(""))
        assertNull(AppMode.fromNameOrNull("   "))
    }

    @Test
    fun `next cycles through all modes`() {
        assertEquals(AppMode.CONCEIVE, AppMode.next(AppMode.PERIOD_TRACKING))
        assertEquals(AppMode.PREGNANCY, AppMode.next(AppMode.CONCEIVE))
        assertEquals(AppMode.PERIMENOPAUSE, AppMode.next(AppMode.PREGNANCY))
        assertEquals(AppMode.PERIOD_TRACKING, AppMode.next(AppMode.PERIMENOPAUSE))
    }

    @Test
    fun `next wraps around from last to first`() {
        val last = AppMode.entries.last()
        val expected = AppMode.entries.first()
        assertEquals(expected, AppMode.next(last))
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
        assertEquals(CyclePhase.MENSTRUATION, CyclePhase.fromNameOrNull("MENSTRUATION"))
        assertEquals(CyclePhase.OVULATION, CyclePhase.fromNameOrNull("ovulation"))
    }

    @Test
    fun `CyclePhase fromNameOrNull returns null for invalid`() {
        assertNull(CyclePhase.fromNameOrNull("invalid"))
        assertNull(CyclePhase.fromNameOrNull(null))
    }
}
