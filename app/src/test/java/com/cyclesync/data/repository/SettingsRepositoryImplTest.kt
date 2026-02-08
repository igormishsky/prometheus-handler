package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.SettingsDao
import com.cyclesync.core.database.entity.SettingsEntity
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.AppTheme
import com.cyclesync.domain.entity.BmiCategory
import com.cyclesync.domain.entity.NotificationPrivacy
import com.cyclesync.domain.entity.Settings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SettingsRepositoryImplTest {

    private lateinit var settingsDao: SettingsDao
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        settingsDao = mockk(relaxed = true)
        repository = SettingsRepositoryImpl(settingsDao)
    }

    @Test
    fun `getSettings maps entity to domain correctly`() = runTest {
        val entity = SettingsEntity(
            id = 1,
            activeMode = "period_tracking",
            birthYear = 1990,
            bmiCategory = "normal",
            typicalCycleLength = 28,
            typicalPeriodLength = 5,
            theme = "system",
            notificationPrivacy = "high",
            onboardingCompleted = true,
            firstLaunchDate = "2024-01-01"
        )
        every { settingsDao.getSettings() } returns flowOf(entity)

        val result = repository.getSettings().first()

        assertNotNull(result)
        assertEquals(AppMode.PERIOD_TRACKING, result!!.activeMode)
        assertEquals(1990, result.birthYear)
        assertEquals(BmiCategory.NORMAL, result.bmiCategory)
        assertEquals(28, result.typicalCycleLength)
        assertEquals(5, result.typicalPeriodLength)
        assertEquals(AppTheme.SYSTEM, result.theme)
        assertEquals(NotificationPrivacy.HIGH, result.notificationPrivacy)
        assertTrue(result.onboardingCompleted)
    }

    @Test
    fun `getSettings handles null entity`() = runTest {
        every { settingsDao.getSettings() } returns flowOf(null)
        val result = repository.getSettings().first()
        assertNull(result)
    }

    @Test
    fun `getSettings handles unknown activeMode gracefully`() = runTest {
        val entity = SettingsEntity(activeMode = "unknown_mode")
        every { settingsDao.getSettings() } returns flowOf(entity)

        val result = repository.getSettings().first()

        assertNotNull(result)
        assertEquals(AppMode.PERIOD_TRACKING, result!!.activeMode)
    }

    @Test
    fun `getSettings handles unknown theme gracefully`() = runTest {
        val entity = SettingsEntity(theme = "unknown_theme")
        every { settingsDao.getSettings() } returns flowOf(entity)

        val result = repository.getSettings().first()

        assertNotNull(result)
        assertEquals(AppTheme.SYSTEM, result!!.theme)
    }

    @Test
    fun `getSettings handles unknown notification privacy gracefully`() = runTest {
        val entity = SettingsEntity(notificationPrivacy = "unknown")
        every { settingsDao.getSettings() } returns flowOf(entity)

        val result = repository.getSettings().first()

        assertNotNull(result)
        assertEquals(NotificationPrivacy.HIGH, result!!.notificationPrivacy)
    }

    @Test
    fun `getSettings clamps cycle length to valid range`() = runTest {
        val entity = SettingsEntity(typicalCycleLength = 5)
        every { settingsDao.getSettings() } returns flowOf(entity)

        val result = repository.getSettings().first()
        assertEquals(15, result!!.typicalCycleLength)
    }

    @Test
    fun `getSettings clamps period length to valid range`() = runTest {
        val entity = SettingsEntity(typicalPeriodLength = 0)
        every { settingsDao.getSettings() } returns flowOf(entity)

        val result = repository.getSettings().first()
        assertEquals(1, result!!.typicalPeriodLength)
    }

    @Test
    fun `getSettingsOnce maps entity correctly`() = runTest {
        val entity = SettingsEntity(
            activeMode = "conceive",
            bmiCategory = "overweight"
        )
        coEvery { settingsDao.getSettingsOnce() } returns entity

        val result = repository.getSettingsOnce()

        assertNotNull(result)
        assertEquals(AppMode.CONCEIVE, result!!.activeMode)
        assertEquals(BmiCategory.OVERWEIGHT, result.bmiCategory)
    }

    @Test
    fun `getSettingsOnce returns null when no settings`() = runTest {
        coEvery { settingsDao.getSettingsOnce() } returns null
        val result = repository.getSettingsOnce()
        assertNull(result)
    }

    @Test
    fun `saveSettings maps domain to entity correctly`() = runTest {
        val settings = Settings(
            activeMode = AppMode.PREGNANCY,
            birthYear = 1985,
            bmiCategory = BmiCategory.OBESE,
            typicalCycleLength = 32,
            typicalPeriodLength = 7,
            theme = AppTheme.DARK,
            notificationPrivacy = NotificationPrivacy.LOW,
            onboardingCompleted = true
        )

        repository.saveSettings(settings)

        coVerify { settingsDao.insertOrReplace(any()) }
    }

    @Test
    fun `all AppMode values map correctly`() = runTest {
        for (mode in AppMode.entries) {
            val entity = SettingsEntity(activeMode = mode.name.lowercase())
            every { settingsDao.getSettings() } returns flowOf(entity)
            val result = repository.getSettings().first()
            assertEquals(mode, result!!.activeMode)
        }
    }

    @Test
    fun `all BmiCategory values map correctly`() = runTest {
        for (bmi in BmiCategory.entries) {
            val entity = SettingsEntity(bmiCategory = bmi.name.lowercase())
            every { settingsDao.getSettings() } returns flowOf(entity)
            val result = repository.getSettings().first()
            assertEquals(bmi, result!!.bmiCategory)
        }
    }

    @Test
    fun `null bmi maps to null`() = runTest {
        val entity = SettingsEntity(bmiCategory = null)
        every { settingsDao.getSettings() } returns flowOf(entity)
        val result = repository.getSettings().first()
        assertNull(result!!.bmiCategory)
    }

    @Test
    fun `invalid date string uses current date`() = runTest {
        val entity = SettingsEntity(firstLaunchDate = "not-a-date")
        every { settingsDao.getSettings() } returns flowOf(entity)
        val result = repository.getSettings().first()
        assertEquals(LocalDate.now(), result!!.firstLaunchDate)
    }
}
