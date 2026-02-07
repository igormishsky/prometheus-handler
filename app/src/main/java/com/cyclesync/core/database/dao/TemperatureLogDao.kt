package com.cyclesync.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyclesync.core.database.entity.TemperatureLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureLogDao {
    @Query("SELECT * FROM temperature_logs ORDER BY date DESC")
    fun getAll(): Flow<List<TemperatureLogEntity>>

    @Query("SELECT * FROM temperature_logs WHERE date = :date")
    suspend fun getByDate(date: String): TemperatureLogEntity?

    @Query("SELECT * FROM temperature_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getByDateRange(startDate: String, endDate: String): List<TemperatureLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TemperatureLogEntity)

    @Delete
    suspend fun delete(log: TemperatureLogEntity)
}
