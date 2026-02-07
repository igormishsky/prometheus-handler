package com.cyclesync.core.di

import android.content.Context
import androidx.room.Room
import com.cyclesync.core.database.CycleSyncDatabase
import com.cyclesync.core.database.dao.AlgorithmStateDao
import com.cyclesync.core.database.dao.CycleDao
import com.cyclesync.core.database.dao.DailyLogDao
import com.cyclesync.core.database.dao.PredictionDao
import com.cyclesync.core.database.dao.SettingsDao
import com.cyclesync.core.database.dao.TemperatureLogDao
import com.cyclesync.core.database.dao.TrackingEntryDao
import com.cyclesync.data.repository.CycleRepositoryImpl
import com.cyclesync.data.repository.DailyLogRepositoryImpl
import com.cyclesync.data.repository.PredictionRepositoryImpl
import com.cyclesync.data.repository.SettingsRepositoryImpl
import com.cyclesync.domain.prediction.PredictionEngine
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.DailyLogRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CycleSyncDatabase {
        val passphrase = SQLiteDatabase.getBytes("cyclesync-secure-key".toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            CycleSyncDatabase::class.java,
            "cyclesync.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideSettingsDao(db: CycleSyncDatabase): SettingsDao = db.settingsDao()
    @Provides fun provideCycleDao(db: CycleSyncDatabase): CycleDao = db.cycleDao()
    @Provides fun provideDailyLogDao(db: CycleSyncDatabase): DailyLogDao = db.dailyLogDao()
    @Provides fun provideTrackingEntryDao(db: CycleSyncDatabase): TrackingEntryDao = db.trackingEntryDao()
    @Provides fun providePredictionDao(db: CycleSyncDatabase): PredictionDao = db.predictionDao()
    @Provides fun provideTemperatureLogDao(db: CycleSyncDatabase): TemperatureLogDao = db.temperatureLogDao()
    @Provides fun provideAlgorithmStateDao(db: CycleSyncDatabase): AlgorithmStateDao = db.algorithmStateDao()

    @Provides
    @Singleton
    fun provideSettingsRepository(dao: SettingsDao): SettingsRepository = SettingsRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideCycleRepository(dao: CycleDao): CycleRepository = CycleRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideDailyLogRepository(logDao: DailyLogDao, entryDao: TrackingEntryDao): DailyLogRepository =
        DailyLogRepositoryImpl(logDao, entryDao)

    @Provides
    @Singleton
    fun providePredictionRepository(dao: PredictionDao): PredictionRepository = PredictionRepositoryImpl(dao)

    @Provides
    @Singleton
    fun providePredictionEngine(): PredictionEngine = PredictionEngine()
}
