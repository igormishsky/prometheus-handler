package com.cyclesync.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyclesync.core.database.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date = :date")
    suspend fun getByDate(date: String): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE date = :date")
    fun getByDateFlow(date: String): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE cycle_id = :cycleId ORDER BY date ASC")
    suspend fun getByCycleId(cycleId: String): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getByDateRange(startDate: String, endDate: String): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getByDateRangeFlow(startDate: String, endDate: String): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DailyLogEntity)

    @Update
    suspend fun update(log: DailyLogEntity)

    @Delete
    suspend fun delete(log: DailyLogEntity)
}
