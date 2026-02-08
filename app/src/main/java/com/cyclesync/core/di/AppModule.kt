package com.cyclesync.core.di

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
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
import com.cyclesync.core.notifications.ReminderManager
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
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val KEYSTORE_ALIAS = "cyclesync_db_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val DB_NAME = "cyclesync.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CycleSyncDatabase {
        val passphrase = getOrCreateDatabaseKey()
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            CycleSyncDatabase::class.java,
            DB_NAME
        )
            .openHelperFactory(factory)
            // Do NOT use fallbackToDestructiveMigration() — it wipes all user data on schema changes.
            // Add explicit Migration objects here when bumping the database version.
            .build()
    }

    private fun getOrCreateDatabaseKey(): ByteArray {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        KEYSTORE_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                keyGenerator.generateKey()
            }

            val key = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val encoded = key.encoded
            if (encoded != null && encoded.isNotEmpty()) {
                SQLiteDatabase.getBytes(
                    CharArray(minOf(encoded.size, 32)) { encoded[it].toInt().toChar() }
                )
            } else {
                getFallbackKey()
            }
        } catch (e: Exception) {
            Timber.w(e, "Android Keystore unavailable, using fallback encryption key")
            getFallbackKey()
        }
    }

    private fun getFallbackKey(): ByteArray {
        return SQLiteDatabase.getBytes("cyclesync-secure-key-v2".toCharArray())
    }

    @Provides
    fun provideSettingsDao(db: CycleSyncDatabase): SettingsDao = db.settingsDao()

    @Provides
    fun provideCycleDao(db: CycleSyncDatabase): CycleDao = db.cycleDao()

    @Provides
    fun provideDailyLogDao(db: CycleSyncDatabase): DailyLogDao = db.dailyLogDao()

    @Provides
    fun provideTrackingEntryDao(db: CycleSyncDatabase): TrackingEntryDao = db.trackingEntryDao()

    @Provides
    fun providePredictionDao(db: CycleSyncDatabase): PredictionDao = db.predictionDao()

    @Provides
    fun provideTemperatureLogDao(db: CycleSyncDatabase): TemperatureLogDao = db.temperatureLogDao()

    @Provides
    fun provideAlgorithmStateDao(db: CycleSyncDatabase): AlgorithmStateDao = db.algorithmStateDao()

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

    @Provides
    @Singleton
    fun provideReminderManager(): ReminderManager = ReminderManager()
}
