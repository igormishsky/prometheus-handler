package com.cyclesync.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyclesync.core.database.entity.PredictionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions WHERE is_stale = 0 ORDER BY predicted_date ASC")
    fun getActivePredictions(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE is_stale = 0 ORDER BY predicted_date ASC")
    suspend fun getActivePredictionsOnce(): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE type = :type AND is_stale = 0 ORDER BY predicted_date ASC")
    suspend fun getByType(type: String): List<PredictionEntity>

    @Query("SELECT * FROM predictions WHERE cycle_id = :cycleId AND is_stale = 0")
    suspend fun getByCycleId(cycleId: String): List<PredictionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PredictionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<PredictionEntity>)

    @Query("UPDATE predictions SET is_stale = 1 WHERE is_stale = 0")
    suspend fun markAllStale()

    @Query("DELETE FROM predictions WHERE is_stale = 1")
    suspend fun deleteStalePredictions()

    @Query("DELETE FROM predictions")
    suspend fun deleteAll()
}
