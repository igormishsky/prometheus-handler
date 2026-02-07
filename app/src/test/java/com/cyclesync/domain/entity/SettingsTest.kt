package com.cyclesync.domain.entity

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
    fun `hasBiometricProtection returns true when pinHash set`() {
        val settings = Settings(pinHash = "abc123")
        assertTrue(settings.hasBiometricProtection)
    }

    @Test
    fun `hasBiometricProtection returns false when pinHash null`() {
        val settings = Settings(pinHash = null)
        assertFalse(settings.hasBiometricProtection)
    }

    @Test
    fun `hasBiometricProtection returns false when pinHash blank`() {
        val settings = Settings(pinHash = "  ")
        assertFalse(settings.hasBiometricProtection)
    }

    @Test
    fun `isConceiveMode returns true for CONCEIVE`() {
        val settings = Settings(activeMode = AppMode.CONCEIVE)
        assertTrue(settings.isConceiveMode)
        assertFalse(settings.isPregnancyMode)
    }

    @Test
    fun `isPregnancyMode returns true for PREGNANCY`() {
        val settings = Settings(activeMode = AppMode.PREGNANCY)
        assertTrue(settings.isPregnancyMode)
        assertFalse(settings.isConceiveMode)
    }

    @Test
    fun `isPerimenopauseMode returns true for PERIMENOPAUSE`() {
        val settings = Settings(activeMode = AppMode.PERIMENOPAUSE)
        assertTrue(settings.isPerimenopauseMode)
    }

    @Test
    fun `bmiCategoryName returns lowercase`() {
        val settings = Settings(bmiCategory = BmiCategory.OVERWEIGHT)
        assertEquals("overweight", settings.bmiCategoryName)
    }

    @Test
    fun `bmiCategoryName returns null when bmiCategory null`() {
        val settings = Settings(bmiCategory = null)
        assertNull(settings.bmiCategoryName)
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
        assertEquals(BmiCategory.NORMAL, BmiCategory.fromNameOrNull("NORMAL"))
        assertEquals(BmiCategory.OBESE, BmiCategory.fromNameOrNull("obese"))
    }

    @Test
    fun `BmiCategory fromNameOrNull returns null for invalid`() {
        assertNull(BmiCategory.fromNameOrNull("invalid"))
        assertNull(BmiCategory.fromNameOrNull(null))
    }

    @Test
    fun `AppTheme fromNameOrNull returns correct theme`() {
        assertEquals(AppTheme.DARK, AppTheme.fromNameOrNull("DARK"))
        assertEquals(AppTheme.LIGHT, AppTheme.fromNameOrNull("light"))
    }

    @Test
    fun `AppTheme fromNameOrNull returns null for invalid`() {
        assertNull(AppTheme.fromNameOrNull("invalid"))
        assertNull(AppTheme.fromNameOrNull(null))
    }

    @Test
    fun `NotificationPrivacy fromNameOrNull returns correct privacy`() {
        assertEquals(NotificationPrivacy.HIGH, NotificationPrivacy.fromNameOrNull("HIGH"))
        assertEquals(NotificationPrivacy.LOW, NotificationPrivacy.fromNameOrNull("low"))
    }

    @Test
    fun `NotificationPrivacy fromNameOrNull returns null for invalid`() {
        assertNull(NotificationPrivacy.fromNameOrNull("invalid"))
        assertNull(NotificationPrivacy.fromNameOrNull(null))
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

    @Test
    fun `all BmiCategory values have non-empty display names`() {
        BmiCategory.entries.forEach { bmi ->
            assertTrue("${bmi.name} should have non-empty displayName", bmi.displayName.isNotBlank())
        }
    }
}
