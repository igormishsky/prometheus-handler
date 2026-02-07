package com.cyclesync.domain.repository

import com.cyclesync.domain.entity.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings?>
    suspend fun getSettingsOnce(): Settings?
    suspend fun saveSettings(settings: Settings)
    suspend fun updateSettings(settings: Settings)
}
