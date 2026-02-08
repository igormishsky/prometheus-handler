package com.cyclesync.domain.entity

import com.cyclesync.core.utils.enumValueOfOrNull
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SettingsTest {

    @Test
    fun `currentAge calculates correctly`() {
        val settings = Settings(birthYear = 1990)
        val expected = LocalDate.now().year - 1990
        assertEquals(expected, settings.currentAge)
    }

    @Test
    fun `currentAge returns null when birthYear is null`() {
        val settings = Settings(birthYear = null)
        assertNull(settings.currentAge)
    }

    @Test
    fun `usesCelsius returns true for celsius`() {
        val settings = Settings(unitsTemp = "celsius")
        assertTrue(settings.usesCelsius)
    }

    @Test
    fun `usesCelsius returns false for fahrenheit`() {
        val settings = Settings(unitsTemp = "fahrenheit")
        assertFalse(settings.usesCelsius)
    }

    @Test
    fun `usesKg returns true for kg`() {
        val settings = Settings(unitsWeight = "kg")
        assertTrue(settings.usesKg)
    }

    @Test
    fun `usesKg returns false for lbs`() {
        val settings = Settings(unitsWeight = "lbs")
        assertFalse(settings.usesKg)
    }

    @Test
    fun `BmiCategory fromNameOrNull returns correct category`() {
        assertEquals(BmiCategory.NORMAL, enumValueOfOrNull<BmiCategory>("NORMAL"))
        assertEquals(BmiCategory.OBESE, enumValueOfOrNull<BmiCategory>("obese"))
    }

    @Test
    fun `BmiCategory fromNameOrNull returns null for invalid`() {
        assertNull(enumValueOfOrNull<BmiCategory>("invalid"))
        assertNull(enumValueOfOrNull<BmiCategory>(null))
    }

    @Test
    fun `AppTheme fromNameOrNull returns correct theme`() {
        assertEquals(AppTheme.DARK, enumValueOfOrNull<AppTheme>("DARK"))
        assertEquals(AppTheme.LIGHT, enumValueOfOrNull<AppTheme>("light"))
    }

    @Test
    fun `AppTheme fromNameOrNull returns null for invalid`() {
        assertNull(enumValueOfOrNull<AppTheme>("invalid"))
        assertNull(enumValueOfOrNull<AppTheme>(null))
    }

    @Test
    fun `NotificationPrivacy fromNameOrNull returns correct privacy`() {
        assertEquals(NotificationPrivacy.HIGH, enumValueOfOrNull<NotificationPrivacy>("HIGH"))
        assertEquals(NotificationPrivacy.LOW, enumValueOfOrNull<NotificationPrivacy>("low"))
    }

    @Test
    fun `NotificationPrivacy fromNameOrNull returns null for invalid`() {
        assertNull(enumValueOfOrNull<NotificationPrivacy>("invalid"))
        assertNull(enumValueOfOrNull<NotificationPrivacy>(null))
    }

    @Test
    fun `all NotificationPrivacy values have non-empty display names`() {
        NotificationPrivacy.entries.forEach { level ->
            assertTrue("${level.name} should have non-empty displayName", level.displayName.isNotBlank())
        }
    }

    @Test
    fun `default settings have sensible values`() {
        val settings = Settings()
        assertEquals(AppMode.PERIOD_TRACKING, settings.activeMode)
        assertEquals(28, settings.typicalCycleLength)
        assertEquals(5, settings.typicalPeriodLength)
        assertEquals(AppTheme.SYSTEM, settings.theme)
        assertEquals(NotificationPrivacy.HIGH, settings.notificationPrivacy)
        assertFalse(settings.onboardingCompleted)
    }
}
