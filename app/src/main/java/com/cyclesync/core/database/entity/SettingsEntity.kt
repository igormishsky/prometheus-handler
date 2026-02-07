package com.cyclesync.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "active_mode") val activeMode: String = "period_tracking",
    @ColumnInfo(name = "birth_year") val birthYear: Int? = null,
    @ColumnInfo(name = "bmi_category") val bmiCategory: String? = null,
    @ColumnInfo(name = "typical_cycle_length") val typicalCycleLength: Int = 28,
    @ColumnInfo(name = "typical_period_length") val typicalPeriodLength: Int = 5,
    @ColumnInfo(name = "pin_hash") val pinHash: String? = null,
    @ColumnInfo(name = "app_icon") val appIcon: String = "default",
    @ColumnInfo(name = "theme") val theme: String = "system",
    @ColumnInfo(name = "units_weight") val unitsWeight: String = "kg",
    @ColumnInfo(name = "units_temp") val unitsTemp: String = "celsius",
    @ColumnInfo(name = "first_launch_date") val firstLaunchDate: String = "",
    @ColumnInfo(name = "last_backup_date") val lastBackupDate: String? = null,
    @ColumnInfo(name = "onboarding_completed") val onboardingCompleted: Boolean = false,
    @ColumnInfo(name = "notification_privacy") val notificationPrivacy: String = "high",
    @ColumnInfo(name = "created_at") val createdAt: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: String = ""
)
