package com.cyclesync.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cyclesync.core.database.dao.AlgorithmStateDao
import com.cyclesync.core.database.dao.CycleDao
import com.cyclesync.core.database.dao.DailyLogDao
import com.cyclesync.core.database.dao.PredictionDao
import com.cyclesync.core.database.dao.SettingsDao
import com.cyclesync.core.database.dao.TemperatureLogDao
import com.cyclesync.core.database.dao.TrackingEntryDao
import com.cyclesync.core.database.entity.AlgorithmStateEntity
import com.cyclesync.core.database.entity.CustomTagEntity
import com.cyclesync.core.database.entity.CustomTagEntryEntity
import com.cyclesync.core.database.entity.CycleEntity
import com.cyclesync.core.database.entity.DailyLogEntity
import com.cyclesync.core.database.entity.MedicationEntity
import com.cyclesync.core.database.entity.MedicationLogEntity
import com.cyclesync.core.database.entity.PredictionEntity
import com.cyclesync.core.database.entity.PregnancyEntity
import com.cyclesync.core.database.entity.PregnancyLogEntity
import com.cyclesync.core.database.entity.SettingsEntity
import com.cyclesync.core.database.entity.TemperatureLogEntity
import com.cyclesync.core.database.entity.TrackingEntryEntity

@Database(
    entities = [
        SettingsEntity::class,
        CycleEntity::class,
        DailyLogEntity::class,
        TrackingEntryEntity::class,
        PredictionEntity::class,
        TemperatureLogEntity::class,
        PregnancyEntity::class,
        PregnancyLogEntity::class,
        MedicationEntity::class,
        MedicationLogEntity::class,
        CustomTagEntity::class,
        CustomTagEntryEntity::class,
        AlgorithmStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CycleSyncDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun trackingEntryDao(): TrackingEntryDao
    abstract fun predictionDao(): PredictionDao
    abstract fun temperatureLogDao(): TemperatureLogDao
    abstract fun algorithmStateDao(): AlgorithmStateDao

    companion object {
        const val DATABASE_NAME = "cyclesync.db"
    }
}
