package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.SettingsDao
import com.cyclesync.core.database.entity.SettingsEntity
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.AppTheme
import com.cyclesync.domain.entity.BmiCategory
import com.cyclesync.domain.entity.NotificationPrivacy
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<Settings?> {
        return settingsDao.getSettings().map { it?.toDomain() }
    }

    override suspend fun getSettingsOnce(): Settings? {
        return settingsDao.getSettingsOnce()?.toDomain()
    }

    override suspend fun saveSettings(settings: Settings) {
        settingsDao.insertOrReplace(settings.toEntity())
    }

    private fun SettingsEntity.toDomain(): Settings = Settings(
        id = id,
        activeMode = AppMode.fromNameOrNull(activeMode) ?: AppMode.PERIOD_TRACKING,
        birthYear = birthYear,
        bmiCategory = BmiCategory.fromNameOrNull(bmiCategory),
        typicalCycleLength = typicalCycleLength.coerceIn(15, 60),
        typicalPeriodLength = typicalPeriodLength.coerceIn(1, 15),
        pinHash = pinHash,
        appIcon = appIcon,
        theme = AppTheme.fromNameOrNull(theme) ?: AppTheme.SYSTEM,
        unitsWeight = unitsWeight,
        unitsTemp = unitsTemp,
        firstLaunchDate = DateUtils.fromIsoStringOrNull(firstLaunchDate) ?: LocalDate.now(),
        lastBackupDate = DateUtils.fromIsoStringOrNull(lastBackupDate),
        onboardingCompleted = onboardingCompleted,
        notificationPrivacy = NotificationPrivacy.fromNameOrNull(notificationPrivacy) ?: NotificationPrivacy.HIGH
    )

    private fun Settings.toEntity(): SettingsEntity {
        val now = DateUtils.toIsoString(LocalDate.now())
        return SettingsEntity(
            id = id,
            activeMode = activeMode.name.lowercase(),
            birthYear = birthYear,
            bmiCategory = bmiCategory?.name?.lowercase(),
            typicalCycleLength = typicalCycleLength,
            typicalPeriodLength = typicalPeriodLength,
            pinHash = pinHash,
            appIcon = appIcon,
            theme = theme.name.lowercase(),
            unitsWeight = unitsWeight,
            unitsTemp = unitsTemp,
            firstLaunchDate = DateUtils.toIsoString(firstLaunchDate),
            lastBackupDate = lastBackupDate?.let { DateUtils.toIsoString(it) },
            onboardingCompleted = onboardingCompleted,
            notificationPrivacy = notificationPrivacy.name.lowercase(),
            createdAt = now,
            updatedAt = now
        )
    }
}
